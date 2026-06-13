package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EntityList
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.ln

object MobCatcherStorage {
    const val CAPTURED_MOBS_TAG = "capturedMobs"
    const val CAPTURED_MOBS_COLUMNS_TAG = "capturedMobsColumns"

    private const val ID_TAG = "id"
    private const val ENTITY_TYPE_TAG = "entityType"
    private const val ENTITY_NBT_TAG = "entityNbt"
    private const val SLOT_TAG = "slot"
    private const val WIDTH_TAG = "width"
    private const val HEIGHT_TAG = "height"
    private const val SLOT_COST_TAG = "slotCost"
    private const val HOSTILE_TAG = "hostile"
    private const val DISPLAY_NAME_TAG = "displayName"
    private const val CURRENT_HEALTH_TAG = "currentHealth"
    private const val MAX_HEALTH_TAG = "maxHealth"
    private const val TAG_COMPOUND = 10
    private const val FOOTPRINT_ASPECT_WIDENING = 1.4
    private const val FOOTPRINT_ASPECT_WEIGHT = 10.0
    private const val FOOTPRINT_OVERFILL_WEIGHT = 0.75
    private const val FOOTPRINT_SCORE_EPSILON = 0.001
    private const val MOB_PART_PREFIX = "mob:"
    private const val STACK_PART_PREFIX = "stack:"
    private const val FIXED_PART_PREFIX = "fixed:"

    fun getCapturedMobs(backpackWrapper: BackpackWrapper): List<CapturedMob> =
        backpackWrapper.capturedMobs

    fun setCapturedMobs(backpackWrapper: BackpackWrapper, capturedMobs: List<CapturedMob>) {
        backpackWrapper.capturedMobs.clear()
        backpackWrapper.capturedMobs.addAll(capturedMobs)
        backpackWrapper.capturedMobsColumns = getColumns(backpackWrapper)
    }

    fun addCapturedMob(backpackWrapper: BackpackWrapper, capturedMob: CapturedMob) {
        backpackWrapper.capturedMobs.add(capturedMob)
        backpackWrapper.capturedMobsColumns = getColumns(backpackWrapper)
    }

    fun removeCapturedMob(backpackWrapper: BackpackWrapper, capturedMobId: java.util.UUID): Boolean {
        val removed = backpackWrapper.capturedMobs.removeIf { it.id == capturedMobId }
        if (removed && backpackWrapper.capturedMobs.isEmpty()) {
            backpackWrapper.capturedMobsColumns = getColumns(backpackWrapper)
        }
        return removed
    }

    fun getCapturedMob(backpackWrapper: BackpackWrapper, capturedMobId: java.util.UUID): CapturedMob? =
        backpackWrapper.capturedMobs.firstOrNull { it.id == capturedMobId }

    fun getCapturedMobsTag(backpackWrapper: BackpackWrapper): NBTTagCompound {
        backpackWrapper.ensureCapturedMobLayoutCurrent()
        return NBTTagCompound().also {
            it.setTag(CAPTURED_MOBS_TAG, serialize(getCapturedMobs(backpackWrapper)))
            it.setInteger(CAPTURED_MOBS_COLUMNS_TAG, backpackWrapper.capturedMobsColumns)
        }
    }

    fun applyCapturedMobsTag(backpackWrapper: BackpackWrapper, nbt: NBTTagCompound) {
        backpackWrapper.capturedMobs.clear()
        backpackWrapper.capturedMobs.addAll(deserializeCapturedMobsTag(nbt))
        backpackWrapper.capturedMobsColumns = if (nbt.hasKey(CAPTURED_MOBS_COLUMNS_TAG)) {
            nbt.getInteger(CAPTURED_MOBS_COLUMNS_TAG)
        } else {
            getColumns(backpackWrapper)
        }
        backpackWrapper.ensureCapturedMobLayoutCurrent()
    }

    fun isSlotBlocked(backpackWrapper: BackpackWrapper, slot: Int): Boolean {
        val columns = getColumns(backpackWrapper)
        return getCapturedMobs(backpackWrapper).any { it.occupiesSlot(slot, columns) }
    }

    fun canFitBasicTier(backpackWrapper: BackpackWrapper, maxSlotCost: Int): Boolean =
        getCapturedMobs(backpackWrapper).all { !it.hostile && it.slotCost <= maxSlotCost }

    fun canFitWithAdditionalInventoryControls(backpackWrapper: BackpackWrapper, additionalControls: Int): Boolean {
        if (additionalControls <= 0) {
            return true
        }
        backpackWrapper.ensureCapturedMobLayoutCurrent()
        val currentColumns = getColumns(backpackWrapper)
        return canFitLayout(backpackWrapper, (currentColumns - additionalControls * 2).coerceAtLeast(1), currentColumns)
    }

    fun findEmptyRectangle(backpackWrapper: BackpackWrapper, footprint: CapturedMobFootprint): Int? {
        backpackWrapper.ensureCapturedMobLayoutCurrent()
        val columns = getColumns(backpackWrapper)
        val rows = ceil(backpackWrapper.backpackInventorySize().toDouble() / columns).toInt()
        val capturedMobs = getCapturedMobs(backpackWrapper)
        return findEmptyRectangle(backpackWrapper, footprint, columns, rows, capturedMobs)
    }

    fun ensureLayoutCurrent(backpackWrapper: BackpackWrapper) {
        val currentColumns = getColumns(backpackWrapper)
        val previousColumns = backpackWrapper.capturedMobsColumns.takeIf { it > 0 } ?: currentColumns
        val capturedMobs = getCapturedMobs(backpackWrapper)
        if (capturedMobs.isEmpty()) {
            backpackWrapper.capturedMobsColumns = currentColumns
            return
        }
        if (previousColumns == currentColumns) {
            return
        }

        val compact = currentColumns > previousColumns
        val fitResult = fitInventoryLayout(
            getInventoryLayoutParts(backpackWrapper, capturedMobs, previousColumns, currentColumns),
            backpackWrapper.backpackInventorySize(),
            currentColumns,
            compact
        )
        if (!fitResult.fits) {
            return
        }

        applyInventoryLayout(backpackWrapper, fitResult)
        backpackWrapper.capturedMobsColumns = currentColumns
    }

    private fun canFitLayout(backpackWrapper: BackpackWrapper, targetColumns: Int, previousColumns: Int): Boolean {
        if (targetColumns == previousColumns) {
            return true
        }
        val capturedMobs = getCapturedMobs(backpackWrapper)
        if (capturedMobs.isEmpty()) {
            return true
        }
        return fitInventoryLayout(
            getInventoryLayoutParts(backpackWrapper, capturedMobs, previousColumns, targetColumns),
            backpackWrapper.backpackInventorySize(),
            targetColumns,
            compact = targetColumns > previousColumns
        ).fits
    }

    private fun getInventoryLayoutParts(
        backpackWrapper: BackpackWrapper,
        capturedMobs: List<CapturedMob>,
        previousColumns: Int,
        currentColumns: Int
    ): List<LayoutPart> {
        val parts = mutableListOf<LayoutPart>()
        for (slot in 0 until backpackWrapper.backpackInventorySize()) {
            val mobAtOrigin = capturedMobs.firstOrNull { it.slot == slot }
            if (mobAtOrigin != null) {
                parts += LayoutPart(
                    MOB_PART_PREFIX + mobAtOrigin.id,
                    getTargetSlot(mobAtOrigin.slot, previousColumns, currentColumns),
                    mobAtOrigin.width,
                    mobAtOrigin.height,
                    getOccupiedSlots(mobAtOrigin, previousColumns, backpackWrapper.backpackInventorySize())
                )
                continue
            }
            if (capturedMobs.any { it.occupiesSlot(slot, previousColumns) }) {
                continue
            }

            val stack = backpackWrapper.backpackItemStackHandler.getStackInSlot(slot)
            if (stack.isEmpty) {
                continue
            }
            val fixed = backpackWrapper.isSlotLocked(slot) || backpackWrapper.isSlotMemorized(slot)
            parts += LayoutPart(
                (if (fixed) FIXED_PART_PREFIX else STACK_PART_PREFIX) + slot,
                slot,
                1,
                1,
                setOf(slot)
            )
        }
        return parts
    }

    private fun applyInventoryLayout(backpackWrapper: BackpackWrapper, fitResult: LayoutFitResult) {
        val stackMoves = fitResult.fittedSlots.mapNotNull { (partId, targetSlot) ->
            if (!partId.startsWith(STACK_PART_PREFIX)) {
                return@mapNotNull null
            }
            val sourceSlot = partId.removePrefix(STACK_PART_PREFIX).toIntOrNull() ?: return@mapNotNull null
            if (sourceSlot == targetSlot ||
                sourceSlot !in 0 until backpackWrapper.backpackInventorySize() ||
                targetSlot !in 0 until backpackWrapper.backpackInventorySize()
            ) {
                return@mapNotNull null
            }
            val stack = backpackWrapper.backpackItemStackHandler.getStackInSlot(sourceSlot)
            if (stack.isEmpty) null else StackMove(sourceSlot, targetSlot, stack.copy())
        }

        if (stackMoves.isNotEmpty()) {
            val sourceSlots = stackMoves.mapTo(mutableSetOf()) { it.sourceSlot }
            val targetSlots = stackMoves.mapTo(mutableSetOf()) { it.targetSlot }
            if (targetSlots.any { !backpackWrapper.backpackItemStackHandler.getStackInSlot(it).isEmpty && it !in sourceSlots }) {
                return
            }
            stackMoves.forEach { backpackWrapper.backpackItemStackHandler.setStackInSlot(it.sourceSlot, net.minecraft.item.ItemStack.EMPTY) }
            stackMoves.forEach {
                backpackWrapper.backpackItemStackHandler.setStackInSlot(
                    it.targetSlot,
                    ItemHandlerHelper.copyStackWithSize(it.stack, it.stack.count)
                )
            }
        }

        val fittedMobs = getCapturedMobs(backpackWrapper).map { capturedMob ->
            fitResult.fittedSlots[MOB_PART_PREFIX + capturedMob.id]?.let { capturedMob.copy(slot = it) } ?: capturedMob
        }
        backpackWrapper.capturedMobs.clear()
        backpackWrapper.capturedMobs.addAll(fittedMobs)
    }

    private fun findEmptyRectangle(
        backpackWrapper: BackpackWrapper,
        footprint: CapturedMobFootprint,
        columns: Int,
        rows: Int,
        capturedMobs: List<CapturedMob>
    ): Int? {
        for (y in 0..rows - footprint.height) {
            for (x in 0..columns - footprint.width) {
                val slot = y * columns + x
                if (isRectangleEmpty(backpackWrapper, slot, footprint, columns, capturedMobs)) {
                    return slot
                }
            }
        }
        return null
    }

    private fun isRectangleEmpty(
        backpackWrapper: BackpackWrapper,
        slot: Int,
        footprint: CapturedMobFootprint,
        columns: Int,
        capturedMobs: List<CapturedMob>
    ): Boolean {
        for (y in 0 until footprint.height) {
            for (x in 0 until footprint.width) {
                val checkedSlot = slot + y * columns + x
                if (checkedSlot !in 0 until backpackWrapper.backpackInventorySize()) {
                    return false
                }
                if (!backpackWrapper.getStackInSlot(checkedSlot).isEmpty) {
                    return false
                }
                if (capturedMobs.any { it.occupiesSlot(checkedSlot, columns) }) {
                    return false
                }
            }
        }
        return true
    }

    fun getFootprint(entity: EntityLivingBase, slotCost: Int): CapturedMobFootprint {
        val entityWidth = maxOf(entity.width.toDouble(), 0.25)
        val entityHeight = maxOf(entity.height.toDouble(), 0.25)
        val targetAspect = entityWidth / entityHeight * FOOTPRINT_ASPECT_WIDENING
        val maxSlotCost = maxOf(1, slotCost)
        var best = CapturedMobFootprint(1, maxSlotCost)
        var bestScore = Double.MAX_VALUE
        var bestAspectError = Double.MAX_VALUE
        var bestOverfill = Int.MAX_VALUE

        for (width in 1..maxSlotCost) {
            for (height in 1..maxSlotCost) {
                val area = width * height
                if (area < slotCost) {
                    continue
                }
                val aspect = width.toDouble() / height
                val aspectError = abs(ln(aspect / targetAspect))
                val overfill = area - slotCost
                val score = aspectError * FOOTPRINT_ASPECT_WEIGHT + overfill * FOOTPRINT_OVERFILL_WEIGHT
                if (isBetterFootprint(
                        score,
                        aspectError,
                        overfill,
                        width,
                        height,
                        bestScore,
                        bestAspectError,
                        bestOverfill,
                        best
                    )
                ) {
                    best = CapturedMobFootprint(width, height)
                    bestScore = score
                    bestAspectError = aspectError
                    bestOverfill = overfill
                }
            }
        }
        return best
    }

    private fun isBetterFootprint(
        score: Double,
        aspectError: Double,
        overfill: Int,
        width: Int,
        height: Int,
        bestScore: Double,
        bestAspectError: Double,
        bestOverfill: Int,
        best: CapturedMobFootprint
    ): Boolean {
        if (score < bestScore - FOOTPRINT_SCORE_EPSILON) return true
        if (score > bestScore + FOOTPRINT_SCORE_EPSILON) return false
        if (aspectError < bestAspectError - FOOTPRINT_SCORE_EPSILON) return true
        if (aspectError > bestAspectError + FOOTPRINT_SCORE_EPSILON) return false
        if (width != best.width) return width > best.width
        if (height != best.height) return height < best.height
        return overfill < bestOverfill
    }

    fun getColumns(backpackWrapper: BackpackWrapper): Int {
        val backgroundColumns = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
        val columnsTaken = (backpackWrapper.tankUpgradeSlots().take(2).size + backpackWrapper.batteryUpgradeSlots().take(1).size) * 2
        return (backgroundColumns - columnsTaken).coerceAtLeast(1)
    }

    private fun getTargetSlot(slot: Int, columns: Int, targetColumns: Int): Int =
        slot / columns * targetColumns + slot % columns

    private fun getOccupiedSlots(capturedMob: CapturedMob, columns: Int, inventorySlots: Int): Set<Int> {
        val occupiedSlots = mutableSetOf<Int>()
        for (y in 0 until capturedMob.height) {
            for (x in 0 until capturedMob.width) {
                val slot = capturedMob.slot + y * columns + x
                if (slot < inventorySlots) {
                    occupiedSlots += slot
                }
            }
        }
        return occupiedSlots
    }

    private fun fitInventoryLayout(
        parts: List<LayoutPart>,
        targetSlots: Int,
        targetColumns: Int,
        compact: Boolean
    ): LayoutFitResult {
        val result = fitInventoryLayoutInternal(parts, targetSlots, targetColumns, compact, compact)
        if (compact || result.fits) {
            return result
        }

        val compactResult = fitInventoryLayoutInternal(parts, targetSlots, targetColumns, compact = true, preserveStacks = false)
        if (compactResult.fits) {
            return compactResult
        }

        val reorderedCompactResult = fitInventoryLayoutInternal(
            orderPartsForCompaction(parts),
            targetSlots,
            targetColumns,
            compact = true,
            preserveStacks = false,
            fillGapsWithStacks = true
        )
        return if (reorderedCompactResult.fits) reorderedCompactResult else compactResult
    }

    private fun orderPartsForCompaction(parts: List<LayoutPart>): List<LayoutPart> =
        parts.sortedWith(
            compareBy<LayoutPart> { compactionPriority(it) }
                .thenByDescending { it.width * it.height }
                .thenBy { it.firstSlot }
        )

    private fun compactionPriority(part: LayoutPart): Int =
        when {
            part.id.startsWith(FIXED_PART_PREFIX) -> 0
            part.id.startsWith(STACK_PART_PREFIX) -> 2
            else -> 1
        }

    private fun fitInventoryLayoutInternal(
        parts: List<LayoutPart>,
        targetSlots: Int,
        targetColumns: Int,
        compact: Boolean,
        preserveStacks: Boolean,
        fillGapsWithStacks: Boolean = false
    ): LayoutFitResult {
        val occupiedSlots = mutableSetOf<Int>()
        val fittedSlots = mutableMapOf<String, Int>()
        var nextSlot = 0

        for ((partIndex, part) in parts.withIndex()) {
            val fittedSlot = findNextFit(
                part,
                if (fillGapsWithStacks && part.id.startsWith(STACK_PART_PREFIX)) 0 else nextSlot,
                targetSlots,
                targetColumns,
                occupiedSlots,
                compact,
                preserveStacks
            )
            if (fittedSlot < 0) {
                return LayoutFitResult(
                    false,
                    fittedSlots,
                    parts.drop(partIndex).flatMapTo(mutableSetOf()) { it.sourceSlots }
                )
            }

            occupy(part, fittedSlot, targetColumns, occupiedSlots)
            fittedSlots[part.id] = fittedSlot
            nextSlot = if (!compact && fittedSlot == part.firstSlot) {
                maxOf(nextSlot, getSlotAfterPart(part, fittedSlot, targetColumns))
            } else {
                fittedSlot + part.width
            }
        }

        return LayoutFitResult(true, fittedSlots)
    }

    private fun findNextFit(
        part: LayoutPart,
        nextSlot: Int,
        targetSlots: Int,
        targetColumns: Int,
        occupiedSlots: Set<Int>,
        compact: Boolean,
        preserveStacks: Boolean
    ): Int {
        if (part.id.startsWith(FIXED_PART_PREFIX)) {
            return if (fits(part, part.firstSlot, targetSlots, targetColumns, occupiedSlots)) part.firstSlot else -1
        }
        if (shouldPreserveFirstSlot(part, compact, preserveStacks) &&
            part.firstSlot < targetSlots &&
            fits(part, part.firstSlot, targetSlots, targetColumns, occupiedSlots)
        ) {
            return part.firstSlot
        }

        val startSlot = if (compact || part.firstSlot >= targetSlots) nextSlot else maxOf(nextSlot, part.firstSlot)
        for (slot in startSlot until targetSlots) {
            if (fits(part, slot, targetSlots, targetColumns, occupiedSlots)) {
                return slot
            }
        }
        return -1
    }

    private fun shouldPreserveFirstSlot(part: LayoutPart, compact: Boolean, preserveStacks: Boolean): Boolean =
        !compact || preserveStacks && part.id.startsWith(STACK_PART_PREFIX)

    private fun fits(part: LayoutPart, slot: Int, targetSlots: Int, targetColumns: Int, occupiedSlots: Set<Int>): Boolean {
        val x = slot % targetColumns
        if (x + part.width > targetColumns) {
            return false
        }
        for (y in 0 until part.height) {
            for (partX in 0 until part.width) {
                val checkedSlot = slot + y * targetColumns + partX
                if (checkedSlot >= targetSlots || checkedSlot in occupiedSlots) {
                    return false
                }
            }
        }
        return true
    }

    private fun occupy(part: LayoutPart, slot: Int, targetColumns: Int, occupiedSlots: MutableSet<Int>) {
        for (y in 0 until part.height) {
            for (x in 0 until part.width) {
                occupiedSlots += slot + y * targetColumns + x
            }
        }
    }

    private fun getSlotAfterPart(part: LayoutPart, slot: Int, targetColumns: Int): Int =
        slot + (part.height - 1) * targetColumns + part.width

    fun serialize(capturedMobs: List<CapturedMob>): NBTTagList =
        NBTTagList().also { list -> capturedMobs.sortedBy { it.slot }.forEach { list.appendTag(serialize(it)) } }

    fun deserialize(list: NBTTagList): List<CapturedMob> =
        (0 until list.tagCount()).mapNotNull { deserialize(list.getCompoundTagAt(it)) }

    private fun serialize(capturedMob: CapturedMob): NBTTagCompound =
        NBTTagCompound().also { tag ->
            tag.setUniqueId(ID_TAG, capturedMob.id)
            tag.setString(ENTITY_TYPE_TAG, capturedMob.entityType.toString())
            tag.setTag(ENTITY_NBT_TAG, capturedMob.entityNbt.copy())
            tag.setInteger(SLOT_TAG, capturedMob.slot)
            tag.setInteger(WIDTH_TAG, capturedMob.width)
            tag.setInteger(HEIGHT_TAG, capturedMob.height)
            tag.setInteger(SLOT_COST_TAG, capturedMob.slotCost)
            tag.setBoolean(HOSTILE_TAG, capturedMob.hostile)
            tag.setString(DISPLAY_NAME_TAG, capturedMob.displayName)
            tag.setInteger(CURRENT_HEALTH_TAG, capturedMob.currentHealth)
            tag.setInteger(MAX_HEALTH_TAG, capturedMob.maxHealth)
        }

    private fun deserialize(tag: NBTTagCompound): CapturedMob? {
        val entityType = ResourceLocation(tag.getString(ENTITY_TYPE_TAG))
        if (!EntityList.isRegistered(entityType)) {
            return null
        }
        return CapturedMob(
            tag.getUniqueId(ID_TAG) ?: return null,
            entityType,
            tag.getCompoundTag(ENTITY_NBT_TAG),
            tag.getInteger(SLOT_TAG),
            tag.getInteger(WIDTH_TAG).coerceAtLeast(1),
            tag.getInteger(HEIGHT_TAG).coerceAtLeast(1),
            tag.getInteger(SLOT_COST_TAG).coerceAtLeast(1),
            tag.getBoolean(HOSTILE_TAG),
            tag.getString(DISPLAY_NAME_TAG),
            tag.getInteger(CURRENT_HEALTH_TAG),
            tag.getInteger(MAX_HEALTH_TAG).coerceAtLeast(1)
        )
    }

    fun deserializeCapturedMobsTag(nbt: NBTTagCompound): List<CapturedMob> =
        deserialize(nbt.getTagList(CAPTURED_MOBS_TAG, TAG_COMPOUND))

    private data class LayoutPart(
        val id: String,
        val firstSlot: Int,
        val width: Int,
        val height: Int,
        val sourceSlots: Set<Int>
    )

    private data class LayoutFitResult(
        val fits: Boolean,
        val fittedSlots: Map<String, Int>,
        val errorSlots: Set<Int> = emptySet()
    )

    private data class StackMove(val sourceSlot: Int, val targetSlot: Int, val stack: net.minecraft.item.ItemStack)
}

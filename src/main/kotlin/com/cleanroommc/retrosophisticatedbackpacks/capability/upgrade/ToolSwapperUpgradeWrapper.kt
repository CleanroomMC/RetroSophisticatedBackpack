package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeFilterUtils.matchesAllowEmpty
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ToolSwapperUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityCow
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemHoe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemSpade
import net.minecraft.item.ItemShears
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.IShearable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import java.util.LinkedList

open class ToolSwapperUpgradeWrapper(
    private val hasSettingsTab: Boolean = false,
    private val swapToolOnKeyPress: Boolean = false,
) : BasicUpgradeWrapper<ToolSwapperUpgradeItem>(Config.toolSwapperUpgrade.filterSlots, Config.toolSwapperUpgrade.slotsInRow),
    IToolSwapperUpgrade, IAdvancedFilterable {
    companion object {
        private const val SHOULD_SWAP_WEAPON_TAG = "ShouldSwapWeapon"
        private const val TOOL_SWAP_MODE_TAG = "ToolSwapMode"
    }

    protected open val exposesAdvancedFilter = false
    override val settingsLangKey = "gui.tool_swapper_settings".asTranslationKey()
    var shouldSwapWeapon = true
    var toolSwapMode = ToolSwapMode.ANY
    override var matchType = IAdvancedFilterable.MatchType.ITEM
    override var oreDictEntries = mutableListOf<String>()
    override var ignoreDurability = true
    override var ignoreNBT = true
    private var lastMinedBlock: Block = Blocks.AIR
    private var toolCacheFor: String? = null
    private val toolCache = LinkedList<ItemStack>()

    init {
        filterType = IBasicFilterable.FilterType.BLACKLIST
    }

    override fun checkFilter(stack: ItemStack): Boolean =
        if (exposesAdvancedFilter) enabled && super<IAdvancedFilterable>.checkFilter(stack)
        else super<BasicUpgradeWrapper>.checkFilter(stack)

    override fun onBlockClick(player: EntityPlayer, wrapper: BackpackWrapper, pos: BlockPos, state: IBlockState): Boolean {
        if (!enabled || player.isCreative || player.isSpectator || toolSwapMode == ToolSwapMode.NO_SWAP || state.material == Material.AIR) {
            return false
        }

        val held = player.heldItemMainhand
        if (held.item is BackpackItem || (toolSwapMode == ToolSwapMode.ONLY_TOOLS && isSword(held)) || (!isSword(held) && isNotTool(held)) || !matchesAllowEmpty(held)) {
            return false
        }

        val heldSpeed = if (isGoodAtBreaking(player, pos, state, held)) {
            if (lastMinedBlock == state.block || state.getBlockHardness(player.world, pos) == 0f) {
                return true
            }
            held.getDestroySpeed(state)
        } else 0f
        lastMinedBlock = state.block
        val selectedSlot = findBestToolSlot(wrapper, player, pos, state, heldSpeed) ?: return false
        return swapMainHandWithBackpackSlot(player, wrapper, selectedSlot)
    }

    override fun onAttackEntity(player: EntityPlayer, wrapper: BackpackWrapper): Boolean {
        if (!enabled || !shouldSwapWeapon) {
            return false
        }

        val held = player.heldItemMainhand
        if (isSword(held)) {
            return true
        }
        if (held.item is BackpackItem || isNotTool(held) || !matchesAllowEmpty(held)) {
            return false
        }

        val selectedSlot = findBestWeaponSlot(wrapper, player, held) ?: return false
        return swapMainHandWithBackpackSlot(player, wrapper, selectedSlot)
    }

    override fun canProcessBlockInteract(): Boolean = swapToolOnKeyPress

    override fun onBlockInteract(player: EntityPlayer, wrapper: BackpackWrapper, world: World, pos: BlockPos, state: IBlockState): Boolean {
        if (!enabled || !swapToolOnKeyPress || player.heldItemMainhand.item is BackpackItem) {
            return false
        }
        return tryToSwapTool(player, wrapper, state.block.registryName?.toString()) {
            itemWorksOnBlock(world, pos, state, it)
        }
    }

    override fun canProcessEntityInteract(): Boolean = swapToolOnKeyPress

    override fun onEntityInteract(player: EntityPlayer, wrapper: BackpackWrapper, entity: Entity): Boolean {
        if (!enabled || !swapToolOnKeyPress || player.heldItemMainhand.item is BackpackItem) {
            return false
        }
        return tryToSwapTool(player, wrapper, entity.javaClass.name) { itemWorksOnEntity(entity, it) }
    }

    private fun findBestToolSlot(wrapper: BackpackWrapper, player: EntityPlayer, pos: BlockPos, state: IBlockState, heldSpeed: Float): Int? {
        var bestSlot: Int? = null
        var bestSpeed = heldSpeed
        for (slot in 0 until wrapper.slots) {
            val stack = wrapper.getStackInSlot(slot)
            if (stack.isEmpty || !matchesAllowEmpty(stack)) {
                continue
            }
            if (!canHarvestDropsWith(state, stack)) continue
            val destroyProgress = getDestroyProgressWith(player, pos, state, stack)
            val speed = stack.getDestroySpeed(state)
            if (speed <= 1.5f && destroyProgress < 1f) continue
            if (destroyProgress >= 1f) {
                return slot
            }
            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = slot
            }
        }
        return bestSlot
    }

    private fun findBestWeaponSlot(wrapper: BackpackWrapper, player: EntityPlayer, held: ItemStack): Int? {
        var bestSwordSlot: Int? = null
        var bestSwordDamage = if (isSword(held)) getAttackDamage(held, player) else 0.0
        var bestAxeSlot: Int? = null
        var bestAxeDamage = if (isAxe(held)) getAttackDamage(held, player) else 0.0
        for (slot in 0 until wrapper.slots) {
            val stack = wrapper.getStackInSlot(slot)
            if (stack.isEmpty || !matchesAllowEmpty(stack)) {
                continue
            }
            val damage = getAttackDamage(stack, player)
            if (isSword(stack)) {
                if (damage > bestSwordDamage) {
                    bestSwordDamage = damage
                    bestSwordSlot = slot
                }
            } else if (isAxe(stack) && damage > bestAxeDamage) {
                bestAxeDamage = damage
                bestAxeSlot = slot
            }
        }
        return bestSwordSlot ?: bestAxeSlot
    }

    private fun tryToSwapTool(
        player: EntityPlayer,
        wrapper: BackpackWrapper,
        targetRegistryName: String?,
        predicate: (ItemStack) -> Boolean
    ): Boolean {
        if (toolCacheFor != targetRegistryName) {
            toolCache.clear()
            toolCacheFor = targetRegistryName
        }

        val held = player.heldItemMainhand
        if (!held.isEmpty && predicate(held) && toolCache.none { isSameTool(it, held) }) {
            toolCache.offer(held.copy().also { it.count = 1 })
        }

        val selectedSlot = findToolToSwapSlot(wrapper, predicate) ?: return false
        val selectedTool = wrapper.getStackInSlot(selectedSlot).copy().also { it.count = 1 }
        if (!swapMainHandWithBackpackSlot(player, wrapper, selectedSlot)) {
            return false
        }
        toolCache.offer(selectedTool)
        return true
    }

    private fun findToolToSwapSlot(wrapper: BackpackWrapper, predicate: (ItemStack) -> Boolean): Int? {
        val alreadyGivenBefore = mutableListOf<ItemStack>()
        for (slot in 0 until wrapper.slots) {
            val stack = wrapper.getStackInSlot(slot)
            if (!stack.isEmpty && matchesAllowEmpty(stack) && predicate(stack)) {
                if (toolCache.none { isSameTool(it, stack) }) {
                    return slot
                }
                alreadyGivenBefore.add(stack.copy().also { it.count = 1 })
            }
        }

        while (toolCache.peek() != null) {
            val cached = toolCache.poll()
            if (alreadyGivenBefore.any { isSameTool(it, cached) }) {
                return findSlotWithSameTool(wrapper, cached, predicate)
            }
        }
        return null
    }

    private fun findSlotWithSameTool(wrapper: BackpackWrapper, tool: ItemStack, predicate: (ItemStack) -> Boolean): Int? {
        for (slot in 0 until wrapper.slots) {
            val stack = wrapper.getStackInSlot(slot)
            if (!stack.isEmpty && matchesAllowEmpty(stack) && predicate(stack) && isSameTool(stack, tool)) {
                return slot
            }
        }
        return null
    }

    private fun isSameTool(first: ItemStack, second: ItemStack): Boolean =
        !first.isEmpty && !second.isEmpty && first.item == second.item

    private fun swapMainHandWithBackpackSlot(player: EntityPlayer, wrapper: BackpackWrapper, slot: Int): Boolean {
        val held = player.heldItemMainhand
        val selectedStack = wrapper.getStackInSlot(slot)
        val tool = wrapper.extractItem(slot, 1, true)
        if (tool.isEmpty) {
            return false
        }
        val canStoreHeld = held.isEmpty ||
                wrapper.insertStack(held.copy(), true).isEmpty ||
                (selectedStack.count == 1 && wrapper.backpackItemStackHandler.isItemValid(slot, held))
        if (!canStoreHeld) return false

        val extractedTool = wrapper.extractItem(slot, 1, false)
        if (extractedTool.isEmpty) return false

        player.setHeldItem(EnumHand.MAIN_HAND, extractedTool)
        player.inventoryContainer.detectAndSendChanges()
        if (!held.isEmpty) {
            val remaining = wrapper.insertStack(held.copy(), false)
            if (!remaining.isEmpty) {
                wrapper.insertItem(slot, remaining, false)
            }
        }
        return true
    }

    private fun isGoodAtBreaking(player: EntityPlayer, pos: BlockPos, state: IBlockState, stack: ItemStack): Boolean =
        canHarvestDropsWith(state, stack) && (stack.getDestroySpeed(state) > 1.5f || getDestroyProgressWith(player, pos, state, stack) >= 1f)

    private fun canHarvestDropsWith(state: IBlockState, stack: ItemStack): Boolean =
        state.material.isToolNotRequired || stack.canHarvestBlock(state)

    private fun getDestroyProgressWith(player: EntityPlayer, pos: BlockPos, state: IBlockState, stack: ItemStack): Float {
        val held = player.heldItemMainhand
        player.setHeldItem(EnumHand.MAIN_HAND, stack)
        return try {
            state.getPlayerRelativeBlockHardness(player, player.world, pos)
        } finally {
            player.setHeldItem(EnumHand.MAIN_HAND, held)
        }
    }

    private fun isNotTool(stack: ItemStack): Boolean =
        stack.isEmpty || !isTool(stack)

    private fun isTool(stack: ItemStack): Boolean =
        stack.item is ItemAxe || stack.item is ItemHoe || stack.item is ItemPickaxe || stack.item is ItemSpade ||
                stack.item is ItemShears || stack.item.getToolClasses(stack).isNotEmpty() ||
                stack.getDestroySpeed(Blocks.STONE.defaultState) > 1f ||
                stack.getDestroySpeed(Blocks.DIRT.defaultState) > 1f ||
                stack.getDestroySpeed(Blocks.LOG.defaultState) > 1f

    private fun isSword(stack: ItemStack): Boolean =
        stack.item is ItemSword

    private fun isAxe(stack: ItemStack): Boolean =
        stack.item is ItemAxe || "axe" in stack.item.getToolClasses(stack)

    private fun getAttackDamage(stack: ItemStack, player: EntityPlayer): Double {
        if (stack.isEmpty) {
            return player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue
        }
        var damage = 0.0
        stack.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.name)
            .forEach { damage += it.amount }
        return damage
    }

    private fun itemWorksOnBlock(world: World, pos: BlockPos, state: IBlockState, stack: ItemStack): Boolean {
        if (stack.item is ItemShears && state.block is IShearable) {
            return (state.block as IShearable).isShearable(stack, world, pos)
        }
        if (stack.item is ItemSpade && state.block == Blocks.GRASS && world.isAirBlock(pos.up())) {
            return true
        }
        val harvestTool = state.block.getHarvestTool(state)
        return harvestTool != null && harvestTool in stack.item.getToolClasses(stack)
    }

    private fun itemWorksOnEntity(entity: Entity, stack: ItemStack): Boolean =
        stack.item is ItemShears && entity is IShearable && entity.isShearable(stack, entity.world, entity.position) ||
                entity is EntityCow && stack.item == Items.BUCKET ||
                entity is EntityAnimal && stack.item == Items.LEAD

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(SHOULD_SWAP_WEAPON_TAG, shouldSwapWeapon)
        nbt.setByte(TOOL_SWAP_MODE_TAG, toolSwapMode.ordinal.toByte())
        if (exposesAdvancedFilter) {
            nbt.setByte(IAdvancedFilterable.MATCH_TYPE_TAG, matchType.ordinal.toByte())
            nbt.setBoolean(IAdvancedFilterable.IGNORE_DURABILITY_TAG, ignoreDurability)
            nbt.setBoolean(IAdvancedFilterable.IGNORE_NBT_TAG, ignoreNBT)
            val oreDictList = NBTTagList()
            for (entry in oreDictEntries) {
                oreDictList.appendTag(NBTTagString(entry))
            }
            nbt.setTag(IAdvancedFilterable.ORE_DICT_LIST_TAG, oreDictList)
        }
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (filterItems.inventory.all(ItemStack::isEmpty) && filterType == IBasicFilterable.FilterType.WHITELIST) {
            filterType = IBasicFilterable.FilterType.BLACKLIST
        }
        shouldSwapWeapon = !nbt.hasKey(SHOULD_SWAP_WEAPON_TAG) || nbt.getBoolean(SHOULD_SWAP_WEAPON_TAG)
        if (nbt.hasKey(TOOL_SWAP_MODE_TAG)) {
            toolSwapMode = ToolSwapMode.entries.getOrElse(nbt.getByte(TOOL_SWAP_MODE_TAG).toInt()) { ToolSwapMode.ANY }
        }
        if (nbt.hasKey(IAdvancedFilterable.MATCH_TYPE_TAG)) {
            matchType = IAdvancedFilterable.MatchType.entries.getOrElse(nbt.getByte(IAdvancedFilterable.MATCH_TYPE_TAG).toInt()) { matchType }
        }
        if (nbt.hasKey(IAdvancedFilterable.IGNORE_DURABILITY_TAG)) {
            ignoreDurability = nbt.getBoolean(IAdvancedFilterable.IGNORE_DURABILITY_TAG)
        }
        if (nbt.hasKey(IAdvancedFilterable.IGNORE_NBT_TAG)) {
            ignoreNBT = nbt.getBoolean(IAdvancedFilterable.IGNORE_NBT_TAG)
        }
        if (nbt.hasKey(IAdvancedFilterable.ORE_DICT_LIST_TAG)) {
            val oreDictList = nbt.getTagList(IAdvancedFilterable.ORE_DICT_LIST_TAG, Constants.NBT.TAG_STRING)
            oreDictEntries.clear()
            for (stringNBT in oreDictList) {
                oreDictEntries.add((stringNBT as NBTTagString).string)
            }
        }
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.TOOL_SWAPPER_UPGRADE_CAPABILITY ||
                exposesAdvancedFilter && capability == Capabilities.ADVANCED_FILTERABLE_CAPABILITY ||
                capability == Capabilities.ITOOL_SWAPPER_UPGRADE_CAPABILITY ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)

    fun hasSettingsTab(): Boolean = hasSettingsTab
}

class AdvancedToolSwapperUpgradeWrapper : ToolSwapperUpgradeWrapper(true, true) {
    override val exposesAdvancedFilter = true
    override val settingsLangKey = "gui.advanced_tool_swapper_settings".asTranslationKey()

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_TOOL_SWAPPER_UPGRADE_CAPABILITY ||
                super.hasCapability(capability, facing)
}

enum class ToolSwapMode {
    ANY,
    ONLY_TOOLS,
    NO_SWAP;

    fun next(): ToolSwapMode =
        entries[(ordinal + 1) % entries.size]
}

package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.UpgradeItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
) : IItemHandler, ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "BackpackInventory"
        private const val UPGRADE_SLOTS_TAG = "UpgradeSlots"
        private const val BACKPACK_INVENTORY_SIZE_TAG = "BackpackInventorySize"
        private const val UPGRADE_SLOTS_SIZE_TAG = "UpgradeSlotsSize"

        private const val MEMORY_STACK_ITEMS_TAG = "MemoryItems"

        private const val UUID_TAG = "UUID"

        const val DEFAULT_MAIN_COLOR: Int = -0x339ec6
        const val DEFAULT_ACCENT_COLOR: Int = -0x9dd1e6
    }

    var isCached: Boolean = false
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
    var upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())
    var mainColor = DEFAULT_MAIN_COLOR
    var accentColor = DEFAULT_ACCENT_COLOR

    fun getTotalStackMultiplier(): Int =
        upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<StackUpgradeItem>()
            .fold(1) { acc, item -> acc * item.multiplier() }

    fun canAddStackUpgrade(newMultiplier: Int): Boolean {
        // Ensures no overflow for vanilla itemstack, no guarantee for modded itemstack
        val currentMultiplier = getTotalStackMultiplier() * 64

        try {
            Math.multiplyExact(currentMultiplier, newMultiplier)

            return true
        } catch (_: ArithmeticException) {
            return false
        }
    }

    fun canRemoveStackUpgrade(originalMultiplier: Int): Boolean =
        canReplaceStackUpgrade(originalMultiplier, 1)

    fun canReplaceStackUpgrade(oldMultiplier: Int, newMultiplier: Int): Boolean {
        val newStackMultiplier = getTotalStackMultiplier() / oldMultiplier * newMultiplier

        for (stack in backpackItemStackHandler.inventory) {
            if (stack.isEmpty)
                continue

            if (stack.count > stack.maxStackSize * newStackMultiplier)
                return false
        }

        return true
    }

    fun canNestBackpack(): Boolean =
        upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<InceptionUpgradeItem>().any()

    fun canRemoveInceptionUpgrade(): Boolean =
        !backpackItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<BackpackItem>().any() ||
                upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<InceptionUpgradeItem>()
                    .count() > 1

    fun canPickupItem(stack: ItemStack): Boolean =
        gatherCapabilityUpgrades(Capabilities.IPICKUP_UPGRADE_CAPABILITY)
            .any { it.canPickup(stack) }

    fun getFeedingStack(foodLevel: Int, health: Float, maxHealth: Float): ItemStack {
        val feedingUpgrades = gatherCapabilityUpgrades(Capabilities.IFEEDING_UPGRADE_CAPABILITY)

        for (upgrade in feedingUpgrades) {
            val feedingStack = upgrade.getFeedingStack(this, foodLevel, health, maxHealth)

            if (!feedingStack.isEmpty)
                return feedingStack
        }

        return ItemStack.EMPTY
    }

    fun canDeposit(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex)
        return gatherCapabilityUpgrades(Capabilities.IDEPOSIT_UPGRADE_CAPABILITY)
            .any { it.canDeposit(stack) }
    }

    fun canRestock(stack: ItemStack): Boolean =
        gatherCapabilityUpgrades(Capabilities.IRESTOCK_UPGRADE_CAPABILITY)
            .any { it.canRestock(stack) }

    fun canInsert(stack: ItemStack): Boolean {
        val filterUpgrades = gatherCapabilityUpgrades(Capabilities.IFILTER_UPGRADE_CAPABILITY)
            .filter { it.enabled }

        return if (filterUpgrades.isEmpty()) true
        else filterUpgrades.any { it.canInsert(stack) }
    }

    fun canExtract(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex)
        val filterUpgrades = gatherCapabilityUpgrades(Capabilities.IFILTER_UPGRADE_CAPABILITY)
            .filter { it.enabled }

        return if (filterUpgrades.isEmpty()) true
        else filterUpgrades.any { it.canInsert(stack) }
    }

    // Setting related

    fun isSlotMemorized(slotIndex: Int): Boolean =
        !backpackItemStackHandler.memoryStack[slotIndex].isEmpty

    fun setMemoryStack(slotIndex: Int) {
        val currentStack = getStackInSlot(slotIndex)

        if (currentStack.isEmpty)
            return

        val copiedStack = currentStack.copy()
        copiedStack.count = 1

        backpackItemStackHandler.memoryStack[slotIndex] = copiedStack
    }

    fun unsetMemoryStack(slotIndex: Int) {
        backpackItemStackHandler.memoryStack[slotIndex] = ItemStack.EMPTY
    }

    // Overrides

    fun getDisplayName(): ITextComponent =
        TextComponentTranslation("container.backpack".asTranslationKey())

    private fun <T> gatherCapabilityUpgrades(capability: Capability<T>): List<T> =
        upgradeItemStackHandler.inventory
            .mapNotNull { it.getCapability(capability, null) }

    override fun getSlots(): Int =
        backpackItemStackHandler.slots

    override fun getStackInSlot(index: Int): ItemStack =
        backpackItemStackHandler.getStackInSlot(index)

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
        backpackItemStackHandler.insertItem(slot, stack, simulate)

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
        backpackItemStackHandler.extractItem(slot, amount, simulate)

    override fun getSlotLimit(slot: Int): Int =
        backpackItemStackHandler.getSlotLimit(slot)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.BACKPACK_CAPABILITY ||
                capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        val backpackNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(backpackNbt, backpackItemStackHandler.inventory)
        nbt.setTag(BACKPACK_INVENTORY_TAG, backpackNbt)
        val upgradesNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(upgradesNbt, upgradeItemStackHandler.inventory)
        nbt.setTag(UPGRADE_SLOTS_TAG, upgradesNbt)
        nbt.setInteger(BACKPACK_INVENTORY_SIZE_TAG, backpackInventorySize())
        nbt.setInteger(UPGRADE_SLOTS_SIZE_TAG, upgradeSlotsSize())

        // Settings
        val memoryNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(memoryNbt, backpackItemStackHandler.memoryStack)
        nbt.setTag(MEMORY_STACK_ITEMS_TAG, memoryNbt)

        nbt.setUniqueId(UUID_TAG, uuid)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey(BACKPACK_INVENTORY_SIZE_TAG))
            backpackInventorySize = { nbt.getInteger(BACKPACK_INVENTORY_SIZE_TAG) }
        if (nbt.hasKey(UPGRADE_SLOTS_SIZE_TAG))
            upgradeSlotsSize = { nbt.getInteger(UPGRADE_SLOTS_SIZE_TAG) }

        uuid = nbt.getUniqueId(UUID_TAG)!!

        backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
        upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())

        if (nbt.hasKey(BACKPACK_INVENTORY_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(
                nbt.getCompoundTag(BACKPACK_INVENTORY_TAG),
                backpackItemStackHandler.inventory
            )

        if (nbt.hasKey(UPGRADE_SLOTS_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(
                nbt.getCompoundTag(UPGRADE_SLOTS_TAG),
                upgradeItemStackHandler.inventory
            )

        // Settings
        BackpackItemStackHelper.loadAllItemsExtended(
            nbt.getCompoundTag(MEMORY_STACK_ITEMS_TAG),
            backpackItemStackHandler.memoryStack
        )
    }
}
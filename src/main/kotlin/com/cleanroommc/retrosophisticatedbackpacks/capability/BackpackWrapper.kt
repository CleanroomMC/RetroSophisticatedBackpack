package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.UpgradeItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
) : IInventory, ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "BackpackInventory"
        private const val UPGRADE_SLOTS_TAG = "UpgradeSlots"
        private const val BACKPACK_INVENTORY_SIZE_TAG = "BackpackInventorySize"
        private const val UPGRADE_SLOTS_SIZE_TAG = "UpgradeSlotsSize"
        private const val UUID_TAG = "UUID"

        const val DEFAULT_MAIN_COLOR: Int = -0x339ec6
        const val DEFAULT_ACCENT_COLOR: Int = -0x9dd1e6
    }

    var isCached: Boolean = false
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), ::getTotalStackMultiplier)
    var upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())
    var mainColor = DEFAULT_MAIN_COLOR
    var accentColor = DEFAULT_ACCENT_COLOR

    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(Capabilities.BACKPACK_CAPABILITY)

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
        upgradeItemStackHandler.inventory
            .mapNotNull { it.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) }
            .any { it.checkFilter(stack) }

    override fun getSizeInventory(): Int =
        backpackInventorySize()

    override fun isEmpty(): Boolean =
        backpackItemStackHandler.inventory.all(ItemStack::isEmpty)

    override fun getStackInSlot(index: Int): ItemStack =
        backpackItemStackHandler.getStackInSlot(index)

    override fun decrStackSize(index: Int, count: Int): ItemStack =
        backpackItemStackHandler.extractItem(index, count, false)

    override fun removeStackFromSlot(index: Int): ItemStack =
        backpackItemStackHandler.extractItem(index, 64, false)

    fun insertStack(index: Int, stack: ItemStack): ItemStack =
        backpackItemStackHandler.insertItem(index, stack, false)

    override fun setInventorySlotContents(index: Int, stack: ItemStack) {
        backpackItemStackHandler.setStackInSlot(index, stack)
    }

    @Deprecated(
        "Use backpackItemStackHandler.getStackLimit(Int,ItemStack) instead",
        ReplaceWith("backpackItemStackHandler.getStackLimit(index,stack)")
    )
    override fun getInventoryStackLimit(): Int =
        Int.MAX_VALUE

    override fun markDirty() {
    }

    override fun isUsableByPlayer(player: EntityPlayer): Boolean =
        true

    override fun openInventory(player: EntityPlayer) {
    }

    override fun closeInventory(player: EntityPlayer) {
    }

    override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
        return backpackItemStackHandler.isItemValid(index, stack)
    }

    override fun getField(id: Int): Int =
        0

    override fun setField(id: Int, value: Int) {}

    override fun getFieldCount(): Int =
        0

    override fun clear() {
        backpackItemStackHandler.inventory.clear()
    }

    override fun getName(): String =
        "container.backpack"

    override fun hasCustomName(): Boolean =
        false

    override fun getDisplayName(): ITextComponent =
        TextComponentTranslation(name.asTranslationKey())

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
        nbt.setUniqueId(UUID_TAG, uuid)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey(BACKPACK_INVENTORY_SIZE_TAG))
            backpackInventorySize = { nbt.getInteger(BACKPACK_INVENTORY_SIZE_TAG) }
        if (nbt.hasKey(UPGRADE_SLOTS_SIZE_TAG))
            upgradeSlotsSize = { nbt.getInteger(UPGRADE_SLOTS_SIZE_TAG) }

        uuid = nbt.getUniqueId(UUID_TAG)!!

        backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), ::getTotalStackMultiplier)
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
    }
}
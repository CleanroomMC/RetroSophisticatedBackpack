package com.cleanroommc.retrosophisticatedbackpack.backpack

import com.cleanroommc.retrosophisticatedbackpack.handlers.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpack.items.BackpackItem
import com.cleanroommc.retrosophisticatedbackpack.items.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.items.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.utils.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
) : ICapabilityProvider, INBTSerializable<NBTTagCompound> {
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
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize())
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

    fun getDisplayName(): ITextComponent =
        TextComponentTranslation("container.backpack".asTranslationKey())

    override fun hasCapability(
        capability: Capability<*>,
        facing: EnumFacing?
    ): Boolean =
        facing == null && capability == CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY

    override fun <T> getCapability(
        capability: Capability<T>,
        facing: EnumFacing?
    ): T? =
        if (hasCapability(capability, facing)) CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!.cast(this) else null

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

        backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize())
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
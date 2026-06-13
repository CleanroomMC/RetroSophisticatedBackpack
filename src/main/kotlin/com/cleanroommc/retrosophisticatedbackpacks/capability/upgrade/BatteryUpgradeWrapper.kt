package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BatteryUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.items.IItemHandler

class BatteryUpgradeWrapper : UpgradeWrapper<BatteryUpgradeItem>(), IBatteryUpgrade {
    companion object {
        private const val ENERGY_TAG = "Energy"
        private const val INVENTORY_TAG = "Inventory"
        private const val INPUT_SLOT = 0
        private const val OUTPUT_SLOT = 1
    }

    override val settingsLangKey = "gui.battery_settings".asTranslationKey()
    override var energyStored = 0
        private set
    override val canExtractEnergy = true
    override val canReceiveEnergy = true

    private val inventory = object : ExposedItemStackHandler(2) {
        override fun getSlotLimit(slot: Int): Int = 1

        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.isEmpty || when (slot) {
                INPUT_SLOT -> isValidEnergyItem(stack, output = false)
                OUTPUT_SLOT -> isValidEnergyItem(stack, output = true)
                else -> false
            }
    }

    override fun getMaxEnergyStored(wrapper: BackpackWrapper): Int =
        getMaxEnergyStored(wrapper, wrapper.getTotalStackMultiplier())

    override fun getMaxEnergyStored(wrapper: BackpackWrapper, stackMultiplier: Int): Int =
        (getSlotRows(wrapper) * Config.batteryUpgrade.energyPerSlotRow * getAdjustedStackMultiplier(stackMultiplier)).toInt()

    override fun receiveEnergy(wrapper: BackpackWrapper, maxReceive: Int, simulate: Boolean): Int {
        val accepted = minOf(maxReceive, getMaxInOut(wrapper), getMaxEnergyStored(wrapper) - energyStored)
        if (!simulate && accepted > 0) {
            energyStored += accepted
        }
        return accepted.coerceAtLeast(0)
    }

    override fun extractEnergy(wrapper: BackpackWrapper, maxExtract: Int, simulate: Boolean): Int {
        val extracted = minOf(maxExtract, getMaxInOut(wrapper), energyStored)
        if (!simulate && extracted > 0) {
            energyStored -= extracted
        }
        return extracted.coerceAtLeast(0)
    }

    override fun tick(wrapper: BackpackWrapper, world: World) {
        if (world.isRemote) {
            return
        }
        if (energyStored < getMaxEnergyStored(wrapper)) {
            receiveFromContainer(wrapper)
        }
        if (energyStored > 0) {
            extractToContainer(wrapper)
        }
    }

    override fun getInventory(): IItemHandler =
        inventory

    private fun receiveFromContainer(wrapper: BackpackWrapper) {
        val stack = inventory.getStackInSlot(INPUT_SLOT)
        val energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null) ?: return
        val toReceive = receiveEnergy(wrapper, getMaxInOut(wrapper), true)
        val extracted = energyStorage.extractEnergy(toReceive, true)
        if (extracted <= 0) {
            return
        }
        energyStorage.extractEnergy(extracted, false)
        receiveEnergy(wrapper, extracted, false)
        inventory.setStackInSlot(INPUT_SLOT, stack)
    }

    private fun extractToContainer(wrapper: BackpackWrapper) {
        val stack = inventory.getStackInSlot(OUTPUT_SLOT)
        val energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null) ?: return
        val toExtract = extractEnergy(wrapper, getMaxInOut(wrapper), true)
        val received = energyStorage.receiveEnergy(toExtract, true)
        if (received <= 0) {
            return
        }
        energyStorage.receiveEnergy(received, false)
        extractEnergy(wrapper, received, false)
        inventory.setStackInSlot(OUTPUT_SLOT, stack)
    }

    private fun isValidEnergyItem(stack: ItemStack, output: Boolean): Boolean {
        val energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null) ?: return false
        return if (output) energyStorage.canReceive() else energyStorage.canExtract() && energyStorage.energyStored > 0
    }

    private fun getMaxInOut(wrapper: BackpackWrapper): Int =
        maxOf(1, (getSlotRows(wrapper) * Config.batteryUpgrade.maxInputOutput * getAdjustedStackMultiplier(wrapper.getTotalStackMultiplier())).toInt())

    private fun getSlotRows(wrapper: BackpackWrapper): Int =
        maxOf(1, (wrapper.backpackInventorySize() + 8) / 9)

    private fun getAdjustedStackMultiplier(stackMultiplier: Int): Double =
        1.0 + Config.batteryUpgrade.stackMultiplierRatio * (stackMultiplier - 1)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setInteger(ENERGY_TAG, energyStored)
        nbt.setTag(INVENTORY_TAG, inventory.serializeNBT())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(ENERGY_TAG))
            energyStored = nbt.getInteger(ENERGY_TAG)
        if (nbt.hasKey(INVENTORY_TAG))
            inventory.deserializeNBT(nbt.getCompoundTag(INVENTORY_TAG))
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.BATTERY_UPGRADE_CAPABILITY ||
                capability == Capabilities.IBATTERY_UPGRADE_CAPABILITY ||
                super<UpgradeWrapper>.hasCapability(capability, facing)
}

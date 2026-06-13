package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.AnvilUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerRepair
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler

class AnvilUpgradeWrapper : UpgradeWrapper<AnvilUpgradeItem>(), IAnvilUpgrade {
    companion object {
        private const val INVENTORY_TAG = "Inventory"
        private const val SHIFT_CLICK_TAG = "ShiftClickIntoStorage"
        private const val ITEM_NAME_TAG = "ItemName"
        private const val MAXIMUM_COST_TAG = "MaximumCost"
        private const val MATERIAL_COST_TAG = "MaterialCost"
        private const val RESULT_TAG = "Result"
    }

    override val settingsLangKey = "gui.anvil_settings".asTranslationKey()
    override var shouldShiftClickIntoStorage = true
    override var itemName = ""
    override var maximumCost = 0
        private set
    override var materialCost = 0
        private set
    private var result = ItemStack.EMPTY
    private val inventory = object : ExposedItemStackHandler(2) {
        override fun onContentsChanged(slot: Int) {
            result = ItemStack.EMPTY
            maximumCost = 0
            materialCost = 0
        }
    }

    override fun getInventory(): IItemHandler =
        inventory

    override fun updateRepairOutput(player: EntityPlayer, world: World): ItemStack {
        val container = createContainer(player, world)
        maximumCost = container.maximumCost
        materialCost = container.materialCost
        result = container.getSlot(2).stack.copy()
        return result.copy()
    }

    override fun canTakeResult(player: EntityPlayer): Boolean =
        !result.isEmpty && maximumCost > 0 && (player.capabilities.isCreativeMode || player.experienceLevel >= maximumCost)

    override fun takeResult(player: EntityPlayer, world: World): ItemStack {
        updateRepairOutput(player, world)
        if (!canTakeResult(player)) {
            return ItemStack.EMPTY
        }
        val container = createContainer(player, world)
        val resultSlot = container.getSlot(2)
        val taken = resultSlot.stack.copy()
        resultSlot.onTake(player, taken.copy())
        inventory.setStackInSlot(0, container.getSlot(0).stack.copy())
        inventory.setStackInSlot(1, container.getSlot(1).stack.copy())
        result = ItemStack.EMPTY
        maximumCost = 0
        materialCost = 0
        return taken
    }

    private fun createContainer(player: EntityPlayer, world: World): ContainerRepair {
        val container = ContainerRepair(player.inventory, world, BlockPos(player), player)
        container.getSlot(0).putStack(inventory.getStackInSlot(0).copy())
        container.getSlot(1).putStack(inventory.getStackInSlot(1).copy())
        container.updateItemName(itemName)
        container.updateRepairOutput()
        return container
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setTag(INVENTORY_TAG, inventory.serializeNBT())
        nbt.setBoolean(SHIFT_CLICK_TAG, shouldShiftClickIntoStorage)
        nbt.setString(ITEM_NAME_TAG, itemName)
        nbt.setInteger(MAXIMUM_COST_TAG, maximumCost)
        nbt.setInteger(MATERIAL_COST_TAG, materialCost)
        if (!result.isEmpty) {
            nbt.setTag(RESULT_TAG, result.writeToNBT(NBTTagCompound()))
        }
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(INVENTORY_TAG))
            inventory.deserializeNBT(nbt.getCompoundTag(INVENTORY_TAG))
        if (nbt.hasKey(SHIFT_CLICK_TAG))
            shouldShiftClickIntoStorage = nbt.getBoolean(SHIFT_CLICK_TAG)
        if (nbt.hasKey(ITEM_NAME_TAG))
            itemName = nbt.getString(ITEM_NAME_TAG)
        if (nbt.hasKey(MAXIMUM_COST_TAG))
            maximumCost = nbt.getInteger(MAXIMUM_COST_TAG)
        if (nbt.hasKey(MATERIAL_COST_TAG))
            materialCost = nbt.getInteger(MATERIAL_COST_TAG)
        if (nbt.hasKey(RESULT_TAG))
            result = ItemStack(nbt.getCompoundTag(RESULT_TAG))
        else if (nbt.hasKey(INVENTORY_TAG))
            result = ItemStack.EMPTY
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ANVIL_UPGRADE_CAPABILITY ||
                capability == Capabilities.IANVIL_UPGRADE_CAPABILITY ||
                super<UpgradeWrapper>.hasCapability(capability, facing)
}

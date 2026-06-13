package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemStackHandler

class UpgradeItemStackHandler(size: Int, private val wrapper: BackpackWrapper? = null) : ItemStackHandler(size) {
    val inventory: NonNullList<ItemStack> =
        stacks

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        val original = getStackInSlot(slot)
        if (!original.isEmpty && !ItemStack.areItemsEqual(original, stack)) {
            original.getCapability(Capabilities.UPGRADE_CAPABILITY, null)?.onBeforeRemoved()
        }
        super.setStackInSlot(slot, stack)
        wrapper?.ensureCapturedMobLayoutCurrent()
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (!simulate && amount > 0) {
            val original = getStackInSlot(slot)
            if (!original.isEmpty) {
                original.getCapability(Capabilities.UPGRADE_CAPABILITY, null)?.onBeforeRemoved()
            }
        }
        return super.extractItem(slot, amount, simulate).also {
            if (!simulate && !it.isEmpty) {
                wrapper?.ensureCapturedMobLayoutCurrent()
            }
        }
    }
}

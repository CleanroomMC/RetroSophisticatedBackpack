package com.cleanroommc.retrosophisticatedbackpacks.capability

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandlerItem
import net.minecraftforge.fluids.capability.IFluidTankProperties

class BackpackFluidItemHandler(
    private val container: ItemStack,
    wrapper: BackpackWrapper
) : IFluidHandlerItem {
    private val delegate = BackpackFluidHandler(wrapper)

    override fun getContainer(): ItemStack = container

    override fun getTankProperties(): Array<IFluidTankProperties> =
        delegate.tankProperties

    override fun fill(resource: FluidStack?, doFill: Boolean): Int =
        delegate.fill(resource, doFill)

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? =
        delegate.drain(resource, doDrain)

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? =
        delegate.drain(maxDrain, doDrain)
}

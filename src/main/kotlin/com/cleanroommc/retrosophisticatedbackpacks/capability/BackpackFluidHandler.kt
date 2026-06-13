package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.TankUpgradeWrapper
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidTankProperties

class BackpackFluidHandler(private val wrapper: BackpackWrapper) : IFluidHandler {
    private fun tanks(): List<TankUpgradeWrapper> =
        wrapper.gatherCapabilityUpgrades(Capabilities.TANK_UPGRADE_CAPABILITY)
            .filterIsInstance<TankUpgradeWrapper>()

    override fun getTankProperties(): Array<IFluidTankProperties> =
        tanks().flatMap { it.getTankProperties(wrapper).toList() }.toTypedArray()

    override fun fill(resource: FluidStack?, doFill: Boolean): Int {
        if (resource == null || resource.amount <= 0) {
            return 0
        }
        var filled = 0
        for (tank in tanks()) {
            val toFill = FluidStack(resource.fluid, resource.amount - filled, resource.tag?.copy())
            filled += tank.fill(wrapper, toFill, doFill)
            if (filled >= resource.amount) {
                return resource.amount
            }
        }
        return filled
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        if (resource == null || resource.amount <= 0) {
            return null
        }
        var drained = 0
        for (tank in tanks()) {
            val stack = tank.drain(wrapper, FluidStack(resource.fluid, resource.amount - drained, resource.tag?.copy()), doDrain) ?: continue
            drained += stack.amount
            if (drained >= resource.amount) {
                return FluidStack(resource.fluid, resource.amount, resource.tag?.copy())
            }
        }
        return if (drained > 0) FluidStack(resource.fluid, drained, resource.tag?.copy()) else null
    }

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
        for (tank in tanks()) {
            val drained = tank.drain(wrapper, maxDrain, doDrain)
            if (drained != null && drained.amount > 0) {
                return drained
            }
        }
        return null
    }
}

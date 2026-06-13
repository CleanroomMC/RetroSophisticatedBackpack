package com.cleanroommc.retrosophisticatedbackpacks.capability

import net.minecraftforge.energy.IEnergyStorage

class BackpackEnergyStorage(private val wrapper: BackpackWrapper) : IEnergyStorage {
    private fun batteries() =
        wrapper.gatherCapabilityUpgrades(Capabilities.IBATTERY_UPGRADE_CAPABILITY)

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        var remaining = maxReceive
        var received = 0
        for (battery in batteries()) {
            val moved = battery.receiveEnergy(wrapper, remaining, simulate)
            received += moved
            remaining -= moved
            if (remaining <= 0) {
                break
            }
        }
        return received
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
        var remaining = maxExtract
        var extracted = 0
        for (battery in batteries()) {
            val moved = battery.extractEnergy(wrapper, remaining, simulate)
            extracted += moved
            remaining -= moved
            if (remaining <= 0) {
                break
            }
        }
        return extracted
    }

    override fun getEnergyStored(): Int =
        batteries().fold(0) { acc, battery -> acc + battery.energyStored }

    override fun getMaxEnergyStored(): Int =
        batteries().fold(0) { acc, battery -> acc + battery.getMaxEnergyStored(wrapper) }

    override fun canExtract(): Boolean =
        batteries().any { it.canExtractEnergy }

    override fun canReceive(): Boolean =
        batteries().any { it.canReceiveEnergy }
}

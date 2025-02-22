package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

interface IToggleable : ISidelessCapabilityProvider {
    companion object {
        const val ENABLED_TAG = "Enabled"
    }

    var enabled: Boolean

    fun toggle() {
        enabled = !enabled
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.TOGGLEABLE_CAPABILITY

    object Impl : IToggleable {
        override var enabled: Boolean
            get() = false
            set(_) {}
    }
}
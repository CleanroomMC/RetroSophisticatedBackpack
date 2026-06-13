package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.MobCatcherUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class MobCatcherUpgradeWrapper(private val advanced: Boolean = false) : UpgradeWrapper<MobCatcherUpgradeItem>() {
    override val settingsLangKey: String = ""

    val isAdvanced: Boolean
        get() = advanced

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.MOB_CATCHER_UPGRADE_CAPABILITY ||
                super.hasCapability(capability, facing)
}

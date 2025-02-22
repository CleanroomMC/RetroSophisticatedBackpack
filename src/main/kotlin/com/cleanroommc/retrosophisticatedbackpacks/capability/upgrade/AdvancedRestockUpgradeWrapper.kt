package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.RestockUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedRestockUpgradeWrapper : AdvancedUpgradeWrapper<RestockUpgradeItem>(), IRestockUpgrade {
    override val settingsLangKey: String = "gui.advanced_restock_settings".asTranslationKey()

    override fun canRestock(stack: ItemStack): Boolean =
        super.checkFilter(stack)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_RESTOCK_UPGRADE_CAPABILITY ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing) ||
                super<IRestockUpgrade>.hasCapability(capability, facing)
}
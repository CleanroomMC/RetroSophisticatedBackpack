package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class DepositUpgradeWidget(
    slotIndex: Int,
    wrapper: PickupUpgradeWrapper
) : ExpandedTabWidget(3, ItemStack(Items.depositUpgrade)) {
    val filterWidget: BasicFilterWidget

    init {
        size(70, 120)

        filterWidget = BasicFilterWidget(wrapper, slotIndex)
            .leftRel(0.5f)
            .top(36)

        child(filterWidget)
            .child(
                TextWidget(IKey.lang("gui.deposit_settings".asTranslationKey()))
                    .size(40, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }
}

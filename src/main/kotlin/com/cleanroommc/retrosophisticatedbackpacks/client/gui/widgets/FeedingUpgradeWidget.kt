package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.FeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class FeedingUpgradeWidget(slotIndex: Int, wrapper: FeedingUpgradeWrapper) :
    BasicExpandedTabWidget<FeedingUpgradeWrapper>(
        slotIndex,
        wrapper,
        ItemStack(Items.feedingUpgrade),
        "gui.feeding_settings".asTranslationKey(),
        filterSyncKey = "feeding_filter"
    )
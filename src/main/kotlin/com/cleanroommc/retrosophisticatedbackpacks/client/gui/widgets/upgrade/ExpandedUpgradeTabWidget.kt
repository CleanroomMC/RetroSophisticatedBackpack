package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.ExpandedTabWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

abstract class ExpandedUpgradeTabWidget<U>(
    slotIndex: Int,
    coveredTabSize: Int,
    delegatedIconStack: ItemStack,
    titleKey: String,
    width: Int = 75
) : ExpandedTabWidget(
    coveredTabSize,
    ItemDrawable(delegatedIconStack),
    titleKey,
    width
) where U : UpgradeWrapper<*> {
    abstract val wrapper: U
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        syncHandler("upgrades", slotIndex)
    }

    override fun updateTabState() {
        wrapper.isTabOpened = !wrapper.isTabOpened
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE) {
            it.writeBoolean(wrapper.isTabOpened)
        }
    }

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }
}
package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.value.ISyncOrValue
import com.cleanroommc.modularui.drawable.ItemDrawable
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
    fun isSameWrapper(other: UpgradeWrapper<*>): Boolean{
        return true//(wrapper === other)
    }

    override fun updateTabState() {
        wrapper.isTabOpened = !wrapper.isTabOpened
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE) {
            it.writeBoolean(wrapper.isTabOpened)
        }
    }

    override fun isValidSyncOrValue(syncHandler: ISyncOrValue): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }
}
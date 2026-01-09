package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.value.ISyncOrValue
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.ExpandedTabWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

abstract class ExpandedUpgradeTabWidget<U>(
    slotIndex: Int,
    wrap: U,
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
    protected var slotSyncHandler: UpgradeSlotSH? = null
    open var wrapper: U = wrap
        set(value) {
            field = value
            onWrapperChange(value)
        }


    init {
        syncHandler("upgrades", slotIndex)
    }

    /**
     * Since it may be difficult to infer the wrapper's generic type for a given subclass (especially if multiple constraints are imposed),
     * this method, albeit hacky, is provided to allow for setting the wrapper without knowing the required class.
     * @return whether the cast was a success.
     */
    fun consumePossibleWrapper(after: Any): Boolean {
        if (after::class == wrapper::class) {
            @Suppress("UNCHECKED_CAST")
            wrapper = after as U
            return true
        }
        return false
    }

    open fun onWrapperChange(after: U) {

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
package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.utils.Alignment
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

abstract class ExpandedTabWidget<T>(
    slotIndex: Int,
    val coveredTabSize: Int,
    delegatedIconStack: ItemStack,
    titleKey: String,
    width: Int = 75
) : ParentWidget<ExpandedTabWidget<T>>() where T : UpgradeWrapper<*> {
    companion object {
        val TAB_TEXTURE: UITexture = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 128, 256)
            .adaptable(4)
            .tiled()
            .build()
    }

    abstract val wrapper: T
    protected val phantomTabWidget: PhantomTabWidget = PhantomTabWidget(ItemDrawable(delegatedIconStack).asWidget())
    protected val titleKeyWidget: TextWidget = TextWidget(IKey.lang(titleKey))
        .alignment(Alignment.Center)
        .topRel(0.5F)
        .expanded()
    protected val upperTabRow: Row = Row()
        .coverChildrenHeight()
        .width(width - 3)
        .child(phantomTabWidget)
        .child(titleKeyWidget) as Row
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        syncHandler("upgrades", slotIndex)

        width(width)
            .height(coveredTabSize * 30)
            .background(TAB_TEXTURE)
            .child(upperTabRow)
    }

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    fun updateTabState() {
        wrapper.isTabOpened = !wrapper.isTabOpened
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE) {
            it.writeBoolean(wrapper.isTabOpened)
        }
    }

    protected inner class PhantomTabWidget(tabIcon: Widget<*>) : SingleChildWidget<PhantomTabWidget>(), Interactable {
        init {
            size(TabWidget.TAB_TEXTURE.width - 5, TabWidget.TAB_TEXTURE.height)
                .child(tabIcon.size(16).pos(8, 6))
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            if (!isEnabled)
                return Interactable.Result.STOP

            if (mouseButton == 0) {
                updateTabState()
                Interactable.playButtonClickSound()
                return Interactable.Result.SUCCESS
            }

            return Interactable.Result.STOP
        }
    }
}
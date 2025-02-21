package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

class UpgradeSlotGroupWidget(panel: BackpackPanel, private val slotSize: Int) : SlotGroupWidget() {
    companion object {
        private val UPPER_TAB_TEXTURE =
            UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
                .uv(0, 0, 25, 5).build()
        private val SLOT_SURROUNDING_TEXTURE =
            UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
                .uv(0, 5, 25, 18).build()
        private val LOWER_TAB_TEXTURE =
            UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
                .uv(0, 199, 25, 5).build()
    }

    val toggleWidgets: List<UpgradeToggleWidget>

    init {
        toggleWidgets = mutableListOf<UpgradeToggleWidget>()

        for (i in 0 until slotSize) {
            val toggleWidget = UpgradeToggleWidget(panel, i)
                .syncHandler("upgrades", i)
                .debugName("upgrade_toggle_$i")

            toggleWidgets.add(toggleWidget)
            child(toggleWidget)
        }
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)
        var y = 5

        UPPER_TAB_TEXTURE.draw(context, 0, 0, 25, 5, widgetTheme)

        for (i in 0 until slotSize) {
            SLOT_SURROUNDING_TEXTURE.draw(context, 0, y, 25, 18, widgetTheme)
            y += 18
        }

        LOWER_TAB_TEXTURE.draw(context, 0, y, 25, 5, widgetTheme)
    }

    class UpgradeToggleWidget(private val panel: BackpackPanel, private val slotIndex: Int) :
        Widget<UpgradeToggleWidget>(), Interactable {
        companion object {
            private const val WIDTH = 9
            private const val HEIGHT = 18

            private val BACKGROUND_TAB_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls.png")
                .imageSize(256, 256)
                .uv(0, 204, WIDTH, HEIGHT)
                .build()
        }

        var isToggleEnabled = false
        private var slotSyncHandler: UpgradeSlotSH? = null

        init {
            size(WIDTH, HEIGHT).left(-4).top(slotIndex * 18 + 4)
            isEnabled = false

            val wrapper = getWrapper()

            if (wrapper != null) {
                isToggleEnabled = wrapper.enabled
                isEnabled = true
            }
        }

        fun getWrapper(): IToggleable? {
            val stack: ItemStack = panel.backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
            return stack.getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null)
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            isToggleEnabled = !isToggleEnabled

            panel.changedByPropertyChange = true
            getWrapper()?.toggle()
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TOGGLE)

            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
            if (syncHandler is UpgradeSlotSH)
                slotSyncHandler = syncHandler
            return slotSyncHandler != null
        }

        override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
            super.drawOverlay(context, widgetTheme)

            if (isToggleEnabled) {
                RSBTextures.TOGGLE_ENABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme)
            } else {
                RSBTextures.TOGGLE_DISABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme)
            }
        }

        override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetTheme) {
            super.drawBackground(context, widgetTheme)

            BACKGROUND_TAB_TEXTURE.draw(context, 0, 0, WIDTH, HEIGHT, widgetTheme)
        }
    }
}
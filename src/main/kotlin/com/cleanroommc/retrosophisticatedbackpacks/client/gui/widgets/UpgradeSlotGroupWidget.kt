package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.value.ISyncOrValue
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.item.ItemStack

class UpgradeSlotGroupWidget(private val panel: BackpackPanel, private val slotSize: Int) : SlotGroupWidget() {
    companion object {
        private val GUI_CONTROLS =
            UITexture.fullImage(Tags.MOD_ID, "gui/gui_controls.png")
        private val UPPER_TAB_TEXTURE =
            UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
                .xy(0, 0, 26, 4).build()
        private val LOWER_TAB_TEXTURE =
            UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
                .xy(0, 198, 25, 6).build()
    }

    val toggleWidgets: List<UpgradeToggleWidget>

    init {
        toggleWidgets = mutableListOf<UpgradeToggleWidget>()

        for (i in 0 until slotSize) {
            val toggleWidget = UpgradeToggleWidget(panel, i)
                .syncHandler("upgrades", i)
                .name("upgrade_toggle_$i")

            toggleWidgets.add(toggleWidget)
            child(toggleWidget)
        }
    }

    override fun onInit() {
        context.recipeViewerSettings.addExclusionArea(this)
    }

    override fun dispose() {
        if (isValid)
            context.recipeViewerSettings.removeExclusionArea(this)
        super.dispose()
    }

    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawBackground(context, widgetTheme)

        val heightWithoutBottom = 6 + slotSize * 16

        UPPER_TAB_TEXTURE.draw(context, 0, 0, 26, 4, widgetTheme.getThemeOrDefault())
        GUI_CONTROLS.drawSubArea(
            0f,
            4f,
            25f,
            (heightWithoutBottom - 4).toFloat(),
            0f,
            4f / 256f,
            25f / 256f,
            heightWithoutBottom / 256f,
            widgetTheme.getThemeOrDefault()
        )
        LOWER_TAB_TEXTURE.draw(context, 0, heightWithoutBottom, 25, 6, widgetTheme.getThemeOrDefault())

    }

    class UpgradeToggleWidget(private val panel: BackpackPanel, private val slotIndex: Int) :
        Widget<UpgradeToggleWidget>(), Interactable {
        companion object {
            private const val WIDTH = 9
            private const val HEIGHT = 18

            private val BACKGROUND_TAB_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls.png")
                .imageSize(256, 256)
                .xy(0, 204, 7, HEIGHT)
                .build()
            private val CONNECTED_BACKGROUND_TAB_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls.png")
                .imageSize(256, 256)
                .xy(0, 205, 7, HEIGHT - 1)
                .build()
            private val SWITCH_BACKGROUND_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls.png")
                .imageSize(256, 256)
                .xy(65, 0, 6, 12)
                .build()
            private val SWITCH_HOVERED_BACKGROUND_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls.png")
                .imageSize(256, 256)
                .xy(71, 0, 6, 12)
                .build()
        }

        var isToggleEnabled = false
        private var slotSyncHandler: UpgradeSlotSH? = null

        init {
            size(WIDTH, HEIGHT).left(-4).top(slotIndex * 16 + 5)
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
            getWrapper()?.toggle()
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TOGGLE)

            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        override fun isValidSyncOrValue(syncHandler: ISyncOrValue): Boolean {
            if (syncHandler is UpgradeSlotSH)
                slotSyncHandler = syncHandler
            return slotSyncHandler != null
        }

        override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            super.drawOverlay(context, widgetTheme)

            if (isHovering)
                SWITCH_HOVERED_BACKGROUND_TEXTURE.draw(context, 3, 3, 6, 12, widgetTheme.getThemeOrDefault())
            
            if (isToggleEnabled)
                RSBTextures.TOGGLE_ENABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme.getThemeOrDefault())
            else
                RSBTextures.TOGGLE_DISABLE_ICON.draw(context, 4, 4, 4, 10, widgetTheme.getThemeOrDefault())
        }

        override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            super.drawBackground(context, widgetTheme)

            val previousHasSwitch = slotIndex > 0 &&
                panel.backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex - 1)
                    .getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null) != null
            if (previousHasSwitch)
                CONNECTED_BACKGROUND_TAB_TEXTURE.draw(context, 0, 1, 7, HEIGHT - 1, widgetTheme.getThemeOrDefault())
            else
                BACKGROUND_TAB_TEXTURE.draw(context, 0, 0, 7, HEIGHT, widgetTheme.getThemeOrDefault())
            SWITCH_BACKGROUND_TEXTURE.draw(context, 3, 3, 6, 12, widgetTheme.getThemeOrDefault())
        }
    }
}

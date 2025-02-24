package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.drawable.AdaptableUITexture
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.ModularScreen
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.ExpandDirection
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.MemorySettingWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.TabWidget
import com.cleanroommc.retrosophisticatedbackpacks.config.ClientConfig

class BackpackSettingPanel(private val parent: BackpackPanel) : ModularPanel("backpack_settings") {
    companion object {
        private const val HEIGHT: Int = 95
        private val LAYERED_TAB_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 124, 256)
            .adaptable(4)
            .tiled()
            .build() as AdaptableUITexture
    }

    val memoryTab: TabWidget

    init {
        size(parent.area.width, HEIGHT)
            .relative(parent)
            .bottom(0)

        memoryTab = TabWidget(0, top = 0, ExpandDirection.LEFT)
        memoryTab.expandedWidget = MemorySettingWidget(parent, memoryTab)
        memoryTab.tabIcon = RSBTextures.BRAIN_ICON

        val grid = Column()
            .size(parent.area.width - 14, HEIGHT - 14)
            .margin(7)
            .child(TextWidget(IKey.str("Configuration Tab")).leftRel(0.5f))

        child(grid)
            .child(memoryTab)
    }

    override fun shouldAnimate(): Boolean =
        ClientConfig.enableAnimation

    override fun isDraggable(): Boolean =
        false

    override fun onOpen(screen: ModularScreen) {
        super.onOpen(screen)
        parent.isMemorySettingTabOpened = memoryTab.showExpanded
    }

    override fun onClose() {
        super.onClose()
        parent.isMemorySettingTabOpened = false
    }

    override fun postDraw(context: ModularGuiContext, transformed: Boolean) {
        super.postDraw(context, transformed)

        // Nasty hack to draw over upgrade tabs
        LAYERED_TAB_TEXTURE.draw(context, 0, 0, 6, flex.area.height, WidgetTheme.getDefault())
    }
}
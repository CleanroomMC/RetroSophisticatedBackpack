package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import net.minecraft.item.ItemStack

abstract class ExpandedTabWidget(val coveredTabSize: Int, private val delegatedIconStack: ItemStack) :
    ParentWidget<ExpandedTabWidget>() {
    companion object {
        val TAB_TEXTURE: UITexture = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 128, 256)
            .adaptable(4)
            .tiled()
            .build()
    }

    internal lateinit var parentTabWidget: TabWidget
    private val tabWidget: PhantomTabWidget = PhantomTabWidget(ItemDrawable(delegatedIconStack).asWidget())

    init {
        child(tabWidget)
            .background(TAB_TEXTURE)
    }

    private inner class PhantomTabWidget(tabIcon: Widget<*>) : SingleChildWidget<PhantomTabWidget>(), Interactable {
        init {
            size(TabWidget.TAB_TEXTURE.width, TabWidget.TAB_TEXTURE.height)
                .child(
                    tabIcon
                        .size(16)
                        .pos(8, 6)
                )
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            if (mouseButton == 0)
                parentTabWidget.onTabClick()

            return Interactable.Result.SUCCESS
        }
    }
}
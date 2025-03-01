package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.utils.Alignment
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.Tags

abstract class ExpandedTabWidget(
    val coveredTabSize: Int,
    delegatedIcon: IDrawable,
    titleKey: String,
    width: Int = 75,
    private val expandDirection: ExpandDirection = ExpandDirection.RIGHT
) : ParentWidget<ExpandedTabWidget>() {
    companion object {
        val TAB_TEXTURE: UITexture = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 128, 256)
            .adaptable(4)
            .tiled()
            .build()
    }

    protected val phantomTabWidget: PhantomTabWidget = PhantomTabWidget(delegatedIcon.asWidget()).top(0)
    protected val titleKeyWidget: TextWidget = TextWidget(IKey.lang(titleKey))
        .alignment(Alignment.Center)
        .topRel(0.5F)
    protected val upperTabRow: Row = Row()
        .coverChildrenHeight() as Row

    init {
        when (expandDirection) {
            ExpandDirection.LEFT -> {
                right(0)

                upperTabRow
                    .child(Widget().width(4).debugName("placeholder"))
                    .child(titleKeyWidget)
                    .child(phantomTabWidget)
            }

            ExpandDirection.RIGHT -> {
                left(0)

                upperTabRow
                    .width(width - 3)
                    .child(phantomTabWidget)
                    .child(titleKeyWidget.expanded())
            }
        }

        width(width)
            .height(coveredTabSize * 30)
            .background(TAB_TEXTURE)
            .child(upperTabRow)
    }

    abstract fun updateTabState()

    protected inner class PhantomTabWidget(tabIcon: Widget<*>) : SingleChildWidget<PhantomTabWidget>(), Interactable {
        init {
            size(32, 28)
            tabIcon.size(16).top(6)

            when (expandDirection) {
                ExpandDirection.LEFT -> {
                    right(0)

                    tabIcon.right(8)
                }

                ExpandDirection.RIGHT -> {
                    tabIcon.left(8)
                }
            }

            child(tabIcon)
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
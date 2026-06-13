package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedVoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.VoidType
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.VoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting

class VoidUpgradeWidget(slotIndex: Int, wrapper: VoidUpgradeWrapper, stack: ItemStack) :
    BasicExpandedTabWidget<VoidUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        coveredTabSize = filterTabSize(wrapper.filterItems.slots, wrapper.slotsInRow),
        width = filterTabWidth(wrapper.slotsInRow),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18
    ) {
    init {
        startingRow
            .height(20)
            .child(createWorkInGuiButton(wrapper.shouldWorkInGui) {
                wrapper.shouldWorkInGui = !wrapper.shouldWorkInGui
                slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_VOID_WORK_IN_GUI) {}
            })
            .child(createVoidTypeButton(wrapper.voidType, Config.voidUpgrade.voidAlwaysEnabled))
    }

    private fun createVoidTypeButton(current: VoidType, alwaysEnabled: Boolean): CyclicVariantButtonWidget {
        val voidTypes = allowedVoidTypes(alwaysEnabled)
        return tabIconButton(voidTypes.map(::voidVariant), voidTypes.indexOf(current).coerceAtLeast(0)) { index ->
            wrapper.voidType = voidTypes[index]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_VOID_TYPE) {
                it.writeEnumValue(voidTypes[index])
            }
        }
    }
}

class AdvancedVoidUpgradeWidget(slotIndex: Int, wrapper: AdvancedVoidUpgradeWrapper, stack: ItemStack) :
    AdvancedExpandedTabWidget<AdvancedVoidUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        coveredTabSize = filterTabSize(wrapper.filterItems.slots, wrapper.slotsInRow),
        width = filterTabWidth(wrapper.slotsInRow),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18
    ) {
    init {
        startingRow
            .height(20)
            .child(createWorkInGuiButton(wrapper.shouldWorkInGui) {
                wrapper.shouldWorkInGui = !wrapper.shouldWorkInGui
                slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_VOID_WORK_IN_GUI) {}
            })
            .child(createVoidTypeButton(wrapper.voidType, Config.advancedVoidUpgrade.voidAlwaysEnabled))
    }

    private fun createVoidTypeButton(current: VoidType, alwaysEnabled: Boolean): CyclicVariantButtonWidget {
        val voidTypes = allowedVoidTypes(alwaysEnabled)
        return tabIconButton(voidTypes.map(::voidVariant), voidTypes.indexOf(current).coerceAtLeast(0)) { index ->
            wrapper.voidType = voidTypes[index]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_VOID_TYPE) {
                it.writeEnumValue(voidTypes[index])
            }
        }
    }
}

internal fun createWorkInGuiButton(shouldWorkInGui: Boolean, toggle: () -> Unit): CyclicVariantButtonWidget =
    tabIconButton(WORK_IN_GUI_VARIANTS, if (shouldWorkInGui) 1 else 0) {
        toggle()
    }

internal fun tabIconButton(
    variants: List<CyclicVariantButtonWidget.Variant>,
    index: Int,
    updater: CyclicVariantButtonWidget.(Int) -> Unit
): CyclicVariantButtonWidget =
    CyclicVariantButtonWidget(
        variants,
        index,
        iconOffset = 1,
        buttonWidth = 18,
        buttonHeight = 18,
        hasCustomTexture = true,
        mousePressedUpdater = updater
    )

private fun allowedVoidTypes(alwaysEnabled: Boolean): List<VoidType> =
    if (alwaysEnabled) VoidType.entries else listOf(VoidType.SLOT_OVERFLOW, VoidType.STORAGE_OVERFLOW)

private fun voidVariant(type: VoidType): CyclicVariantButtonWidget.Variant =
    when (type) {
        VoidType.ALWAYS -> CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.void_always".asTranslationKey()),
            RSBTextures.VOID_ALWAYS_ICON
        )
        VoidType.SLOT_OVERFLOW -> CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.void_slot_overflow".asTranslationKey()),
            RSBTextures.VOID_SLOT_OVERFLOW_ICON,
            listOf(IKey.lang("gui.void_slot_overflow_detail".asTranslationKey()).style(TextFormatting.GRAY))
        )
        VoidType.STORAGE_OVERFLOW -> CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.void_storage_overflow".asTranslationKey()),
            RSBTextures.VOID_STORAGE_OVERFLOW_ICON,
            listOf(IKey.lang("gui.void_storage_overflow_detail".asTranslationKey()).style(TextFormatting.GRAY))
        )
    }

private val WORK_IN_GUI_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.only_automatic".asTranslationKey()), RSBTextures.WORK_IN_GUI_OFF_ICON),
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.works_in_gui".asTranslationKey()), RSBTextures.WORK_IN_GUI_ON_ICON),
)

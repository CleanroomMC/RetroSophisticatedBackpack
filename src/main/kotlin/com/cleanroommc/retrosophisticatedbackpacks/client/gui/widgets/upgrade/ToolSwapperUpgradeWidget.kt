package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedToolSwapperUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.ToolSwapMode
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedToolSwapperUpgradeWidget(slotIndex: Int, wrapper: AdvancedToolSwapperUpgradeWrapper, stack: ItemStack) :
    AdvancedExpandedTabWidget<AdvancedToolSwapperUpgradeWrapper>(
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
            .child(createSwapWeaponButton())
            .child(createToolSwapModeButton())
    }

    private fun createSwapWeaponButton(): CyclicVariantButtonWidget =
        tabIconButton(SWAP_WEAPON_VARIANTS, if (wrapper.shouldSwapWeapon) 1 else 0) {
            wrapper.shouldSwapWeapon = !wrapper.shouldSwapWeapon
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_TOOL_SWAPPER_SWAP_WEAPON) {}
        }

    private fun createToolSwapModeButton(): CyclicVariantButtonWidget =
        tabIconButton(TOOL_SWAP_MODE_VARIANTS, wrapper.toolSwapMode.ordinal) {
            wrapper.toolSwapMode = ToolSwapMode.entries[it]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_TOOL_SWAPPER_MODE) {
                it.writeEnumValue(wrapper.toolSwapMode)
            }
        }
}

private val SWAP_WEAPON_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(
        IKey.lang("gui.tool_swapper_swap_weapon_disabled".asTranslationKey()),
        RSBTextures.TOOL_SWAPPER_DO_NOT_SWAP_WEAPON_ICON,
        listOf(IKey.lang("gui.tool_swapper_swap_weapon_disabled.detail".asTranslationKey()).style(IKey.GRAY))
    ),
    CyclicVariantButtonWidget.Variant(
        IKey.lang("gui.tool_swapper_swap_weapon_enabled".asTranslationKey()),
        RSBTextures.TOOL_SWAPPER_SWAP_WEAPON_ICON,
        listOf(IKey.lang("gui.tool_swapper_swap_weapon_enabled.detail".asTranslationKey()).style(IKey.GRAY))
    ),
)

private val TOOL_SWAP_MODE_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(
        IKey.lang("gui.tool_swapper_any".asTranslationKey()),
        RSBTextures.TOOL_SWAPPER_SWAP_TOOLS_ICON,
        listOf(IKey.lang("gui.tool_swapper_any.detail".asTranslationKey()).style(IKey.GRAY))
    ),
    CyclicVariantButtonWidget.Variant(
        IKey.lang("gui.tool_swapper_only_tools".asTranslationKey()),
        RSBTextures.TOOL_SWAPPER_ONLY_TOOLS_ICON,
        listOf(IKey.lang("gui.tool_swapper_only_tools.detail".asTranslationKey()).style(IKey.GRAY))
    ),
    CyclicVariantButtonWidget.Variant(
        IKey.lang("gui.tool_swapper_no_swap".asTranslationKey()),
        RSBTextures.TOOL_SWAPPER_NO_SWAP_ICON,
        listOf(IKey.lang("gui.tool_swapper_no_swap.detail".asTranslationKey()).style(IKey.GRAY))
    ),
)

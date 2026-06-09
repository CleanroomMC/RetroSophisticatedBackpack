package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.VoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class VoidUpgradeWidget(
    slotIndex: Int,
    wrapper: VoidUpgradeWrapper
) : BasicExpandedTabWidget<VoidUpgradeWrapper>(
    slotIndex, wrapper,
    ItemStack(Items.voidUpgrade),
    wrapper.settingsLangKey,
    coveredTabSize = 5
) {
    companion object {
        internal val TRANSFER_SOURCE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.upgrade_or_world_interaction".asTranslationKey()),
                RSBTextures.NOT_WORK_IN_GUI_ICON
            ), CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.any_interaction".asTranslationKey()),
                RSBTextures.WORKS_IN_GUI_ICON
            )
        )

        internal val VOID_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.void_any".asTranslationKey()),
                RSBTextures.VOID_ANY_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.void_overflow".asTranslationKey()),
                RSBTextures.VOID_OVERFLOW_ICON
            )
        )
    }

    val transferSourceButton: CyclicVariantButtonWidget
    val voidTypeButton: CyclicVariantButtonWidget

    init {
        transferSourceButton = CyclicVariantButtonWidget(TRANSFER_SOURCE_VARIANTS, wrapper.transferSource.ordinal) {
            this@VoidUpgradeWidget.wrapper.transferSource = IVoidUpgrade.TransferSource.entries[it]
            updateWrapper()
        }
        voidTypeButton = CyclicVariantButtonWidget(VOID_TYPE_VARIANTS, wrapper.voidType.ordinal) {
            this@VoidUpgradeWidget.wrapper.voidType = IVoidUpgrade.VoidType.entries[it]
            updateWrapper()
        }

        startingRow
            .leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(transferSourceButton)
            .child(voidTypeButton)
    }

    fun updateWrapper() {
        filterWidget.slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_VOID) {
            it.writeEnumValue(this@VoidUpgradeWidget.wrapper.transferSource)
            it.writeEnumValue(this@VoidUpgradeWidget.wrapper.voidType)
        }
    }
}

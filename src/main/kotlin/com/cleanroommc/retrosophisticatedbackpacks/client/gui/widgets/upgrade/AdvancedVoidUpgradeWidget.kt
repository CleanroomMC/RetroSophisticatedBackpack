package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedVoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade.VoidUpgradeWidget.Companion.TRANSFER_SOURCE_VARIANTS
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade.VoidUpgradeWidget.Companion.VOID_TYPE_VARIANTS
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

class AdvancedVoidUpgradeWidget(
    slotIndex: Int,
    wrapper: AdvancedVoidUpgradeWrapper
) : AdvancedExpandedTabWidget<AdvancedVoidUpgradeWrapper>(
    slotIndex,
    wrapper,
    ItemStack(Items.advancedVoidUpgrade),
    wrapper.settingsLangKey,
    coveredTabSize = 6
) {
    val transferSourceButton: CyclicVariantButtonWidget
    val voidTypeButton: CyclicVariantButtonWidget

    init {
        transferSourceButton = CyclicVariantButtonWidget(TRANSFER_SOURCE_VARIANTS, wrapper.transferSource.ordinal) {
            this@AdvancedVoidUpgradeWidget.wrapper.transferSource = IVoidUpgrade.TransferSource.entries[it]
            updateWrapper()
        }
        voidTypeButton = CyclicVariantButtonWidget(VOID_TYPE_VARIANTS, wrapper.voidType.ordinal) {
            this@AdvancedVoidUpgradeWidget.wrapper.voidType = IVoidUpgrade.VoidType.entries[it]
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
            it.writeEnumValue(this@AdvancedVoidUpgradeWidget.wrapper.transferSource)
            it.writeEnumValue(this@AdvancedVoidUpgradeWidget.wrapper.voidType)
        }
    }
}

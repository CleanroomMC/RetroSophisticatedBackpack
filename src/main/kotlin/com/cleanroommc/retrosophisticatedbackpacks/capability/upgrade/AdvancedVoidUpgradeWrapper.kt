package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.VoidUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedVoidUpgradeWrapper : AdvancedUpgradeWrapper<VoidUpgradeItem>(), IVoidUpgrade {
    override val settingsLangKey: String = "gui.advanced_void_settings".asTranslationKey()
    override var transferSource: IVoidUpgrade.TransferSource = IVoidUpgrade.TransferSource.UPGRADE_OR_WORLD_INTERACTION
    override var voidType: IVoidUpgrade.VoidType = IVoidUpgrade.VoidType.ANY

    override fun canVoid(stack: ItemStack, transferSource: IVoidUpgrade.TransferSource, voidType: IVoidUpgrade.VoidType): Boolean =
        checkFilter(stack) && this.voidType == voidType &&
                (this.transferSource == IVoidUpgrade.TransferSource.ALL || this.transferSource == transferSource)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_VOID_UPGRADE_CAPABILITY ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing) ||
                super<IVoidUpgrade>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setByte(IVoidUpgrade.TRANSFER_SOURCE_TAG, transferSource.ordinal.toByte())
        nbt.setByte(IVoidUpgrade.VOID_TYPE_TAG, voidType.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        transferSource = IVoidUpgrade.TransferSource.entries[nbt.getByte(IVoidUpgrade.TRANSFER_SOURCE_TAG).toInt()]
        voidType = IVoidUpgrade.VoidType.entries[nbt.getByte(IVoidUpgrade.VOID_TYPE_TAG).toInt()]
    }
}

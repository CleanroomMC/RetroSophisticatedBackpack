package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper.Impl.deserializeNBT
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

abstract class UpgradeItem(registryName: String, val hasTab: Boolean = false) : ItemBase() {
    init {
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())

        Items.ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(TextComponentTranslation("tooltip.${registryName!!.path}".asTranslationKey()).formattedText)
    }

    override fun getNBTShareTag(stack: ItemStack): NBTTagCompound? {
        var nbt = super.getNBTShareTag(stack)
        val wrapper = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: return nbt
        
        if (nbt != null) nbt.setTag("Capability", wrapper.serializeNBT())
        else nbt = wrapper.serializeNBT()
        
        return nbt
    }

    override fun readNBTShareTag(stack: ItemStack, nbt: NBTTagCompound?) {
        if (nbt == null)
            return
        
        val wrapper = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: return
        
        if (nbt.hasKey("Capability")) wrapper.deserializeNBT(nbt.getCompoundTag("Capability"))
        else wrapper.deserializeNBT(nbt)
    }
}
package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World

abstract class UpgradeItem(registryName: String, val hasTab: Boolean = false, val upgradeGroup: String? = null) : ItemBase() {
    init {
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())

        Items.ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        addUpgradeTooltip(tooltip)
    }

    protected fun addUpgradeTooltip(tooltip: MutableList<String>, vararg args: Any) {
        val path = registryName!!.path
        val key = "item.${registryName!!.namespace}.$path.tooltip"
        val legacyKey = "tooltip.$path".asTranslationKey()
        val tooltipText = when {
            I18n.hasKey(key) -> I18n.format(key, *args)
            I18n.hasKey(legacyKey) -> I18n.format(legacyKey, *args)
            else -> return
        }
        tooltipText.replace("\\n", "\n").split('\n').forEach { tooltip.add(TextFormatting.DARK_GRAY.toString() + it) }
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

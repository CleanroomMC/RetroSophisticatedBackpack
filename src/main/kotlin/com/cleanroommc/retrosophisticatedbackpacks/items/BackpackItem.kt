package com.cleanroommc.retrosophisticatedbackpacks.items

import baubles.api.BaubleType
import baubles.api.IBauble
import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.blocks.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.handlers.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handlers.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.inventory.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.inventory.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.utils.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fml.common.Optional

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles", striprefs = true)
class BackpackItem(
    registryName: String,
    backpackBlock: BackpackBlock,
    val numberOfSlots: () -> Int,
    val numberOfUpgradeSlots: () -> Int,
    val tier: BackpackTier,
) : ItemBlock(backpackBlock), IModelRegister, IGuiHolder<PlayerInventoryGuiData>, IBauble {
    init {
        setMaxStackSize(1)
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())

        Items.ITEMS.add(this)
        Items.BACKPACK_ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    override fun onItemRightClick(worldIn: World, player: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack?> {
        if (!worldIn.isRemote) {
            PlayerInventoryGuiFactory.open(player, handIn)
        }

        return super.onItemRightClick(worldIn, player, handIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val wrapper = BackpackWrapper(numberOfSlots, numberOfUpgradeSlots)
        nbt?.let(wrapper::deserializeNBT)
        return wrapper
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        // Only cache on server
        if (!worldIn.isRemote && entityIn is EntityPlayerMP) {
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

            if (!wrapper.isCached) {
                CapabilityHandler.cacheBackpackInventory(wrapper)
            }
        }

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected)
    }

    override fun addInformation(
        stack: ItemStack,
        worldIn: World?,
        tooltip: MutableList<String>,
        flagIn: ITooltipFlag
    ) {
        tooltip.add(
            TextComponentTranslation(
                "tooltip.backpack.inventory_size".asTranslationKey(),
                numberOfSlots()
            ).formattedText
        )
        tooltip.add(
            TextComponentTranslation(
                "tooltip.backpack.upgrade_slots_size".asTranslationKey(),
                numberOfUpgradeSlots()
            ).formattedText
        )

        if (Interactable.hasShiftDown()) {
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
            tooltip.add(
                TextComponentTranslation(
                    "tooltip.backpack.stack_multiplier".asTranslationKey(),
                    wrapper.getTotalStackMultiplier()
                ).formattedText
            )
        } else {
            tooltip.add(TextComponentTranslation("tooltip.shift_to_reveal".asTranslationKey()).formattedText)
        }
    }

    override fun buildUI(data: PlayerInventoryGuiData, syncManager: PanelSyncManager): ModularPanel {
        val stack = data.usedItemStack
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)!!
        return BackpackGuiHolder.ItemStackGuiHolder(wrapper).buildUI(data, syncManager)
    }

    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }

    @Optional.Method(modid = "baubles")
    override fun getBaubleType(stack: ItemStack): BaubleType =
        BaubleType.BODY
}
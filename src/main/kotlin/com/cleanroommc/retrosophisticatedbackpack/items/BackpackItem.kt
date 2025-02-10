package com.cleanroommc.retrosophisticatedbackpack.items

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.factory.HandGuiData
import com.cleanroommc.modularui.factory.ItemGuiFactory
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpack.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpack.blocks.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpack.client.BackpackItemStackRenderer
import com.cleanroommc.retrosophisticatedbackpack.handlers.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpack.handlers.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpack.inventory.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpack.utils.IModelRegister
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey
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

class BackpackItem(
    registryName: String,
    backpackBlock: BackpackBlock,
    val numberOfSlots: () -> Int,
    val numberOfUpgradeSlots: () -> Int,
    val tier: BackpackTier,
) : ItemBlock(backpackBlock), IModelRegister, IGuiHolder<HandGuiData> {
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
            ItemGuiFactory.INSTANCE.open(player as EntityPlayerMP, handIn)
        }

        return super.onItemRightClick(worldIn, player, handIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val backpackWrapper = BackpackWrapper(numberOfSlots, numberOfUpgradeSlots)

        if (nbt != null) {
            backpackWrapper.deserializeNBT(nbt)
        }

        return backpackWrapper
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        // Only cache on server
        if (!worldIn.isRemote && entityIn is EntityPlayerMP) {
            val backpackInventory = stack.getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!

            if (!backpackInventory.isCached) {
                CapabilityHandler.cacheBackpackInventory(backpackInventory)
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
            val backpackInventory = stack.getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!
            tooltip.add(
                TextComponentTranslation(
                    "tooltip.backpack.stack_multiplier".asTranslationKey(),
                    backpackInventory.getTotalStackMultiplier()
                ).formattedText
            )
        } else {
            tooltip.add(TextComponentTranslation("tooltip.shift_to_reveal".asTranslationKey()).formattedText)
        }
    }

    override fun buildUI(data: HandGuiData, syncManager: PanelSyncManager): ModularPanel {
        val stack = data.usedItemStack
        val backpackInv = stack.getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!
        return BackpackGuiHolder.ItemStackGuiHolder(backpackInv).buildUI(data, syncManager)
    }

    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }
}
package com.cleanroommc.retrosophisticatedbackpacks.item

import baubles.api.BaubleType
import baubles.api.IBauble
import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.items.IItemHandler

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

    override fun onItemUseFirst(
        player: EntityPlayer,
        world: World,
        pos: BlockPos,
        side: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float,
        hand: EnumHand
    ): EnumActionResult {
        if (player.isSneaking) {
            val stack = player.getHeldItem(hand)
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)
                ?: return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ)
            val tileEntity = world.getTileEntity(pos)
                ?: return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ)
            var transferred = BackpackInventoryHelper.attemptDepositOnTileEntity(wrapper, tileEntity, side)
            transferred =
                BackpackInventoryHelper.attemptRestockFromTileEntity(wrapper, tileEntity, side) || transferred

            if (transferred) {
                world.playSound(
                    null,
                    player.position,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    SoundCategory.BLOCKS,
                    0.5f,
                    0.5f
                )

                return EnumActionResult.SUCCESS
            }
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand)
    }

    override fun onItemUse(
        player: EntityPlayer,
        worldIn: World,
        pos: BlockPos,
        hand: EnumHand,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float
    ): EnumActionResult {
        val te = worldIn.getTileEntity(pos)

        if (player.isSneaking && (te is IItemHandler || te is IInventory))
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)

        if (!worldIn.isRemote)
            PlayerInventoryGuiFactory.open(player, hand)

        return EnumActionResult.SUCCESS
    }

    override fun onItemRightClick(worldIn: World, player: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack?> {
        if (!worldIn.isRemote) {
            PlayerInventoryGuiFactory.open(player, handIn)
        }

        return ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(handIn))
    }

    override fun itemInteractionForEntity(
        stack: ItemStack,
        playerIn: EntityPlayer,
        target: EntityLivingBase,
        hand: EnumHand
    ): Boolean {
        if (playerIn.isSneaking) {
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)
                ?: return super.itemInteractionForEntity(stack, playerIn, target, hand)
            var transferred = BackpackInventoryHelper.attemptDepositOnEntity(wrapper, target)
            transferred =
                BackpackInventoryHelper.attemptRestockFromEntity(wrapper, target) || transferred

            return if (transferred) {
                playerIn.world.playSound(
                    null,
                    playerIn.position,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    SoundCategory.BLOCKS,
                    0.5f,
                    0.5f
                )

                true
            } else false
        }

        return super.itemInteractionForEntity(stack, playerIn, target, hand)
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

    override fun getNBTShareTag(stack: ItemStack): NBTTagCompound? {
        var nbt = super.getNBTShareTag(stack)
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return nbt

        if (nbt != null) nbt.setTag("Capability", wrapper.serializeNBT())
        else nbt = wrapper.serializeNBT()

        return nbt
    }

    override fun readNBTShareTag(stack: ItemStack, nbt: NBTTagCompound?) {
        if (nbt == null)
            return

        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

        if (nbt.hasKey("Capability")) wrapper.deserializeNBT(nbt.getCompoundTag("Capability"))
        else wrapper.deserializeNBT(nbt)
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
            val stackHint =
                if (wrapper.isStackedByMultiplication()) "(xM)"
                else "(+M)"

            tooltip.add(
                TextComponentTranslation(
                    "tooltip.backpack.stack_multiplier".asTranslationKey(),
                    wrapper.getTotalStackMultiplier(),
                    TextComponentString(stackHint).setStyle(Style().setColor(TextFormatting.RED)).formattedText
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
package com.cleanroommc.retrosophisticatedbackpacks.item

import baubles.api.BaubleType
import baubles.api.IBauble
import baubles.api.render.IRenderBauble
import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.UISettings
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.client.BackpackBipedModel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData.InventoryType
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.EntityEquipmentSlot
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
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles", striprefs = true)
@Optional.Interface(iface = "baubles.api.render.IRenderBauble", modid = "baubles", striprefs = true)
class BackpackItem(
    registryName: String,
    backpackBlock: BackpackBlock,
    val numberOfSlots: () -> Int,
    val numberOfUpgradeSlots: () -> Int,
    val tier: BackpackTier,
) : ItemBlock(backpackBlock), IModelRegister, IGuiHolder<PlayerInventoryGuiData>, IBauble, IRenderBauble {
    // FIXME: Later when adding tank upgrade and its corresponding model, we should change this implementation to
    // hashmap, and the key would depends on the count of tanks upgrades
    private var cachedBipedModel: BackpackBipedModel? = null
    
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

        if (player.isSneaking && te != null)
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

    override fun isValidArmor(stack: ItemStack, armorType: EntityEquipmentSlot, entity: Entity): Boolean =
        armorType == EntityEquipmentSlot.CHEST

    override fun getEquipmentSlot(stack: ItemStack): EntityEquipmentSlot = EntityEquipmentSlot.CHEST

    @SideOnly(Side.CLIENT)
    override fun getArmorModel(
        entityLiving: EntityLivingBase,
        itemStack: ItemStack,
        armorSlot: EntityEquipmentSlot,
        _default: ModelBiped
    ): ModelBiped? {
        if (armorSlot == EntityEquipmentSlot.CHEST) {
            val model = if (cachedBipedModel != null) cachedBipedModel
            else {
                cachedBipedModel = BackpackBipedModel(itemStack)
                cachedBipedModel
            }
            
            model?.setModelAttributes(_default)
            return model
        }

        return null
    }

    override fun getNBTShareTag(stack: ItemStack): NBTTagCompound? {
        var nbt = super.getNBTShareTag(stack)
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return nbt

        if (nbt != null) nbt.setTag("BackpackCapability", wrapper.serializeNBT())
        else {
            nbt = NBTTagCompound()
            nbt.setTag("BackpackCapability", wrapper.serializeNBT())
        }

        return nbt
    }

    override fun readNBTShareTag(stack: ItemStack, nbt: NBTTagCompound?) {
        super.readNBTShareTag(stack, nbt)
        if (nbt == null)
            return
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

        if (nbt.hasKey("BackpackCapability")) {
            wrapper.deserializeNBT(nbt.getCompoundTag("BackpackCapability"))
        }
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

    override fun buildUI(
        data: PlayerInventoryGuiData,
        syncManager: PanelSyncManager,
        uiSettings: UISettings
    ): ModularPanel {
        val stack = data.usedItemStack
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)!!
        val slotIndex = if (data.inventoryType == InventoryType.PLAYER_INVENTORY) data.slotIndex else null
        uiSettings.customContainer { BackpackContainer(wrapper, slotIndex) }
        val holder = BackpackGuiHolder.ItemStackGuiHolder(wrapper)
        return holder.buildUI(data, syncManager, uiSettings)
    }

    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }

    @Optional.Method(modid = "baubles")
    override fun getBaubleType(stack: ItemStack): BaubleType =
        BaubleType.BODY

    @Optional.Method(modid = "baubles")
    @SideOnly(Side.CLIENT)
    override fun onPlayerBaubleRender(
        itemStack: ItemStack,
        entityPlayer: EntityPlayer,
        renderType: IRenderBauble.RenderType,
        partialTicks: Float
    ) {
        if (renderType != IRenderBauble.RenderType.BODY)
            return

        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, 0.3, 0.225)
        GlStateManager.rotate(180f, 1f, 0f, 0f)
        
        if (entityPlayer.isSneaking) {
            GlStateManager.translate(0.0f, -0.05f, 0.0f)
            GlStateManager.rotate(28.647888f, 1.0f, 0.0f, 0.0f)
        }

        val mc = Minecraft.getMinecraft()

        mc.renderItem.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED)
    
        GlStateManager.popMatrix()
    }
}

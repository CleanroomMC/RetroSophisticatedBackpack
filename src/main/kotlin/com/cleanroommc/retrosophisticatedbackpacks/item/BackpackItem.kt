package com.cleanroommc.retrosophisticatedbackpacks.item

import baubles.api.BaubleType
import baubles.api.IBauble
import baubles.api.render.IRenderBauble
import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.UISettings
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.handler.BackpackTooltipHandler
import com.cleanroommc.retrosophisticatedbackpacks.client.BackpackBipedModel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData.InventoryType
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.mixin.EntityItemAccessor
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
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
        val wrapper = BackpackWrapper(numberOfSlots, numberOfUpgradeSlots, containerStack = stack)
        nbt?.let(wrapper::deserializeNBT)
        return wrapper
    }

    override fun shouldCauseReequipAnimation(oldStack: ItemStack, newStack: ItemStack, slotChanged: Boolean): Boolean =
        slotChanged || oldStack.item !== newStack.item || oldStack.metadata != newStack.metadata

    override fun shouldCauseBlockBreakReset(oldStack: ItemStack, newStack: ItemStack): Boolean =
        oldStack.item !== newStack.item || oldStack.metadata != newStack.metadata

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        if (!worldIn.isRemote && entityIn is EntityPlayerMP) {
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

            if (entityIn.ticksExisted % 20 == 0)
                wrapper.feed(entityIn, wrapper)

            wrapper.tickUpgrades(entityIn, worldIn, entityIn.posX, entityIn.posY, entityIn.posZ)

            if (!Config.tickDedupeLogicDisabled && !wrapper.isCached)
                CapabilityHandler.cacheBackpackInventory(wrapper)
        }
    }

    override fun onEntityItemUpdate(entityItem: EntityItem): Boolean {
        val wrapper = entityItem.item.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return false
        if (wrapper.gatherCapabilityUpgrades(Capabilities.EVERLASTING_UPGRADE_CAPABILITY).isEmpty()) {
            return false
        }

        entityItem.lifespan = Int.MAX_VALUE
        entityItem.setEntityInvulnerable(true)
        (entityItem as EntityItemAccessor).`rsb$setAge`(0)
        if (entityItem.posY < 1.0) {
            entityItem.setPosition(entityItem.posX, 1.0, entityItem.posZ)
            entityItem.motionY = 0.2
        }
        val material = entityItem.world.getBlockState(BlockPos(entityItem)).material
        if (material == net.minecraft.block.material.Material.WATER || material == net.minecraft.block.material.Material.LAVA) {
            entityItem.motionY = 0.08
            entityItem.fallDistance = 0f
        }
        return false
    }

    override fun isValidArmor(stack: ItemStack, armorType: EntityEquipmentSlot, entity: Entity): Boolean =
        armorType == EntityEquipmentSlot.CHEST

    override fun getEquipmentSlot(stack: ItemStack): EntityEquipmentSlot = EntityEquipmentSlot.CHEST

    @SideOnly(Side.CLIENT)
    override fun getArmorModel(
        entityLiving: EntityLivingBase,
        itemStack: ItemStack,
        armorSlot: EntityEquipmentSlot,
        default: ModelBiped
    ): ModelBiped? {
        if (armorSlot == EntityEquipmentSlot.CHEST) {
            val model = if (cachedBipedModel != null) cachedBipedModel
            else {
                cachedBipedModel = BackpackBipedModel(itemStack)
                cachedBipedModel
            }

            model?.setModelAttributes(default)
            return model
        }

        return null
    }

    override fun getNBTShareTag(stack: ItemStack): NBTTagCompound? {
        var nbt = super.getNBTShareTag(stack)?.copy()
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
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)
        if (flagIn.isAdvanced && wrapper != null) {
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "UUID: ${wrapper.uuid}")
        }
        if (!BackpackTooltipHandler.shouldShowContentsTooltip()) {
            tooltip.add(
                TextComponentTranslation(
                    "tooltip.backpack.press_for_contents".asTranslationKey(),
                    TextComponentTranslation("tooltip.backpack.shift".asTranslationKey())
                        .setStyle(net.minecraft.util.text.Style().setColor(TextFormatting.AQUA))
                        .formattedText
                ).setStyle(net.minecraft.util.text.Style().setColor(TextFormatting.GRAY)).formattedText
            )
            return
        }

        wrapper?.let { BackpackTooltipHandler.addTooltipLines(it, tooltip) }
    }

    override fun buildUI(
        data: PlayerInventoryGuiData,
        syncManager: PanelSyncManager,
        uiSettings: UISettings
    ): ModularPanel {
        val stack = data.usedItemStack
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)!!
        val slotIndex = if (data.inventoryType == InventoryType.PLAYER_INVENTORY) data.slotIndex else null
        uiSettings.customContainer { BackpackContainer(wrapper, slotIndex, data.inventoryType, data.slotIndex, data.targetEntity) }
        uiSettings.canInteractWith {
            if (data.targetEntity.isDead) false
            else it.getDistance(data.targetEntity) <= 4.0
        }
        val holder = BackpackGuiHolder.ItemStackGuiHolder(wrapper)
        return holder.buildUI(data, syncManager, uiSettings)
    }

    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }

    @Optional.Method(modid = "baubles")
    override fun onWornTick(stack: ItemStack, player: EntityLivingBase) {
        this.onUpdate(stack, player.world, player, -1, false)
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
        BackpackBipedModel.renderBackpack(itemStack, entityPlayer)

        GlStateManager.popMatrix()
    }
}

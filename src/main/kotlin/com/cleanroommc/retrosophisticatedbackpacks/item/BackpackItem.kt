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
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.client.BackpackBipedModel
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData.InventoryType
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.BlockLiquid
import net.minecraft.block.state.IBlockState
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
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
import net.minecraftforge.fluids.IFluidBlock
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
    companion object {
        private const val FLOATING_HEIGHT = 0.25

        private fun isFluid(state: IBlockState): Boolean =
            state.material.isLiquid || state.block is BlockLiquid || state.block is IFluidBlock
    }

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
        if (nbt != null) {
            val data = if (nbt.hasKey("Parent")) nbt.getCompoundTag("Parent") else nbt
            wrapper.deserializeNBT(data)
        }
        return wrapper
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

        if (!wrapper.isCached)
            CapabilityHandler.cacheBackpackInventory(wrapper)

        if (!worldIn.isRemote && entityIn is EntityPlayerMP) {
            if (entityIn.ticksExisted % 20 == 0)
                wrapper.feed(entityIn, wrapper)
        }
    }

    override fun onEntityItemUpdate(entityItem: EntityItem): Boolean {
        val wrapper = entityItem.item.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return false

        if (entityItem.world.isRemote) {
            BackpackSoundManager.transferPlayingOwnership(entityItem, null, wrapper)
        }

        if (wrapper.hasEverlastingJukeboxUpgrade()) {
            // Tries to levitate the item to the top of the void / fluid
            val pos = BlockPos(entityItem.posX, entityItem.posY, entityItem.posZ)
            val posBelow = BlockPos(entityItem.posX, entityItem.posY - 0.1, entityItem.posZ)
            var targetY: Double? = null

            if (entityItem.posY <= FLOATING_HEIGHT) {
                targetY = FLOATING_HEIGHT
            } else if (isFluid(entityItem.world.getBlockState(pos)) || isFluid(entityItem.world.getBlockState(posBelow))) {
                var topFluidPos = if (isFluid(entityItem.world.getBlockState(pos))) pos else posBelow
                while (isFluid(entityItem.world.getBlockState(topFluidPos.up()))) {
                    topFluidPos = topFluidPos.up()
                    if (topFluidPos.y >= 256) break
                }
                targetY = topFluidPos.y.toDouble() + 1.0
            }

            if (targetY != null) {
                entityItem.setNoGravity(true)
                if (entityItem.posY < targetY) {
                    entityItem.motionY = 0.08
                }
                if (entityItem.posY + entityItem.motionY >= targetY) {
                    if (targetY == FLOATING_HEIGHT) {
                        entityItem.setPosition(entityItem.posX, targetY, entityItem.posZ)
                        entityItem.motionX = .0
                        entityItem.motionY = .0
                        entityItem.motionZ = .0
                    } else {
                        entityItem.setPosition(entityItem.posX, targetY, entityItem.posZ)
                        entityItem.motionY = .0
                    }
                }
            } else {
                entityItem.setNoGravity(false)
            }

            // Spawns villager happy particles to indicate the everlasting upgrade is active
            if (entityItem.world.isRemote && entityItem.world.rand.nextInt(8) == 0) {
                val x = entityItem.posX + (entityItem.world.rand.nextDouble() - .5) * .6
                val y = entityItem.posY + entityItem.world.rand.nextDouble() * .6
                val z = entityItem.posZ + (entityItem.world.rand.nextDouble() - .5) * .6

                entityItem.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x, y, z, .0, .0, .0)
            }
        }

        return super.onEntityItemUpdate(entityItem)
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
            val model = if (cachedBipedModel != null) {
                cachedBipedModel!!.backpackItemStack = itemStack
                cachedBipedModel
            } else {
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

            for (i in 0 until wrapper.upgradeSlotsSize()) {
                val upgradeStack = wrapper.upgradeItemStackHandler.getStackInSlot(i)

                if (upgradeStack.isEmpty)
                    continue

                val upgradeWrapper = upgradeStack.getCapability(Capabilities.UPGRADE_CAPABILITY, null)

                if (upgradeWrapper is IToggleable) {
                    val toggledColor = if (upgradeWrapper.enabled) TextFormatting.GREEN else TextFormatting.RED

                    tooltip.add(
                        TextComponentTranslation(
                            "tooltip.backpack.upgrade_slot.toggleable".asTranslationKey(),
                            upgradeStack.displayName,
                            TextComponentTranslation("tooltip.upgrade.state.${upgradeWrapper.enabled}".asTranslationKey()).setStyle(
                                Style().setColor(toggledColor)
                            ).formattedText
                        ).setStyle(Style().setColor(TextFormatting.GRAY)).formattedText
                    )
                } else if (upgradeStack.item is UpgradeItem) {
                    tooltip.add(
                        TextComponentTranslation(
                            "tooltip.backpack.upgrade_slot.default".asTranslationKey(),
                            upgradeStack.displayName
                        ).setStyle(Style().setColor(TextFormatting.GRAY)).formattedText
                    )
                }
            }
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

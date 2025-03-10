package com.cleanroommc.retrosophisticatedbackpacks.block

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import git.jbredwards.fluidlogged_api.api.block.IFluidloggable
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.EnumPushReaction
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.items.ItemHandlerHelper

@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
@Optional.Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api")
class BackpackBlock(
    registryName: String,
    explosionResistance: Float,
    val tier: BackpackTier,
) : Block(Material.CARPET), ITileEntityProvider, IModelRegister.Block, IFluidloggable {
    companion object {
        val LEFT_TANK: PropertyBool = PropertyBool.create("left_tank")
        val RIGHT_TANK: PropertyBool = PropertyBool.create("right_tank")
        val BATTERY: PropertyBool = PropertyBool.create("battery")
        val FACING: PropertyDirection = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
        const val BEDROCK_RESISTANCE = 3600000

        private val BOOL_PROPERTIES = arrayOf(LEFT_TANK, RIGHT_TANK, BATTERY)
    }

    constructor(
        registryName: String,
        tier: BackpackTier,
    ) : this(registryName, 0.8f, tier)

    init {
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)

        setResistance(explosionResistance)
        setHardness(0.8f)
        setSoundType(SoundType.CLOTH)
        setLightOpacity(0)
        defaultState = blockState.baseState
            .withProperty(LEFT_TANK, false)
            .withProperty(RIGHT_TANK, false)
            .withProperty(BATTERY, false)
            .withProperty(FACING, EnumFacing.NORTH)

        Blocks.BLOCKS.add(this)
        Blocks.BACKPACK_BLOCKS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    override fun getBlockFaceShape(
        worldIn: IBlockAccess,
        state: IBlockState,
        pos: BlockPos,
        face: EnumFacing
    ): BlockFaceShape =
        BlockFaceShape.UNDEFINED

    override fun isFullCube(state: IBlockState): Boolean =
        false

    override fun isOpaqueCube(state: IBlockState): Boolean =
        false

    override fun getRenderLayer(): BlockRenderLayer =
        BlockRenderLayer.CUTOUT

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
        when (val facing = state.getValue(FACING)) {
            EnumFacing.NORTH, EnumFacing.SOUTH -> AxisAlignedBB(
                1 / 16.0,
                0.0,
                4 / 16.0,
                15 / 16.0,
                14 / 16.0,
                12 / 16.0
            )

            EnumFacing.WEST, EnumFacing.EAST -> AxisAlignedBB(4 / 16.0, 0.0, 1 / 16.0, 12 / 16.0, 14 / 16.0, 15 / 16.0)
            EnumFacing.DOWN, EnumFacing.UP -> throw IllegalStateException("Backpack block does not have $facing side")
        }

    override fun createBlockState(): BlockStateContainer =
        BlockStateContainer(this, *arrayOf(LEFT_TANK, RIGHT_TANK, BATTERY, FACING))

    override fun getStateForPlacement(
        world: World,
        pos: BlockPos,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float,
        meta: Int,
        placer: EntityLivingBase,
        hand: EnumHand
    ): IBlockState =
        defaultState.withProperty(FACING, placer.horizontalFacing.opposite)

    override fun withRotation(state: IBlockState, rot: Rotation): IBlockState =
        state.withProperty(FACING, rot.rotate(state.getValue(FACING)))

    override fun withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
        state.withProperty(FACING, mirrorIn.mirror(state.getValue(FACING)))

    override fun getStateFromMeta(meta: Int): IBlockState {
        val leftTank = (meta and 0b10000) shr 4 == 1
        val rightTank = (meta and 0b01000) shr 3 == 1
        val battery = (meta and 0b00100) shr 2 == 1
        val facing = EnumFacing.byHorizontalIndex(meta and 0b00011)

        return defaultState
            .withProperty(LEFT_TANK, leftTank)
            .withProperty(RIGHT_TANK, rightTank)
            .withProperty(BATTERY, battery)
            .withProperty(FACING, facing)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        var meta = 0

        for (boolProp in BOOL_PROPERTIES) {
            if (state.getValue(boolProp))
                meta = meta or 1
            meta = meta shl 1
        }

        meta = meta or state.getValue(FACING).horizontalIndex
        return meta
    }

    override fun getPushReaction(state: IBlockState): EnumPushReaction {
        return EnumPushReaction.DESTROY
    }

    override fun hasComparatorInputOverride(state: IBlockState): Boolean =
        true

    override fun getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int {
        val tileEntity = worldIn.getTileEntity(pos) as? BackpackTileEntity ?: return 0

        return ItemHandlerHelper.calcRedstoneFromInventory(tileEntity)
    }

    override fun onBlockPlacedBy(
        worldIn: World,
        pos: BlockPos,
        state: IBlockState,
        placer: EntityLivingBase,
        stack: ItemStack
    ) {
        val backpackInventory = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
        val tileEntity = worldIn.getTileEntity(pos) as? BackpackTileEntity ?: return

        tileEntity.wrapper.deserializeNBT(backpackInventory.serializeNBT())
    }

    override fun onBlockActivated(
        worldIn: World,
        pos: BlockPos,
        state: IBlockState,
        playerIn: EntityPlayer,
        hand: EnumHand,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float
    ): Boolean {
        if (!worldIn.isRemote) {
            if (playerIn.isSneaking) {
                worldIn.playSound(playerIn, pos, SoundEvents.BLOCK_CLOTH_BREAK, SoundCategory.BLOCKS, 1f, 0.5f)
                dropBlockAsItem(worldIn, pos, state, 0)
                worldIn.setBlockState(pos, net.minecraft.init.Blocks.AIR.defaultState)

                return true
            }

            val tileEntity = worldIn.getTileEntity(pos) as? BackpackTileEntity ?: return true

            tileEntity.openGui(playerIn)
        } else {
            if (playerIn.isSneaking) {
                worldIn.playSound(playerIn, pos, SoundEvents.BLOCK_CLOTH_BREAK, SoundCategory.BLOCKS, 1f, 0.5f)
            }
        }

        return true
    }

    override fun hasTileEntity(state: IBlockState): Boolean =
        true

    override fun createNewTileEntity(
        worldIn: World,
        meta: Int
    ): TileEntity =
        BackpackTileEntity()

    override fun onBlockHarvested(worldIn: World, pos: BlockPos, state: IBlockState, player: EntityPlayer) {
        if (player.isCreative)
            dropBlockAsItem(worldIn, pos, state, 0)
        super.onBlockHarvested(worldIn, pos, state, player)
    }

    override fun getDrops(
        drops: NonNullList<ItemStack>,
        world: IBlockAccess,
        pos: BlockPos,
        state: IBlockState,
        fortune: Int
    ) {
        val tileEntity = world.getTileEntity(pos) as? BackpackTileEntity ?: return
        val stack = ItemStack(Item.getItemFromBlock(this))
        val tileEntityBackpackInventory = tileEntity.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
        val stackBackpackInventory = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
        stackBackpackInventory.deserializeNBT(tileEntityBackpackInventory.serializeNBT())

        drops.add(stack)
    }

    override fun removedByPlayer(
        state: IBlockState,
        world: World,
        pos: BlockPos,
        player: EntityPlayer,
        willHarvest: Boolean
    ): Boolean {
        if (willHarvest) return true
        return super.removedByPlayer(state, world, pos, player, false)
    }

    override fun harvestBlock(
        worldIn: World,
        player: EntityPlayer,
        pos: BlockPos,
        state: IBlockState,
        te: TileEntity?,
        stack: ItemStack
    ) {
        super.harvestBlock(worldIn, player, pos, state, te, stack)
        worldIn.setBlockToAir(pos)
    }
}
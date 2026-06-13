package com.cleanroommc.retrosophisticatedbackpacks.client

import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class BackpackBlockEntityRenderer : TileEntitySpecialRenderer<BackpackTileEntity>() {
    override fun render(
        te: BackpackTileEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        destroyStage: Int,
        alpha: Float
    ) {
        if (te.wrapper.getDisplayItem() == null) {
            return
        }

        val state = te.world.getBlockState(te.pos)
        if (state.block !is BackpackBlock) {
            return
        }

        GlStateManager.pushMatrix()
        GlStateManager.translate(x + 0.5, y, z + 0.5)
        GlStateManager.rotate(facingRotation(state.getValue(BackpackBlock.FACING)), 0f, 1f, 0f)
        BackpackDisplayItemRenderer.render(te.wrapper)
        GlStateManager.popMatrix()
    }

    private fun facingRotation(facing: EnumFacing): Float =
        when (facing) {
            EnumFacing.NORTH -> 180f
            EnumFacing.SOUTH -> 0f
            EnumFacing.WEST -> 90f
            EnumFacing.EAST -> -90f
            else -> 0f
        }
}

package com.cleanroommc.retrosophisticatedbackpacks.client

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.google.common.collect.ImmutableMap
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.*
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.resources.IResourceManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.model.ICustomModelLoader
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.common.model.IModelState
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.util.vector.Vector3f
import java.util.*
import java.util.function.Function

@SideOnly(Side.CLIENT)
class BackpackDynamicModel private constructor(
    private val modelParts: Map<ModelPart, IModel>,
    private val tier: BackpackTier
) : IModel {
    companion object {
        private val BACKPACK_MODELS_RESOURCE_LOCATION = ModelPart.entries.filter {
            it == ModelPart.BASE || it.name.endsWith("POUCH")
        }.associateBy({ it }, {
            val path = "block/backpack_${it.name.lowercase(Locale.ENGLISH)}"

            ResourceLocation(Tags.MOD_ID, path)
        })

        private val BACKPACK_CLOTH_RESOURCE_LOCATION = ResourceLocation(Tags.MOD_ID, "block/backpack_cloth")
        private val BACKPACK_BORDER_RESOURCE_LOCATION = ResourceLocation(Tags.MOD_ID, "block/backpack_border")
        private val BACKPACK_CLIP_RESOURCE_LOCATIONS = BackpackTier.entries.associateBy(
            { it },
            { ResourceLocation(Tags.MOD_ID, "block/${it.registryName}_clips") }
        )
        private val BACKPACK_TEXTURE_RESOURCE_LOCATIONS = listOf(
            BACKPACK_CLOTH_RESOURCE_LOCATION,
            BACKPACK_BORDER_RESOURCE_LOCATION,
            *BACKPACK_CLIP_RESOURCE_LOCATIONS.values.toTypedArray()
        )
    }

    override fun bake(
        state: IModelState,
        format: VertexFormat,
        bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite>
    ): IBakedModel {
        return BackpackBakedModel(modelParts.mapValues { (_, model) ->
            model.retexture(ImmutableMap.of("clips", BACKPACK_CLIP_RESOURCE_LOCATIONS[tier]!!.toString()))
                .bake(state, format, bakedTextureGetter)
        })
    }

    override fun getDependencies(): Collection<ResourceLocation> =
        BACKPACK_MODELS_RESOURCE_LOCATION.values

    override fun getTextures(): Collection<ResourceLocation> =
        BACKPACK_TEXTURE_RESOURCE_LOCATIONS

    private class BackpackBakedModel(private val models: Map<ModelPart, IBakedModel>) : IBakedModel {
        companion object {
            private val ITEM_TRANSFORMS = ItemCameraTransforms(
                ItemTransformVec3f(
                    Vector3f(85f, -90f, 0f),
                    Vector3f(0f, -2 / 16f, -4.5f / 16f),
                    Vector3f(0.75f, 0.75f, 0.75f)
                ), ItemTransformVec3f(
                    Vector3f(85f, -90f, 0f),
                    Vector3f(0f, -2 / 16f, -4.5f / 16f),
                    Vector3f(0.75f, 0.75f, 0.75f)
                ), ItemTransformVec3f(
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0.5f, 0.5f, 0.5f)
                ), ItemTransformVec3f(
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0.5f, 0.5f, 0.5f)
                ), ItemTransformVec3f(
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0f, 14.25f / 16f, 0f),
                    Vector3f(1f, 1f, 1f)
                ), ItemTransformVec3f(
                    Vector3f(30f, 225f, 0f),
                    Vector3f(0f, 1.25f / 16f, 0f),
                    Vector3f(0.9f, 0.9f, 0.9f)
                ), ItemTransformVec3f(
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0f, 3 / 16f, 0f),
                    Vector3f(0.5f, 0.5f, 0.5f)
                ), ItemTransformVec3f(
                    Vector3f(0f, 0f, 0f),
                    Vector3f(0f, 0f, -2.25f / 16f),
                    Vector3f(0.75f, 0.75f, 0.75f)
                )
            )
        }

        private val overrideList = BackpackItemOverrideList(this)
        var tankLeft = false
        var tankRight = false
        var battery = false

        override fun getQuads(
            state: IBlockState?,
            side: EnumFacing?,
            rand: Long
        ): List<BakedQuad> {
            val ret = models[ModelPart.BASE]!!.getQuads(state, side, rand).toMutableList()

            models[ModelPart.BASE]!!

            ret.addAll(models[ModelPart.FRONT_POUCH]!!.getQuads(state, side, rand))
            ret.addAll(models[ModelPart.LEFT_POUCH]!!.getQuads(state, side, rand))
            ret.addAll(models[ModelPart.RIGHT_POUCH]!!.getQuads(state, side, rand))

            if (state != null) {
                val facing = state.getValue(BackpackBlock.FACING)

                return BakedQuadHelper.rotateQuadsCentered(ret, facing)
            }

            return ret
        }

        override fun isAmbientOcclusion(): Boolean =
            true

        override fun isGui3d(): Boolean =
            true

        override fun isBuiltInRenderer(): Boolean =
            false

        override fun getParticleTexture(): TextureAtlasSprite =
            models[ModelPart.BASE]!!.particleTexture

        override fun getOverrides(): ItemOverrideList =
            overrideList

        override fun getItemCameraTransforms(): ItemCameraTransforms =
            ITEM_TRANSFORMS
    }

    private class BackpackItemOverrideList(private val backpackModel: BackpackBakedModel) : ItemOverrideList(listOf()) {
        override fun handleItemState(
            originalModel: IBakedModel,
            stack: ItemStack,
            world: World?,
            entity: EntityLivingBase?
        ): IBakedModel {
            backpackModel.tankLeft = false
            backpackModel.tankRight = false
            backpackModel.battery = false

            return backpackModel
        }
    }

    class Loader : ICustomModelLoader {
        override fun onResourceManagerReload(resourceManager: IResourceManager) {}

        override fun accepts(modelLocation: ResourceLocation): Boolean =
            Blocks.BACKPACK_BLOCKS.any { it.registryName == modelLocation }

        override fun loadModel(modelLocation: ResourceLocation): IModel {
            val modelParts = BACKPACK_MODELS_RESOURCE_LOCATION.mapValues { (_, location) ->
                ModelLoaderRegistry.getModel(location)
            }

            return BackpackDynamicModel(
                modelParts,
                BackpackTier.valueOf(modelLocation.path.removePrefix("backpack_").uppercase(Locale.ENGLISH)),
            )
        }
    }

    private enum class ModelPart {
        BASE,
        BATTERY,
        FRONT_POUCH,
        LEFT_POUCH,
        LEFT_TANK,
        RIGHT_POUCH,
        RIGHT_TANK,
    }
}
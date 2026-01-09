package com.cleanroommc.retrosophisticatedbackpacks.mixin;

import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
@SideOnly(Side.CLIENT)
public abstract class LayerArmorBaseMixin<T extends ModelBase> implements LayerRenderer<EntityLivingBase> {
    @Shadow
    private RenderLivingBase<?> renderer;
    @Shadow
    private float colorR;
    @Shadow
    private float colorG;
    @Shadow
    private float colorB;
    @Shadow
    private float alpha;

    @Shadow
    protected abstract T getArmorModelHook(EntityLivingBase entity, ItemStack itemStack, EntityEquipmentSlot slot, T model);

    @Shadow
    protected abstract void setModelSlotVisible(T p_188359_1_, EntityEquipmentSlot slotIn);

    @Inject(method = "renderArmorLayer", at = @At(value = "TAIL"))
    private void injectRenderArmorLayer(EntityLivingBase entityLivingBaseIn,
                                        float limbSwing,
                                        float limbSwingAmount,
                                        float partialTicks,
                                        float ageInTicks,
                                        float netHeadYaw,
                                        float headPitch,
                                        float scale,
                                        EntityEquipmentSlot slotIn,
                                        CallbackInfo info) {
        LayerArmorBase<T> thisObject = (LayerArmorBase<T>) (Object) this;
        ItemStack itemStack = entityLivingBaseIn.getItemStackFromSlot(slotIn);

        if (itemStack.getItem() instanceof BackpackItem) {
            BackpackItem backpackItem = (BackpackItem) itemStack.getItem();

            if (backpackItem.getEquipmentSlot(itemStack) != slotIn)
                return;

            T t = thisObject.getModelFromSlot(slotIn);
            t = getArmorModelHook(entityLivingBaseIn, itemStack, slotIn, t);
            System.out.println("KEK");
            t.setModelAttributes(renderer.getMainModel());
            t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
            setModelSlotVisible(t, slotIn);

            GlStateManager.color(colorR, colorG, colorB, alpha);
            t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            if (itemStack.hasEffect()) {
                LayerArmorBase.renderEnchantedGlint(
                        renderer, 
                        entityLivingBaseIn, 
                        t, 
                        limbSwing, 
                        limbSwingAmount, 
                        partialTicks, 
                        ageInTicks, 
                        netHeadYaw, 
                        headPitch, 
                        scale
                );
            }
        }
    }
}

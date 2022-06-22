package com.gim.events;

import com.gim.GenshinImpactMod;
import com.gim.registry.Attributes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class GenshinStatsRender {
    private final static int packedLight = LightTexture.pack(15, 15);

    @SubscribeEvent
    public static void onNameTagRender(RenderNameplateEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            AttributeInstance attributeInstance = ((LivingEntity) event.getEntity()).getAttribute(Attributes.level);
            if (attributeInstance != null && attributeInstance.getValue() > 0) {
                int entityLevel = (int) attributeInstance.getValue();
                int playerLevel = (int) Minecraft.getInstance().player.getAttributeValue(Attributes.level);

                ChatFormatting chatFormatting = entityLevel == playerLevel
                        ? ChatFormatting.YELLOW
                        : playerLevel > entityLevel
                        ? ChatFormatting.GREEN
                        : ChatFormatting.RED;

                Component component = new TranslatableComponent(GenshinImpactMod.ModID + ".level", entityLevel).withStyle(chatFormatting);

                renderNameTag(event.getEntity(), component, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
                        new Vec3(0, event.getEntity().getBbHeight() + 0.5, 0),
                        0.025f,
                        true,
                        0.25f);

                renderNameTag(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
                        new Vec3(0, event.getEntity().getBbHeight() + 0.75, 0),
                        0.025f,
                        true,
                        0.25f);

                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onEndLevelRender(RenderLevelLastEvent event) {
        for (ShowDamage.TextParticle particle : ShowDamage.getAll()) {

            // do not showing damage stats
            if (ShowDamage.IndicatingType.DAMAGE.equals(particle.getType()) && !GenshinImpactMod.CONFIG.getKey().indicateDamage.get()) {
                continue;
            }

            // do not showing reaction stats
            if (ShowDamage.IndicatingType.REACTION.equals(particle.getType()) && !GenshinImpactMod.CONFIG.getKey().indicateReactions.get()) {
                continue;
            }

            Vec3 position = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            Vec3 particlePos = particle.getPos();
            Vec3 prevPos = particle.getPrevPos();

            double d0 = Mth.lerp(event.getPartialTick(), prevPos.x, particlePos.x);
            double d1 = Mth.lerp(event.getPartialTick(), prevPos.y, particlePos.y);
            double d2 = Mth.lerp(event.getPartialTick(), prevPos.z, particlePos.z);

            event.getPoseStack().pushPose();
            event.getPoseStack().translate(
                    d0 - position.x,
                    d1 - position.y,
                    d2 - position.z
            );

            renderNameTag(
                    particle.getOwner(),
                    particle.getText(),
                    event.getPoseStack(),
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    packedLight,
                    Vec3.ZERO,
                    particle.getScale(),
                    false,
                    1
            );

            event.getPoseStack().popPose();
        }
    }

    protected static void renderNameTag(Entity entity, Component text, PoseStack poseStack, MultiBufferSource bufferSource, int light, Vec3 translatePos,
                                        float scale, boolean drawBackground, float opacity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 finalPos = entity.position().add(translatePos);

        double d0 = entityRenderDispatcher.distanceToSqr(finalPos.x, finalPos.y, finalPos.z);
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(entity, d0)) {
            int i = "deadmau5".equals(text.getString()) ? -10 : 0;
            poseStack.pushPose();
            poseStack.translate(translatePos.x, translatePos.y, translatePos.z);
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-scale, -scale, scale);
            Matrix4f matrix4f = poseStack.last().pose();
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(opacity);
            int j = (int) (f1 * 255.0F) << 24;
            Font font = Minecraft.getInstance().font;
            float f2 = (float) (-font.width(text) / 2);

            if (drawBackground) {
                font.drawInBatch(text, f2, (float) i, 553648127, false, matrix4f, bufferSource, false, j, light);
            }

            font.drawInBatch(text, f2, (float) i, -1, true, matrix4f, bufferSource, false, 0, light);
            poseStack.popPose();
        }
    }
}

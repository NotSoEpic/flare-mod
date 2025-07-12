package wawa.flares.shot_flare;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import wawa.flares.AllEntities;
import wawa.flares.Flares;

@EventBusSubscriber(modid = Flares.MODID, value = Dist.CLIENT)
public class FlareEntityRenderer extends EntityRenderer<FlareEntity> {
    public static ResourceLocation TEXTURE = Flares.resource("textures/entity/flare/sparkle.png");
    protected FlareEntityRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(final FlareEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(final FlareEntity livingEntity, final Frustum camera, final double camX, final double camY, final double camZ) {
        return true;
    }

    @Override
    public void render(final FlareEntity entity, final float entityYaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        entity.updateLight(partialTick);
        final Vector3f pos = entity.getPosition(partialTick).toVector3f();
        final Quaternionf quaternion = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        quaternion.rotateZ(entity.tickCount / 10);
        final VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderBillboardedQuad(vertexConsumer, poseStack, quaternion,
                entity.color,
                0, 0, 1, 1);
    }

    public static void renderBillboardedQuad(final VertexConsumer buffer, final PoseStack poseStack, final Quaternionf quaternion,
                                       final int color,
                                       final float u0, final float v0,
                                       final float u1, final float v1) {
        final PoseStack.Pose pose = poseStack.last();
        vertex(pose, buffer, quaternion, color,  1, -1, u1, v1);
        vertex(pose, buffer, quaternion, color,  1,  1, u1, v0);
        vertex(pose, buffer, quaternion, color, -1,  1, u0, v0);
        vertex(pose, buffer, quaternion, color, -1, -1, u0, v1);
    }

    private static void vertex(final PoseStack.Pose pose,
                        final VertexConsumer consumer,
                        final Quaternionf quaternion,
                        final int color,
                        final float xo, final float yo,
                        final float u, final float v) {
        final Vector3f vec = new Vector3f(xo, yo, 0.5f).rotate(quaternion);
        consumer.addVertex(pose, vec.x, vec.y, vec.z)
                .setColor(color)
                .setUv(u, v)
                .setUv2(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // fullbright sky and block light
                .setNormal(pose, 0f, -1f, 0f);
    }

    @SubscribeEvent
    public static void registerRenderer(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AllEntities.FLARE.get(), FlareEntityRenderer::new);
    }
}

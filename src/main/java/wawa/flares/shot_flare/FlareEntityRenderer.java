package wawa.flares.shot_flare;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import wawa.flares.AllEntities;
import wawa.flares.AllRenderTypes;
import wawa.flares.Flares;

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
        if (entity.tickCount > entity.getMaxAge()) {
            return;
        }
        entity.updateLight(partialTick);
        renderFlare(bufferSource, poseStack, entity.tickCount + partialTick, entity.getMaxAge(), entity.isTrackable(), entity.color, 1);
    }

    public static void renderFlare(final MultiBufferSource bufferSource, final PoseStack poseStack,
                                   final float tickCount, final int maxAge, final boolean trackable, final int color, final float scale) {
        final Quaternionf quaternion = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
//        quaternion.div(poseStack.last().pose().getNormalizedRotation(new Quaternionf())); // todo sable compat when i figure out how quaternions work
        final Vector3f dist = poseStack.last().pose().getTranslation(new Vector3f());
        float len = dist.length();
        if (len > 256) {
            // slightly scaling length to help prevent z fighting?
            dist.mul((256 - len * 1.01f) / len);
            poseStack.translate(dist.x, dist.y, dist.z);
            len = 256;
        }
        final float scaledScale = scale * (1 + len / 64);
        final float scaledScaleScaled = scaledScale * FlareEntity.getIntensity(trackable, tickCount, maxAge);
        final int frameI = (int) (tickCount) / 10;
        quaternion.rotateZ(frameI);
        renderBillboardedQuad(bufferSource.getBuffer(AllRenderTypes.flareBloom(TEXTURE)), poseStack, quaternion,
                color, scaledScaleScaled, 0.25f,
                0, 0, 1, 0.5f);
        final float glowScale = frameI % 3 * 0.2f + 0.3f;
        renderBillboardedQuad(bufferSource.getBuffer(AllRenderTypes.flare(TEXTURE)), poseStack, quaternion,
                color, glowScale * scaledScaleScaled, 0.1f,
                0, 0.5f, 1, 1);
    }

    public static void renderBillboardedQuad(final VertexConsumer buffer, final PoseStack poseStack, final Quaternionf quaternion,
                                       final int color, final float scale, final float zOff,
                                       final float u0, final float v0,
                                       final float u1, final float v1) {
        final PoseStack.Pose pose = poseStack.last();
        vertex(pose, buffer, quaternion, color,  scale, -scale, zOff, u1, v1);
        vertex(pose, buffer, quaternion, color,  scale,  scale, zOff, u1, v0);
        vertex(pose, buffer, quaternion, color, -scale,  scale, zOff, u0, v0);
        vertex(pose, buffer, quaternion, color, -scale, -scale, zOff, u0, v1);
    }

    private static void vertex(final PoseStack.Pose pose,
                        final VertexConsumer consumer,
                        final Quaternionf quaternion,
                        final int color,
                        final float xo, final float yo, final float zo,
                        final float u, final float v) {
        final Vector3f vec = new Vector3f(xo, yo, zo).rotate(quaternion);
        consumer.addVertex(pose, vec.x, vec.y, vec.z)
                .setColor(color)
                .setUv(u, v)
                .setLight(LightTexture.FULL_BRIGHT)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(pose, 0f, 1f, 0f);
    }

    public static void registerRenderer(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AllEntities.FLARE.get(), FlareEntityRenderer::new);
    }
}

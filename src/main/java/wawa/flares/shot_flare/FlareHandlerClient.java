package wawa.flares.shot_flare;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Quaternionf;
import wawa.flares.Flares;
import wawa.flares.mixin.LevelRendererGetter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FlareHandlerClient {
    private static final HashMap<ClientLevel, HashMap<UUID, FlareData>> flares = new HashMap<>();

    public static void put(final ClientLevel level, final FlareData data) {
        Flares.LOGGER.info("Storing flare client {}", data.getEntity());
        flares.computeIfAbsent(level, l -> new HashMap<>()).put(data.getEntity(), data);
    }

    public static void remove(final ClientLevel level, final UUID id) {
        Flares.LOGGER.info("Removing flare client {}", id);
        flares.computeIfAbsent(level, l -> new HashMap<>()).remove(id);
    }

    @SubscribeEvent
    public static void tickFlares(final ClientTickEvent.Pre event) {
        flares.forEach((level, flareMap) -> {
            for(final Iterator<Map.Entry<UUID, FlareData>> it = flareMap.entrySet().iterator(); it.hasNext();) {
                final FlareData flare = it.next().getValue();
                if (flare.unloadedTick()) {
                    it.remove();
                    Flares.LOGGER.info("Removing flare client {}", flare.getEntity());
                }
            }
        });
    }

    @SubscribeEvent
    public static void renderFlares(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            final HashMap<UUID, FlareData> renderable = flares.get(Minecraft.getInstance().level);
            if (renderable != null) {
                FogRenderer.setupNoFog();

                final PoseStack poseStack = event.getPoseStack();
                final Quaternionf quaternion = new Quaternionf(event.getCamera().rotation());
                final MultiBufferSource.BufferSource bufferSource = ((LevelRendererGetter)event.getLevelRenderer()).getBuffers().bufferSource();
                final VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(FlareEntityRenderer.TEXTURE));
                renderable.forEach((uuid, data) -> {
                    poseStack.pushPose();
                    final Vec3 pos = data.getPos().add(
                            data.getVel().scale(event.getPartialTick().getGameTimeDeltaTicks())
                    ).subtract(event.getCamera().getPosition());
                    final Quaternionf quaternion2 = new Quaternionf(quaternion).rotateZ(data.getLife() / 10);
                    poseStack.translate(pos.x, pos.y, pos.z);
                    FlareEntityRenderer.renderBillboardedQuad(vertexConsumer, poseStack, quaternion2, data.getColor(), 0, 0, 1, 1);
                    poseStack.popPose();
                });
                // sucks to suck i guess boowomp
                // FogRenderer.setupFog(event.getCamera(), FogRenderer.FogMode.FOG_TERRAIN, Math.max(f1, 32.0F), flag1, f);
                bufferSource.endBatch();
            }
        }
    }
}

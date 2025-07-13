package wawa.flares.shot_flare;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import wawa.flares.Flares;
import wawa.flares.mixin.LevelRendererGetter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FlareHandlerClient {
    private static final HashMap<ClientLevel, HashMap<UUID, FlareData>> flares = new HashMap<>();

    public static HashMap<UUID, FlareData> getFlaresIn(final ClientLevel clientLevel) {
        return flares.computeIfAbsent(clientLevel, l -> new HashMap<>());
    }

    public static void addFlare(final ClientLevel clientLevel, final FlareData data) {
        final FlareData oldData = getFlaresIn(clientLevel).put(data.getEntity(), data);
        if (oldData != null && oldData.isLoaded()) {
            data.setLoaded(true);
        }
    }

    public static void flareEntityTick(final ClientLevel clientLevel, final FlareEntity flare) {
        final FlareData data = getFlaresIn(clientLevel).get(flare.getUUID());
        if (data == null) {
            getFlaresIn(clientLevel).put(flare.getUUID(), new FlareData(true, flare));
        } else {
            data.copyFromEntity(flare);
            data.setLoaded(true);
        }
    }

    public static void setUnloaded(final ClientLevel clientLevel, final UUID flare) {
        final FlareData data = getFlaresIn(clientLevel).get(flare);
        if (data != null) {
            data.setLoaded(false);
        }
    }

    public static void remove(final ClientLevel clientLevel, final UUID flare) {
        getFlaresIn(clientLevel).remove(flare);
    }

    @SubscribeEvent
    public static void tickFlares(final LevelTickEvent.Pre event) {
        flares.forEach((level, flareMap) -> {
            for(final Iterator<Map.Entry<UUID, FlareData>> it = flareMap.entrySet().iterator(); it.hasNext();) {
                final FlareData flare = it.next().getValue();
                if (flare.unloadedTick()) {
                    it.remove();
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
                final MultiBufferSource.BufferSource bufferSource = ((LevelRendererGetter)event.getLevelRenderer()).getBuffers().bufferSource();
                renderable.forEach((uuid, data) -> {
                    if (data.isLoaded()) {
                        return;
                    }
                    final Vec3 pos = data.getPos().add(
                            data.getVel().scale(event.getPartialTick().getGameTimeDeltaTicks())
                    ).subtract(event.getCamera().getPosition());
                    final float scale = (float) (pos.length() / 64f);
                    poseStack.pushPose();
                    poseStack.translate(pos.x, pos.y, pos.z);
                    FlareEntityRenderer.renderFlare(bufferSource, poseStack, data.getLife(), data.getColor(), scale);
                    poseStack.popPose();
                });
                // sucks to suck i guess boowomp
                // FogRenderer.setupFog(event.getCamera(), FogRenderer.FogMode.FOG_TERRAIN, Math.max(f1, 32.0F), flag1, f);
                bufferSource.endBatch();
            }
        }
    }
}

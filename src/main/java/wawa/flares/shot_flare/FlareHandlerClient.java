package wawa.flares.shot_flare;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import wawa.flares.FlareConfig;
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
        final FlareData oldData = getFlaresIn(clientLevel).put(data.getUuid(), data);
        if (oldData != null && oldData.isLoaded()) {
            data.setLoaded(true);
        }
    }

    public static void flareEntityTick(final ClientLevel clientLevel, final FlareEntity flare) {
        if (flare.getRemovalReason() != null) {
            return;
        }
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
        if (event.getLevel() instanceof final ClientLevel clientLevel) {
            final HashMap<UUID, FlareData> flareMap = flares.get(clientLevel);
            if (flareMap != null) {
                for (final Iterator<Map.Entry<UUID, FlareData>> it = flareMap.entrySet().iterator(); it.hasNext(); ) {
                    final FlareData flare = it.next().getValue();
                    if (flare.unloadedTick()) {
                        it.remove();
                    }
                }
            }
        }
    }

    public static boolean canRenderEntityDirectly(final FlareData data, final ClientLevel level) {
        final BlockPos blockPos = data.getBlockPos();
        return (level.isOutsideBuildHeight(blockPos.getY()) || Minecraft.getInstance().levelRenderer.isSectionCompiled(blockPos));
    }

    private static final ResourceLocation FLARE_BLOOM_SHADER = Flares.resource("flare_bloom");
    @SubscribeEvent
    public static void renderFlares(final RenderLevelStageEvent event) {
        final ShaderProgram shader = VeilRenderSystem.setShader(FLARE_BLOOM_SHADER);
        if (shader != null) {
            shader.bind();
            shader.getUniform("bloom").setFloat(FlareConfig.CONFIG.bloomIntensity.get().floatValue());
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            final HashMap<UUID, FlareData> renderable = flares.get(Minecraft.getInstance().level);
            if (renderable != null) {
                final float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
                final PoseStack poseStack = event.getPoseStack();
                final MultiBufferSource.BufferSource bufferSource = ((LevelRendererGetter)event.getLevelRenderer()).getBuffers().bufferSource();
                renderable.forEach((uuid, data) -> {
                    if (data.isLoaded() && canRenderEntityDirectly(data, Minecraft.getInstance().level)) {
                        return;
                    }
                    Vec3 pos = data.getPos().add(
                            data.getVel().scale(event.getPartialTick().getGameTimeDeltaTicks())
                    ).subtract(event.getCamera().getPosition());
                    double distance = pos.length();
                    if (distance > 1e6) {
                        return;
                    }
                    poseStack.pushPose();
                    poseStack.translate(pos.x, pos.y, pos.z);
                    FlareEntityRenderer.renderFlare(bufferSource, poseStack, data.getLife() + partialTick, data.getMaxLife(), true, data.getColor(), 1, false);
                    poseStack.popPose();
                });
            }
        }
    }
}

package wawa.flares;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class AllRenderTypes extends RenderType {
    public AllRenderTypes(final String name, final VertexFormat format, final VertexFormat.Mode mode, final int bufferSize, final boolean affectsCrumbling, final boolean sortOnUpload, final Runnable setupState, final Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final Function<ResourceLocation, RenderType> FLARE_BLOOM = texture -> {
        RenderType.CompositeState flareBloom = RenderType.CompositeState.builder()
                .setOutputState(VeilRenderSystem.BLOOM_SHARD)
                .setShaderState(RenderStateShard.RENDERTYPE_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setWriteMaskState(RenderType.COLOR_WRITE)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(true);
        RenderType.CompositeState flare = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .createCompositeState(true);
        return VeilRenderType.layered(create(Flares.MODID + ":flare_bloom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, TRANSIENT_BUFFER_SIZE, true, true, flareBloom), create(Flares.MODID + ":flare_standard", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, TRANSIENT_BUFFER_SIZE, true, true, flare));
    };

    // todo figure out why bloom is causing terrain to flicker black??
    public static RenderType flareBloom(final ResourceLocation texture) {
        if (FlareConfig.CONFIG.doBloom.get()) {
            return FLARE_BLOOM.apply(texture);
        } else {
            return entityTranslucent(texture);
        }
    }
}

package wawa.flares;

import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class AllRenderTypes extends RenderType {
    public AllRenderTypes(final String name, final VertexFormat format, final VertexFormat.Mode mode, final int bufferSize, final boolean affectsCrumbling, final boolean sortOnUpload, final Runnable setupState, final Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final ResourceLocation FLARE_BLOOM_TYPE = Flares.resource("flare_bloom");
    private static final ResourceLocation FLARE_TYPE = Flares.resource("flare");

    public static RenderType flareBloom(final ResourceLocation texture) {
        if (FlareConfig.CONFIG.doBloom.get()) {
            final RenderType renderType = VeilRenderType.get(FLARE_BLOOM_TYPE, texture);
            if (renderType != null) {
                return renderType;
            }
        }
        return flare(texture);
    }

    public static RenderType flare(final ResourceLocation texture) {
        final RenderType renderType = VeilRenderType.get(FLARE_TYPE, texture);
        if (renderType != null) {
            return renderType;
        }
        return entityTranslucent(texture);
    }
}

package wawa.flares;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FlareConfig {
    public static final FlareConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.BooleanValue doBloom;
    public final ModConfigSpec.DoubleValue bloomIntensity;
    public final ModConfigSpec.BooleanValue doDynamicLights;

    private FlareConfig(final ModConfigSpec.Builder builder) {
        this.doBloom = builder.define("do_bloom", true);
        this.bloomIntensity = builder.defineInRange("bloom_intensity", 2d, 1, 11);
        this.doDynamicLights = builder.define("do_dynamic_lights", true);
    }

    static {
        final Pair<FlareConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(FlareConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}

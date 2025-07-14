package wawa.flares;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FlareConfig {
    public static final FlareConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.ConfigValue<Boolean> doBloom;
    public final ModConfigSpec.ConfigValue<Boolean> doDynamicLights;

    private FlareConfig(final ModConfigSpec.Builder builder) {
        this.doBloom = builder.define("do_bloom", true);
        this.doDynamicLights = builder.define("do_dynamic_lights", true);
    }

    static {
        final Pair<FlareConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(FlareConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}

package wawa.flares;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import wawa.flares.data_component.FlareComponent;

import java.util.function.Supplier;

public class AllComponents {
    public static void init(final IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Flares.MODID);
    public static final Supplier<DataComponentType<FlareComponent>> FLARE = COMPONENTS.registerComponentType("flare",
            b -> b.persistent(FlareComponent.CODEC).networkSynchronized(FlareComponent.STREAM_CODEC));
}

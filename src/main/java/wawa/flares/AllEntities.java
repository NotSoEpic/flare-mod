package wawa.flares;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import wawa.flares.shot_flare.FlareEntity;

import java.util.function.Supplier;

public class AllEntities {
    public static void init(final IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Flares.MODID);
    public static final Supplier<EntityType<FlareEntity>> FLARE = ENTITY_TYPES.register("flare",
            () -> EntityType.Builder.of(FlareEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("flare")
    );
}

package wawa.flares;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import wawa.flares.recipe.FlareDyeRecipe;

import java.util.function.Supplier;

public class AllRecipes {
//    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
//            DeferredRegister.create(Registries.RECIPE_TYPE, Flares.MODID);
//
//    public static final Supplier<RecipeType<FlareDyeRecipe>> FLARE_DYE =
//            RECIPE_TYPES.register(
//                    "flare_dye",
//                    () -> RecipeType.simple(Flares.resource("flare_dye"))
//            );

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Flares.MODID);

    public static final Supplier<RecipeSerializer<FlareDyeRecipe>> FLARE_DYE_SERIALIZER =
            RECIPE_SERIALIZERS.register("flare_dye", FlareDyeRecipe.Serializer::new);

    public static void init(final IEventBus modEventBus) {
//        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}

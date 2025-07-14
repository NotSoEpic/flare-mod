package wawa.flares;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class FlareDatagenData {
    // what the fuck
//    public static class ItemTag extends ItemTagsProvider {
//
//        public ItemTag(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider, final ExistingFileHelper existingFileHelper) {
////            super(output, lookupProvider,
////                    wiwi -> wiwi.
////                    Flares.MODID,
////                    existingFileHelper);
//        }
//
//        @Override
//        protected void addTags(final HolderLookup.Provider provider) {
//            this.tag(AllTags.USABLE_FLARES)
//                    .add(AllItems.FLARE.get(), AllItems.SIGNALLING_FLARE.get())
//            ;
//            this.tag(AllTags.FLARES)
//                    .add(AllItems.FLARE_SHELL.get(), AllItems.SIGNALLING_FLARE_SHELL.get())
//                    .addTag(AllTags.USABLE_FLARES)
//            ;
//        }
//    }

    public static class Recipe extends RecipeProvider {
        public Recipe(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(final RecipeOutput recipeOutput) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, AllItems.FLARE_GUN.get(), 1)
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('C', Tags.Items.INGOTS_COPPER)
                    .define('n', Tags.Items.NUGGETS_IRON)
                    .pattern("n  ")
                    .pattern("ICC")
                    .pattern("I  ")
                    .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                    .save(recipeOutput);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, AllItems.ILLUMINATION_FLARE_SHELL.get(), 4)
                    .requires(Tags.Items.INGOTS_IRON)
                    .requires(Tags.Items.GUNPOWDERS)
                    .requires(Items.CHARCOAL)
                    .requires(Tags.Items.DUSTS_GLOWSTONE)
                    .group("flare_shells")
                    .unlockedBy("has_flare_gun", has(AllItems.FLARE_GUN))
                    .save(recipeOutput, Flares.resource("flare_shell_charcoal"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, AllItems.SIGNALLING_FLARE_SHELL.get(), 4)
                    .requires(Tags.Items.INGOTS_COPPER)
                    .requires(Tags.Items.GUNPOWDERS)
                    .requires(Items.CHARCOAL)
                    .requires(Tags.Items.DUSTS_REDSTONE)
                    .group("signalling_flare_shells")
                    .unlockedBy("has_flare_gun", has(AllItems.FLARE_GUN))
                    .save(recipeOutput, Flares.resource("signalling_flare_shell_charcoal"));

            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, AllItems.ILLUMINATION_FLARE_SHELL.get(), 16)
                    .requires(Tags.Items.INGOTS_IRON)
                    .requires(Items.BLAZE_POWDER)
                    .requires(Tags.Items.DUSTS_GLOWSTONE)
                    .group("flare_shells")
                    .unlockedBy("has_flare_shell", has(AllTags.FLARES))
                    .save(recipeOutput, Flares.resource("flare_shell_blaze_powder"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, AllItems.SIGNALLING_FLARE_SHELL.get(), 16)
                    .requires(Tags.Items.INGOTS_COPPER)
                    .requires(Items.BLAZE_POWDER)
                    .requires(Tags.Items.DUSTS_REDSTONE)
                    .group("signalling_flare_shells")
                    .unlockedBy("has_flare_shell", has(AllTags.FLARES))
                    .save(recipeOutput, Flares.resource("signalling_flare_shell_blaze_powder"));
        }
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();


//        generator.addProvider(
//                event.includeServer(),
//                new ItemTag(output, lookupProvider, existingFileHelper)
//        );
        generator.addProvider(
                event.includeServer(),
                new Recipe(output, lookupProvider)
        );
    }
}

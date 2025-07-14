package wawa.flares;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class FlareDatagenAssets {
    public static class Lang extends LanguageProvider {
        public Lang(final PackOutput output) {
            super(output, Flares.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            this.add("itemGroup.flares", "Flare Guns");

            this.add(AllItems.FLARE_GUN.get(), "Flare Gun");

            this.add(AllItems.ILLUMINATION_FLARE_SHELL.get(), "Illumination Flare Shell");
            this.add(AllItems.SIGNALLING_FLARE_SHELL.get(), "Signalling Flare Shell");

            this.add(AllItems.FLARE.get(), "Illumination Flare");
            this.add(AllItems.SIGNALLING_FLARE.get(), "Signalling Flare");

            this.add(AllEntities.FLARE.get(), "Flare");

            this.add("flares.configuration.do_bloom", "Enable Bloom");
            this.add("flares.configuration.do_dynamic_lights", "Enable Dynamic Lights");
        }
    }

    public static class Model extends ItemModelProvider {
        public Model(final PackOutput output, final ExistingFileHelper existingFileHelper) {
            super(output, Flares.MODID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            this.basicItem(AllItems.ILLUMINATION_FLARE_SHELL.get());
            this.basicItem(AllItems.SIGNALLING_FLARE_SHELL.get());
        }
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        event.createProvider(Lang::new);
        event.addProvider(new Model(output, existingFileHelper));
    }
}

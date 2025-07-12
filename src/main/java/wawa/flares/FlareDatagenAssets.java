package wawa.flares;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Flares.MODID, value = Dist.CLIENT)
public class FlareDatagenAssets {
    public static class Lang extends LanguageProvider {
        public Lang(final PackOutput output) {
            super(output, Flares.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            this.add("itemGroup.flares", "Flare Guns");
            this.add(AllItems.FLARE_GUN.get(), "Flare Gun");
            this.add(AllItems.FLARE.get(), "Flare");
        }
    }

    public static class Model extends ItemModelProvider {
        public Model(final PackOutput output, final ExistingFileHelper existingFileHelper) {
            super(output, Flares.MODID, existingFileHelper);
        }

        @Override
        protected void registerModels() {

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

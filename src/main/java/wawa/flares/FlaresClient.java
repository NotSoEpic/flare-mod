package wawa.flares;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.item.FlareGunItem;
import wawa.flares.shot_flare.FlareEntityRenderer;
import wawa.flares.shot_flare.FlareHandlerClient;

@Mod(value = Flares.MODID, dist = Dist.CLIENT)
public class FlaresClient {
    public FlaresClient(final IEventBus modEventBus, final ModContainer modContainer) {
        final IEventBus neoBus = NeoForge.EVENT_BUS;

        modEventBus.addListener(FlaresClient::onClientSetup);
        modEventBus.addListener(FlaresClient::onColorHandler);
        modEventBus.addListener(FlareEntityRenderer::registerRenderer);

        neoBus.addListener(FlareHandlerClient::tickFlares);
        neoBus.addListener(FlareHandlerClient::renderFlares);
        modEventBus.addListener(FlareHandlerClient::modConfigReload);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modContainer.registerConfig(ModConfig.Type.CLIENT, FlareConfig.CONFIG_SPEC);
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            FlareGunItem.registerPredicate();
        });
    }

    public static void onColorHandler(final RegisterColorHandlersEvent.Item event) {
        event.register(FlareComponent::tint, AllItems.FLARE.get(), AllItems.SIGNALLING_FLARE.get());
    }
}


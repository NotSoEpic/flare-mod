package wawa.flares;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.item.FlareGunItem;
import wawa.flares.shot_flare.FlareHandlerClient;

@Mod(value = Flares.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Flares.MODID, value = Dist.CLIENT)
public class FlaresClient {
    public FlaresClient(final IEventBus modEventBus, final ModContainer container) {
        IEventBus neoBus = NeoForge.EVENT_BUS;

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        neoBus.addListener(FlareHandlerClient::tickFlares);
        neoBus.addListener(FlareHandlerClient::renderFlares);
    }

    @SubscribeEvent
    static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            FlareGunItem.registerPredicate();
        });
    }

    @SubscribeEvent
    static void onColorHandler(final RegisterColorHandlersEvent.Item event) {
        event.register(FlareComponent::tint, AllItems.FLARE.get());
    }
}


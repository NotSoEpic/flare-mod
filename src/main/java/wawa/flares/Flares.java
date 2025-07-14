package wawa.flares;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import wawa.flares.shot_flare.FlareHandlerServer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Flares.MODID)
public class Flares {
    public static final String MODID = "flares";
    public static ResourceLocation resource(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FLARE_GUN_TAB = CREATIVE_MODE_TABS.register("flares", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.flares"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> AllItems.FLARE_GUN.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                AllItems.addTabItems(output);
            }).build());

    public Flares(final IEventBus modEventBus, final ModContainer modContainer) {
        final IEventBus neoBus = NeoForge.EVENT_BUS;

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(FlareDatagenAssets::onGatherData);
        modEventBus.addListener(FlareDatagenData::onGatherData);

        neoBus.addListener(FlareHandlerServer::tickFlares);

        AllComponents.init(modEventBus);
        AllItems.init(modEventBus);
        AllEntities.init(modEventBus);
        AllPackets.init();
        CREATIVE_MODE_TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Hi ^w^ hiii omg hai :3 hewwo x3");
    }
}

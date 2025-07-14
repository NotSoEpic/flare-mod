package wawa.flares;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.item.FlareGunItem;
import wawa.flares.item.FlareItem;

public class AllItems {
    public static void init(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addTabItems(final CreativeModeTab.Output output) {
        output.accept(FLARE_GUN.get());
        output.accept(FLARE_SHELL.get());
        output.accept(SIGNALLING_FLARE_SHELL.get());
        for (final DyeColor color : DyeColor.values()) {
            ItemStack stack = FLARE.toStack();
            stack.set(AllComponents.FLARE.get(), new FlareComponent(color.getFireworkColor() | 0xFF000000, false));
            output.accept(stack);
            stack = SIGNALLING_FLARE.toStack();
            stack.set(AllComponents.FLARE.get(), new FlareComponent(color.getFireworkColor() | 0xFF000000, true));
            output.accept(stack);
        }
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Flares.MODID);

    public static final DeferredItem<FlareGunItem> FLARE_GUN = ITEMS.register("flare_gun",
            () -> new FlareGunItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLARE_SHELL = ITEMS.registerSimpleItem("flare_shell");
    public static final DeferredItem<Item> SIGNALLING_FLARE_SHELL = ITEMS.registerSimpleItem("signalling_flare_shell");

    public static final DeferredItem<Item> FLARE = ITEMS.register("flare",
            () -> new FlareItem(new Item.Properties()
                    .component(AllComponents.FLARE.get(), FlareComponent.DEFAULT)
            ));

    public static final DeferredItem<Item> SIGNALLING_FLARE = ITEMS.register("signalling_flare",
            () -> new FlareItem(new Item.Properties()
                    .component(AllComponents.FLARE.get(), FlareComponent.DEFAULT_SIGNALLING)
            ));
}

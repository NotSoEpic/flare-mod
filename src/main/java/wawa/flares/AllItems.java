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

public class AllItems {
    public static void init(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static void addTabItems(final CreativeModeTab.Output output) {
        output.accept(FLARE_GUN.get());
        for (final DyeColor color : DyeColor.values()) {
            final ItemStack stack = FLARE.toStack();
            stack.set(AllComponents.FLARE.get(), new FlareComponent(color.getFireworkColor() | 0xFF000000));
            output.accept(stack);
        }
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Flares.MODID);
    public static final DeferredItem<FlareGunItem> FLARE_GUN = ITEMS.register("flare_gun",
            () -> new FlareGunItem(new Item.Properties()));
    public static final DeferredItem<Item> FLARE = ITEMS.register("flare",
            () -> new Item(new Item.Properties()
                    .component(AllComponents.FLARE.get(), FlareComponent.DEFAULT)
            ));
}

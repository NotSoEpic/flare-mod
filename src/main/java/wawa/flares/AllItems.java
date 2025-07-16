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
        output.accept(ILLUMINATION_FLARE_SHELL.get());
        output.accept(SIGNALLING_FLARE_SHELL.get());
        for (final DyeColor color : DyeColor.values()) {
            final ItemStack stack = FLARE.toStack();
            stack.set(AllComponents.FLARE.get(), FlareComponent.illuminating(color.getTextureDiffuseColor()));
            output.accept(stack);
        }
        for (final DyeColor color : DyeColor.values()) {
            final ItemStack stack = SIGNALLING_FLARE.toStack();
            stack.set(AllComponents.FLARE.get(), FlareComponent.signalling(color.getTextureDiffuseColor()));
            output.accept(stack);
        }
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Flares.MODID);

    public static final DeferredItem<FlareGunItem> FLARE_GUN = ITEMS.register("flare_gun",
            () -> new FlareGunItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ILLUMINATION_FLARE_SHELL = ITEMS.registerSimpleItem("illumination_flare_shell");
    public static final DeferredItem<Item> SIGNALLING_FLARE_SHELL = ITEMS.registerSimpleItem("signalling_flare_shell");

    public static final DeferredItem<Item> FLARE = ITEMS.register("illumination_flare",
            () -> new FlareItem(new Item.Properties()
                    .component(AllComponents.FLARE.get(), FlareComponent.illuminating(DyeColor.WHITE.getTextureDiffuseColor()))
            ));

    public static final DeferredItem<Item> SIGNALLING_FLARE = ITEMS.register("signalling_flare",
            () -> new FlareItem(new Item.Properties()
                    .component(AllComponents.FLARE.get(), FlareComponent.signalling(DyeColor.WHITE.getTextureDiffuseColor()))
            ));
}

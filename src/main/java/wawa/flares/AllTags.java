package wawa.flares;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AllTags {
    public static final TagKey<Item> FLARES = TagKey.create(
            Registries.ITEM,
            Flares.resource("flares")
    );
    public static final TagKey<Item> USABLE_FLARES = TagKey.create(
            Registries.ITEM,
            Flares.resource("usable_flares")
    );
}

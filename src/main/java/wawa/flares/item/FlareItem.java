package wawa.flares.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wawa.flares.AllComponents;
import wawa.flares.data_component.FlareComponent;

import java.util.List;

public class FlareItem extends Item {
    public FlareItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final FlareComponent component = stack.get(AllComponents.FLARE);
        if (component != null) {
            component.appendHoverText(context, tooltipComponents, tooltipFlag);
        }
    }
}

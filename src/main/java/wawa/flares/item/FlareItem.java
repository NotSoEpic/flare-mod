package wawa.flares.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import wawa.flares.AllComponents;
import wawa.flares.AllItems;
import wawa.flares.FlareConfig;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.shot_flare.FlareEntity;

import java.util.List;

public class FlareItem extends Item implements ProjectileItem {
    public FlareItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final FlareComponent component = stack.get(AllComponents.FLARE);
        if (component != null) {
            component.appendHoverText(context, tooltipComponents, tooltipFlag);
            if (!component.trackable() && !FlareConfig.CONFIG.doDynamicLights.get()) {
                tooltipComponents.add(Component.literal("kinda useless because dynamic lights are off :c"));
            }
        }
    }

    @Override
    public Projectile asProjectile(final Level level, final Position pos, final ItemStack stack, final Direction direction) {
        final FlareEntity flare = new FlareEntity(level, stack);
        flare.setPos(pos.x(), pos.y(), pos.z());
        return flare;
    }

    private static final DispenseConfig dispenseConfig = DispenseConfig.builder()
            .power(3)
            .build();

    @Override
    public DispenseConfig createDispenseConfig() {
        return dispenseConfig;
    }

    public static void init() {
        DispenserBlock.registerProjectileBehavior(AllItems.FLARE);
        DispenserBlock.registerProjectileBehavior(AllItems.SIGNALLING_FLARE);
    }
}

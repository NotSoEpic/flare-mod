package wawa.flares.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wawa.flares.AllComponents;

import java.util.List;

public record FlareComponent(int argbColor, boolean trackable) {
    public static FlareComponent DEFAULT = new FlareComponent(-1, false);
    public static FlareComponent DEFAULT_SIGNALLING = new FlareComponent(-1, true);
    public static final Codec<FlareComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(FlareComponent::argbColor),
                    Codec.BOOL.optionalFieldOf("trackable", false).forGetter(FlareComponent::trackable)
            ).apply(instance, FlareComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlareComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FlareComponent::argbColor,
            ByteBufCodecs.BOOL, FlareComponent::trackable,
            FlareComponent::new
    );

    public boolean isDarkerThanDark() {
        return this.argbColor == (DyeColor.BLACK.getFireworkColor() | 0xFF000000);
    }

    public static int tint(final ItemStack stack, final int tintIndex) {
        final FlareComponent component = stack.get(AllComponents.FLARE.get());
        if (component != null) {
            if (tintIndex == 1) {
                return component.argbColor();
            }
        }
        return -1;
    }

    public void appendHoverText(final Item.TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
//        if (this.trackable) {
//            tooltipComponents.add(Component.translatable("flares.tooltip.trackable"));
//        }
    }
}

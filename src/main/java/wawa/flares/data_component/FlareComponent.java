package wawa.flares.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import wawa.flares.AllComponents;

public record FlareComponent(int argbColor, boolean trackable) {
    public static FlareComponent DEFAULT = new FlareComponent(-1, false);
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

    public static int tint(final ItemStack stack, final int tintIndex) {
        if (tintIndex != 1) {
            return -1;
        }
        final FlareComponent component = stack.get(AllComponents.FLARE.get());
        if (component != null) {
            return component.argbColor();
        }
        return -1;
    }
}

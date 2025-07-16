package wawa.flares.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import wawa.flares.AllComponents;
import wawa.flares.AllRecipes;
import wawa.flares.data_component.FlareComponent;

import java.util.ArrayList;

public class FlareDyeRecipe extends CustomRecipe {
    private final Ingredient shell;
    private final ItemStack result;
    public FlareDyeRecipe(final CraftingBookCategory category, final Ingredient shell, final ItemStack result) {
        super(category);
        this.shell = shell;
        this.result = result;
    }

    public Ingredient getShell() {
        return this.shell;
    }

    public ItemStack getResult() {
        return this.result.copy();
    }

    public ItemStack getResult(final int count) {
        return this.result.copyWithCount(count);
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        int shells = 0;
        for (final ItemStack item : input.items()) {
            if (item.isEmpty()) {
                continue;
            }
            if (this.getShell().test(item)) {
                shells++;
            } else if (!(item.getItem() instanceof DyeItem)) {
                return false;
            }
        }
        return shells > 0;
    }

    @Override
    public ItemStack assemble(final CraftingInput input, final HolderLookup.Provider registries) {
        int shells = 0;
        final ArrayList<DyeItem> dyes = new ArrayList<>();
        for (final ItemStack item : input.items()) {
            if (item.isEmpty()) {
                continue;
            }
            if (this.getShell().test(item)) {
                shells++;
            } else if (item.getItem() instanceof final DyeItem dyeItem) {
                dyes.add(dyeItem);
            } else {
                return ItemStack.EMPTY;
            }
        }
        return shells > 0 ? this.mixDyesIntoResult(dyes, shells) : ItemStack.EMPTY;
    }

    public static int mixTextDyes(final ArrayList<DyeItem> dyes) {
        if (dyes.isEmpty()) {
            return 0;
        }
        // copied from DyedItemColor
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int i1 = 0;

        for (final DyeItem dyeitem : dyes) {
            final int j3 = dyeitem.getDyeColor().getTextureDiffuseColor();
            final int i2 = FastColor.ARGB32.red(j3);
            final int j2 = FastColor.ARGB32.green(j3);
            final int k2 = FastColor.ARGB32.blue(j3);
            l += Math.max(i2, Math.max(j2, k2));
            i += i2;
            j += j2;
            k += k2;
            i1++;
        }

        int l2 = i / i1;
        int i3 = j / i1;
        int k3 = k / i1;
        final float f = (float)l / (float)i1;
        final float f1 = (float)Math.max(l2, Math.max(i3, k3));
        l2 = (int)((float)l2 * f / f1);
        i3 = (int)((float)i3 * f / f1);
        k3 = (int)((float)k3 * f / f1);
        return FastColor.ARGB32.color(255, l2, i3, k3);
    }

    private ItemStack mixDyesIntoResult(final ArrayList<DyeItem> dyes, final int shells) {
        final int color = mixTextDyes(dyes);
        if (color == 0) {
            return ItemStack.EMPTY;
        }

        final ItemStack result = this.getResult(shells);
        final boolean trackable = result.has(AllComponents.FLARE) && result.get(AllComponents.FLARE).trackable();
        int maxAge = trackable ? FlareComponent.SIGNALLING_MAX_AGE : FlareComponent.ILLUMINATING_MAX_AGE;
        if (result.has(AllComponents.FLARE)) {
            maxAge = result.get(AllComponents.FLARE).maxAge();
        }
        result.set(AllComponents.FLARE, new FlareComponent(color, maxAge, trackable));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AllRecipes.FLARE_DYE_SERIALIZER.get();
    }

//    @Override
//    public RecipeType<?> getType() {
//        return AllRecipes.FLARE_DYE.get();
//    }

    public static class Serializer implements RecipeSerializer<FlareDyeRecipe> {
        public static final MapCodec<FlareDyeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CustomRecipe::category),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FlareDyeRecipe::getShell),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FlareDyeRecipe::getResult)
                ).apply(instance, FlareDyeRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, FlareDyeRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<FlareDyeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FlareDyeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static FlareDyeRecipe fromNetwork(final RegistryFriendlyByteBuf buffer) {
            final CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            final Ingredient shellIngredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            final ItemStack flareResult = ItemStack.STREAM_CODEC.decode(buffer);
            return new FlareDyeRecipe(category, shellIngredient, flareResult);
        }

        private static void toNetwork(final RegistryFriendlyByteBuf buffer, final FlareDyeRecipe recipe) {
            buffer.writeEnum(recipe.category());
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getShell());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResult());
        }
    }
}

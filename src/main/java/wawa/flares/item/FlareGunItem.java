package wawa.flares.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wawa.flares.AllComponents;
import wawa.flares.AllEntities;
import wawa.flares.AllItems;
import wawa.flares.Flares;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.shot_flare.FlareEntity;

import java.util.List;
import java.util.function.Predicate;

public class FlareGunItem extends ProjectileWeaponItem {
    public static int CHARGE_TIME = 10;
    public FlareGunItem(final Properties properties) {
        super(properties);
    }

    public static void registerPredicate() {
        ItemProperties.register(
                AllItems.FLARE_GUN.get(),
                Flares.resource("loaded"),
                (stack, level, entity, seed) -> isLoaded(stack) ? 1.0f : 0.0f
        );
    }

    public static boolean isLoaded(final ItemStack itemstack) {
        final ChargedProjectiles chargedprojectiles = itemstack.get(DataComponents.CHARGED_PROJECTILES);
        return chargedprojectiles != null && !chargedprojectiles.isEmpty();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack itemstack = player.getItemInHand(hand);
        if (isLoaded(itemstack)) {
            this.performShooting(level, player, hand, itemstack);
            return InteractionResultHolder.consume(itemstack);
        } else if (!player.getProjectile(itemstack).isEmpty()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public int getUseDuration(final ItemStack stack, final LivingEntity entity) {
        return CHARGE_TIME + 3;
    }

    @Override
    public UseAnim getUseAnimation(final ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(final ItemStack stack, final Level level, final LivingEntity livingEntity, final int timeCharged) {
        final int i = this.getUseDuration(stack, livingEntity) - timeCharged;
        final float f = getPowerForTime(i, stack, livingEntity);
        if (f >= 1.0F && !isCharged(stack)) {
            final boolean success = tryLoadFlares(livingEntity, stack);
        }
    }
    private static float getPowerForTime(final int timeLeft, final ItemStack stack, final LivingEntity shooter) {
        float f = (float)timeLeft / (float)CHARGE_TIME;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    public static boolean isCharged(final ItemStack crossbowStack) {
        final ChargedProjectiles chargedprojectiles = crossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        return !chargedprojectiles.isEmpty();
    }

    private static boolean tryLoadFlares(final LivingEntity shooter, final ItemStack flareGunStack) {
        final List<ItemStack> list = draw(flareGunStack, shooter.getProjectile(flareGunStack), shooter);
        if (!list.isEmpty()) {
            flareGunStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean useOnRelease(final ItemStack stack) {
        return stack.is(this);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltipComponents, final TooltipFlag tooltipFlag) {
        final ChargedProjectiles chargedprojectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedprojectiles != null && !chargedprojectiles.isEmpty()) {
            final ItemStack itemstack = chargedprojectiles.getItems().getFirst();
            final MutableComponent name = itemstack.getDisplayName().copy();
            final FlareComponent component = itemstack.get(AllComponents.FLARE);
            if (component != null) {
                name.withColor(component.argbColor());
            }
            tooltipComponents.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(name));
        }
    }

    public void performShooting(final Level level, final LivingEntity shooter, final InteractionHand hand, final ItemStack weapon) {
        final ChargedProjectiles chargedprojectiles = weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (chargedprojectiles != null && !chargedprojectiles.isEmpty() &&
                level instanceof final ServerLevel serverlevel) {
            this.shoot(serverlevel, shooter, shooter.getUsedItemHand(), weapon, chargedprojectiles.getItems(), 3.0F, 1.0F, true, null);
        }
    }

    @Override
    protected Projectile createProjectile(final Level level, final LivingEntity shooter, final ItemStack weapon, final ItemStack ammo, final boolean isCrit) {
        final FlareEntity entity = AllEntities.FLARE.get().create(level);
        entity.setPos(shooter.getEyePosition());
        entity.setPickupItemStack(ammo);
        final FlareComponent component = ammo.get(AllComponents.FLARE);
        if (component != null) {
            entity.setTrackable(component.trackable());
        }
        return entity;
    }

    @Override
    protected void shootProjectile(final LivingEntity shooter, final Projectile projectile, final int index, final float velocity, final float inaccuracy, final float angle, @Nullable final LivingEntity target) {
        projectile.setDeltaMovement(shooter.getLookAngle().scale(velocity));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> false;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles(final ItemStack stack) {
        return s -> s.is(AllItems.FLARE);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable final Player player, final ItemStack projectileWeaponItem) {
        return ItemStack.EMPTY;
    }
}

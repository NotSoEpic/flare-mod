package wawa.flares.shot_flare;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.PointLight;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wawa.flares.AllComponents;
import wawa.flares.AllEntities;
import wawa.flares.AllItems;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.mixinterface.SetRemovedListener;
import wawa.flares.packets.FlareKillPacket;

import java.util.List;

public class FlareEntity extends AbstractArrow implements SetRemovedListener {
    private static final EntityDataAccessor<ItemStack> FLARE_ITEM = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRACKABLE = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.BOOLEAN);
    private PointLight outerLight;
    private PointLight innerLight;
    public boolean theShadowsCuttingDeeper = false;
    public int color = -1;
    public FlareEntity(final EntityType<FlareEntity> entityType, final Level level) {
        super(entityType, level);
    }

    public FlareEntity(final Level level, final ItemStack itemStack) {
        super(AllEntities.FLARE.get(), level);
        this.setPickupItemStack(itemStack.copyWithCount(1));
        final FlareComponent component = itemStack.get(AllComponents.FLARE);
        if (component != null) {
            this.setTrackable(component.trackable());
        }
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLARE_ITEM, AllItems.FLARE.toStack());
        builder.define(IN_GROUND, false);
        builder.define(TRACKABLE, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.entityData.set(IN_GROUND, this.inGround);
        if (this.isTrackable()) {
            if (!this.inGround) {
                this.setDeltaMovement(extraTickMovement(this.getDeltaMovement()));
                this.level().addParticle(
                        ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0,
                        0.005,
                        0
                );
            }
            if (this.level() instanceof final ServerLevel serverLevel) {
                FlareHandlerServer.flareEntityTick(serverLevel, this);
            } else if (this.level() instanceof final ClientLevel clientLevel) {
                FlareHandlerClient.flareEntityTick(clientLevel, this);
            }
        }
        this.tickCount++;
        if (this.tickCount > 1200) {
            this.discard();
        }
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        return true;
    }

    @Override
    protected void doKnockback(final LivingEntity entity, final DamageSource damageSource) {
        super.doKnockback(entity, damageSource);
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
    }

    public static Vec3 extraTickMovement(final Vec3 deltaMovement) {
        return deltaMovement.multiply(0.97, 0.95, 0.97).subtract(0, 0.002, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return this.isTrackable() ? 0 : super.getDefaultGravity();
    }

    @Override
    protected void onHitBlock(final BlockHitResult result) {
        super.onHitBlock(result);
        this.entityData.set(IN_GROUND, this.inGround);
    }

    public boolean isTrackable() {
        return this.entityData.get(TRACKABLE);
    }

    public void setTrackable(final boolean v) {
        this.entityData.set(TRACKABLE, v);
    }

    @Override
    public void onSyncedDataUpdated(final List<SynchedEntityData.DataValue<?>> dataValues) {
        for (final SynchedEntityData.DataValue<?> dataValue : dataValues) {
            if (dataValue.id() == FLARE_ITEM.id()) {
                final FlareComponent component = ((ItemStack)dataValue.value()).get(AllComponents.FLARE);
                if (component != null) {
                    if (this.outerLight == null) {
                        this.theShadowsCuttingDeeper = component.isDarkerThanDark();
                        this.outerLight = new PointLight();
                        this.outerLight.setPosition(this.getX(), this.getY(), this.getZ());
                        this.outerLight.setBrightness(this.theShadowsCuttingDeeper ? -1f : 1f);
                        this.outerLight.setRadius(25.0f);

                        this.innerLight = new PointLight();
                        this.innerLight.setPosition(this.getX(), this.getY(), this.getZ());
                        this.innerLight.setBrightness(this.theShadowsCuttingDeeper ? -3f : 3f);
                        this.innerLight.setRadius(2.5f);

                        this.color = component.argbColor();
                        this.outerLight.setColor(this.color);
                        this.innerLight.setColor(this.color);

                        VeilRenderSystem.renderer().getLightRenderer().addLight(this.outerLight);
                        VeilRenderSystem.renderer().getLightRenderer().addLight(this.innerLight);
                    }
                }

                return;
            } else if (dataValue.id() == IN_GROUND.id()) {
                this.inGround = (boolean)dataValue.value();
            } else if (dataValue.id() == TRACKABLE.id()) {
                if (!(boolean)dataValue.value() && this.level() instanceof final ClientLevel clientLevel) {
                    FlareHandlerClient.remove(clientLevel, this.uuid);
                }
            }
        }
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        if (this.outerLight != null) {
            VeilRenderSystem.renderer().getLightRenderer().removeLight(this.outerLight);
            VeilRenderSystem.renderer().getLightRenderer().removeLight(this.innerLight);
        }
    }

    public void updateLight(final float partialTick) {
        final Vec3 pos = this.getPosition(partialTick);
        if (this.outerLight != null) {
            this.outerLight.setPosition(pos.x, pos.y, pos.z);
            this.innerLight.setPosition(pos.x, pos.y, pos.z);
            if (this.tickCount > 800) {
                float brightness = (1200f - this.tickCount) / (1200f - 800f);
                if (this.theShadowsCuttingDeeper) {
                    brightness *= -1;
                }
                this.outerLight.setBrightness(brightness);
                this.outerLight.setRadius(25 * brightness);
                this.innerLight.setBrightness(brightness * 3);
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return AllItems.FLARE.toStack();
    }

    @Override
    public void setPickupItemStack(final ItemStack pickupItemStack) {
        super.setPickupItemStack(pickupItemStack);
        this.setSyncedStack(pickupItemStack);
    }

    private void setSyncedStack(final ItemStack stack) {
        this.getEntityData().set(FLARE_ITEM, stack);
        final FlareComponent component = stack.get(AllComponents.FLARE);
        if (component != null) {
            this.color = component.argbColor();
        }
    }

    public boolean inGround() {
        return this.inGround;
    }

    @Override
    protected void tickDespawn() {
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TickCount", this.tickCount);
        compound.putBoolean("Trackable", this.isTrackable());
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tickCount = compound.getInt("TickCount");
        this.setTrackable(compound.getBoolean("Trackable"));
        this.entityData.set(IN_GROUND, this.inGround);
        if (this.level() instanceof final ServerLevel serverLevel) {
            if (FlareHandlerServer.isRemoved(this.uuid)) {
                this.remove(RemovalReason.DISCARDED);
                return;
            }

            final FlareData data = FlareHandlerServer.get(serverLevel, this.uuid);
            if (data != null) {
                data.applyToEntity(this);
            }
        }
    }

    @Override
    public void flares$onRemoved(final RemovalReason reason) {
        if (this.isTrackable()) {
            if (this.level() instanceof final ServerLevel serverLevel) {
                if (reason.shouldSave()) {
                    FlareHandlerServer.setUnloaded(serverLevel, this.uuid);
                }
                if (reason.shouldDestroy()) {
                    VeilPacketManager.level(serverLevel).sendPacket(new FlareKillPacket(this.uuid));
                    FlareHandlerServer.remove(serverLevel, this.uuid);
                }
            } else if (this.level() instanceof final ClientLevel clientLevel) {
                if (reason.shouldSave()) {
                    FlareHandlerClient.setUnloaded(clientLevel, this.uuid);
                } else {
                    FlareHandlerClient.remove(clientLevel, this.uuid);
                }
            }
        }
    }
}

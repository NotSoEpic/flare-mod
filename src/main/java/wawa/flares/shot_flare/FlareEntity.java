package wawa.flares.shot_flare;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
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
import wawa.flares.FlareConfig;
import wawa.flares.data_component.FlareComponent;
import wawa.flares.mixinterface.SetRemovedListener;
import wawa.flares.packets.FlareKillPacket;

import java.util.List;

public class FlareEntity extends AbstractArrow implements SetRemovedListener {
    private static final EntityDataAccessor<ItemStack> FLARE_ITEM = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> TICK_COUNT = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_AGE = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRACKABLE = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.BOOLEAN);
    private LightRenderHandle<PointLightData> outerLight;
    private LightRenderHandle<PointLightData> innerLight;
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
            this.setMaxAge(component.maxAge());
        }
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLARE_ITEM, AllItems.FLARE.toStack());
        builder.define(TICK_COUNT, 0);
        builder.define(MAX_AGE, 2400);
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
            }
            if (this.level() instanceof final ServerLevel serverLevel) {
                if (FlareHandlerServer.get(serverLevel).isRemoved(this.uuid)) {
                    this.discard();
                } else {
                    FlareHandlerServer.get(serverLevel).flareEntityTick(serverLevel, this);
                }
            } else if (this.level() instanceof final ClientLevel clientLevel) {
                FlareHandlerClient.flareEntityTick(clientLevel, this);
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
        }

        this.syncTickCount(this.tickCount);
        if (this.tickCount > this.getMaxAge()) {
            this.discard();
        }
    }

    private void syncTickCount(final int v) {
        this.tickCount = v;
        this.getEntityData().set(TICK_COUNT, v);
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
        return deltaMovement.multiply(0.99, 0.99, 0.99).subtract(0, 0.001, 0);
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

    public int getMaxAge() {
        return this.entityData.get(MAX_AGE);
    }

    public void setMaxAge(final int maxAge) {
        this.entityData.set(MAX_AGE, maxAge);
    }

    @Override
    public void onSyncedDataUpdated(final List<SynchedEntityData.DataValue<?>> dataValues) {
        for (final SynchedEntityData.DataValue<?> dataValue : dataValues) {
            if (dataValue.id() == FLARE_ITEM.id()) {
                final FlareComponent component = ((ItemStack)dataValue.value()).get(AllComponents.FLARE);
                if (component != null) {
                    this.color = component.argbColor();
                    if (FlareConfig.CONFIG.doDynamicLights.get() && this.outerLight == null) {
                        final PointLightData outerLight = new PointLightData();
                        outerLight.setPosition(this.getX(), this.getY(), this.getZ());
                        outerLight.setBrightness(1f);
                        outerLight.setRadius(25.0f);

                        final PointLightData innerLight = new PointLightData();
                        innerLight.setPosition(this.getX(), this.getY(), this.getZ());
                        innerLight.setBrightness(3f);
                        innerLight.setRadius(2.5f);

                        outerLight.setColor(this.color);
                        innerLight.setColor(this.color);

                        this.outerLight = VeilRenderSystem.renderer().getLightRenderer().addLight(outerLight);
                        this.innerLight = VeilRenderSystem.renderer().getLightRenderer().addLight(innerLight);
                    }
                }

                return;
            } else if (dataValue.id() == TICK_COUNT.id()) {
                this.tickCount = (int)dataValue.value();
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
            this.outerLight.free();
            this.innerLight.free();
        }
    }

    public static float getIntensity(final boolean trackable, final float tickCount, final int maxAge) {
        if (trackable) {
            if (tickCount < 100) {
                return tickCount / 100;
            } else if (tickCount > (maxAge - 400)) {
                return (maxAge - tickCount) / 400f;
            }
        } else {
            if (tickCount < 5) {
                return tickCount / 5;
            } else if (tickCount > (maxAge - 1200)) {
                return (maxAge - tickCount) / 1200f;
            }
        }
        return 1;
    }

    public void updateLight(final float partialTick) {
        final Vec3 pos = this.getPosition(partialTick);
        if (this.outerLight != null) {
            final PointLightData outerLight = this.outerLight.getLightData();
            final PointLightData innerLight = this.innerLight.getLightData();
            outerLight.setPosition(pos.x, pos.y, pos.z);
            innerLight.setPosition(pos.x, pos.y, pos.z);
            float brightness = getIntensity(this.isTrackable(), this.tickCount, this.getMaxAge());
            outerLight.setBrightness(brightness);
            outerLight.setRadius(25 * brightness);
            innerLight.setBrightness(brightness * 3);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return AllItems.FLARE.toStack();
    }

    @Override
    public void setPickupItemStack(final ItemStack pickupItemStack) {
        super.setPickupItemStack(pickupItemStack);
        this.setSyncedStack(this.getPickupItem());
    }

    private void setSyncedStack(final ItemStack stack) {
        this.getEntityData().set(FLARE_ITEM, stack);
        final FlareComponent component = stack.get(AllComponents.FLARE);
        if (component != null) {
            this.color = component.argbColor();
            this.setMaxAge(component.maxAge());
            this.setTrackable(component.trackable());
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
        compound.putInt("MaxAge", this.getMaxAge());
        compound.putBoolean("Trackable", this.isTrackable());
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tickCount = compound.getInt("TickCount");
        this.setMaxAge(compound.getInt("MaxAge"));
        this.setTrackable(compound.getBoolean("Trackable"));
        this.entityData.set(IN_GROUND, this.inGround);
        if (this.level() instanceof final ServerLevel serverLevel) {
            final FlareData data = FlareHandlerServer.get(serverLevel).get(this.uuid);
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
                    FlareHandlerServer.get(serverLevel).setUnloaded(this.uuid);
                }
                if (reason.shouldDestroy()) {
                    VeilPacketManager.level(serverLevel).sendPacket(new FlareKillPacket(this.uuid));
                    FlareHandlerServer.get(serverLevel).remove(this.uuid);
                }
            } else if (this.level() instanceof final ClientLevel clientLevel) {
                // clientside reason is always discarded, but will be removed with the flare kill packet if its truly gone
                FlareHandlerClient.setUnloaded(clientLevel, this.uuid);
            }
        }
    }
}

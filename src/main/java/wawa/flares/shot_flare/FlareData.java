package wawa.flares.shot_flare;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FlareData {
    public static final Codec<FlareData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(FlareData::getColor),
                    Vec3.CODEC.fieldOf("pos").forGetter(FlareData::getPos),
                    Vec3.CODEC.fieldOf("vel").forGetter(FlareData::getVel),
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(FlareData::getUuid),
                    Codec.INT.fieldOf("life").forGetter(FlareData::getLife),
                    Codec.BOOL.fieldOf("inGround").forGetter(FlareData::isInGround)
            ).apply(instance, FlareData::new));

    public static final StreamCodec<ByteBuf, FlareData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private boolean entityLoaded;

    private int color;
    private Vec3 pos;
    private Vec3 vel;
    private UUID uuid;
    private int life;
    private boolean inGround;

    public FlareData(final boolean entityLoaded, final FlareEntity uuid) {
        this.entityLoaded = entityLoaded;
        this.copyFromEntity(uuid);
    }

    public FlareData(final int color, final Vec3 pos, final Vec3 vel, final UUID uuid, final int life, final boolean inGround) {
        this(false, color, pos, vel, uuid, life, inGround);
    }

    public FlareData(final boolean entityLoaded, final int color, final Vec3 pos, final Vec3 vel, final UUID uuid, final int life, final boolean inGround) {
        this.entityLoaded = entityLoaded;
        this.color = color;
        this.pos = pos;
        this.vel = vel;
        this.uuid = uuid;
        this.life = life;
        this.inGround = inGround;
    }

    public void setLoaded(final boolean v) {
        this.entityLoaded = v;
    }

    public boolean isLoaded() {
        return this.entityLoaded;
    }

    public void copyFromEntity(final FlareEntity entity) {
        this.color = entity.color;
        this.pos = entity.position();
        this.vel = entity.getDeltaMovement();
        this.uuid = entity.getUUID();
        this.life = entity.tickCount;
        this.inGround = entity.inGround();
    }

    public void applyToEntity(final FlareEntity entity) {
        entity.color = this.color;
        entity.setPos(this.pos);
        entity.setDeltaMovement(this.vel);
        entity.tickCount = this.life;
        entity.setOnGround(this.inGround);
    }

    public boolean unloadedTick() {
        if (!this.entityLoaded) {
            this.tickMovement();
            this.life++;
            return this.shouldRemove();
        }
        return false;
    }

    public void tickMovement() {
        if (!this.inGround) {
            this.pos = this.pos.add(this.vel);
            this.vel = FlareEntity.extraTickMovement(this.vel.scale(0.99));
        }
    }

    public boolean shouldRemove() {
        return this.life > 1200;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    public int getColor() {
        return this.color;
    }

    public Vec3 getPos() {
        return this.pos;
    }

    public BlockPos getBlockPos() {
        return BlockPos.containing(this.pos.x, this.pos.y, this.pos.z);
    }

    public Vec3 getVel() {
        return this.vel;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getLife() {
        return this.life;
    }

    public boolean isInGround() {
        return this.inGround;
    }
}

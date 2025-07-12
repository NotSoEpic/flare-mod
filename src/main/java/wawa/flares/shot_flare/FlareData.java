package wawa.flares.shot_flare;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import wawa.flares.Flares;

import java.util.UUID;

public class FlareData {
    public static final Codec<FlareData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(FlareData::getColor),
                    Vec3.CODEC.fieldOf("pos").forGetter(FlareData::getPos),
                    Vec3.CODEC.fieldOf("vel").forGetter(FlareData::getVel),
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(FlareData::getEntity),
                    Codec.INT.fieldOf("life").forGetter(FlareData::getLife),
                    Codec.BOOL.fieldOf("inGround").forGetter(FlareData::isInGround)
            ).apply(instance, FlareData::new));

    public static final StreamCodec<ByteBuf, FlareData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private int color;
    private Vec3 pos;
    private Vec3 vel;
    private UUID entity;
    private int life;
    private boolean inGround;

    public FlareData(final FlareEntity entity) {
        this.copyFromEntity(entity);
    }

    public FlareData(final int color, final Vec3 pos, final Vec3 vel, final UUID entity, final int life, final boolean inGround) {
        this.color = color;
        this.pos = pos;
        this.vel = vel;
        this.entity = entity;
        this.life = life;
        this.inGround = inGround;
    }

    public void copyFromEntity(final FlareEntity entity) {
        this.color = entity.color;
        this.pos = entity.position();
        this.vel = entity.getDeltaMovement();
        this.entity = entity.getUUID();
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
        this.tickMovement();
        this.life++;
        return this.shouldRemove();
    }

    public void tickMovement() {
        if (!this.inGround) {
            this.pos = this.pos.add(this.vel);
            this.vel = this.vel.scale(0.99).add(0, -0.05, 0);
        }
    }

    public boolean shouldRemove() {
        return this.life > 1200;
    }

    @Override
    public String toString() {
        return "FlareData{" +
                "color=" + this.color +
                ", pos=" + this.pos +
                ", vel=" + this.vel +
                ", entity=" + this.entity +
                ", life=" + this.life +
                ", inGround=" + this.inGround +
                '}';
    }

    @Override
    public int hashCode() {
        return this.entity.hashCode();
    }

    public int getColor() {
        return this.color;
    }

    public Vec3 getPos() {
        return this.pos;
    }

    public Vec3 getVel() {
        return this.vel;
    }

    public UUID getEntity() {
        return this.entity;
    }

    public int getLife() {
        return this.life;
    }

    public boolean isInGround() {
        return this.inGround;
    }
}

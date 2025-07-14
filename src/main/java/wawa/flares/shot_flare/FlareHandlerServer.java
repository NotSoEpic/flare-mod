package wawa.flares.shot_flare;

import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import wawa.flares.Flares;
import wawa.flares.packets.FlareDataPacket;

import java.util.*;

public class FlareHandlerServer extends SavedData {
    private final Map<UUID, FlareData> flares;
    private final Set<UUID> removed;

    public FlareHandlerServer(final Map<UUID, FlareData> flares, final Set<UUID> removed) {
        this.flares = flares;
        this.removed = removed;
    }
    public FlareHandlerServer() {
        this(new HashMap<>(), new HashSet<>());
    }

    public List<UUID> getRemovedAsList() {
        return this.removed.stream().toList();
    }

    public FlareData get(final UUID id) {
        this.setDirty();
        return this.flares.get(id);
    }

    public void flareEntityTick(final ServerLevel serverLevel, final FlareEntity flare) {
        if (flare.getRemovalReason() != null) {
            return;
        }
        this.setDirty();
        FlareData data = this.flares.get(flare.getUUID());
        if (data == null) {
            data = new FlareData(true, flare);
            data.setSyncable(serverLevel.canSeeSky(flare.blockPosition()));
            this.flares.put(flare.getUUID(), data);
            if (data.isSyncable()) {
                VeilPacketManager.level(serverLevel).sendPacket(new FlareDataPacket(data));
            }
        } else {
            if (data.isLoaded()) {
                data.copyFromEntity(flare);
            } else {
                data.applyToEntity(flare);
                data.setLoaded(true);
            }
            data.setSyncable(serverLevel.canSeeSky(flare.blockPosition()));
        }
    }

    public void setUnloaded(final UUID flare) {
        final FlareData data = this.flares.get(flare);
        if (data != null) {
            this.setDirty();
            data.setLoaded(false);
        }
    }

    public boolean isRemoved(final UUID flare) {
        return this.removed.contains(flare);
    }

    public void remove(final UUID flare) {
        this.setDirty();
        this.flares.remove(flare);
        this.removed.add(flare);
    }

    @SubscribeEvent
    public static void tickFlares(final LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof final ServerLevel serverLevel) {
            get(serverLevel).tickFlaresLevel(serverLevel);
        }
    }

    public void tickFlaresLevel(final ServerLevel level) {
        final ArrayList<FlareDataPacket> packets = new ArrayList<>();
        final DistanceManager distanceManager = level.getChunkSource().chunkMap.getDistanceManager();
        for (final Iterator<Map.Entry<UUID, FlareData>> it = this.flares.entrySet().iterator(); it.hasNext(); ) {
            final FlareData flare = it.next().getValue();
            if (!distanceManager.inEntityTickingRange(ChunkPos.asLong(flare.getBlockPos()))) {
                this.setDirty();
                flare.setLoaded(false);
            }
            if (flare.unloadedTick()) {
                this.setDirty();
                it.remove();
                this.removed.add(flare.getUuid());
            }

            if (flare.isSyncable() && level.getServer().getTickCount() % 20 == 0) {
                packets.add(new FlareDataPacket(flare));
            }
        }

        if (!packets.isEmpty()) {
            final FlareDataPacket[] packetArray = packets.toArray(new FlareDataPacket[0]);
            VeilPacketManager.level(level).sendPacket(packetArray);
        }
    }

    private static final String ID = Flares.MODID + "_flare_data";
    public static FlareHandlerServer get(final ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(new Factory<>(FlareHandlerServer::new, FlareHandlerServer::load), ID);
    }

//    public static final Codec<FlareHandlerServer> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    Codec.unboundedMap(UUIDUtil.CODEC.xmap(UUID::toString, UUID::fromString), FlareData.CODEC).fieldOf("active_flares").forGetter(h -> h.flares),
//                    UUIDUtil.CODEC_SET.fieldOf("expired_flares").forGetter(h -> h.removed)
//            ).apply(instance, FlareHandlerServer::new));
    @Override
    public CompoundTag save(final CompoundTag compoundTag, final HolderLookup.Provider provider) {
        final CompoundTag flareData = new CompoundTag();
        this.flares.forEach((id, data) -> {
            flareData.put(id.toString(), FlareData.CODEC.encode(data, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        });
        compoundTag.put("flare_data", flareData);
        compoundTag.put("expired_flares", UUIDUtil.CODEC_SET.encode(this.removed, NbtOps.INSTANCE, new ListTag()).getOrThrow());
        return compoundTag;
    }

    public static FlareHandlerServer load(final CompoundTag tag, final HolderLookup.Provider lookupProvider) {
        final HashMap<UUID, FlareData> flareData = new HashMap<>();
        final CompoundTag nbtFlareData = tag.getCompound("flare_data");
        nbtFlareData.getAllKeys().forEach(key -> {
            flareData.put(UUID.fromString(key), FlareData.CODEC.decode(NbtOps.INSTANCE, nbtFlareData.get(key)).getOrThrow().getFirst());
        });
        final Set<UUID> expiredFlares = UUIDUtil.CODEC_SET.decode(NbtOps.INSTANCE, tag.getList("expired_flares", CompoundTag.TAG_INT_ARRAY)).getOrThrow().getFirst();
        return new FlareHandlerServer(flareData, expiredFlares);
    }
}

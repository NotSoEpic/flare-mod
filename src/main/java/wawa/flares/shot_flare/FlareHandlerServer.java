package wawa.flares.shot_flare;

import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import wawa.flares.Flares;
import wawa.flares.packets.FlareDataPacket;

import java.util.*;

public class FlareHandlerServer {
    private static final HashMap<ServerLevel, HashMap<UUID, FlareData>> flares = new HashMap<>();
    private static final HashSet<UUID> removed = new HashSet<>();

    public static HashMap<UUID, FlareData> getFlaresIn(final ServerLevel serverLevel) {
        return flares.computeIfAbsent(serverLevel, l -> new HashMap<>());
    }

    public static void flareEntityTick(final ServerLevel serverLevel, final FlareEntity flare) {
        final FlareData data = getFlaresIn(serverLevel).get(flare.getUUID());
        if (data == null) {
            getFlaresIn(serverLevel).put(flare.getUUID(), new FlareData(true, flare));
        } else {
            if (data.isLoaded()) {
                data.copyFromEntity(flare);
            } else {
                data.applyToEntity(flare);
                data.setLoaded(true);
            }
        }
    }

    public static @Nullable FlareData get(final ServerLevel serverLevel, final UUID flare) {
        return getFlaresIn(serverLevel).get(flare);
    }

    public static void setUnloaded(final ServerLevel serverLevel, final UUID flare) {
        final FlareData data = getFlaresIn(serverLevel).get(flare);
        if (data != null) {
            data.setLoaded(false);
        }
    }

    public static boolean isRemoved(final UUID flare) {
        return removed.contains(flare);
    }

    public static void remove(final ServerLevel serverLevel, final UUID flare) {
        getFlaresIn(serverLevel).remove(flare);
        removed.add(flare);
    }

    @SubscribeEvent
    public static void tickFlares(final ServerTickEvent.Post event) {
        flares.forEach((level, flareMap) -> {
            final ArrayList<FlareDataPacket> packets = new ArrayList<>();
            final DistanceManager distanceManager = level.getChunkSource().chunkMap.getDistanceManager();
            for(final Iterator<Map.Entry<UUID, FlareData>> it = flareMap.entrySet().iterator(); it.hasNext();) {
                final FlareData flare = it.next().getValue();
                if (distanceManager.inEntityTickingRange(ChunkPos.asLong(flare.getBlockPos()))) {
                    flare.setLoaded(false);
                }
                if (flare.unloadedTick()) {
                    it.remove();
                    removed.add(flare.getEntity());
                }

                if (event.getServer().getTickCount() % 20 == 0) {
                    packets.add(new FlareDataPacket(flare));
                }
            }

            if (!packets.isEmpty()) {
                final FlareDataPacket[] packetArray = packets.toArray(new FlareDataPacket[0]);
                VeilPacketManager.level(level).sendPacket(packetArray);
            }
        });
    }
}

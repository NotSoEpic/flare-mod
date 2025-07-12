package wawa.flares.shot_flare;

import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import wawa.flares.Flares;
import wawa.flares.packets.FlareDataPacket;

import java.util.*;

public class FlareHandlerServer {
    private static final HashMap<ServerLevel, HashMap<UUID, FlareData>> flares = new HashMap<>();
    private static final HashSet<UUID> removed = new HashSet<>();

    public static void put(final ServerLevel level, final FlareEntity flare) {
        final FlareData data = new FlareData(flare);
        flares.computeIfAbsent(level, l -> new HashMap<>()).put(data.getEntity(), data);
        Flares.LOGGER.info("Storing flare server {}", data.getEntity());
        VeilPacketManager.level(level).sendPacket(new FlareDataPacket(data));
    }

    public static @Nullable FlareData remove(final ServerLevel level, final UUID id) {
        if (flares.containsKey(level)) {
            return flares.get(level).remove(id);
        }
        return null;
    }

    public static boolean checkRemoved(final UUID id) {
        return removed.remove(id);
    }

    @SubscribeEvent
    public static void tickFlares(final ServerTickEvent.Post event) {
        flares.forEach((level, flareMap) -> {
            final ArrayList<FlareDataPacket> packets = new ArrayList<>();
            for(final Iterator<Map.Entry<UUID, FlareData>> it = flareMap.entrySet().iterator(); it.hasNext();) {
                final FlareData flare = it.next().getValue();
                if (flare.unloadedTick()) {
                    Flares.LOGGER.info("Removing flare server {}", flare.getEntity());
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

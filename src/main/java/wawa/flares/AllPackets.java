package wawa.flares;

import foundry.veil.api.network.VeilPacketManager;
import wawa.flares.packets.FlareDataPacket;
import wawa.flares.packets.FlareKillPacket;

public class AllPackets {
    public static VeilPacketManager INSTANCE = VeilPacketManager.create(Flares.MODID, "0.1");

    public static void init() {
        INSTANCE.registerClientbound(FlareDataPacket.TYPE, FlareDataPacket.STREAM_CODEC, FlareDataPacket::handle);
        INSTANCE.registerClientbound(FlareKillPacket.TYPE, FlareKillPacket.STREAM_CODEC, FlareKillPacket::handle);
    }
}

package wawa.flares.packets;

import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import wawa.flares.Flares;
import wawa.flares.shot_flare.FlareData;
import wawa.flares.shot_flare.FlareHandlerClient;

import java.util.UUID;

public record FlareKillPacket(UUID id) implements CustomPacketPayload {
    public static final Type<FlareKillPacket> TYPE = new Type<>(Flares.resource("flare_scrindongulode"));

    public static final StreamCodec<ByteBuf, FlareKillPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FlareKillPacket::id,
            FlareKillPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ClientPacketContext context) {
        FlareHandlerClient.remove(context.client().level, this.id);
    }
}

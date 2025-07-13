package wawa.flares.packets;

import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import wawa.flares.Flares;
import wawa.flares.shot_flare.FlareData;
import wawa.flares.shot_flare.FlareHandlerClient;

public record FlareDataPacket(FlareData data) implements CustomPacketPayload {
    public static final Type<FlareDataPacket> TYPE = new Type<>(Flares.resource("flare_data"));

    public static final StreamCodec<ByteBuf, FlareDataPacket> STREAM_CODEC = StreamCodec.composite(
            FlareData.STREAM_CODEC, FlareDataPacket::data,
            FlareDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ClientPacketContext context) {
        FlareHandlerClient.addFlare(context.client().level, this.data);
    }
}

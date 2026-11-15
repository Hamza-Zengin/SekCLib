package com.sekwah.sekclib.network.s2c;

import com.sekwah.sekclib.SekCLib;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Tells the client that they should be moving and at what velocity
 */
public class ClientVelocityPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientVelocityPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "client_velocity"));

    public static final StreamCodec<FriendlyByteBuf, ClientVelocityPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientVelocityPacket::encode, ClientVelocityPacket::decode);

    private final int xa;
    private final int ya;
    private final int za;

    public ClientVelocityPacket(int x, int y, int z) {
        this.xa = x;
        this.ya = y;
        this.za = z;
    }

    public ClientVelocityPacket(Vec3 velocity) {
        double maxVelocity = 3.9D;
        double d1 = Mth.clamp(velocity.x, -maxVelocity, maxVelocity);
        double d2 = Mth.clamp(velocity.y, -maxVelocity, maxVelocity);
        double d3 = Mth.clamp(velocity.z, -maxVelocity, maxVelocity);
        this.xa = (int) (d1 * 8000.0D);
        this.ya = (int) (d2 * 8000.0D);
        this.za = (int) (d3 * 8000.0D);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public int getXa() {
        return xa;
    }

    public int getYa() {
        return ya;
    }

    public int getZa() {
        return za;
    }

    public static void encode(ClientVelocityPacket msg, FriendlyByteBuf outBuffer) {
        outBuffer.writeShort(msg.xa);
        outBuffer.writeShort(msg.ya);
        outBuffer.writeShort(msg.za);
    }

    public static ClientVelocityPacket decode(FriendlyByteBuf inBuffer) {
        short x = inBuffer.readShort();
        short y = inBuffer.readShort();
        short z = inBuffer.readShort();
        return new ClientVelocityPacket(x, y, z);
    }
}

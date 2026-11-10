package com.sekwah.sekclib;

import com.sekwah.sekclib.network.s2c.ClientCapabilitySyncPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PacketDecodeBoundsTest {

    @Test
    void testNegativeCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(-5);  // negative capCount

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.decode(buf));
        assertNotNull(packet);
        assertTrue(packet.capabilityData.isEmpty());
    }

    @Test
    void testExcessiveCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(99999); // excessive capCount

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.decode(buf));
        assertNotNull(packet);
        assertTrue(packet.capabilityData.isEmpty());
    }
}

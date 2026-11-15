package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.network.s2c.ClientCapabilitySyncPacket;
import com.sekwah.sekclib.network.s2c.ClientVelocityPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PacketDecodeBoundsTest {

    @Test
    void testNegativeCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(-5);  // negative capCount

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(buf));
        assertNotNull(packet);
        assertEquals(123, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
    }

    @Test
    void testExcessiveCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(99999); // excessive capCount

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(buf));
        assertNotNull(packet);
        assertEquals(123, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
    }

    @Test
    void testVelocityPacketStreamCodecRoundTrip() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClientVelocityPacket original = new ClientVelocityPacket(100, 250, -400);

        ClientVelocityPacket.STREAM_CODEC.encode(buf, original);
        ClientVelocityPacket decoded = ClientVelocityPacket.STREAM_CODEC.decode(buf);

        assertEquals(100, decoded.getXa());
        assertEquals(250, decoded.getYa());
        assertEquals(-400, decoded.getZa());
    }

    @Test
    void testCapabilityEntryWithAttachmentType() {
        AttachmentType<Object> attachment = AttachmentType.builder(Object::new).build();
        Identifier id = Identifier.fromNamespaceAndPath("sekclib", "test_cap");
        CapabilityEntry entry = new CapabilityEntry(id, () -> attachment, Object.class);

        assertEquals(id, entry.getResourceSyncName());
        assertSame(attachment, entry.getAttachmentType());
        assertEquals(Object.class, entry.getCapabilityClass());
    }
}

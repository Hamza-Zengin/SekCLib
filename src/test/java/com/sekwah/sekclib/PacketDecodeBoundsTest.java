package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.tracker.ISyncTrackerData;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.IdentifierSetSyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.IntSyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.StringSetSyncTracker;
import com.sekwah.sekclib.network.s2c.ClientCapabilitySyncPacket;
import com.sekwah.sekclib.network.s2c.ClientVelocityPacket;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PacketDecodeBoundsTest {

    public static class DummyPacketCap {
        public int value = 0;
        public Set<Identifier> ids = new HashSet<>();
        public Set<String> tags = new HashSet<>();
    }

    private static CapabilityEntry registeredCapEntry;
    private static int registeredCapId = -1;

    private static synchronized int ensureRegisteredTestCapability() throws Exception {
        if (registeredCapId < 0) {
            Identifier id = Identifier.fromNamespaceAndPath("sekclib", "packet_decode_bounds_test_cap");
            CapabilityEntry entry = new CapabilityEntry(id, () -> null, DummyPacketCap.class);
            entry.addSyncEntry(new SyncEntry("value", DummyPacketCap.class.getField("value"), 0, 0, true, new IntSyncTracker()));
            entry.addSyncEntry(new SyncEntry("ids", DummyPacketCap.class.getField("ids"), 0, 1, true, new IdentifierSetSyncTracker()));
            entry.addSyncEntry(new SyncEntry("tags", DummyPacketCap.class.getField("tags"), 0, 2, true, new StringSetSyncTracker()));
            Registry.register(SekCLibRegistries.CAPABILITY_REGISTRY, id, entry);
            registeredCapEntry = entry;
            registeredCapId = SekCLibRegistries.CAPABILITY_REGISTRY.getId(entry);
        }
        return registeredCapId;
    }

    @Test
    void testNegativeCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(-5);  // negative capCount
        buf.writeInt(999); // trailing bytes
        buf.writeByte(0x7F);

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(buf));
        assertNotNull(packet);
        assertEquals(123, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void testExcessiveCapCountHandledSafely() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123); // entityId
        buf.writeInt(99999); // excessive capCount
        buf.writeInt(42);  // trailing bytes

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(buf));
        assertNotNull(packet);
        assertEquals(123, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void testUnregisteredCapIdDrainsTrailingBytes() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(123);    // entityId
        buf.writeInt(2);      // capCount
        buf.writeInt(999999); // unregistered capId
        buf.writeInt(1);      // trailing trackerCount
        buf.writeByte(0);     // trailing trackerId
        buf.writeBoolean(false);

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(() -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(buf));
        assertNotNull(packet);
        assertEquals(123, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
        assertEquals(0, buf.readableBytes());
    }

    @Test
    void testInvalidTrackerCountDrainsBufferDirectlyAndInMultiCapPacket() throws Exception {
        int capId = ensureRegisteredTestCapability();

        // Direct decodeSyncTrackers negative trackerCount
        FriendlyByteBuf negBuf = new FriendlyByteBuf(Unpooled.buffer());
        negBuf.writeInt(-4);
        negBuf.writeInt(12345);
        List<ISyncTrackerData> negTrackers = assertDoesNotThrow(
                () -> ClientCapabilitySyncPacket.decodeSyncTrackers(registeredCapEntry, negBuf));
        assertTrue(negTrackers.isEmpty());
        assertEquals(0, negBuf.readableBytes());

        // Direct decodeSyncTrackers excessive trackerCount
        FriendlyByteBuf highBuf = new FriendlyByteBuf(Unpooled.buffer());
        highBuf.writeInt(10000);
        highBuf.writeByte(0x55);
        List<ISyncTrackerData> highTrackers = assertDoesNotThrow(
                () -> ClientCapabilitySyncPacket.decodeSyncTrackers(registeredCapEntry, highBuf));
        assertTrue(highTrackers.isEmpty());
        assertEquals(0, highBuf.readableBytes());

        // Multi-capability packet: cap 0 has excessive trackerCount, cap 1 bytes follow
        FriendlyByteBuf multiBuf = new FriendlyByteBuf(Unpooled.buffer());
        multiBuf.writeInt(321);   // entityId
        multiBuf.writeInt(2);     // capCount
        multiBuf.writeInt(capId); // cap 0 id
        multiBuf.writeInt(5000);  // invalid trackerCount on cap 0
        multiBuf.writeInt(capId); // trailing cap 1 id
        multiBuf.writeInt(1);     // trailing cap 1 trackerCount
        multiBuf.writeByte(0);
        multiBuf.writeBoolean(true);
        multiBuf.writeInt(77);

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(
                () -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(multiBuf));
        assertNotNull(packet);
        assertEquals(321, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
        assertEquals(0, multiBuf.readableBytes());
    }

    @Test
    void testOutOfBoundsTrackerIdAndNullCapabilityDrainBuffer() throws Exception {
        int capId = ensureRegisteredTestCapability();

        // capability == null inside decodeSyncTrackers
        FriendlyByteBuf nullCapBuf = new FriendlyByteBuf(Unpooled.buffer());
        nullCapBuf.writeInt(1);      // trackerCount
        nullCapBuf.writeByte(0);     // trackerId
        nullCapBuf.writeBoolean(true);
        nullCapBuf.writeInt(999);    // trailing payload
        List<ISyncTrackerData> nullCapTrackers = assertDoesNotThrow(
                () -> ClientCapabilitySyncPacket.decodeSyncTrackers(null, nullCapBuf));
        assertTrue(nullCapTrackers.isEmpty());
        assertEquals(0, nullCapBuf.readableBytes());

        // trackerId >= capability.getSyncEntries().size() in multi-cap packet
        FriendlyByteBuf oobBuf = new FriendlyByteBuf(Unpooled.buffer());
        oobBuf.writeInt(456);   // entityId
        oobBuf.writeInt(2);     // capCount
        oobBuf.writeInt(capId); // cap 0 id
        oobBuf.writeInt(1);     // trackerCount
        oobBuf.writeByte(99);   // out-of-bounds trackerId
        oobBuf.writeBoolean(true);
        oobBuf.writeInt(123);   // trailing payload
        oobBuf.writeInt(capId); // trailing cap 1

        ClientCapabilitySyncPacket packet = assertDoesNotThrow(
                () -> ClientCapabilitySyncPacket.STREAM_CODEC.decode(oobBuf));
        assertNotNull(packet);
        assertEquals(456, packet.getEntityId());
        assertTrue(packet.capabilityData.isEmpty());
        assertEquals(0, oobBuf.readableBytes());
    }

    @Test
    void testSetSyncTrackerBoundsDrainTrailingBytesOnlyWhenInvalid() {
        IdentifierSetSyncTracker idTracker = new IdentifierSetSyncTracker();
        FriendlyByteBuf invalidIdBuf = new FriendlyByteBuf(Unpooled.buffer());
        invalidIdBuf.writeInt(-2);
        invalidIdBuf.writeInt(999);
        assertTrue(idTracker.decode(invalidIdBuf).isEmpty());
        assertEquals(0, invalidIdBuf.readableBytes());

        FriendlyByteBuf emptyIdBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyIdBuf.writeInt(0);
        emptyIdBuf.writeInt(42);
        assertTrue(idTracker.decode(emptyIdBuf).isEmpty());
        assertEquals(4, emptyIdBuf.readableBytes(), "Valid empty set must not consume subsequent tracker bytes");

        StringSetSyncTracker strTracker = new StringSetSyncTracker();
        FriendlyByteBuf excessiveStrBuf = new FriendlyByteBuf(Unpooled.buffer());
        excessiveStrBuf.writeInt(9999);
        excessiveStrBuf.writeUtf("trailing");
        assertTrue(strTracker.decode(excessiveStrBuf).isEmpty());
        assertEquals(0, excessiveStrBuf.readableBytes());

        FriendlyByteBuf emptyStrBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyStrBuf.writeInt(0);
        emptyStrBuf.writeInt(42);
        assertTrue(strTracker.decode(emptyStrBuf).isEmpty());
        assertEquals(4, emptyStrBuf.readableBytes(), "Valid empty set must not consume subsequent tracker bytes");
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

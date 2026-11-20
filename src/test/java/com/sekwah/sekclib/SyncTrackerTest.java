package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.*;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SyncTrackerTest {

    @Test
    void testIntSyncTracker() {
        IntSyncTracker tracker = new IntSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        tracker.encode(42, buf);
        tracker.encode(-9999, buf);

        assertEquals(42, tracker.decode(buf));
        assertEquals(-9999, tracker.decode(buf));
    }

    @Test
    void testFloatSyncTracker() {
        FloatSyncTracker tracker = new FloatSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        tracker.encode(3.14159f, buf);
        assertEquals(3.14159f, tracker.decode(buf), 0.0001f);

        // Verify epsilon delta sync (MIN_SYNC_DIFF = 0.1f)
        assertEquals(0.1f, FloatSyncTracker.MIN_SYNC_DIFF, 0.0001f);

        // Initial sync when sendValue is null
        assertTrue(tracker.shouldSend(10.0f, null));

        // Exact equal values should not be sent
        assertFalse(tracker.shouldSend(10.0f, 10.0f));

        // Changes below MIN_SYNC_DIFF threshold (diff = 0.05f < 0.1f) should NOT trigger sync
        assertFalse(tracker.shouldSend(10.05f, 10.0f));
        assertFalse(tracker.shouldSend(9.95f, 10.0f));

        // Changes at or exceeding MIN_SYNC_DIFF threshold should trigger sync
        assertTrue(tracker.shouldSend(10.1f, 10.0f));
        assertTrue(tracker.shouldSend(10.25f, 10.0f));
        assertTrue(tracker.shouldSend(9.85f, 10.0f));

        // Critical min boundary condition (e.g. chakra / stamina depleted to 0.0f):
        // Even if diff is small (0.05f < 0.1f), reaching 0.0f boundary MUST trigger sync
        assertTrue(tracker.shouldSend(0.0f, 0.05f));
        assertFalse(tracker.shouldSend(0.0f, 0.0f));

        // Custom bounds (min = 0.0f, max = 100.0f)
        FloatSyncTracker boundedTracker = new FloatSyncTracker(0.0f, 100.0f);
        assertTrue(boundedTracker.shouldSend(100.0f, 99.95f)); // Reaching max boundary
        assertFalse(boundedTracker.shouldSend(50.05f, 50.0f));  // Mid-range change below threshold
    }

    @Test
    void testBoolSyncTracker() {
        BoolSyncTracker tracker = new BoolSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        tracker.encode(true, buf);
        tracker.encode(false, buf);

        assertTrue(tracker.decode(buf));
        assertFalse(tracker.decode(buf));
    }

    @Test
    void testByteSyncTracker() {
        ByteSyncTracker tracker = new ByteSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        tracker.encode((byte) 0x7F, buf);
        assertEquals((byte) 0x7F, tracker.decode(buf));
    }

    @Test
    void testStringSyncTracker() {
        StringSyncTracker tracker = new StringSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        String testStr = "SekCLib Network Sync 2.0.0";
        tracker.encode(testStr, buf);
        assertEquals(testStr, tracker.decode(buf));
    }

    @Test
    void testVec3SyncTracker() {
        Vec3SyncTracker tracker = new Vec3SyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        Vec3 vec = new Vec3(12.5, -4.2, 100.8);
        tracker.encode(vec, buf);
        Vec3 decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals(12.5, decoded.x, 0.0001);
        assertEquals(-4.2, decoded.y, 0.0001);
        assertEquals(100.8, decoded.z, 0.0001);
    }

    @Test
    void testIdentifierSyncTracker() {
        IdentifierSyncTracker tracker = new IdentifierSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        Identifier loc = Identifier.fromNamespaceAndPath("narutomod", "textures/gui/jutsu.png");
        tracker.encode(loc, buf);
        Identifier decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals(loc, decoded);
    }

    @Test
    void testCompoundTagSyncTracker() {
        CompoundTagSyncTracker tracker = new CompoundTagSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        CompoundTag tag = new CompoundTag();
        tag.putString("clan", "uchiha");
        tag.putInt("chakraExp", 1500);
        tag.putBoolean("awakened", true);

        CompoundTag subTag = new CompoundTag();
        subTag.putDouble("jutsuDamageMultiplier", 1.5);
        tag.put("stats", subTag);

        tracker.encode(tag, buf);
        CompoundTag decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals("uchiha", decoded.getStringOr("clan", ""));
        assertEquals(1500, decoded.getIntOr("chakraExp", 0));
        assertTrue(decoded.getBooleanOr("awakened", false));
        assertEquals(1.5, decoded.getCompoundOrEmpty("stats").getDoubleOr("jutsuDamageMultiplier", 0.0));

        // Test clone
        CompoundTag cloned = tracker.clone(tag);
        assertNotNull(cloned);
        assertNotSame(tag, cloned);
        assertEquals(tag, cloned);

        // Test null clone
        assertNull(tracker.clone(null));
    }

    @Test
    void testIdentifierSetSyncTracker() {
        IdentifierSetSyncTracker tracker = new IdentifierSetSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        Set<Identifier> set = new HashSet<>();
        set.add(Identifier.fromNamespaceAndPath("narutomod", "rasengan"));
        set.add(Identifier.fromNamespaceAndPath("narutomod", "chidori"));
        set.add(Identifier.fromNamespaceAndPath("narutomod", "shadow_clone"));

        tracker.encode(set, buf);
        Set<Identifier> decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals(3, decoded.size());
        assertTrue(decoded.contains(Identifier.fromNamespaceAndPath("narutomod", "rasengan")));
        assertTrue(decoded.contains(Identifier.fromNamespaceAndPath("narutomod", "chidori")));
        assertTrue(decoded.contains(Identifier.fromNamespaceAndPath("narutomod", "shadow_clone")));

        // Test clone
        Set<Identifier> cloned = tracker.clone(set);
        assertNotNull(cloned);
        assertNotSame(set, cloned);
        assertEquals(set, cloned);

        // Test decode bounds check with invalid size
        FriendlyByteBuf invalidBuf = new FriendlyByteBuf(Unpooled.buffer());
        invalidBuf.writeInt(-5);
        assertTrue(tracker.decode(invalidBuf).isEmpty());

        FriendlyByteBuf excessiveBuf = new FriendlyByteBuf(Unpooled.buffer());
        excessiveBuf.writeInt(9999);
        assertTrue(tracker.decode(excessiveBuf).isEmpty());
    }

    @Test
    void testStringSetSyncTracker() {
        StringSetSyncTracker tracker = new StringSetSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        Set<String> set = new HashSet<>();
        set.add("fire_style");
        set.add("lightning_blade");
        set.add("flying_raijin");

        tracker.encode(set, buf);
        Set<String> decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals(3, decoded.size());
        assertTrue(decoded.contains("fire_style"));
        assertTrue(decoded.contains("lightning_blade"));
        assertTrue(decoded.contains("flying_raijin"));

        // Test clone
        Set<String> cloned = tracker.clone(set);
        assertNotNull(cloned);
        assertNotSame(set, cloned);
        assertEquals(set, cloned);

        // Test decode bounds check
        FriendlyByteBuf invalidBuf = new FriendlyByteBuf(Unpooled.buffer());
        invalidBuf.writeInt(-1);
        assertTrue(tracker.decode(invalidBuf).isEmpty());

        FriendlyByteBuf excessiveBuf = new FriendlyByteBuf(Unpooled.buffer());
        excessiveBuf.writeInt(1000);
        assertTrue(tracker.decode(excessiveBuf).isEmpty());
    }

    @Test
    void testSyncTrackerDataLiveValue() {
        com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerData liveTrackerData =
                new com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerData(null, "active_live_data");
        assertNotNull(liveTrackerData.getSendValue());
        assertEquals("active_live_data", liveTrackerData.getSendValue());
    }

    public static class SyncEntryTestTarget {
        public int intVal = 10;
        public float floatVal = 1.5f;
        public boolean boolVal = false;
        public byte byteVal = 7;
        public double doubleVal = 2.75;
        public long longVal = 123456789L;
        public String stringVal = "initial";
        public Identifier idVal = Identifier.fromNamespaceAndPath("sekclib", "initial");
        public Vec3 vecVal = new Vec3(1.0, 2.0, 3.0);
        public Set<String> stringSet = new HashSet<>(Set.of("alpha"));
        public Set<Identifier> idSet = new HashSet<>(Set.of(Identifier.fromNamespaceAndPath("sekclib", "one")));
        private int privateIntVal = 42;
    }

    @Test
    void testSyncEntryGetAndSetValuePrimitivesObjectsAndCollections() throws Throwable {
        SyncEntryTestTarget target = new SyncEntryTestTarget();

        SyncEntry intEntry = new SyncEntry("intVal", SyncEntryTestTarget.class.getField("intVal"), 0, 0, true, new IntSyncTracker());
        assertEquals(10, intEntry.getValue(target));
        intEntry.setValue(target, 99);
        assertEquals(99, target.intVal);
        assertEquals(99, intEntry.getValue(target));
        assertFalse(intEntry.isCloneableSerializer());

        SyncEntry floatEntry = new SyncEntry("floatVal", SyncEntryTestTarget.class.getField("floatVal"), 1, 1, false, new FloatSyncTracker());
        assertEquals(1.5f, (Float) floatEntry.getValue(target), 0.0001f);
        floatEntry.setValue(target, 42.25f);
        assertEquals(42.25f, target.floatVal, 0.0001f);
        assertEquals(42.25f, (Float) floatEntry.getValue(target), 0.0001f);

        SyncEntry boolEntry = new SyncEntry("boolVal", SyncEntryTestTarget.class.getField("boolVal"), 0, 2, true, new BoolSyncTracker());
        assertEquals(false, boolEntry.getValue(target));
        boolEntry.setValue(target, true);
        assertTrue(target.boolVal);
        assertEquals(true, boolEntry.getValue(target));

        SyncEntry byteEntry = new SyncEntry("byteVal", SyncEntryTestTarget.class.getField("byteVal"), 0, 3, true, new ByteSyncTracker());
        assertEquals((byte) 7, byteEntry.getValue(target));
        byteEntry.setValue(target, (byte) -5);
        assertEquals((byte) -5, target.byteVal);

        SyncEntry doubleEntry = new SyncEntry("doubleVal", SyncEntryTestTarget.class.getField("doubleVal"), 0, 4, true, null);
        assertEquals(2.75, (Double) doubleEntry.getValue(target), 0.0001);
        doubleEntry.setValue(target, -12.5);
        assertEquals(-12.5, target.doubleVal, 0.0001);
        assertFalse(doubleEntry.isCloneableSerializer());

        SyncEntry longEntry = new SyncEntry("longVal", SyncEntryTestTarget.class.getField("longVal"), 0, 5, true, null);
        assertEquals(123456789L, longEntry.getValue(target));
        longEntry.setValue(target, 987654321L);
        assertEquals(987654321L, target.longVal);

        SyncEntry stringEntry = new SyncEntry("stringVal", SyncEntryTestTarget.class.getField("stringVal"), 0, 6, true, new StringSyncTracker());
        assertEquals("initial", stringEntry.getValue(target));
        stringEntry.setValue(target, "updated");
        assertEquals("updated", target.stringVal);
        stringEntry.setValue(target, null);
        assertNull(target.stringVal);
        assertNull(stringEntry.getValue(target));
        assertFalse(stringEntry.isCloneableSerializer());

        Identifier updatedId = Identifier.fromNamespaceAndPath("sekclib", "updated");
        SyncEntry idEntry = new SyncEntry("idVal", SyncEntryTestTarget.class.getField("idVal"), 0, 7, true, new IdentifierSyncTracker());
        assertEquals(Identifier.fromNamespaceAndPath("sekclib", "initial"), idEntry.getValue(target));
        idEntry.setValue(target, updatedId);
        assertEquals(updatedId, target.idVal);

        Vec3 updatedVec = new Vec3(-3.0, 6.0, 9.0);
        SyncEntry vecEntry = new SyncEntry("vecVal", SyncEntryTestTarget.class.getField("vecVal"), 0, 8, true, new Vec3SyncTracker());
        assertEquals(new Vec3(1.0, 2.0, 3.0), vecEntry.getValue(target));
        vecEntry.setValue(target, updatedVec);
        assertEquals(updatedVec, target.vecVal);

        Set<String> newStringSet = new HashSet<>(Set.of("beta", "gamma"));
        SyncEntry stringSetEntry = new SyncEntry("stringSet", SyncEntryTestTarget.class.getField("stringSet"), 0, 9, false, new StringSetSyncTracker());
        assertEquals(Set.of("alpha"), stringSetEntry.getValue(target));
        stringSetEntry.setValue(target, newStringSet);
        assertEquals(Set.of("beta", "gamma"), target.stringSet);
        assertTrue(stringSetEntry.isCloneableSerializer());

        Set<Identifier> newIdSet = new HashSet<>(Set.of(updatedId));
        SyncEntry idSetEntry = new SyncEntry("idSet", SyncEntryTestTarget.class.getField("idSet"), 0, 10, false, new IdentifierSetSyncTracker());
        assertEquals(Set.of(Identifier.fromNamespaceAndPath("sekclib", "one")), idSetEntry.getValue(target));
        idSetEntry.setValue(target, newIdSet);
        assertEquals(Set.of(updatedId), target.idSet);
        assertTrue(idSetEntry.isCloneableSerializer());

        Field privateField = SyncEntryTestTarget.class.getDeclaredField("privateIntVal");
        privateField.setAccessible(true);
        SyncEntry privateEntry = new SyncEntry("privateIntVal", privateField, 0, 11, false, new IntSyncTracker());
        assertEquals(42, privateEntry.getValue(target));
        privateEntry.setValue(target, 77);
        assertEquals(77, privateEntry.getValue(target));

        // Null field safety
        SyncEntry nullFieldEntry = new SyncEntry("missing", null, 0, 12, false, new CompoundTagSyncTracker());
        assertNull(nullFieldEntry.getValue(target));
        assertDoesNotThrow(() -> nullFieldEntry.setValue(target, "ignored"));
        assertTrue(nullFieldEntry.isCloneableSerializer());
    }

    @Test
    void testSyncTrackerMinTicksLeftClampedToZeroWhenIdle() throws Throwable {
        SyncEntryTestTarget target = new SyncEntryTestTarget();
        SyncEntry entry = new SyncEntry("intVal", SyncEntryTestTarget.class.getField("intVal"), 2, 0, true, new IntSyncTracker());
        SyncTracker tracker = new SyncTracker(entry);

        tracker.tick(target);
        assertTrue(tracker.isMarkedForSend());
        assertEquals(2, tracker.getMinTicksLeft());

        // Countdown 2 -> 1 -> 0, then stays clamped at 0 on idle ticks
        tracker.tick(target);
        assertFalse(tracker.isMarkedForSend());
        assertEquals(1, tracker.getMinTicksLeft());

        tracker.tick(target);
        assertFalse(tracker.isMarkedForSend());
        assertEquals(0, tracker.getMinTicksLeft());

        for (int i = 0; i < 25; i++) {
            tracker.tick(target);
            assertFalse(tracker.isMarkedForSend());
            assertEquals(0, tracker.getMinTicksLeft(), "minTicksLeft must not decrement below 0 when idle");
        }
    }
}

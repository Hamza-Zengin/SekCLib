package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented.*;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

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

        String testStr = "SekCLib Network Sync 1.1.0";
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
    void testResourceLocationSyncTracker() {
        ResourceLocationSyncTracker tracker = new ResourceLocationSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        ResourceLocation loc = new ResourceLocation("narutomod", "textures/gui/jutsu.png");
        tracker.encode(loc, buf);
        ResourceLocation decoded = tracker.decode(buf);

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
        assertEquals("uchiha", decoded.getString("clan"));
        assertEquals(1500, decoded.getInt("chakraExp"));
        assertTrue(decoded.getBoolean("awakened"));
        assertEquals(1.5, decoded.getCompound("stats").getDouble("jutsuDamageMultiplier"));

        // Test clone
        CompoundTag cloned = tracker.clone(tag);
        assertNotNull(cloned);
        assertNotSame(tag, cloned);
        assertEquals(tag, cloned);

        // Test null clone
        assertNull(tracker.clone(null));
    }

    @Test
    void testResourceLocationSetSyncTracker() {
        ResourceLocationSetSyncTracker tracker = new ResourceLocationSetSyncTracker();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        Set<ResourceLocation> set = new HashSet<>();
        set.add(new ResourceLocation("narutomod", "rasengan"));
        set.add(new ResourceLocation("narutomod", "chidori"));
        set.add(new ResourceLocation("narutomod", "shadow_clone"));

        tracker.encode(set, buf);
        Set<ResourceLocation> decoded = tracker.decode(buf);

        assertNotNull(decoded);
        assertEquals(3, decoded.size());
        assertTrue(decoded.contains(new ResourceLocation("narutomod", "rasengan")));
        assertTrue(decoded.contains(new ResourceLocation("narutomod", "chidori")));
        assertTrue(decoded.contains(new ResourceLocation("narutomod", "shadow_clone")));

        // Test clone
        Set<ResourceLocation> cloned = tracker.clone(set);
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
        com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerData liveTrackerData =
                new com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerData(null, "active_live_data");
        assertNotNull(liveTrackerData.getSendValue());
        assertEquals("active_live_data", liveTrackerData.getSendValue());
    }
}

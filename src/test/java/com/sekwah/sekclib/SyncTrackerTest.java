package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

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
}

package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.broadcaster.CapabilityInfo;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTracker;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented.IntSyncTracker;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented.StringSetSyncTracker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the core tracking, minTicks throttling, cloning, and global/private
 * partitioning logic used by {@link com.sekwah.sekclib.capabilitysync.capabilitysync.broadcaster.CapabilityBroadcaster}.
 */
public class CapabilityBroadcasterTest {

    public static class DummyCapability {
        public int globalCounter = 0;
        public int throttledValue = 10;
        public Set<String> privateTags = new HashSet<>();
    }

    @Test
    void testMinTicksThrottlingAndMarking() throws Throwable {
        Field field = DummyCapability.class.getField("throttledValue");
        SyncEntry entry = new SyncEntry("throttledValue", field, 3, 0, true, new IntSyncTracker());
        SyncTracker tracker = new SyncTracker(entry);
        DummyCapability cap = new DummyCapability();

        // First tick: minTicksLeft starts at 0 -> --minTicksLeft <= 0 -> sends initial value and resets minTicksLeft to 3
        tracker.tick(cap);
        assertTrue(tracker.isMarkedForSend());
        assertEquals(10, tracker.getSendValue());

        // Change value immediately; next 2 ticks must be throttled because minTicks = 3 (3 -> 2 -> 1 > 0)
        cap.throttledValue = 25;
        tracker.tick(cap);
        assertFalse(tracker.isMarkedForSend(), "Tick 1 after send should be throttled");
        assertEquals(10, tracker.getSendValue());

        tracker.tick(cap);
        assertFalse(tracker.isMarkedForSend(), "Tick 2 after send should be throttled");
        assertEquals(10, tracker.getSendValue());

        // 3rd tick after send: --minTicksLeft reaches 0 -> should send new value
        tracker.tick(cap);
        assertTrue(tracker.isMarkedForSend(), "Tick 3 after send should flush throttled update");
        assertEquals(25, tracker.getSendValue());

        // Unchanged value on next eligible ticks should not mark for send
        for (int i = 0; i < 4; i++) {
            tracker.tick(cap);
            assertFalse(tracker.isMarkedForSend());
        }
    }

    @Test
    void testMarkSentValueResetsPendingAndClonesMutableSets() throws Throwable {
        Field field = DummyCapability.class.getField("privateTags");
        SyncEntry entry = new SyncEntry("privateTags", field, 5, 1, false, new StringSetSyncTracker());
        SyncTracker tracker = new SyncTracker(entry);
        DummyCapability cap = new DummyCapability();
        cap.privateTags.add("alpha");

        tracker.markSentValue(cap.privateTags);
        assertFalse(tracker.isMarkedForSend());

        // Mutating the source set must not mutate the cloned sendValue snapshot
        cap.privateTags.add("beta");
        assertEquals(Set.of("alpha"), tracker.getSendValue());
    }

    @Test
    void testGlobalVsPrivateEntryPartitioning() throws Throwable {
        Field globalField = DummyCapability.class.getField("globalCounter");
        Field privateField = DummyCapability.class.getField("privateTags");

        SyncEntry globalEntry = new SyncEntry("globalCounter", globalField, 0, 0, true, new IntSyncTracker());
        SyncEntry privateEntry = new SyncEntry("privateTags", privateField, 0, 1, false, new StringSetSyncTracker());

        SyncTracker globalTracker = new SyncTracker(globalEntry);
        SyncTracker privateTracker = new SyncTracker(privateEntry);

        DummyCapability cap = new DummyCapability();
        cap.globalCounter = 42;
        cap.privateTags.add("secret");

        globalTracker.tick(cap);
        privateTracker.tick(cap);

        CapabilityInfo info = new CapabilityInfo(1, null);
        for (SyncTracker t : new SyncTracker[]{globalTracker, privateTracker}) {
            if (t.isMarkedForSend()) {
                if (t.getSyncEntry().isSyncGlobally()) {
                    info.changedEntries.add(t);
                } else {
                    info.changedPrivateEntries.add(t);
                }
            }
        }

        assertEquals(1, info.changedEntries.size());
        assertEquals(42, info.changedEntries.get(0).getSendValue());
        assertEquals(1, info.changedPrivateEntries.size());
        assertEquals(Set.of("secret"), info.changedPrivateEntries.get(0).getSendValue());
    }
}

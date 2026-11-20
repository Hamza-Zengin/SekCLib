package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityBroadcaster;
import com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityInfo;
import com.sekwah.sekclib.capabilitysync.capability.SyncData;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.IntSyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.implemented.StringSetSyncTracker;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the core tracking, minTicks throttling, cloning, and global/private
 * partitioning logic used by {@link com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityBroadcaster}.
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

        // Unchanged value on next eligible ticks should not mark for send and should clamp minTicksLeft to 0
        for (int i = 0; i < 4; i++) {
            tracker.tick(cap);
            assertFalse(tracker.isMarkedForSend());
            assertTrue(tracker.getMinTicksLeft() >= 0, "minTicksLeft must never go negative");
        }
        assertEquals(0, tracker.getMinTicksLeft(), "minTicksLeft should be clamped to 0 when idle");
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

    @Test
    void testMinTicksLeftDoesNotGoNegativeWhenIdle() throws Throwable {
        Field field = DummyCapability.class.getField("globalCounter");
        SyncEntry entry = new SyncEntry("globalCounter", field, 0, 0, true, new IntSyncTracker());
        SyncTracker tracker = new SyncTracker(entry);
        DummyCapability cap = new DummyCapability();

        // Initial tick sends baseline value
        tracker.tick(cap);
        assertTrue(tracker.isMarkedForSend());
        assertEquals(0, tracker.getMinTicksLeft());

        // Many idle ticks must never drive minTicksLeft negative
        for (int i = 0; i < 50; i++) {
            tracker.tick(cap);
            assertFalse(tracker.isMarkedForSend());
            assertEquals(0, tracker.getMinTicksLeft(), "Idle tick " + i + " should keep minTicksLeft clamped at 0");
        }

        // Next value change should still trigger immediately
        cap.globalCounter = 99;
        tracker.tick(cap);
        assertTrue(tracker.isMarkedForSend());
        assertEquals(99, tracker.getSendValue());
    }

    private static IAttachmentHolder createInMemoryHolder() {
        Map<AttachmentType<?>, Object> attachments = new IdentityHashMap<>();
        return (IAttachmentHolder) Proxy.newProxyInstance(
                IAttachmentHolder.class.getClassLoader(),
                new Class<?>[]{IAttachmentHolder.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("hasData".equals(name) && args != null && args.length == 1) {
                        return attachments.containsKey(args[0]);
                    }
                    if ("getData".equals(name) && args != null && args.length == 1) {
                        return attachments.get(args[0]);
                    }
                    if ("setData".equals(name) && args != null && args.length == 2) {
                        return attachments.put((AttachmentType<?>) args[0], args[1]);
                    }
                    return null;
                }
        );
    }

    @Test
    void testBroadcasterSkipsSyncDataAllocationWithoutSyncedCapability() {
        assertFalse(CapabilityBroadcaster.hasAnySyncedCapability(null));
        assertDoesNotThrow(() -> CapabilityBroadcaster.checkCapData(null));
        assertDoesNotThrow(() -> CapabilityBroadcaster.broadcastCapChanges(null, false));
        assertTrue(CapabilityBroadcaster.getSpectators(null).isEmpty());

        AttachmentType<SyncData> syncDataType = AttachmentType.builder(SyncData::new).build();
        AttachmentType<DummyCapability> capType = AttachmentType.builder(DummyCapability::new).build();
        CapabilityEntry capEntry = new CapabilityEntry(
                Identifier.fromNamespaceAndPath("sekclib", "dummy_cap"),
                () -> capType,
                DummyCapability.class
        );
        List<CapabilityEntry> capabilities = List.of(capEntry);

        // Entity without any synced capability or SyncData must not get SyncData allocated
        IAttachmentHolder vanillaEntityHolder = createInMemoryHolder();
        assertFalse(CapabilityBroadcaster.hasAnySyncedCapability(vanillaEntityHolder, capabilities));
        assertFalse(CapabilityBroadcaster.shouldSyncEntity(vanillaEntityHolder, syncDataType, capabilities));
        assertFalse(vanillaEntityHolder.hasData(syncDataType), "Vanilla entity without synced caps must not get SyncData");

        // Entity that has a registered synced capability must be eligible for syncing
        vanillaEntityHolder.setData(capType, new DummyCapability());
        assertTrue(CapabilityBroadcaster.hasAnySyncedCapability(vanillaEntityHolder, capabilities));
        assertTrue(CapabilityBroadcaster.shouldSyncEntity(vanillaEntityHolder, syncDataType, capabilities));
        assertFalse(vanillaEntityHolder.hasData(syncDataType), "Capability detection itself must not allocate SyncData");

        // Entity that already holds SyncData remains eligible
        IAttachmentHolder existingSyncHolder = createInMemoryHolder();
        existingSyncHolder.setData(syncDataType, new SyncData());
        assertFalse(CapabilityBroadcaster.hasAnySyncedCapability(existingSyncHolder, capabilities));
        assertTrue(CapabilityBroadcaster.shouldSyncEntity(existingSyncHolder, syncDataType, capabilities));
    }

    @Test
    void testCapabilityEntryCachesResolvedAttachmentType() {
        AttachmentType<DummyCapability> capType = AttachmentType.builder(DummyCapability::new).build();
        AtomicInteger supplierCalls = new AtomicInteger();
        CapabilityEntry entry = new CapabilityEntry(
                Identifier.fromNamespaceAndPath("sekclib", "cached_cap"),
                () -> {
                    supplierCalls.incrementAndGet();
                    return capType;
                },
                DummyCapability.class
        );

        assertSame(capType, entry.getAttachmentType());
        assertSame(capType, entry.getAttachmentType());
        assertSame(capType, entry.getAttachmentType());
        assertEquals(1, supplierCalls.get(), "AttachmentType supplier should only be resolved once");
    }

    @Test
    void testSyncDataEnsureTrackersFastPathAndNonAllocation() throws Throwable {
        AtomicInteger hasDataCalls = new AtomicInteger();
        Map<AttachmentType<?>, Object> attachments = new IdentityHashMap<>();
        IAttachmentHolder countingHolder = (IAttachmentHolder) Proxy.newProxyInstance(
                IAttachmentHolder.class.getClassLoader(),
                new Class<?>[]{IAttachmentHolder.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("hasData".equals(name) && args != null && args.length == 1) {
                        hasDataCalls.incrementAndGet();
                        return attachments.containsKey(args[0]);
                    }
                    if ("getData".equals(name) && args != null && args.length == 1) {
                        return attachments.get(args[0]);
                    }
                    if ("setData".equals(name) && args != null && args.length == 2) {
                        return attachments.put((AttachmentType<?>) args[0], args[1]);
                    }
                    return null;
                }
        );

        AttachmentType<DummyCapability> capType = AttachmentType.builder(DummyCapability::new).build();
        CapabilityEntry capEntry = new CapabilityEntry(
                Identifier.fromNamespaceAndPath("sekclib", "fast_path_cap"),
                () -> capType,
                DummyCapability.class
        );
        capEntry.addSyncEntry(new SyncEntry("globalCounter", DummyCapability.class.getField("globalCounter"), 0, 0, true, new IntSyncTracker()));
        capEntry.addSyncEntry(new SyncEntry("privateTags", DummyCapability.class.getField("privateTags"), 0, 1, false, new StringSetSyncTracker()));
        List<CapabilityEntry> capabilities = List.of(capEntry);

        SyncData syncData = new SyncData();
        // Empty capabilities list returns before touching holder
        syncData.ensureTrackers(countingHolder, List.of());
        assertEquals(0, hasDataCalls.get());
        assertTrue(syncData.getCapabilityTrackers().isEmpty());

        // Attach capability and ensure trackers are populated once
        countingHolder.setData(capType, new DummyCapability());
        syncData.ensureTrackers(countingHolder, capabilities);
        assertEquals(1, hasDataCalls.get());
        assertEquals(1, syncData.getCapabilityTrackers().size());
        assertEquals(2, syncData.getCapabilityTrackers().get(0).getSyncTrackers().size());

        // Steady-state calls must hit the early-return fast-path without looping or calling hasData
        hasDataCalls.set(0);
        for (int i = 0; i < 100; i++) {
            syncData.ensureTrackers(countingHolder, capabilities);
        }
        assertEquals(0, hasDataCalls.get(), "ensureTrackers fast-path should skip iterating capabilities once all are tracked");
        assertEquals(1, syncData.getCapabilityTrackers().size());
    }
}

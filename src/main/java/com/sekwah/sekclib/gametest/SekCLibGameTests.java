package com.sekwah.sekclib.gametest;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.RegisterCapabilitySyncEvent;
import com.sekwah.sekclib.capabilitysync.annotation.Sync;
import com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityBroadcaster;
import com.sekwah.sekclib.capabilitysync.capability.SyncData;
import com.sekwah.sekclib.capabilitysync.capability.SyncDataCapabilityHandler;
import com.sekwah.sekclib.capabilitysync.tracker.CapabilityTracker;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTracker;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NeoForge GameTests for SekCLib capability sync and spectator tracking.
 */
public class SekCLibGameTests {

    public static class TestSyncCap {
        @Sync(minTicks = 1)
        public int privateChakra = 100;

        @Sync(minTicks = 1, syncGlobally = true)
        public int globalFlag = 0;

        @Sync(minTicks = 3)
        public int throttledValue = 0;
    }

    public static final DeferredRegister<AttachmentType<?>> TEST_ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SekCLib.MOD_ID);

    public static final Supplier<AttachmentType<TestSyncCap>> TEST_CAP =
            TEST_ATTACHMENTS.register("test_sync_cap", () -> AttachmentType.builder(TestSyncCap::new).build());

    private static final Identifier EMPTY_STRUCTURE = Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "empty");

    private static final ResourceKey<Consumer<GameTestHelper>> FN_SPECTATOR_LOOKUP =
            ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "spectator_lookup"));
    private static final ResourceKey<Consumer<GameTestHelper>> FN_TRACKER_SYNC =
            ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "tracker_sync"));
    private static final ResourceKey<Consumer<GameTestHelper>> FN_MIN_TICKS_THROTTLE =
            ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "min_ticks_throttle"));

    public static boolean isGameTestEnabled() {
        return Boolean.getBoolean("neoforge.enableGameTest")
                || !System.getProperty("neoforge.enabledGameTestNamespaces", "").isEmpty();
    }

    public static void register(IEventBus modEventBus) {
        if (!isGameTestEnabled()) {
            return;
        }
        TEST_ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(SekCLibGameTests::registerCapabilitySync);
        modEventBus.addListener(SekCLibGameTests::registerTestFunctions);
        modEventBus.addListener(SekCLibGameTests::registerGameTests);
    }

    private static void registerCapabilitySync(RegisterCapabilitySyncEvent event) {
        event.registerPlayerCap(
                Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "test_sync_cap"),
                TEST_CAP,
                TestSyncCap.class
        );
    }

    private static void registerTestFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> {
            helper.register(FN_SPECTATOR_LOOKUP, SekCLibGameTests::testSpectatorLookup);
            helper.register(FN_TRACKER_SYNC, SekCLibGameTests::testTrackerSync);
            helper.register(FN_MIN_TICKS_THROTTLE, SekCLibGameTests::testMinTicksThrottle);
        });
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "default"),
                new TestEnvironmentDefinition.AllOf()
        );
        event.registerTest(
                Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "spectator_lookup"),
                new FunctionGameTestInstance(FN_SPECTATOR_LOOKUP, new TestData<>(env, EMPTY_STRUCTURE, 40, 0, true))
        );
        event.registerTest(
                Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "tracker_sync"),
                new FunctionGameTestInstance(FN_TRACKER_SYNC, new TestData<>(env, EMPTY_STRUCTURE, 40, 0, true))
        );
        event.registerTest(
                Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "min_ticks_throttle"),
                new FunctionGameTestInstance(FN_MIN_TICKS_THROTTLE, new TestData<>(env, EMPTY_STRUCTURE, 40, 0, true))
        );
    }

    private static void testSpectatorLookup(GameTestHelper helper) {
        ServerPlayer target = helper.makeMockServerPlayerInLevel();
        ServerPlayer spectator = helper.makeMockServerPlayerInLevel();
        ServerPlayer survivalOther = helper.makeMockServerPlayerInLevel();

        target.setGameMode(GameType.SURVIVAL);
        survivalOther.setGameMode(GameType.SURVIVAL);
        spectator.setGameMode(GameType.SPECTATOR);

        helper.assertTrue(CapabilityBroadcaster.getSpectators(target).isEmpty(),
                "No spectators should be returned before camera is attached");

        spectator.setCamera(target);
        List<ServerPlayer> spectators = CapabilityBroadcaster.getSpectators(target);
        helper.assertValueEqual(1, spectators.size(), "Expected exactly 1 spectator viewing target");
        helper.assertTrue(spectators.get(0) == spectator, "Expected spectator to match camera viewer");
        helper.assertFalse(spectators.contains(survivalOther), "Survival player must not be listed as spectator");

        // Detach camera back to self
        spectator.setCamera(spectator);
        helper.assertTrue(CapabilityBroadcaster.getSpectators(target).isEmpty(),
                "Spectator should be removed once camera detaches");

        helper.succeed();
    }

    private static void testTrackerSync(GameTestHelper helper) {
        ServerPlayer target = helper.makeMockServerPlayerInLevel();
        TestSyncCap cap = target.getData(TEST_CAP.get());
        SyncData syncData = target.getData(SyncDataCapabilityHandler.SYNC_DATA.get());
        syncData.ensureTrackers(target);

        SyncTracker privateTracker = findTracker(syncData, "privateChakra");
        SyncTracker globalTracker = findTracker(syncData, "globalFlag");
        helper.assertTrue(privateTracker != null, "privateChakra tracker must be registered");
        helper.assertTrue(globalTracker != null, "globalFlag tracker must be registered");
        helper.assertFalse(privateTracker.getSyncEntry().isSyncGlobally(), "privateChakra must not be global");
        helper.assertTrue(globalTracker.getSyncEntry().isSyncGlobally(), "globalFlag must be global");

        cap.privateChakra = 42;
        cap.globalFlag = 7;
        privateTracker.tick(cap);
        globalTracker.tick(cap);

        helper.assertTrue(privateTracker.isMarkedForSend(), "Changed privateChakra must be marked for send");
        helper.assertTrue(globalTracker.isMarkedForSend(), "Changed globalFlag must be marked for send");
        helper.assertValueEqual(42, privateTracker.getSendValue(), "privateChakra sendValue mismatch");
        helper.assertValueEqual(7, globalTracker.getSendValue(), "globalFlag sendValue mismatch");

        helper.succeed();
    }

    private static void testMinTicksThrottle(GameTestHelper helper) {
        ServerPlayer target = helper.makeMockServerPlayerInLevel();
        TestSyncCap cap = target.getData(TEST_CAP.get());
        SyncData syncData = target.getData(SyncDataCapabilityHandler.SYNC_DATA.get());
        syncData.ensureTrackers(target);

        SyncTracker throttled = findTracker(syncData, "throttledValue");
        helper.assertTrue(throttled != null, "throttledValue tracker must be registered");

        // Initial tick synchronizes baseline
        cap.throttledValue = 1;
        throttled.tick(cap);
        throttled.markSentValue(1);

        // Next change within minTicks=3 window should not immediately mark for send
        cap.throttledValue = 2;
        throttled.tick(cap);
        boolean markedEarly = throttled.isMarkedForSend();
        helper.assertFalse(markedEarly, "throttledValue should not send before minTicks=3 elapses");

        throttled.tick(cap);
        throttled.tick(cap);
        helper.assertTrue(throttled.isMarkedForSend(), "throttledValue should mark for send after minTicks=3");

        helper.succeed();
    }

    private static SyncTracker findTracker(SyncData syncData, String fieldName) {
        for (CapabilityTracker ct : syncData.getCapabilityTrackers()) {
            CapabilityEntry entry = ct.getCapabilityEntry();
            if (entry != null && entry.getRegistryName().getPath().equals("test_sync_cap")) {
                for (SyncTracker st : ct.getSyncTrackers()) {
                    if (st.getSyncEntry().getVarName().equals(fieldName)) {
                        return st;
                    }
                }
            }
        }
        return null;
    }
}

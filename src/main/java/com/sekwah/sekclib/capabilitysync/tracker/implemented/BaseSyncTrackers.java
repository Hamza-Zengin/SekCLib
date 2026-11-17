package com.sekwah.sekclib.capabilitysync.tracker.implemented;

import com.google.gson.reflect.TypeToken;
import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.RegisterSyncTrackerTypeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = SekCLib.MOD_ID)
public class BaseSyncTrackers {
    @SubscribeEvent
    public static void registerSyncTrackerEvent(RegisterSyncTrackerTypeEvent event) {
        IntSyncTracker intTracker = new IntSyncTracker();
        event.registerSyncTracker(int.class, intTracker);
        event.registerSyncTracker(Integer.class, intTracker);

        FloatSyncTracker floatTracker = new FloatSyncTracker();
        event.registerSyncTracker(float.class, floatTracker);
        event.registerSyncTracker(Float.class, floatTracker);

        event.registerSyncTracker(Identifier.class, new IdentifierSyncTracker());
        event.registerSyncTracker(String.class, new StringSyncTracker());

        BoolSyncTracker boolTracker = new BoolSyncTracker();
        event.registerSyncTracker(boolean.class, boolTracker);
        event.registerSyncTracker(Boolean.class, boolTracker);

        ByteSyncTracker byteTracker = new ByteSyncTracker();
        event.registerSyncTracker(byte.class, byteTracker);
        event.registerSyncTracker(Byte.class, byteTracker);

        event.registerSyncTracker(Vec3.class, new Vec3SyncTracker());

        // CompoundTag sync tracker
        event.registerSyncTracker(CompoundTag.class, new CompoundTagSyncTracker());

        // Generic collection sync trackers
        IdentifierSetSyncTracker identifierSetTracker = new IdentifierSetSyncTracker();
        event.registerSyncTracker(new TypeToken<Set<Identifier>>(){}.getType(), identifierSetTracker);
        event.registerSyncTracker(new TypeToken<HashSet<Identifier>>(){}.getType(), identifierSetTracker);

        StringSetSyncTracker stringSetTracker = new StringSetSyncTracker();
        event.registerSyncTracker(new TypeToken<Set<String>>(){}.getType(), stringSetTracker);
        event.registerSyncTracker(new TypeToken<HashSet<String>>(){}.getType(), stringSetTracker);
    }
}

package com.sekwah.sekclib.capabilitysync;

import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerSerializer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.lang.reflect.Type;

public class RegisterSyncTrackerTypeEvent extends Event implements IModBusEvent {

    public void registerSyncTracker(Class<?> clazz, SyncTrackerSerializer<?> syncTracker) {
        CapabilitySyncRegistry.registerSyncTrackerType(clazz, syncTracker);
    }

    public void registerSyncTracker(Type type, SyncTrackerSerializer<?> syncTracker) {
        CapabilitySyncRegistry.registerSyncTrackerType(type, syncTracker);
    }
}

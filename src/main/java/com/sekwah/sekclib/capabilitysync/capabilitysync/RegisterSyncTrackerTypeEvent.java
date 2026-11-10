package com.sekwah.sekclib.capabilitysync.capabilitysync;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.lang.reflect.Type;

public class RegisterSyncTrackerTypeEvent extends Event implements IModBusEvent {

    public void registerSyncTracker(Class clazz, SyncTrackerSerializer syncTracker) {
        CapabilitySyncRegistry.registerSyncTrackerType(clazz, syncTracker);
    }

    public void registerSyncTracker(Type type, SyncTrackerSerializer syncTracker) {
        CapabilitySyncRegistry.registerSyncTrackerType(type, syncTracker);
    }
}

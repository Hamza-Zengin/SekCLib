package com.sekwah.sekclib.capabilitysync.capabilitysync;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.annotation.Sync;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.LoadingFailedException;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Currently only designed for players though if there is demand we can add entity syncing too.
 *
 * Though entities should probably just use DataWatchers.
 */
public class CapabilitySyncRegistry {

    private static final Map<Type, SyncTrackerSerializer> SYNC_TRACKER_SERIALIZER = new HashMap<>();

    /**
     * @param capability the capability to process
     * @param clazz so that the fields can be pre-grabbed.
     */
    @SuppressWarnings("squid:S3011")
    public static void registerPlayerCap(ResourceLocation resourceSyncName, Capability<?> capability, Class<?> clazz) {
        List<Field> values = Arrays.stream(clazz.getDeclaredFields())
                .filter(value -> value.isAnnotationPresent(Sync.class))
                .sorted(Comparator.comparing(Field::getName)).toList();
        CapabilityEntry capabilityEntry = new CapabilityEntry(resourceSyncName, capability, clazz);
        SekCLibRegistries.CAPABILITY_REGISTRY.register(resourceSyncName, capabilityEntry);
        List<ModLoadingException> errors = new ArrayList<>();
        int trackerId = 0;
        for (Field field : values) {
            Sync sync = field.getAnnotation(Sync.class);
            Type fieldType = field.getGenericType();
            SyncTrackerSerializer serializer = SYNC_TRACKER_SERIALIZER.get(fieldType);
            if (serializer == null) {
                serializer = SYNC_TRACKER_SERIALIZER.get(field.getType());
            }

            if(serializer == null) {
                String message = String.format("@Sync used on unsupported type %s (generic: %s). (Class: %s, Field: %s)", field.getType().getName(), fieldType.getTypeName(), clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(new ModLoadingException(null, ModLoadingStage.COMMON_SETUP, message, null));
            }
            try {
                field.setAccessible(true);
            } catch(UnsupportedOperationException e) {
                String message = String.format("Failed to set field accessible. (Class: %s, Field: %s)", clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(new ModLoadingException(null, ModLoadingStage.COMMON_SETUP, message, null));
            }
            try {
                SyncEntry entry = new SyncEntry(field.getName(), field, sync.minTicks(), trackerId, sync.syncGlobally(), serializer);
                capabilityEntry.addSyncEntry(entry);
                trackerId++;
            } catch (IllegalAccessException e) {
                String message = String.format("There was a problem un-reflecting (Class: %s, Field: %s)", clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(new ModLoadingException(null, ModLoadingStage.COMMON_SETUP, message, null));
            }
        }
        if(!errors.isEmpty()) {
            throw new LoadingFailedException(errors);
        }
    }

    public static void registerSyncTrackerType(Type type, SyncTrackerSerializer syncTracker) {
        SYNC_TRACKER_SERIALIZER.put(type, syncTracker);
    }

    public static void registerSyncTrackerType(Class clazz, SyncTrackerSerializer syncTracker) {
        SYNC_TRACKER_SERIALIZER.put(clazz, syncTracker);
    }

    public static SyncTrackerSerializer getTrackerSerializer(Type type) {
        return SYNC_TRACKER_SERIALIZER.get(type);
    }

    public static SyncTrackerSerializer getTrackerSerializer(Class clazz) {
        return SYNC_TRACKER_SERIALIZER.get(clazz);
    }

    /**
     * @return list of capabilities for syncing later
     */
    public static Collection<CapabilityEntry> getPlayerCapabilities() {
        return SekCLibRegistries.CAPABILITY_REGISTRY.getValues();
    }
}

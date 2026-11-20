package com.sekwah.sekclib.capabilitysync;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.annotation.Sync;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerSerializer;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

/**
 * Currently only designed for players though if there is demand we can add entity syncing too.
 *
 * Though entities should probably just use DataWatchers.
 */
public class CapabilitySyncRegistry {

    private static final Map<Type, SyncTrackerSerializer> SYNC_TRACKER_SERIALIZER = new HashMap<>();
    private static volatile List<CapabilityEntry> cachedCapabilities;

    public static void registerPlayerCap(Identifier resourceSyncName, AttachmentType<?> attachmentType, Class<?> clazz) {
        registerPlayerCap(resourceSyncName, () -> attachmentType, clazz, null);
    }

    public static void registerPlayerCap(Identifier resourceSyncName, Supplier<? extends AttachmentType<?>> attachmentType, Class<?> clazz) {
        registerPlayerCap(resourceSyncName, attachmentType, clazz, null);
    }

    /**
     * @param resourceSyncName the registry id for client/server sync
     * @param attachmentType   the attachment type supplier to process
     * @param clazz            so that the fields can be pre-grabbed.
     * @param registerHelper   optional RegisterEvent helper for NeoForge registry lifecycle
     */
    @SuppressWarnings("squid:S3011")
    public static void registerPlayerCap(Identifier resourceSyncName,
                                         Supplier<? extends AttachmentType<?>> attachmentType,
                                         Class<?> clazz,
                                         RegisterEvent.RegisterHelper<CapabilityEntry> registerHelper) {
        List<Field> values = Arrays.stream(clazz.getDeclaredFields())
                .filter(value -> value.isAnnotationPresent(Sync.class))
                .sorted(Comparator.comparing(Field::getName)).toList();
        CapabilityEntry capabilityEntry = new CapabilityEntry(resourceSyncName, attachmentType, clazz);
        if (registerHelper != null) {
            registerHelper.register(resourceSyncName, capabilityEntry);
        } else {
            Registry.register(SekCLibRegistries.CAPABILITY_REGISTRY, resourceSyncName, capabilityEntry);
        }
        cachedCapabilities = null;
        List<String> errors = new ArrayList<>();
        int trackerId = 0;
        for (Field field : values) {
            Sync sync = field.getAnnotation(Sync.class);
            Type fieldType = field.getGenericType();
            SyncTrackerSerializer serializer = SYNC_TRACKER_SERIALIZER.get(fieldType);
            if (serializer == null) {
                serializer = SYNC_TRACKER_SERIALIZER.get(field.getType());
            }

            if (serializer == null) {
                String message = String.format("@Sync used on unsupported type %s (generic: %s). (Class: %s, Field: %s)", field.getType().getName(), fieldType.getTypeName(), clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(message);
            }
            try {
                field.setAccessible(true);
            } catch (UnsupportedOperationException e) {
                String message = String.format("Failed to set field accessible. (Class: %s, Field: %s)", clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(message);
            }
            try {
                SyncEntry entry = new SyncEntry(field.getName(), field, sync.minTicks(), trackerId, sync.syncGlobally(), serializer);
                capabilityEntry.addSyncEntry(entry);
                trackerId++;
            } catch (IllegalAccessException e) {
                String message = String.format("There was a problem un-reflecting (Class: %s, Field: %s)", clazz.getName(), field.getName());
                SekCLib.LOGGER.error(message);
                errors.add(message);
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Failed to register player capability sync for " + resourceSyncName + ":\n" + String.join("\n", errors));
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
        List<CapabilityEntry> cached = cachedCapabilities;
        int registrySize = SekCLibRegistries.CAPABILITY_REGISTRY.size();
        if (cached == null || cached.size() != registrySize) {
            cached = SekCLibRegistries.CAPABILITY_REGISTRY.stream().toList();
            cachedCapabilities = cached;
        }
        return cached;
    }
}

package com.sekwah.sekclib.capabilitysync;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CapabilityEntry {
    private final Identifier resourceSyncName;
    private final Supplier<? extends AttachmentType<?>> attachmentType;
    private final Class<?> clazz;
    private volatile AttachmentType<?> resolvedAttachmentType;

    private final List<SyncEntry> syncEntries = new ArrayList<>();

    private final Map<String, SyncEntry> syncEntryHashMap = new HashMap<>();

    public CapabilityEntry(Identifier resourceSyncName, Supplier<? extends AttachmentType<?>> attachmentType, Class<?> clazz) {
        this.resourceSyncName = resourceSyncName;
        this.attachmentType = attachmentType;
        this.clazz = clazz;
    }

    public CapabilityEntry(Identifier resourceSyncName, AttachmentType<?> attachmentType, Class<?> clazz) {
        this(resourceSyncName, () -> attachmentType, clazz);
    }

    public Identifier getResourceSyncName() {
        return resourceSyncName;
    }

    public AttachmentType<?> getAttachmentType() {
        AttachmentType<?> cached = this.resolvedAttachmentType;
        if (cached == null && this.attachmentType != null) {
            cached = this.attachmentType.get();
            if (cached != null) {
                this.resolvedAttachmentType = cached;
            }
        }
        return cached;
    }

    public List<SyncEntry> getSyncEntries() {
        return this.syncEntries;
    }

    public SyncEntry getSyncEntryByName(String key) {
        return this.syncEntryHashMap.get(key);
    }

    public void addSyncEntry(SyncEntry entry) {
        if (!this.syncEntryHashMap.containsKey(entry.getName())) {
            this.syncEntries.add(entry);
            this.syncEntryHashMap.put(entry.getName(), entry);
        }
    }

    public Class<?> getCapabilityClass() {
        return clazz;
    }
}

package com.sekwah.sekclib.capabilitysync.capability;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.CapabilitySyncRegistry;
import com.sekwah.sekclib.capabilitysync.tracker.CapabilityTracker;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTracker;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncData implements ISyncData {

    private final List<CapabilityTracker> capabilityTrackers = new ArrayList<>();
    private final Set<CapabilityEntry> trackedEntries = new HashSet<>();

    public void addCapabilityTracker(CapabilityTracker syncTracker) {
        capabilityTrackers.add(syncTracker);
        if (syncTracker.getCapabilityEntry() != null) {
            trackedEntries.add(syncTracker.getCapabilityEntry());
        }
    }

    public void ensureTrackers(LivingEntity entity) {
        Collection<CapabilityEntry> capabilities = CapabilitySyncRegistry.getPlayerCapabilities();
        if (trackedEntries.size() >= capabilities.size()) {
            return;
        }
        ensureTrackers((IAttachmentHolder) entity, capabilities);
    }

    public void ensureTrackers(IAttachmentHolder entity, Collection<CapabilityEntry> capabilities) {
        if (entity == null || capabilities == null || trackedEntries.size() >= capabilities.size()) {
            return;
        }
        for (CapabilityEntry capabilityEntry : capabilities) {
            AttachmentType<?> attachmentType = capabilityEntry.getAttachmentType();
            if (attachmentType != null && entity.hasData(attachmentType) && trackedEntries.add(capabilityEntry)) {
                CapabilityTracker capabilityTracker = new CapabilityTracker(capabilityEntry);
                for (SyncEntry entry : capabilityEntry.getSyncEntries()) {
                    capabilityTracker.addSyncTracker(new SyncTracker(entry));
                }
                addCapabilityTracker(capabilityTracker);
            }
        }
    }

    @Override
    public List<CapabilityTracker> getCapabilityTrackers() {
        return capabilityTrackers;
    }
}

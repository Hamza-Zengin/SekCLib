package com.sekwah.sekclib.capabilitysync.capability;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.CapabilitySyncRegistry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.CapabilityTracker;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTracker;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
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
        for (CapabilityEntry capabilityEntry : CapabilitySyncRegistry.getPlayerCapabilities()) {
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

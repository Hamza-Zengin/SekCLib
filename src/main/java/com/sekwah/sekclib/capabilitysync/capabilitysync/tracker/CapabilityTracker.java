package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker;

import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.List;

public class CapabilityTracker {

    private final List<SyncTracker> syncTrackerList = new ArrayList<>();
    private final CapabilityEntry capabilityEntry;

    public CapabilityTracker(CapabilityEntry capClass) {
        this.capabilityEntry = capClass;
    }

    public void addSyncTracker(SyncTracker syncTracker) {
        this.syncTrackerList.add(syncTracker);
    }

    public CapabilityEntry getCapabilityEntry() {
        return this.capabilityEntry;
    }

    public AttachmentType<?> getAttachmentType() {
        return this.capabilityEntry.getAttachmentType();
    }

    public List<SyncTracker> getSyncTrackers() {
        return this.syncTrackerList;
    }
}

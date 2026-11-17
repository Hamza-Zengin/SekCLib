package com.sekwah.sekclib.capabilitysync.tracker;

import com.sekwah.sekclib.capabilitysync.SyncEntry;

public interface ISyncTrackerData {
    SyncEntry getSyncEntry();
    Object getSendValue();
}

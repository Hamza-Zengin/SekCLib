package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerClone;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.Set;

public class StringSetSyncTracker implements SyncTrackerSerializer<Set<String>>, SyncTrackerClone<Set<String>> {

    private static final int MAX_ELEMENTS = 256;
    private static final int MAX_STRING_LENGTH = 256;

    @Override
    public void encode(Set<String> objectToSend, FriendlyByteBuf outBuffer) {
        if (objectToSend == null) {
            outBuffer.writeInt(-1);
            return;
        }
        int count = Math.min(objectToSend.size(), MAX_ELEMENTS);
        outBuffer.writeInt(count);
        int written = 0;
        for (String str : objectToSend) {
            if (written++ >= MAX_ELEMENTS) {
                break;
            }
            outBuffer.writeUtf(str, MAX_STRING_LENGTH);
        }
    }

    @Override
    public Set<String> decode(FriendlyByteBuf inBuffer) {
        int size = inBuffer.readInt();
        if (size <= 0 || size > MAX_ELEMENTS) {
            return new HashSet<>();
        }
        Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(inBuffer.readUtf(MAX_STRING_LENGTH));
        }
        return set;
    }

    @Override
    public Set<String> clone(Set<String> data) {
        return data != null ? new HashSet<>(data) : new HashSet<>();
    }
}

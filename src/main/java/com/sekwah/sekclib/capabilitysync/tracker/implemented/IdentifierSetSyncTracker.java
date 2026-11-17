package com.sekwah.sekclib.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerClone;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

public class IdentifierSetSyncTracker implements SyncTrackerSerializer<Set<Identifier>>, SyncTrackerClone<Set<Identifier>> {

    private static final int MAX_ELEMENTS = 256;

    @Override
    public void encode(Set<Identifier> objectToSend, FriendlyByteBuf outBuffer) {
        if (objectToSend == null) {
            outBuffer.writeInt(-1);
            return;
        }
        int count = Math.min(objectToSend.size(), MAX_ELEMENTS);
        outBuffer.writeInt(count);
        int written = 0;
        for (Identifier loc : objectToSend) {
            if (written++ >= MAX_ELEMENTS) {
                break;
            }
            outBuffer.writeIdentifier(loc);
        }
    }

    @Override
    public Set<Identifier> decode(FriendlyByteBuf inBuffer) {
        int size = inBuffer.readInt();
        if (size <= 0 || size > MAX_ELEMENTS) {
            return new HashSet<>();
        }
        Set<Identifier> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(inBuffer.readIdentifier());
        }
        return set;
    }

    @Override
    public Set<Identifier> clone(Set<Identifier> data) {
        return data != null ? new HashSet<>(data) : new HashSet<>();
    }
}

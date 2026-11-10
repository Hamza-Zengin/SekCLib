package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerClone;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ResourceLocationSetSyncTracker implements SyncTrackerSerializer<Set<ResourceLocation>>, SyncTrackerClone<Set<ResourceLocation>> {

    private static final int MAX_ELEMENTS = 256;

    @Override
    public void encode(Set<ResourceLocation> objectToSend, FriendlyByteBuf outBuffer) {
        if (objectToSend == null) {
            outBuffer.writeInt(-1);
            return;
        }
        int count = Math.min(objectToSend.size(), MAX_ELEMENTS);
        outBuffer.writeInt(count);
        int written = 0;
        for (ResourceLocation loc : objectToSend) {
            if (written++ >= MAX_ELEMENTS) {
                break;
            }
            outBuffer.writeResourceLocation(loc);
        }
    }

    @Override
    public Set<ResourceLocation> decode(FriendlyByteBuf inBuffer) {
        int size = inBuffer.readInt();
        if (size <= 0 || size > MAX_ELEMENTS) {
            return new HashSet<>();
        }
        Set<ResourceLocation> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(inBuffer.readResourceLocation());
        }
        return set;
    }

    @Override
    public Set<ResourceLocation> clone(Set<ResourceLocation> data) {
        return data != null ? new HashSet<>(data) : new HashSet<>();
    }
}

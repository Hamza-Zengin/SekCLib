package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerClone;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class CompoundTagSyncTracker implements SyncTrackerSerializer<CompoundTag>, SyncTrackerClone<CompoundTag> {

    @Override
    public void encode(CompoundTag objectToSend, FriendlyByteBuf outBuffer) {
        outBuffer.writeNbt(objectToSend);
    }

    @Override
    public CompoundTag decode(FriendlyByteBuf inBuffer) {
        return inBuffer.readNbt();
    }

    @Override
    public CompoundTag clone(CompoundTag data) {
        return data != null ? data.copy() : null;
    }
}

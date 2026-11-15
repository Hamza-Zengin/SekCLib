package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class IdentifierSyncTracker implements SyncTrackerSerializer<Identifier> {

    @Override
    public void encode(Identifier objectToSend, FriendlyByteBuf outBuffer) {
        outBuffer.writeIdentifier(objectToSend);
    }

    @Override
    public Identifier decode(FriendlyByteBuf inBuffer) {
        return inBuffer.readIdentifier();
    }
}

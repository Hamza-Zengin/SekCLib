package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker;

import net.minecraft.network.FriendlyByteBuf;

public interface SyncTrackerSerializer<T> {
    void encode(T objectToSend, FriendlyByteBuf outBuffer);

    T decode(FriendlyByteBuf inBuffer);

    default boolean shouldSend(T currentValue, T sendValue) {
        return !java.util.Objects.equals(sendValue, currentValue);
    }
}

package com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.implemented;

import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerSerializer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

public class FloatSyncTracker implements SyncTrackerSerializer<Float> {

    public static final float MIN_SYNC_DIFF = 0.1f;
    private final float min;
    private final float max;

    public FloatSyncTracker() {
        this(0.0f, Float.MAX_VALUE);
    }

    public FloatSyncTracker(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getMinSyncDiff() {
        return MIN_SYNC_DIFF;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Override
    public void encode(Float objectToSend, FriendlyByteBuf outBuffer) {
        outBuffer.writeFloat(objectToSend);
    }

    @Override
    public Float decode(FriendlyByteBuf inBuffer) {
        return inBuffer.readFloat();
    }

    @Override
    public boolean shouldSend(Float currentValue, Float sendValue) {
        if (sendValue == null) {
            return currentValue != null;
        }
        if (currentValue == null) {
            return true;
        }
        if (Objects.equals(currentValue, sendValue)) {
            return false;
        }
        // Min veya max sınırlarına ulaşıldığında mutlaka senkronize et
        if (currentValue <= this.min || currentValue >= this.max) {
            return true;
        }
        return Math.abs(currentValue - sendValue) >= MIN_SYNC_DIFF;
    }
}

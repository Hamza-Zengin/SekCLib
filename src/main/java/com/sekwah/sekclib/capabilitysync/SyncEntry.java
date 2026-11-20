package com.sekwah.sekclib.capabilitysync;

import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerClone;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerSerializer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class SyncEntry {

    private final Field field;
    private final SyncTrackerSerializer syncTrackerSerializer;

    /**
     * Which id for the current class. Used for encoding and decoding over the network.
     */
    private final int trackerId;
    private final String name;
    private final MethodHandle getter;
    private final MethodHandle setter;
    private final MethodHandle invokableGetter;
    private final MethodHandle invokableSetter;
    private final boolean cloneableSerializer;
    private final int minTicks;
    private final boolean syncGlobally;

    public SyncEntry(String name, Field field, int minTicks, int trackerId, boolean syncGlobally, SyncTrackerSerializer syncTrackerSerializer) throws IllegalAccessException {
        this.name = name;
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        if (field != null) {
            this.getter = lookup.unreflectGetter(field);
            this.setter = lookup.unreflectSetter(field);
        } else {
            this.getter = null;
            this.setter = null;
        }
        this.invokableGetter = this.getter != null
                ? this.getter.asType(MethodType.methodType(Object.class, Object.class))
                : null;
        this.invokableSetter = this.setter != null
                ? this.setter.asType(MethodType.methodType(void.class, Object.class, Object.class))
                : null;
        this.cloneableSerializer = syncTrackerSerializer instanceof SyncTrackerClone;
        this.field = field;
        this.trackerId = trackerId;
        this.minTicks = minTicks;
        this.syncGlobally = syncGlobally;
        this.syncTrackerSerializer = syncTrackerSerializer;
    }

    public String getName() {
        return this.name;
    }

    public Field getField() {
        return this.field;
    }

    public int getMinTicks() {
        return this.minTicks;
    }

    public boolean isSyncGlobally() {
        return this.syncGlobally;
    }

    public MethodHandle getGetter() {
        return this.getter;
    }

    public MethodHandle getSetter() {
        return this.setter;
    }

    public Object getValue(Object target) throws Throwable {
        if (this.invokableGetter == null) {
            return null;
        }
        return this.invokableGetter.invokeExact(target);
    }

    public void setValue(Object target, Object value) throws Throwable {
        if (this.invokableSetter != null) {
            this.invokableSetter.invokeExact(target, value);
        }
    }

    public boolean isCloneableSerializer() {
        return this.cloneableSerializer;
    }

    public SyncTrackerSerializer getSerializer() {
        return this.syncTrackerSerializer;
    }

    public int getTrackerId() {
        return trackerId;
    }
}

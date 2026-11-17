package com.sekwah.sekclib.capabilitysync;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

public class RegisterCapabilitySyncEvent extends Event implements IModBusEvent {

    private final RegisterEvent.RegisterHelper<CapabilityEntry> registerHelper;

    public RegisterCapabilitySyncEvent() {
        this(null);
    }

    public RegisterCapabilitySyncEvent(RegisterEvent.RegisterHelper<CapabilityEntry> registerHelper) {
        this.registerHelper = registerHelper;
    }

    /**
     * @param resourceSyncName - This is needed to help sync with the client.
     * @param attachmentType   - Supplier of the attachment type to pull the data from.
     * @param clazz            - The class to pull the annotation data from.
     */
    public void registerPlayerCap(Identifier resourceSyncName, Supplier<? extends AttachmentType<?>> attachmentType, Class<?> clazz) {
        CapabilitySyncRegistry.registerPlayerCap(resourceSyncName, attachmentType, clazz, this.registerHelper);
    }

    /**
     * @param resourceSyncName - This is needed to help sync with the client.
     * @param attachmentType   - The attachment type to pull the data from.
     * @param clazz            - The class to pull the annotation data from.
     */
    public void registerPlayerCap(Identifier resourceSyncName, AttachmentType<?> attachmentType, Class<?> clazz) {
        registerPlayerCap(resourceSyncName, () -> attachmentType, clazz);
    }

}

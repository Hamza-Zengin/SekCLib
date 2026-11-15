package com.sekwah.sekclib.registries;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.RegisterCapabilitySyncEvent;
import com.sekwah.sekclib.capabilitysync.capabilitysync.RegisterSyncTrackerTypeEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class SekCLibRegistries {

    public static final ResourceKey<Registry<CapabilityEntry>> CAPABILITY_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "capability_sync"));

    public static final Registry<CapabilityEntry> CAPABILITY_REGISTRY =
            new RegistryBuilder<>(CAPABILITY_REGISTRY_KEY).sync(true).create();

    /**
     * There may be a better way of doing this though this is to sync the id's between client and server
     * and allow for much shorter packets to be sent.
     *
     * Do not manually register to these registries, it will be handled all within SekCLib
     *
     * @param event NewRegistryEvent from NeoForge
     */
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(CAPABILITY_REGISTRY);
    }

    /**
     * Fires sync tracker type and capability sync registration events during RegisterEvent
     * so entries are registered before NeoForge freezes CAPABILITY_REGISTRY.
     *
     * @param event RegisterEvent from NeoForge
     */
    public static void registerCapabilityEntries(RegisterEvent event) {
        if (event.getRegistryKey().equals(CAPABILITY_REGISTRY_KEY)) {
            ModLoader.postEvent(new RegisterSyncTrackerTypeEvent());
            event.register(CAPABILITY_REGISTRY_KEY, helper ->
                    ModLoader.postEvent(new RegisterCapabilitySyncEvent(helper))
            );
        }
    }

}

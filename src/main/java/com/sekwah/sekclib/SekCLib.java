package com.sekwah.sekclib;

import com.sekwah.sekclib.capabilitysync.capability.SyncDataCapabilityHandler;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SekCLib.MOD_ID)
public class SekCLib {

    public static final String MOD_ID = "sekclib";

    public static final Logger LOGGER = LogManager.getLogger("SekC Lib");

    public SekCLib(IEventBus modEventBus, ModContainer modContainer) {
        SyncDataCapabilityHandler.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(SekCLibRegistries::registerRegistries);
        modEventBus.addListener(SekCLibRegistries::registerCapabilityEntries);
    }

}

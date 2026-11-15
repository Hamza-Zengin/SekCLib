package com.sekwah.sekclib.network.s2c;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.capabilitysync.broadcaster.CapabilityInfo;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.ISyncTrackerData;
import com.sekwah.sekclib.capabilitysync.capabilitysync.tracker.SyncTrackerUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

@EventBusSubscriber(modid = SekCLib.MOD_ID, value = Dist.CLIENT)
public class ClientPayloadHandler {

    @SubscribeEvent
    public static void registerClientPayloads(final RegisterClientPayloadHandlersEvent event) {
        event.register(ClientCapabilitySyncPacket.TYPE, ClientPayloadHandler::handleCapabilitySync);
        event.register(ClientVelocityPacket.TYPE, ClientPayloadHandler::handleVelocity);
    }

    public static void handleCapabilitySync(final ClientCapabilitySyncPacket msg, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            Entity entity = level.getEntity(msg.getEntityId());
            if (entity instanceof LivingEntity livingEntity) {
                for (CapabilityInfo capInfo : msg.capabilityData) {
                    if (capInfo.capability == null || capInfo.capability.getAttachmentType() == null) {
                        continue;
                    }
                    Object targetCap = livingEntity.getData(capInfo.capability.getAttachmentType());
                    if (targetCap == null) {
                        continue;
                    }
                    List<SyncEntry> syncEntries = capInfo.capability.getSyncEntries();
                    for (ISyncTrackerData syncTrackerData : capInfo.changedEntries) {
                        int trackerId = syncTrackerData.getSyncEntry().getTrackerId();
                        if (trackerId < 0 || trackerId >= syncEntries.size()) {
                            continue;
                        }
                        SyncEntry syncEntry = syncEntries.get(trackerId);
                        try {
                            Object syncValue = syncTrackerData.getSendValue();
                            if (syncTrackerData.getSyncEntry().getSerializer() instanceof SyncTrackerUpdater updater) {
                                syncValue = updater.updateTracker(syncValue, syncEntry.getGetter().invoke(targetCap));
                            }
                            syncEntry.getSetter().invoke(targetCap, syncValue);
                        } catch (Throwable e) {
                            SekCLib.LOGGER.error("There was a problem setting a value", e);
                        }
                    }
                }
            }
        });
    }

    public static void handleVelocity(final ClientVelocityPacket msg, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (msg.getYa() > 0) {
                    player.setOnGround(false);
                }
                player.lerpMotion(new Vec3(msg.getXa() / 8000.0D, msg.getYa() / 8000.0D, msg.getZa() / 8000.0D));
            }
        });
    }
}

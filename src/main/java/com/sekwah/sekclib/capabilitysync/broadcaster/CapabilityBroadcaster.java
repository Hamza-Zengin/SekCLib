package com.sekwah.sekclib.capabilitysync.broadcaster;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.CapabilitySyncRegistry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.capability.SyncData;
import com.sekwah.sekclib.capabilitysync.capability.SyncDataCapabilityHandler;
import com.sekwah.sekclib.capabilitysync.tracker.CapabilityTracker;
import com.sekwah.sekclib.capabilitysync.tracker.ISyncTrackerData;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTracker;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerData;
import com.sekwah.sekclib.network.SekCPacketHandler;
import com.sekwah.sekclib.network.s2c.ClientCapabilitySyncPacket;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Handle sending the capabilities to users.
 */
public class CapabilityBroadcaster {

    /**
     * Checks whether the given entity currently has any registered synced capability attached.
     *
     * @param entity the entity to inspect
     * @return true if at least one synced capability attachment is present on the entity
     */
    public static boolean hasAnySyncedCapability(LivingEntity entity) {
        return hasAnySyncedCapability(entity, CapabilitySyncRegistry.getPlayerCapabilities());
    }

    public static boolean hasAnySyncedCapability(IAttachmentHolder holder, Collection<CapabilityEntry> capabilities) {
        if (holder == null || capabilities == null || capabilities.isEmpty()) {
            return false;
        }
        for (CapabilityEntry capabilityEntry : capabilities) {
            AttachmentType<?> attachmentType = capabilityEntry.getAttachmentType();
            if (attachmentType != null && holder.hasData(attachmentType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldSyncEntity(IAttachmentHolder holder, AttachmentType<SyncData> syncDataType, Collection<CapabilityEntry> capabilities) {
        if (holder == null) {
            return false;
        }
        return (syncDataType != null && holder.hasData(syncDataType)) || hasAnySyncedCapability(holder, capabilities);
    }

    /**
     * Also tick the sync trackers to make sure the data is up to date and flag what should be sent.
     *
     * @param player
     */
    public static void checkCapData(LivingEntity player) {
        if (player == null) {
            return;
        }
        AttachmentType<SyncData> syncDataType = SyncDataCapabilityHandler.SYNC_DATA.get();
        if (!player.hasData(syncDataType) && !hasAnySyncedCapability(player)) {
            return;
        }
        SyncData syncData = player.getData(syncDataType);
        syncData.ensureTrackers(player);
        List<CapabilityTracker> capTrackers = syncData.getCapabilityTrackers();
        boolean anyMarked = false;
        for (CapabilityTracker capTracker : capTrackers) {
            AttachmentType<?> attachmentType = capTracker.getAttachmentType();
            if (attachmentType != null && player.hasData(attachmentType)) {
                Object data = player.getData(attachmentType);
                for (SyncTracker syncTracker : capTracker.getSyncTrackers()) {
                    try {
                        syncTracker.tick(data);
                        if (syncTracker.isMarkedForSend()) {
                            anyMarked = true;
                        }
                    } catch (Throwable e) {
                        SekCLib.LOGGER.error("There was a problem updating a sync tracker", e);
                    }
                }
            }
        }

        if (anyMarked) {
            broadcastCapChanges(player, false);
        }
    }

    private static List<CapabilityInfo> collectEntries(LivingEntity entity, boolean returnAll) {
        if (entity == null) {
            return Collections.emptyList();
        }
        AttachmentType<SyncData> syncDataType = SyncDataCapabilityHandler.SYNC_DATA.get();
        if (!entity.hasData(syncDataType) && !hasAnySyncedCapability(entity)) {
            return Collections.emptyList();
        }
        List<CapabilityInfo> capInfoList = null;
        SyncData syncData = entity.getData(syncDataType);
        syncData.ensureTrackers(entity);
        List<CapabilityTracker> capTrackers = syncData.getCapabilityTrackers();
        for (CapabilityTracker capTracker : capTrackers) {
            CapabilityEntry capEntry = capTracker.getCapabilityEntry();
            AttachmentType<?> attachmentType = capTracker.getAttachmentType();
            CapabilityInfo capInfo = null;

            if (returnAll && attachmentType != null && entity.hasData(attachmentType)) {
                Object data = entity.getData(attachmentType);
                for (SyncTracker syncTracker : capTracker.getSyncTrackers()) {
                    SyncEntry syncEntry = syncTracker.getSyncEntry();
                    Object liveValue;
                    try {
                        liveValue = syncEntry.getValue(data);
                        syncTracker.markSentValue(liveValue);
                        liveValue = syncTracker.getSendValue();
                    } catch (Throwable t) {
                        liveValue = syncTracker.getSendValue();
                    }
                    if (capInfo == null) {
                        capInfo = new CapabilityInfo(SekCLibRegistries.CAPABILITY_REGISTRY.getId(capEntry), capEntry);
                    }
                    ISyncTrackerData trackerData = new SyncTrackerData(syncEntry, liveValue);
                    if (syncEntry.isSyncGlobally()) {
                        capInfo.changedEntries.add(trackerData);
                    } else {
                        capInfo.changedPrivateEntries.add(trackerData);
                    }
                }
            } else {
                for (SyncTracker syncTracker : capTracker.getSyncTrackers()) {
                    if (returnAll || syncTracker.isMarkedForSend()) {
                        if (capInfo == null) {
                            capInfo = new CapabilityInfo(SekCLibRegistries.CAPABILITY_REGISTRY.getId(capEntry), capEntry);
                        }
                        if (syncTracker.getSyncEntry().isSyncGlobally()) {
                            capInfo.changedEntries.add(syncTracker);
                        } else {
                            capInfo.changedPrivateEntries.add(syncTracker);
                        }
                    }
                }
            }

            if (capInfo != null) {
                if (capInfoList == null) {
                    capInfoList = new ArrayList<>();
                }
                capInfoList.add(capInfo);
            }
        }
        return capInfoList != null ? capInfoList : Collections.emptyList();
    }

    /**
     * Collect player
     *
     * @param entity - what player to get the data off to check the syncing.
     */
    public static void broadcastCapChanges(LivingEntity entity, boolean sendAll) {

        List<CapabilityInfo> dataToSend = collectEntries(entity, sendAll);

        if (dataToSend.isEmpty()) {
            return;
        }

        boolean hasPrivate = false;
        boolean hasGlobal = false;
        for (CapabilityInfo info : dataToSend) {
            if (!info.changedPrivateEntries.isEmpty()) {
                hasPrivate = true;
            }
            if (!info.changedEntries.isEmpty()) {
                hasGlobal = true;
            }
            if (hasPrivate && hasGlobal) {
                break;
            }
        }

        // If there are private entries, send full bundle to self, and global bundle to tracking
        if (hasPrivate && entity instanceof ServerPlayer serverPlayer) {
            ClientCapabilitySyncPacket selfData = new ClientCapabilitySyncPacket(entity, dataToSend, true);
            if (!selfData.capabilityData.isEmpty()) {
                SekCPacketHandler.sendToPlayer(selfData, serverPlayer);
                // Spectators currently viewing this player through their camera also receive private data
                // so the target's UI can be rendered from the spectator's perspective.
                for (ServerPlayer spectator : getSpectators(serverPlayer)) {
                    SekCPacketHandler.sendToPlayer(selfData, spectator);
                }
            }
            if (hasGlobal) {
                ClientCapabilitySyncPacket forOthers = new ClientCapabilitySyncPacket(entity, dataToSend, false);
                if (!forOthers.capabilityData.isEmpty()) {
                    SekCPacketHandler.sendToTracking(forOthers, entity);
                }
            }
        } else if (hasGlobal) {
            // When only globally visible capabilities change, send a single packet to TRACKING_ENTITY_AND_SELF
            ClientCapabilitySyncPacket packet = new ClientCapabilitySyncPacket(entity, dataToSend, false);
            if (!packet.capabilityData.isEmpty()) {
                SekCPacketHandler.sendToTrackingAndSelf(packet, entity);
            }
        }

    }

    public static void broadcastCapToPlayer(LivingEntity entity, ServerPlayer player) {
        List<CapabilityInfo> dataToSend = collectEntries(entity, true);

        if (dataToSend.isEmpty()) {
            return;
        }
        ClientCapabilitySyncPacket forOthers = new ClientCapabilitySyncPacket(entity, dataToSend, false);
        if (!forOthers.capabilityData.isEmpty()) {
            SekCPacketHandler.sendToPlayer(forOthers, player);
        }
    }

    /**
     * Players in spectator mode whose camera is currently attached to the given player.
     *
     * @param target the player being spectated
     * @return the spectating players, never null
     */
    public static List<ServerPlayer> getSpectators(ServerPlayer target) {
        if (target == null) {
            return Collections.emptyList();
        }
        List<ServerPlayer> spectators = null;
        for (ServerPlayer other : target.level().players()) {
            if (other != target && other.isSpectator() && other.getCamera() == target) {
                if (spectators == null) {
                    spectators = new ArrayList<>();
                }
                spectators.add(other);
            }
        }
        return spectators != null ? spectators : Collections.emptyList();
    }

    /**
     * Send the full state, including private entries, of the target to a spectator that has just
     * started viewing them.
     *
     * @param target    the player being spectated
     * @param spectator the spectating player
     */
    public static void broadcastFullToSpectator(ServerPlayer target, ServerPlayer spectator) {
        List<CapabilityInfo> dataToSend = collectEntries(target, true);
        if (dataToSend.isEmpty()) {
            return;
        }
        ClientCapabilitySyncPacket packet = new ClientCapabilitySyncPacket(target, dataToSend, true);
        if (!packet.capabilityData.isEmpty()) {
            SekCPacketHandler.sendToPlayer(packet, spectator);
        }
    }
}

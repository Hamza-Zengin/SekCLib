package com.sekwah.sekclib.network.s2c;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.CapabilityEntry;
import com.sekwah.sekclib.capabilitysync.SyncEntry;
import com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityInfo;
import com.sekwah.sekclib.capabilitysync.tracker.ISyncTrackerData;
import com.sekwah.sekclib.capabilitysync.tracker.SyncTrackerData;
import com.sekwah.sekclib.registries.SekCLibRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structure
 * int what player it targets
 * int how many capabilities
 * <p>
 * int capability sync id
 * int how many fields to sync
 * <p>
 * int id for which field on the capability to sync. (this stops types needing to by synced with a registry)
 * boolean has data (not null) (if its null skip encoding & decoding)
 * (push to the tracker to handle the encoding and decoding)
 */
public class ClientCapabilitySyncPacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientCapabilitySyncPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SekCLib.MOD_ID, "client_capability_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClientCapabilitySyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientCapabilitySyncPacket::encode, ClientCapabilitySyncPacket::decode);

    private static final int MAX_SYNC_ENTRIES = 256;

    private final int entityId;
    public List<CapabilityInfo> capabilityData;
    private boolean includePrivate;

    /**
     * To construct the data for serialisation
     *
     * @param entity
     * @param capabilityData
     * @param includePrivate - should include non global tags
     */
    public ClientCapabilitySyncPacket(LivingEntity entity, List<CapabilityInfo> capabilityData, boolean includePrivate) {
        this.entityId = entity.getId();
        this.capabilityData = new ArrayList<>(capabilityData);
        this.includePrivate = includePrivate;
        if (!includePrivate) {
            this.capabilityData.removeIf(data -> data.changedEntries.isEmpty());
        }
    }

    private ClientCapabilitySyncPacket(int player, List<CapabilityInfo> capabilityData) {
        this.entityId = player;
        this.capabilityData = capabilityData;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public int getEntityId() {
        return entityId;
    }

    public static void encodeSyncTrackers(List<ISyncTrackerData> trackers, FriendlyByteBuf outBuffer) {
        for (ISyncTrackerData tracker : trackers) {
            outBuffer.writeByte(tracker.getSyncEntry().getTrackerId());
            outBuffer.writeBoolean(tracker.getSendValue() != null);
            if (tracker.getSendValue() != null) {
                tracker.getSyncEntry().getSerializer().encode(tracker.getSendValue(), outBuffer);
            }
        }
    }

    /**
     * @param capability for context on how to de-code the capabilities
     * @param inBuffer
     * @return
     */
    public static List<ISyncTrackerData> decodeSyncTrackers(CapabilityEntry capability, FriendlyByteBuf inBuffer) {
        int trackerCount = inBuffer.readInt();
        if (trackerCount < 0 || trackerCount > MAX_SYNC_ENTRIES) {
            SekCLib.LOGGER.warn("Received invalid tracker count: {}", trackerCount);
            inBuffer.skipBytes(inBuffer.readableBytes());
            return Collections.emptyList();
        }
        List<ISyncTrackerData> syncTrackerDataList = new ArrayList<>(trackerCount);
        for (int i = 0; i < trackerCount; i++) {
            if (!inBuffer.isReadable()) {
                break;
            }
            int trackerId = inBuffer.readUnsignedByte();
            boolean hasData = inBuffer.readBoolean();
            if (capability == null || trackerId < 0 || trackerId >= capability.getSyncEntries().size()) {
                SekCLib.LOGGER.warn("Invalid tracker id: {} for capability", trackerId);
                inBuffer.skipBytes(inBuffer.readableBytes());
                return syncTrackerDataList;
            }
            SyncEntry tracker = capability.getSyncEntries().get(trackerId);
            Object data = hasData ? tracker.getSerializer().decode(inBuffer) : null;
            syncTrackerDataList.add(new SyncTrackerData(tracker, data));
        }
        return syncTrackerDataList;
    }

    public static void encode(ClientCapabilitySyncPacket msg, FriendlyByteBuf outBuffer) {
        outBuffer.writeInt(msg.entityId);
        outBuffer.writeInt(msg.capabilityData.size());
        for (CapabilityInfo capInfo : msg.capabilityData) {
            outBuffer.writeInt(capInfo.capabilityId);
            int count = capInfo.changedEntries.size();
            if (msg.includePrivate) count += capInfo.changedPrivateEntries.size();
            outBuffer.writeInt(count);

            encodeSyncTrackers(capInfo.changedEntries, outBuffer);
            if (msg.includePrivate) encodeSyncTrackers(capInfo.changedPrivateEntries, outBuffer);
        }
    }

    public static ClientCapabilitySyncPacket decode(FriendlyByteBuf inBuffer) {
        int playerId = inBuffer.readInt();
        int capCount = inBuffer.readInt();
        if (capCount < 0 || capCount > MAX_SYNC_ENTRIES) {
            SekCLib.LOGGER.warn("Received invalid capability count: {}", capCount);
            inBuffer.skipBytes(inBuffer.readableBytes());
            return new ClientCapabilitySyncPacket(playerId, Collections.emptyList());
        }
        List<CapabilityInfo> capabilityInfoList = new ArrayList<>(capCount);
        for (int i = 0; i < capCount; i++) {
            if (!inBuffer.isReadable()) {
                break;
            }
            int capId = inBuffer.readInt();
            CapabilityEntry capability = SekCLibRegistries.CAPABILITY_REGISTRY.byId(capId);
            if (capability == null) {
                SekCLib.LOGGER.warn("Received capability sync packet for unregistered capability id: {}", capId);
                inBuffer.skipBytes(inBuffer.readableBytes());
                break;
            }
            List<ISyncTrackerData> syncTrackers = decodeSyncTrackers(capability, inBuffer);
            if (!syncTrackers.isEmpty()) {
                CapabilityInfo capInfo = new CapabilityInfo(capId, capability);
                capInfo.changedEntries.addAll(syncTrackers);
                capabilityInfoList.add(capInfo);
            }
        }

        return new ClientCapabilitySyncPacket(playerId, capabilityInfoList);
    }
}

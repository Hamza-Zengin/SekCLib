package com.sekwah.sekclib.network;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.network.s2c.ClientCapabilitySyncPacket;
import com.sekwah.sekclib.network.s2c.ClientVelocityPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SekCLib.MOD_ID)
public class SekCPacketHandler {

    public static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ClientCapabilitySyncPacket.TYPE, ClientCapabilitySyncPacket.STREAM_CODEC);
        registrar.playToClient(ClientVelocityPacket.TYPE, ClientVelocityPacket.STREAM_CODEC);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static void sendToAll(CustomPacketPayload payload) {
        try {
            PacketDistributor.sendToAllPlayers(payload);
        } catch (UnsupportedOperationException ignored) {
            // Clients without the SekCLib channel (e.g., mock test players) are skipped.
        }
    }

    public static void sendToTracking(CustomPacketPayload payload, LivingEntity entity) {
        try {
            PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        } catch (UnsupportedOperationException ignored) {
            // Clients without the SekCLib channel (e.g., mock test players) are skipped.
        }
    }

    public static void sendToTrackingAndSelf(CustomPacketPayload payload, LivingEntity entity) {
        try {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
        } catch (UnsupportedOperationException ignored) {
            // Clients without the SekCLib channel (e.g., mock test players) are skipped.
        }
    }
}

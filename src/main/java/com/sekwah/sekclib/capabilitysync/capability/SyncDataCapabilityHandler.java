package com.sekwah.sekclib.capabilitysync.capability;

import com.sekwah.sekclib.SekCLib;
import com.sekwah.sekclib.capabilitysync.broadcaster.CapabilityBroadcaster;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@EventBusSubscriber(modid = SekCLib.MOD_ID)
public class SyncDataCapabilityHandler {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SekCLib.MOD_ID);

    public static final Supplier<AttachmentType<SyncData>> SYNC_DATA =
            ATTACHMENT_TYPES.register("sync_data", () -> AttachmentType.builder(SyncData::new).build());

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerUpdate(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            CapabilityBroadcaster.checkCapData(serverPlayer);
            checkSpectatorCamera(serverPlayer);
        }
    }

    /** Last camera entity id a spectator was attached to, used to detect camera target changes. */
    private static final Map<UUID, Integer> SPECTATOR_CAMERA = new ConcurrentHashMap<>();

    /**
     * When a spectator starts viewing another player, send that player's full data (including private
     * entries) so UI that depends on it works in spectator mode.
     */
    private static void checkSpectatorCamera(ServerPlayer spectator) {
        if (!spectator.isSpectator() || spectator.getCamera() == spectator) {
            if (!SPECTATOR_CAMERA.isEmpty()) {
                SPECTATOR_CAMERA.remove(spectator.getUUID());
            }
            return;
        }
        int cameraId = spectator.getCamera().getId();
        Integer previous = SPECTATOR_CAMERA.put(spectator.getUUID(), cameraId);
        if ((previous == null || previous != cameraId) && spectator.getCamera() instanceof ServerPlayer target) {
            CapabilityBroadcaster.broadcastFullToSpectator(target, spectator);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!SPECTATOR_CAMERA.isEmpty()) {
            SPECTATOR_CAMERA.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityLivingUpdate(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !(entity instanceof Player) && !entity.level().isClientSide()) {
            CapabilityBroadcaster.checkCapData(entity);
        }
    }

    @SubscribeEvent
    public static void dimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CapabilityBroadcaster.broadcastCapChanges(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CapabilityBroadcaster.broadcastCapChanges(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CapabilityBroadcaster.broadcastCapChanges(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(SYNC_DATA.get()).ensureTrackers(serverPlayer);
        }
    }

    /**
     * Server side event
     * @param event the event
     */
    @SubscribeEvent
    public static void playerTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof LivingEntity livingEntity) {
            CapabilityBroadcaster.broadcastCapToPlayer(livingEntity, serverPlayer);
        }
    }

}

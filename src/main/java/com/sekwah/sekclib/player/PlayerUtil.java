package com.sekwah.sekclib.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Compatibility wrapper for older mod versions.
 *
 * @deprecated Use {@link com.sekwah.sekclib.util.PlayerUtil} instead.
 */
@Deprecated(since = "1.0.2", forRemoval = false)
public class PlayerUtil extends com.sekwah.sekclib.util.PlayerUtil {

    public static void setVelocity(Player player, Vec3 velocity, boolean ignoreGround) {
        com.sekwah.sekclib.util.PlayerUtil.setVelocity(player, velocity, ignoreGround);
    }

    public static void setVelocity(Player player, double x, double y, double z, boolean ignoreGround) {
        com.sekwah.sekclib.util.PlayerUtil.setVelocity(player, x, y, z, ignoreGround);
    }
}

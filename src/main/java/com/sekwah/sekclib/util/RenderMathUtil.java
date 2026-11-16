package com.sekwah.sekclib.util;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Pure, stateless math and PoseStack scaling helpers for client rendering.
 */
public final class RenderMathUtil {

    /** Packed light coordinates for full brightness (Block 15, Sky 15). */
    public static final int FULL_BRIGHT = 0x00F000F0;

    /** Packed light coordinates for minimum ambient baseline (Block 12, Sky 12). */
    public static final int MIN_AMBIENT_LIGHT = 0x00C000C0;

    private RenderMathUtil() {
    }

    public static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public static float lerp(float start, float end, float t) {
        return start + (end - start) * clamp01(t);
    }

    /**
     * Computes a linear fade-out multiplier in [0..1] starting at {@code fadeStart}.
     * Returns 1.0F when {@code progress <= fadeStart}, and 0.0F when {@code progress >= 1.0F}.
     */
    public static float fadeOut(float progress, float fadeStart) {
        if (progress <= fadeStart) {
            return 1.0F;
        }
        if (fadeStart >= 1.0F) {
            return 0.0F;
        }
        return clamp01(1.0F - ((progress - fadeStart) / (1.0F - fadeStart)));
    }

    public static int clampMinLight(int packedLight, int minLight) {
        return Math.max(packedLight, minLight);
    }

    public static void scaleUniform(PoseStack poseStack, float scale) {
        poseStack.scale(scale, scale, scale);
    }

    /**
     * Applies standard Minecraft entity model inversion and uniform scaling (-scale, -scale, scale).
     */
    public static void scaleEntityModel(PoseStack poseStack, float scale) {
        poseStack.scale(-scale, -scale, scale);
    }
}

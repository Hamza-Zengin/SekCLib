package com.sekwah.sekclib;

import com.sekwah.sekclib.util.ColorUtil;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class ColorUtilTest {

    @Test
    void testRecalculateHueWithinBounds() {
        // Test edge cases and a full sweep of hues and brightness
        float[] hues = {0f, 30f, 60f, 90f, 120f, 180f, 240f, 255f, 360f};
        int[] brightnessLevels = {0, 10, 25, 50, 75, 90, 100};

        for (float hue : hues) {
            for (int brightness : brightnessLevels) {
                Color c = assertDoesNotThrow(() -> ColorUtil.recalculateHue(hue, brightness),
                        "recalculateHue threw exception for hue=" + hue + " brightness=" + brightness);
                assertNotNull(c);
                assertTrue(c.getRed() >= 0 && c.getRed() <= 255, "Red out of bounds: " + c.getRed());
                assertTrue(c.getGreen() >= 0 && c.getGreen() <= 255, "Green out of bounds: " + c.getGreen());
                assertTrue(c.getBlue() >= 0 && c.getBlue() <= 255, "Blue out of bounds: " + c.getBlue());
            }
        }
    }

    @Test
    void testToMCColor() {
        Color red = new Color(255, 0, 0);
        TextColor mcRed = ColorUtil.toMCColor(red);
        assertNotNull(mcRed);
        assertEquals(0xFF0000, mcRed.getValue());

        Color blue = new Color(0, 0, 255);
        TextColor mcBlue = ColorUtil.toMCColor(blue);
        assertNotNull(mcBlue);
        assertEquals(0x0000FF, mcBlue.getValue());
    }

    @Test
    void testColorToInt() {
        Color green = new Color(0, 255, 0);
        int intColor = ColorUtil.colorToInt(green);
        assertEquals(0x00FF00, intColor);
    }

    @Test
    void testArgbPackingAndClamping() {
        assertEquals(0xFFFFFFFF, ColorUtil.argb(1.0F, 1.0F, 1.0F, 1.0F));
        assertEquals(0x00000000, ColorUtil.argb(0.0F, 0.0F, 0.0F, 0.0F));
        // Out-of-bounds values should clamp cleanly to [0..1]
        assertEquals(0xFF00FF00, ColorUtil.argb(1.5F, -0.5F, 2.0F, -1.0F));
    }

    @Test
    void testLerpArgb() {
        int black = 0x00000000;
        int white = 0xFFFFFFFF;
        assertEquals(black, ColorUtil.lerpArgb(black, white, -0.5F));
        assertEquals(white, ColorUtil.lerpArgb(black, white, 1.5F));

        int mid = ColorUtil.lerpArgb(0xFF000000, 0xFFFFFFFF, 0.5F);
        assertEquals(0xFF, (mid >>> 24) & 0xFF);
        assertEquals(128, (mid >>> 16) & 0xFF);
        assertEquals(128, (mid >>> 8) & 0xFF);
        assertEquals(128, mid & 0xFF);
    }

    @Test
    void testScaleAlpha() {
        int color = 0xFF112233;
        assertEquals(0x00112233, ColorUtil.scaleAlpha(color, 0.0F));
        assertEquals(0xFF112233, ColorUtil.scaleAlpha(color, 1.5F));
        int half = ColorUtil.scaleAlpha(0x80112233, 0.5F);
        assertEquals(64, (half >>> 24) & 0xFF);
        assertEquals(0x112233, half & 0x00FFFFFF);
    }
}

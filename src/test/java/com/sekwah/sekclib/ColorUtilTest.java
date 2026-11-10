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
}

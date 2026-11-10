package com.sekwah.sekclib.util;

import net.minecraft.network.chat.TextColor;
import java.awt.Color;

public class ColorUtil {

    /**
     * Useful for being paired with a user controlled color selector.
     * @param hue range 0-255
     * @param brightness range 0-100
     * @return resulting color
     */
    public static Color recalculateHue(float hue, int brightness) {
        double radiansHue = Math.toRadians(hue);
        // Note: 0.5D instead of 1/2 (which evaluated to 0 in integer division)
        // Clamp input to Math.sqrt to prevent NaN from negative cosine
        double cosR = Math.max(0.0D, Math.cos(radiansHue) + 0.5D);
        double cosG = Math.max(0.0D, Math.cos(radiansHue - (Math.PI + 1.0D) / 2.0D) + 0.5D);
        double cosB = Math.max(0.0D, Math.cos(radiansHue + (Math.PI + 1.0D) / 2.0D) + 0.5D);

        int red = (int) (Math.sqrt(cosR) * 255.0D);
        int green = (int) (Math.sqrt(cosG) * 255.0D);
        int blue = (int) (Math.sqrt(cosB) * 255.0D);

        if (brightness > 50) {
            float multiValue = (float) (brightness - 50) / 50f;
            red = (int) ((255 - red) * multiValue + red);
            green = (int) ((255 - green) * multiValue + green);
            blue = (int) ((255 - blue) * multiValue + blue);
        } else if (brightness < 50) {
            float multiValue = (float) brightness / 50f;
            red = (int) (red * multiValue);
            green = (int) (green * multiValue);
            blue = (int) (blue * multiValue);
        }

        // Clamp to valid 0-255 RGB range to prevent IllegalArgumentException
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new Color(red, green, blue);
    }

    /**
     * Convert java.awt.Color to Minecraft TextColor
     *
     * @param color java.awt.Color
     * @return minecraft TextColor
     */
    public static TextColor toMCColor(Color color) {
        return TextColor.parseColor(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
    }

    public static int colorToInt(Color color) {
        TextColor mcColor = toMCColor(color);
        return mcColor != null ? mcColor.getValue() : (color.getRGB() & 0xFFFFFF);
    }

}

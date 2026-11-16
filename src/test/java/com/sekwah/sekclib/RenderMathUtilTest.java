package com.sekwah.sekclib;

import com.sekwah.sekclib.util.RenderMathUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RenderMathUtilTest {

    @Test
    void testConstants() {
        assertEquals(15728880, RenderMathUtil.FULL_BRIGHT);
        assertEquals(12583104, RenderMathUtil.MIN_AMBIENT_LIGHT);
    }

    @Test
    void testClamp01() {
        assertEquals(0.0F, RenderMathUtil.clamp01(-1.0F));
        assertEquals(0.5F, RenderMathUtil.clamp01(0.5F));
        assertEquals(1.0F, RenderMathUtil.clamp01(2.5F));
    }

    @Test
    void testLerp() {
        assertEquals(10.0F, RenderMathUtil.lerp(10.0F, 20.0F, -0.2F), 1e-5F);
        assertEquals(15.0F, RenderMathUtil.lerp(10.0F, 20.0F, 0.5F), 1e-5F);
        assertEquals(20.0F, RenderMathUtil.lerp(10.0F, 20.0F, 1.5F), 1e-5F);
    }

    @Test
    void testFadeOut() {
        assertEquals(1.0F, RenderMathUtil.fadeOut(0.0F, 0.82F), 1e-5F);
        assertEquals(1.0F, RenderMathUtil.fadeOut(0.82F, 0.82F), 1e-5F);
        assertEquals(0.5F, RenderMathUtil.fadeOut(0.91F, 0.82F), 1e-5F);
        assertEquals(0.0F, RenderMathUtil.fadeOut(1.0F, 0.82F), 1e-5F);
        assertEquals(0.0F, RenderMathUtil.fadeOut(1.5F, 0.82F), 1e-5F);
    }

    @Test
    void testClampMinLight() {
        assertEquals(RenderMathUtil.MIN_AMBIENT_LIGHT, RenderMathUtil.clampMinLight(0, RenderMathUtil.MIN_AMBIENT_LIGHT));
        assertEquals(RenderMathUtil.FULL_BRIGHT, RenderMathUtil.clampMinLight(RenderMathUtil.FULL_BRIGHT, RenderMathUtil.MIN_AMBIENT_LIGHT));
    }
}

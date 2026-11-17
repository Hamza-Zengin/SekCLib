package com.sekwah.sekclib;

import com.sekwah.sekclib.util.ModelUtils;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelUtilsTest {

    @Test
    void testCreateBlankHumanoidMeshContainsAllRequiredParts() {
        MeshDefinition mesh = ModelUtils.createBlankHumanoidMesh();
        assertNotNull(mesh);

        PartDefinition root = mesh.getRoot();
        assertNotNull(root.getChild("head"));
        assertNotNull(root.getChild("head").getChild("hat"));
        assertNotNull(root.getChild("body"));
        assertNotNull(root.getChild("right_arm"));
        assertNotNull(root.getChild("left_arm"));
        assertNotNull(root.getChild("right_leg"));
        assertNotNull(root.getChild("left_leg"));
    }
}

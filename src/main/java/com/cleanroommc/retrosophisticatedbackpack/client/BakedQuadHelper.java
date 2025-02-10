package com.cleanroommc.retrosophisticatedbackpack.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

public class BakedQuadHelper {
    public static List<BakedQuad> rotateQuadsCentered(List<BakedQuad> originalQuads, EnumFacing facing) {
        List<BakedQuad> rotatedQuads = new ArrayList<>();

        // Calculate model center
        float centerX = calculateCenter(originalQuads, 0);
        float centerY = calculateCenter(originalQuads, 1);
        float centerZ = calculateCenter(originalQuads, 2);

        // Rotation matrix setup
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.setIdentity();

        // Determine rotation based on facing
        switch (facing) {
            case SOUTH:
                rotationMatrix.setRotation(new javax.vecmath.AxisAngle4f(0, 1, 0, (float) Math.PI));
                break;
            case EAST:
                rotationMatrix.setRotation(new javax.vecmath.AxisAngle4f(0, 1, 0, -(float) Math.PI / 2));
                break;
            case WEST:
                rotationMatrix.setRotation(new javax.vecmath.AxisAngle4f(0, 1, 0, (float) Math.PI / 2));
                break;
            default:
                return originalQuads;
        }

        for (BakedQuad quad : originalQuads) {
            int[] vertexData = quad.getVertexData().clone();
            VertexFormat format = quad.getFormat();
            int stride = format.getIntegerSize();

            for (int i = 0; i < 4; i++) {
                // Extract vertex position
                float x = Float.intBitsToFloat(vertexData[i * stride]) - centerX;
                float y = Float.intBitsToFloat(vertexData[i * stride + 1]) - centerY;
                float z = Float.intBitsToFloat(vertexData[i * stride + 2]) - centerZ;

                // Apply rotation
                Vector4f vertex = new Vector4f(x, y, z, 1.0f);
                rotationMatrix.transform(vertex);

                // Translate back and update vertex data
                vertexData[i * stride] = Float.floatToIntBits(vertex.x + centerX);
                vertexData[i * stride + 1] = Float.floatToIntBits(vertex.y + centerY);
                vertexData[i * stride + 2] = Float.floatToIntBits(vertex.z + centerZ);
            }

            // Create new rotated quad
            rotatedQuads.add(new BakedQuad(vertexData, quad.getTintIndex(),
                    facing, quad.getSprite(),
                    quad.shouldApplyDiffuseLighting(), format));
        }

        return rotatedQuads;
    }

    private static float calculateCenter(List<BakedQuad> quads, int coordinate) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (BakedQuad quad : quads) {
            int[] vertexData = quad.getVertexData();
            int stride = quad.getFormat().getIntegerSize();

            for (int i = 0; i < 4; i++) {
                float value = Float.intBitsToFloat(vertexData[i * stride + coordinate]);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }

        return (min + max) / 2;
    }
}

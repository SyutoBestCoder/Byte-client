package com.syuto.bytes.utils.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.StructureSpawns;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;

import java.awt.*;

import static com.syuto.bytes.Byte.mc;

public class RenderUtils {
    private static final Color black = Color.black;
    private static final Color gray = Color.darkGray;
    private static final Color green = Color.green;
    private static final Color yellow = Color.yellow;
    private static final Color red = Color.red;
    private static final Color orange = Color.orange;
    private static final Color white = Color.white;

    public static final int[] lastViewport = new int[4];
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();


    public static void renderBlock(BlockPos pos, RenderWorldEvent event, Color color) {
        Box box = new Box(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        float cameraX = (float) mc.gameRenderer.getCamera().getPos().x;
        float cameraY = (float) mc.gameRenderer.getCamera().getPos().y;
        float cameraZ = (float) mc.gameRenderer.getCamera().getPos().z;

        float minX = (float) box.minX - cameraX;
        float maxX = (float) box.maxX - cameraX;
        float minY = (float) box.minY - cameraY;
        float maxY = (float) box.maxY - cameraY;
        float minZ = (float) box.minZ - cameraZ;
        float maxZ = (float) box.maxZ - cameraZ;

        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();

        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), a = 25;

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

        postRender(vb, matrixStack);
    }

    public static void renderBox(Vec3d pos, Vec3d l, Entity entity, RenderWorldEvent event, float delta) {
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        Box box = new Box(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).offset(-camPos.x, -camPos.y, -camPos.z);

        float minX = (float) (box.minX - pos.getX()) - 0.12f;
        float maxX = (float) (box.maxX - pos.getX()) + 0.12f;
        float minY = (float) (box.minY - pos.getY()) - 0.12f;
        float maxY = (float) (box.maxY - pos.getY()) + 0.12f;
        float minZ = (float) (box.minZ - pos.getZ()) - 0.12f;
        float maxZ = (float) (box.maxZ - pos.getZ()) + 0.12f;


        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();
        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        matrixStack.translate(pos.x, pos.y, pos.z);

        int r = 255, g = 255, b = 255, a = 75;

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

        postRender(vb, matrixStack);
    }


    public static void renderBox(Entity e, RenderWorldEvent event, float delta) {
        float interpolatedX = (float) (e.lastRenderX + (e.getX() - e.lastRenderX) * delta - mc.gameRenderer.getCamera().getPos().x);
        float interpolatedY = (float) (e.lastRenderY + (e.getY() - e.lastRenderY) * delta - mc.gameRenderer.getCamera().getPos().y);
        float interpolatedZ = (float) (e.lastRenderZ + (e.getZ() - e.lastRenderZ) * delta - mc.gameRenderer.getCamera().getPos().z);

        Box box = e.getBoundingBox();

        float minX = (float) (box.minX - e.getX()) - 0.12f;
        float maxX = (float) (box.maxX - e.getX()) + 0.12f;
        float minY = (float) (box.minY - e.getY()) - 0.12f;
        float maxY = (float) (box.maxY - e.getY()) + 0.12f;
        float minZ = (float) (box.minZ - e.getZ()) - 0.12f;
        float maxZ = (float) (box.maxZ - e.getZ()) + 0.12f;

        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();
        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        matrixStack.translate(interpolatedX, interpolatedY, interpolatedZ);

        int r = 255, g = 255, b = 255, a = 75;

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);


        postRender(vb, matrixStack);
    }

    public static void renderp(Vec3d from, Vec3d to, RenderWorldEvent event, float delta) {
        MatrixStack matrixStack = event.matrixStack;
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        double camX = mc.gameRenderer.getCamera().getPos().x;
        double camY = mc.gameRenderer.getCamera().getPos().y;
        double camZ = mc.gameRenderer.getCamera().getPos().z;

        // Reference: player's bounding box (for hitbox size)
        Box box = mc.player.getBoundingBox();

        // These represent how far the hitbox extends relative to the player's position
        float offMinX = (float) (box.minX - mc.player.getX()) - 0.12f;
        float offMaxX = (float) (box.maxX - mc.player.getX()) + 0.12f;
        float offMinY = (float) (box.minY - mc.player.getY()) - 0.12f;
        float offMaxY = (float) (box.maxY - mc.player.getY()) + 0.12f;
        float offMinZ = (float) (box.minZ - mc.player.getZ()) - 0.12f;
        float offMaxZ = (float) (box.maxZ - mc.player.getZ()) + 0.12f;

        // Use the interpolated 'to' position (camera space)
        float x = (float) (to.x - camX);
        float y = (float) (to.y - camY);
        float z = (float) (to.z - camZ);

        // Apply the player's hitbox offsets to the interpolated position
        float minX = x + offMinX;
        float maxX = x + offMaxX;
        float minY = y + offMinY;
        float maxY = y + offMaxY;
        float minZ = z + offMinZ;
        float maxZ = z + offMaxZ;

        BufferBuilder vb = getBufferBuilder(event.matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();

        int r = 255, g = 255, b = 255, a = 75;

        // Bottom face
        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

        // Top face
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        // Sides
        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        vb.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        vb.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        vb.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

        postRender(vb, matrixStack);
    }



    public static void renderHealth(Entity e, RenderWorldEvent event, float currentHealth, float maxHealth, float targetHealthRatio, float delta) {
        float h = (currentHealth / maxHealth);
        int barHeight = (int) (74.0D * h);
        Color healthColor = h < 0.3D ? red : (h < 0.5D ? orange : (h < 0.7D ? yellow : green));

        float x = (float) (e.lastRenderX + (e.getX() - e.lastRenderX) * delta - mc.gameRenderer.getCamera().getPos().x);
        float y = (float) (e.lastRenderY + (e.getY() - e.lastRenderY) * delta - mc.gameRenderer.getCamera().getPos().y);
        float z = (float) (e.lastRenderZ + (e.getZ() - e.lastRenderZ) * delta - mc.gameRenderer.getCamera().getPos().z);

        MatrixStack matrixStack = event.matrixStack;

        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();

        matrixStack.translate(x, y - 0.2D, z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.getCameraEntity().getYaw()));
        matrixStack.scale(0.03F, 0.03F, 0.03F);

        int barX = 21;
        int barWidth = 4;
        int fullBarHeight = 75;

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, barX, -1, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth, -1, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth, fullBarHeight, 0).color(black.getRGB());
        bufferBuilder.vertex(matrix, barX, fullBarHeight, 0).color(black.getRGB());

        bufferBuilder.vertex(matrix, barX + 1, (float) barHeight, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, (float) barHeight, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, fullBarHeight - 1, 0).color(gray.getRGB());
        bufferBuilder.vertex(matrix, barX + 1, fullBarHeight - 1, 0).color(gray.getRGB());

        bufferBuilder.vertex(matrix, barX + 1, 0, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, 0, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + barWidth - 1, (float) barHeight, 0).color(healthColor.getRGB());
        bufferBuilder.vertex(matrix, barX + 1, (float) barHeight, 0).color(healthColor.getRGB());
        postRender(bufferBuilder, event.matrixStack);
    }

    public static void drawText(DrawContext context, String text, float x, float y, int color) {
        MatrixStack matrixStack = context.getMatrices();

        matrixStack.push();

        context.drawText(
                mc.textRenderer,
                text,
                (int) x,
                (int) y,
                color,
                true

        );

        matrixStack.pop();

    }

    public static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, float width, int color) {
        MatrixStack matrix = context.getMatrices();
        BufferBuilder buffer = getBufferBuilder(matrix, VertexFormat.DrawMode.QUADS);
        preRender();

        Matrix4f pos = matrix.peek().getPositionMatrix();
        float dx = x2 - x1;
        float dy = y2 - y1;

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        dx /= len;
        dy /= len;

        float px = -dy * width / 2;
        float py = dx * width / 2;

        buffer.vertex(pos, x1 + px, y1 + py, 0).color(color);
        buffer.vertex(pos, x2 + px, y2 + py, 0).color(color);
        buffer.vertex(pos, x2 - px, y2 - py, 0).color(color);
        buffer.vertex(pos, x1 - px, y1 - py, 0).color(color);

        postRender(buffer, matrix);
    }

    public static void drawRect(MatrixStack matrixStack, float left, float right, float top, float bottom, int color) {
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, top, 0.0f).color(color);
        bufferBuilder.vertex(matrix, left, top, 0.0f).color(color);

        postRender(bufferBuilder, matrixStack);
    }

    public static void drawRect(DrawContext event, float x, float y, float width, float height, int color) {
        float left = x - width / 2;
        float right = x + width / 2;
        float top = y - height / 2;
        float bottom = y + height / 2;

        MatrixStack matrixStack = event.getMatrices();
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.QUADS);
        preRender();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, top, 0.0f).color(color);
        bufferBuilder.vertex(matrix, left, top, 0.0f).color(color);

        postRender(bufferBuilder, matrixStack);
    }

    public static void drawRectOutline(MatrixStack matrixStack, float left, float right, float top, float bottom, int color) {
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.DEBUG_LINE_STRIP);
        preRender();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        bufferBuilder.vertex(matrix, left, top, 0.0f).color(color);
        bufferBuilder.vertex(matrix, left, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, bottom, 0.0f).color(color);
        bufferBuilder.vertex(matrix, right, top, 0.0f).color(color);

        bufferBuilder.vertex(matrix, left, top, 0.0f).color(color);


        postRender(bufferBuilder, matrixStack);
    }


    public static void drawLine(MatrixStack matrixStack, Vec3d start, Vec3d end, int color) {
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        start = start.subtract(camPos);
        end = end.subtract(camPos);

        preRender();
        BufferBuilder bufferBuilder = getBufferBuilder(matrixStack, VertexFormat.DrawMode.DEBUG_LINE_STRIP);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        RenderSystem.lineWidth(10);
        bufferBuilder.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(color);
        bufferBuilder.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(color);

        postRender(bufferBuilder, matrixStack);
    }


    public static void drawRectOutline2(MatrixStack matrixStack, float x, float y, float width, float height, int color) {
        float delta = mc.getRenderTickCounter().getTickDelta(true);

        float xx = (float) (mc.player.lastRenderX + (mc.player.getX() - mc.player.lastRenderX) * delta - mc.gameRenderer.getCamera().getPos().x);
        float yy = (float) (mc.player.lastRenderY + (mc.player.getY() - mc.player.lastRenderY) * delta - mc.gameRenderer.getCamera().getPos().y) + 2.2f;
        float zz = (float) (mc.player.lastRenderZ + (mc.player.getZ() - mc.player.lastRenderZ) * delta - mc.gameRenderer.getCamera().getPos().z);

        matrixStack.push();
        matrixStack.translate(xx, yy, zz);

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.getCameraEntity().getYaw()));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.getCameraEntity().getPitch()));

        float scale = 0.02f;
        matrixStack.scale(-scale, -scale, scale);

        TextRenderer textRenderer = mc.textRenderer;
        var text = mc.player.getName();


        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;

        int padding = 2;
        int left = -textWidth / 2 - padding;
        int right = textWidth / 2 + padding;
        int top = -padding;
        int bottom = textHeight + padding;

        RenderUtils.drawRect(matrixStack, left, right, top, bottom, new Color(0,0,0, 3).getRGB());

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        textRenderer.draw(
                text,
                -textWidth / 2f,
                0,
                Color.red.getRGB(),
                false,
                matrixStack.peek().getPositionMatrix(),
                mc.getBufferBuilders().getEntityVertexConsumers(),
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                15728880
        );

        matrixStack.pop();
        mc.getBufferBuilders().getEntityVertexConsumers().draw();


        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }




    public static Vec3d worldToScreen(Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;


        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0f).mul(lastWorldSpaceMatrix);

        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), lastViewport, target);

        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public static void preRender() {
        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);
        GL46.glDisable(GL46.GL_CULL_FACE);
        GL46.glDisable(GL46.GL_DEPTH_TEST);
    }

    public static BufferBuilder getBufferBuilder(MatrixStack matrixStack, VertexFormat.DrawMode drawMode) {
        matrixStack.push();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        return tessellator.begin(drawMode, VertexFormats.POSITION_COLOR);
    }

    public static void postRender(BufferBuilder bufferBuilder, MatrixStack matrixStack) {
        matrixStack.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.setShaderColor(1,1,1,1);

        GL46.glDisable(GL46.GL_BLEND);
        GL46.glEnable(GL46.GL_CULL_FACE);
        GL46.glEnable(GL46.GL_DEPTH_TEST);
    }
}

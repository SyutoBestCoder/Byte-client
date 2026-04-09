package com.syuto.bytes.utils.impl.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.awt.*;

import static com.syuto.bytes.Byte.mc;

/**
 * RenderUtils — Fabric Yarn 1.21.11
 *
 * In 1.21.11 the rendering API is:
 *  - RenderLayer.of(String name, RenderSetup renderSetup)
 *  - RenderSetup is built via RenderSetup.builder().pipeline(...).build()
 *  - Vertices are written with BufferBuilder (from ByteBufferBuilder allocator)
 *    using buffer.vertex(matrix, x, y, z).color(r, g, b, a)
 *  - Drawing is done via RenderLayer.draw(BuiltBuffer) during the draw phase
 *  - Tessellator / begin() / .color() chaining are GONE
 */
public class RenderUtils {

    // -----------------------------------------------------------------------
    // RenderPipeline definitions
    // -----------------------------------------------------------------------

    /** Filled quads — no depth test, no cull. ESP boxes / block fills. */
    private static final RenderPipeline PIPELINE_QUADS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/bytes_quads")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .build()
    );

    /** Line strip — no depth test, no cull. Outlines and 3-D lines. */
    private static final RenderPipeline PIPELINE_LINES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/bytes_lines")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .build()
    );

    // -----------------------------------------------------------------------
    // RenderLayer definitions — RenderLayer.of(name, RenderSetup)
    // -----------------------------------------------------------------------

    private static final RenderType LAYER_QUADS = RenderType.create(
            "bytes_quads",
            RenderSetup.builder(PIPELINE_QUADS).createRenderSetup()
    );

    private static final RenderType LAYER_LINES = RenderType.create(
            "bytes_lines",
            RenderSetup.builder(PIPELINE_LINES).createRenderSetup()
    );

    // -----------------------------------------------------------------------
    // Per-frame ByteBufferBuilder allocators (reused across calls)
    // -----------------------------------------------------------------------

    private static final ByteBufferBuilder ALLOC_QUADS = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);
    private static final ByteBufferBuilder ALLOC_LINES = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);

    // -----------------------------------------------------------------------
    // Colors
    // -----------------------------------------------------------------------

    private static final Color BLACK  = Color.BLACK;
    private static final Color GRAY   = Color.DARK_GRAY;
    private static final Color GREEN  = Color.GREEN;
    private static final Color YELLOW = Color.YELLOW;
    private static final Color RED    = Color.RED;
    private static final Color ORANGE = Color.ORANGE;

    // -----------------------------------------------------------------------
    // World-space matrix state (populated by your mixin each frame)
    // -----------------------------------------------------------------------

    public static final int[]    lastViewport         = new int[4];
    public static final Matrix4f lastProjMat          = new Matrix4f();
    public static final Matrix4f lastModMat           = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    // -----------------------------------------------------------------------
    // Internal helpers — begin a fresh BufferBuilder for a given pipeline
    // -----------------------------------------------------------------------

    private static BufferBuilder beginQuads() {
        return new BufferBuilder(ALLOC_QUADS,
                PIPELINE_QUADS.getVertexFormatMode(),
                PIPELINE_QUADS.getVertexFormat());
    }

    private static BufferBuilder beginLines() {
        return new BufferBuilder(ALLOC_LINES,
                PIPELINE_LINES.getVertexFormatMode(),
                PIPELINE_LINES.getVertexFormat());
    }

    // -----------------------------------------------------------------------
    // World rendering — block highlight
    // -----------------------------------------------------------------------

    public static void renderBlock(BlockPos pos, RenderWorldEvent event, Color color) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float minX = (float)(pos.getX() - camX);
        float maxX = minX + 1f;
        float minY = (float)(pos.getY() - camY);
        float maxY = minY + 1f;
        float minZ = (float)(pos.getZ() - camZ);
        float maxZ = minZ + 1f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, minX, maxX, minY, maxY, minZ, maxZ,
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 25 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    // -----------------------------------------------------------------------
    // World rendering — entity / position boxes
    // -----------------------------------------------------------------------

    public static void renderBox(Vec3 pos, Vec3 l, Entity entity, RenderWorldEvent event, float delta) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        AABB box = entity.getBoundingBox();
        float offMinX = (float)(box.minX - entity.getX()) - 0.12f;
        float offMaxX = (float)(box.maxX - entity.getX()) + 0.12f;
        float offMinY = (float)(box.minY - entity.getY()) - 0.12f;
        float offMaxY = (float)(box.maxY - entity.getY()) + 0.12f;
        float offMinZ = (float)(box.minZ - entity.getZ()) - 0.12f;
        float offMaxZ = (float)(box.maxZ - entity.getZ()) + 0.12f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(pos.x - camX, pos.y - camY, pos.z - camZ);
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, offMinX, offMaxX, offMinY, offMaxY, offMinZ, offMaxZ, 1f, 1f, 1f, 75 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void renderBox(Entity e, RenderWorldEvent event, float delta) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float ix = (float)(e.xOld + (e.getX() - e.xOld) * delta - camX);
        float iy = (float)(e.yOld + (e.getY() - e.yOld) * delta - camY);
        float iz = (float)(e.zOld + (e.getZ() - e.zOld) * delta - camZ);

        AABB box = e.getBoundingBox();
        float minX = (float)(box.minX - e.getX()) - 0.12f;
        float maxX = (float)(box.maxX - e.getX()) + 0.12f;
        float minY = (float)(box.minY - e.getY()) - 0.12f;
        float maxY = (float)(box.maxY - e.getY()) + 0.12f;
        float minZ = (float)(box.minZ - e.getZ()) - 0.12f;
        float maxZ = (float)(box.maxZ - e.getZ()) + 0.12f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(ix, iy, iz);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, minX, maxX, minY, maxY, minZ, maxZ, 1f, 1f, 1f, 75 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void renderBoxOutlineAtPos(double x, double y, double z, RenderWorldEvent event) {
        // Define the bounding box size (standard Minecraft player-sized)
        float width = 0.6f; // X and Z size
        float height = 1.8f; // Y size

        float minX = (float)(-width / 2);
        float maxX = (float)(width / 2);
        float minY = 0f;
        float maxY = height;
        float minZ = (float)(-width / 2);
        float maxZ = (float)(width / 2);

        // Get camera position
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        // Relative position to camera
        float rx = (float)(x - camX);
        float ry = (float)(y - camY);
        float rz = (float)(z - camZ);

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(rx, ry, rz);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = beginLines(); // Use lines for outline only
        drawBoxOutline(vb, mat, minX, maxX, minY, maxY, minZ, maxZ, 1f, 1f, 1f, 1f); // white RGBA
        LAYER_LINES.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void renderp(Vec3 trackedPos, Entity target, RenderWorldEvent event, float delta) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        AABB box = target.getBoundingBox();
        float offMinX = (float)(box.minX - target.getX()) - 0.12f;
        float offMaxX = (float)(box.maxX - target.getX()) + 0.12f;
        float offMinY = (float)(box.minY - target.getY()) - 0.12f;
        float offMaxY = (float)(box.maxY - target.getY()) + 0.12f;
        float offMinZ = (float)(box.minZ - target.getZ()) - 0.12f;
        float offMaxZ = (float)(box.maxZ - target.getZ()) + 0.12f;

        float rx = (float)(trackedPos.x - camX);
        float ry = (float)(trackedPos.y - camY);
        float rz = (float)(trackedPos.z - camZ);

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(rx, ry, rz);
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, offMinX, offMaxX, offMinY, offMaxY, offMinZ, offMaxZ, 1f, 1f, 1f, 75 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    // -----------------------------------------------------------------------
    // World rendering — health bar billboard
    // -----------------------------------------------------------------------

    public static void renderHealth(Entity e, RenderWorldEvent event,
                                    float currentHealth, float maxHealth,
                                    float targetHealthRatio, float delta) {
        float h = currentHealth / maxHealth;
        int barHeight = (int)(74f * h);
        Color healthColor = h < 0.3f ? RED : (h < 0.5f ? ORANGE : (h < 0.7f ? YELLOW : GREEN));

        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        Vec3 pos = e.getPosition(event.partialTicks);

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(pos.x - cam.x, pos.y - cam.y - 0.2f, pos.z - cam.z);
        ms.mulPose(Axis.YP.rotationDegrees(-mc.getCameraEntity().getYRot()));
        ms.scale(0.03f, 0.03f, 0.03f);

        Matrix4f matrix = ms.last().pose();

        int barX          = 21;
        int barWidth      = 4;
        int fullBarHeight = 75;

        BufferBuilder vb = beginQuads();

        // Background — black
        addQuad(vb, matrix, barX, -1, barX + barWidth, fullBarHeight,
                BLACK.getRed() / 255f, BLACK.getGreen() / 255f, BLACK.getBlue() / 255f, 1f);

        // Empty portion — dark gray
        addQuad(vb, matrix, barX + 1, barHeight, barX + barWidth - 1, fullBarHeight - 1,
                GRAY.getRed() / 255f, GRAY.getGreen() / 255f, GRAY.getBlue() / 255f, 1f);

        // Health fill — color-coded
        addQuad(vb, matrix, barX + 1, 0, barX + barWidth - 1, barHeight,
                healthColor.getRed() / 255f, healthColor.getGreen() / 255f, healthColor.getBlue() / 255f, 1f);

        LAYER_QUADS.draw(vb.buildOrThrow());
        ms.popPose();
    }

    // -----------------------------------------------------------------------
    // HUD helpers — DrawContext
    // -----------------------------------------------------------------------

    public static void drawText(GuiGraphics context, String text, float x, float y, int color) {
        context.drawString(mc.font, text, (int) x, (int) y, color, true);
    }

    /** Filled HUD rect — safe with Matrix3x2fStack via context.fill(). */
    public static void drawRect(GuiGraphics context, float x, float y, float width, float height, int color) {
        context.fill(
                (int)(x - width  / 2f), (int)(y - height / 2f),
                (int)(x + width  / 2f), (int)(y + height / 2f),
                color
        );
    }

    /** Filled rect from a 3-D world-space MatrixStack. */
    public static void drawRect(PoseStack matrixStack, float left, float right,
                                float top, float bottom, int color) {
        Matrix4f mat = matrixStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color         & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        BufferBuilder vb = beginQuads();
        vb.addVertex(mat, left,  bottom, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, right, bottom, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, right, top,    0f).setColor(r, g, b, a);
        vb.addVertex(mat, left,  top,    0f).setColor(r, g, b, a);
        LAYER_QUADS.draw(vb.buildOrThrow());
    }

    public static void drawRectOutline(PoseStack matrixStack, float left, float right,
                                       float top, float bottom, int color) {
        Matrix4f mat = matrixStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color         & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        BufferBuilder vb = beginLines();
        vb.addVertex(mat, left,  top,    0f).setColor(r, g, b, a);
        vb.addVertex(mat, left,  bottom, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, right, bottom, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, right, top,    0f).setColor(r, g, b, a);
        vb.addVertex(mat, left,  top,    0f).setColor(r, g, b, a);
        LAYER_LINES.draw(vb.buildOrThrow());
    }

    /** 3-D world-space line between two points. */
    public static void drawLine(PoseStack matrixStack, Vec3 start, Vec3 end, int color) {
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        Vec3 s = start.subtract(cam);
        Vec3 e = end.subtract(cam);

        Matrix4f mat = matrixStack.last().pose();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float bl = (color        & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        BufferBuilder vb = beginLines();
        vb.addVertex(mat, (float) s.x, (float) s.y, (float) s.z).setColor(r, g, bl, a);
        vb.addVertex(mat, (float) e.x, (float) e.y, (float) e.z).setColor(r, g, bl, a);
        LAYER_LINES.draw(vb.buildOrThrow());
    }

    /** Renders the local player's name tag as a 3-D billboard above their head. */
    public static void drawRectOutline2(PoseStack matrixStack,
                                        float x, float y, float width, float height,
                                        int color) {
        float delta = mc.getDeltaTracker().getGameTimeDeltaTicks();

        float xx = (float)(mc.player.xOld + (mc.player.getX() - mc.player.xOld) * delta
                - mc.gameRenderer.getMainCamera().position().x);
        float yy = (float)(mc.player.yOld + (mc.player.getY() - mc.player.yOld) * delta
                - mc.gameRenderer.getMainCamera().position().y) + 2.2f;
        float zz = (float)(mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * delta
                - mc.gameRenderer.getMainCamera().position().z);

        matrixStack.pushPose();
        matrixStack.translate(xx, yy, zz);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-mc.getCameraEntity().getYRot()));
        matrixStack.mulPose(Axis.XP.rotationDegrees(mc.getCameraEntity().getXRot()));
        matrixStack.scale(-0.02f, -0.02f, 0.02f);

        Font tr = mc.font;
        var text = mc.player.getName();
        int tw      = tr.width(text);
        int padding = 2;

        RenderUtils.drawRect(matrixStack,
                -tw / 2f - padding, tw / 2f + padding,
                -padding,           tr.lineHeight + padding,
                new Color(0, 0, 0, 3).getRGB());

        GlStateManager._disableCull();
        GlStateManager._disableDepthTest();

        tr.drawInBatch(
                text, -tw / 2f, 0f, RED.getRGB(), false,
                matrixStack.last().pose(),
                mc.renderBuffers().bufferSource(),
                Font.DisplayMode.SEE_THROUGH,
                0, 15728880
        );

        mc.renderBuffers().bufferSource().endBatch();
        matrixStack.popPose();

        GlStateManager._enableCull();
        GlStateManager._enableDepthTest();
    }

    // -----------------------------------------------------------------------
    // World-to-screen projection
    // -----------------------------------------------------------------------

    public static Vec3 worldToScreen(Vec3 pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getScreenHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.position().x;
        double deltaY = pos.y - camera.position().y;
        double deltaZ = pos.z - camera.position().z;

        Vector4f transformed = new Vector4f(
                (float) deltaX, (float) deltaY, (float) deltaZ, 1f
        ).mul(lastWorldSpaceMatrix);

        new Matrix4f(lastProjMat).mul(new Matrix4f(lastModMat)).project(
                transformed.x(), transformed.y(), transformed.z(),
                lastViewport, target
        );

        return new Vec3(
                target.x / mc.getWindow().getGuiScale(),
                (displayHeight - target.y) / mc.getWindow().getGuiScale(),
                target.z
        );
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Adds a single Z-flat quad (used for the health bar). */
    private static void addQuad(BufferBuilder vb, Matrix4f mat,
                                float x1, float y1, float x2, float y2,
                                float r, float g, float b, float a) {
        vb.addVertex(mat, x1, y1, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, x2, y1, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, x2, y2, 0f).setColor(r, g, b, a);
        vb.addVertex(mat, x1, y2, 0f).setColor(r, g, b, a);
    }

    /** Fills all 6 faces of an AABB. */
    private static void fillBox(BufferBuilder vb, Matrix4f mat,
                                float minX, float maxX,
                                float minY, float maxY,
                                float minZ, float maxZ,
                                float r, float g, float b, float a) {
        // Bottom
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        // Top
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        // North (z-)
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        // South (z+)
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        // West (x-)
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        // East (x+)
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
    }

    private static void drawBoxOutline(BufferBuilder vb, Matrix4f mat,
                                       float minX, float maxX,
                                       float minY, float maxY,
                                       float minZ, float maxZ,
                                       float r, float g, float b, float a) {
        // Bottom edges
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);

        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);

        // Top face edges
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);

        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);

        // Vertical edges
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);

        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);

        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
    }
}
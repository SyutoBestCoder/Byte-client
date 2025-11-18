package com.syuto.bytes.utils.impl.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.syuto.bytes.Byte.mc;

public class Snow {

    private static final Color snowflakeColor = Color.WHITE;
    private static final List<Snowflake> snowflakes = new ArrayList<>();
    private static final Random random = new Random();
    private static Identifier imageIdentifier = Identifier.of("byte", "background/snow.png");



    private static class Snowflake {
        float x, y, size, speed;

        public Snowflake(float x, float y, float size, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }

        public void fall() {
            this.y += this.speed;
            if (this.y > MinecraftClient.getInstance().getWindow().getScaledHeight()) {
                this.y = -this.size;
                this.x = random.nextInt(MinecraftClient.getInstance().getWindow().getScaledWidth());
            }
        }
    }

    public static void renderSnowflakes(DrawContext event) {
        MatrixStack matrices = event.getMatrices();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        matrices.push();

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        if (snowflakes.isEmpty()) {
            for (int i = 0; i < 75; i++) {
                float x = random.nextInt(width);
                float y = random.nextInt(height);
                float size = 10 + random.nextFloat() * 3;
                float speed = 1 + random.nextFloat() * 2;
                snowflakes.add(new Snowflake(x, y, size, speed));
            }
        }

        for (Snowflake snowflake : snowflakes) {
            snowflake.fall();


            event.drawTexture(
                    RenderLayer::getGuiTextured,
                    imageIdentifier,
                    (int) snowflake.x, (int) snowflake.y,
                    0, 0,
                    (int) snowflake.size, (int) snowflake.size,
                    (int) snowflake.size, (int) snowflake.size
            );
        }
        matrices.pop();
    }

}

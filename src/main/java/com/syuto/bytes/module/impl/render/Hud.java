package com.syuto.bytes.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.AttributeSwap;
import com.syuto.bytes.setting.impl.ColorSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import dev.blend.ThemeHandler;
import dev.blend.util.render.Alignment;
import dev.blend.util.render.DrawUtil;
import dev.blend.util.render.Gradient;
import dev.blend.util.render.ResourceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.ChatFormatting;

import static com.syuto.bytes.Byte.mc;

public class Hud extends Module {

    public final ColorSetting primary = new ColorSetting("Color", this, new Color(255, 0, 255));
    private final HashMap<String, Double> xOffsets = new HashMap<>();
    private final HashMap<String, Double> heights = new HashMap<>();
    public static HashMap<String, String> modules = new HashMap<>();

    public Hud() {
        super("Hud", "hud bro", Category.RENDER);
    }

    @EventHandler
    public void onRenderTick(RenderTickEvent event) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        List<Module> allModules = ModuleManager.modules.stream()
                .filter(mod -> mod.isEnabled() || xOffsets.getOrDefault(mod.getName(), -300.0) > -290)
                .sorted((m1, m2) -> {
                    double width1 = DrawUtil.getStringWidth(m1.getName(), 12, ResourceManager.FontResources.regular)
                            + (m1.getSuffix().isEmpty() ? 0 : DrawUtil.getStringWidth(" " + m1.getSuffix(), 12, ResourceManager.FontResources.regular));
                    double width2 = DrawUtil.getStringWidth(m2.getName(), 12, ResourceManager.FontResources.regular)
                            + (m2.getSuffix().isEmpty() ? 0 : DrawUtil.getStringWidth(" " + m2.getSuffix(), 12, ResourceManager.FontResources.regular));
                    return Double.compare(width2, width1);
                })
                .toList();

        double yPosition = 1;

        for (Module mod : allModules) {
            String moduleName = mod.getName();
            String suffix = mod.getSuffix();

            double nameWidth = DrawUtil.getStringWidth(moduleName, 12, ResourceManager.FontResources.regular);
            double suffixWidth = suffix.isEmpty() ? 0 : DrawUtil.getStringWidth("" + suffix, 12, ResourceManager.FontResources.regular);
            double totalWidth = nameWidth + suffixWidth;

            // x: slide in from right (positive = on screen, negative = off screen)
            double xTarget = mod.isEnabled() ? 0 : -(totalWidth + 10);
            double xCurrent = xOffsets.getOrDefault(moduleName, (totalWidth + 10)); // always start off-screen if unseen
            xCurrent -= (xTarget + xCurrent) * 0.2;
            xOffsets.put(moduleName, xCurrent);

            // height: animate from 0 to 13 for smooth y push
            double heightTarget = mod.isEnabled() ? 13 : 0;
            double heightCurrent = heights.getOrDefault(moduleName, 0.0); // always start collapsed
            heightCurrent += (heightTarget - heightCurrent) * 0.2;
            heights.put(moduleName, heightCurrent);

            double xPosition = screenWidth - 2 + xCurrent;

            // fade alpha based on x progress
            float alpha = (float) Math.min(1.0, (xCurrent + totalWidth + 10) / (totalWidth + 10));
            Color moduleColor = applyAlpha(primary.getValue(), alpha);
            Color suffixColor = applyAlpha(Color.GRAY, alpha);

            DrawUtil.begin();
            if (!suffix.isEmpty()) {
                DrawUtil.drawString(" " + suffix, xPosition, yPosition, 12, suffixColor, Alignment.TOP_RIGHT, ResourceManager.FontResources.regular);
                DrawUtil.drawString(moduleName, xPosition - suffixWidth, yPosition, 12, moduleColor, Alignment.TOP_RIGHT, ResourceManager.FontResources.regular);
            } else {
                DrawUtil.drawString(moduleName, xPosition, yPosition, 12, moduleColor, Alignment.TOP_RIGHT, ResourceManager.FontResources.regular);
            }
            DrawUtil.end();

            yPosition += heightCurrent;
        }
    }

    private Color applyAlpha(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * Math.max(0, Math.min(1, alpha))));
    }
}


package com.syuto.bytes.module.impl.render;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ColorSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import dev.blend.util.render.Alignment;
import dev.blend.util.render.DrawUtil;
import dev.blend.util.render.Gradient;
import dev.blend.util.render.ResourceManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.syuto.bytes.Byte.mc;

public class Hud extends Module {

    public final ColorSetting primary = new ColorSetting("Color", this, new Color(255, 0, 255));

    public static HashMap<String, String> modules = new HashMap<>();
    public List<Module> sortedModules = List.of();

    public Hud() {
        super("Hud", "hud bro", Category.RENDER);
    }




    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        sortedModules = ModuleManager.modules.stream()
                .filter(Module::isEnabled)
                .sorted((module1, module2) -> {
                    String displayText1 = module1.getSuffix().isEmpty()
                            ? module1.getName()
                            : module1.getName() + Formatting.GRAY + " " + module1.getSuffix();

                    String displayText2 = module2.getSuffix().isEmpty()
                            ? module2.getName()
                            : module2.getName() + Formatting.GRAY + " " + module2.getSuffix();

                    return Integer.compare(mc.textRenderer.getWidth(displayText2), mc.textRenderer.getWidth(displayText1));
                })
                .toList();
    }

    @EventHandler
    public void onRenderTick(RenderTickEvent event) {
        MatrixStack matrices = event.context.getMatrices();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int yPosition = 1;
        final Color currentColor = primary.getValue();


       // matrices.push();

        for (Module mod : sortedModules) {
            String moduleName = mod.getName();
            Module module = ModuleManager.getModule(moduleName.toLowerCase());
            if (module != null) {
                String suffix = module.getSuffix();
                String displayText = suffix.isEmpty() ? moduleName : moduleName + Formatting.GRAY + " " + suffix;

                int textWidth = mc.textRenderer.getWidth(displayText);
                int xPosition = screenWidth - textWidth - 2;

                RenderUtils.drawText(
                        event.context,
                        displayText,
                        xPosition,
                        yPosition,
                        currentColor.getRGB()


                );

                yPosition += mc.textRenderer.fontHeight + 3;
            }
        }

        int fps = mc.getCurrentFps();
        RenderUtils.drawText(
                event.context,
                String.valueOf(fps),
                10,
                10,
                currentColor.getRGB()


        );


       // matrices.pop();
    }
}


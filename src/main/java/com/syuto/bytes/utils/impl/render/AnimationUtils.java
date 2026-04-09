package com.syuto.bytes.utils.impl.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class AnimationUtils {

    @Setter
    @Getter
    public static boolean isBlocking;

    @Setter
    @Getter
    public static ItemStack spoofedItem;

    public static float height = -0.1f;

    public static void animate(PoseStack matrices, float swingProgress, float f) {
        float sine = (float) Math.sin(Mth.sqrt(swingProgress) * Math.PI) ;

        /*switch() {
            case "Exhibition" -> {
                matrices.translate(0.1, 0, -0.1);
                matrices.mulPose(Axis.XP.rotationDegrees(-sine * 50));
                matrices.mulPose(Axis.YP.rotationDegrees(-sine * 30));
            }

            case "Vanilla" -> {
                matrices.translate(0.1, 0,-0.1);
                matrices.mulPose(Axis.YP.rotationDegrees(45.0f + f * -20.0f));
                matrices.mulPose(Axis.ZP.rotationDegrees(sine * -20.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(sine * -80.0f));
                matrices.mulPose(Axis.YP.rotationDegrees(-45.0f));
            }

            case "Spin" -> {
                float spin = -(System.currentTimeMillis() / 2 % 360);
                matrices.translate(-0.1, 0,-0.2);
                matrices.mulPose(Axis.ZP.rotationDegrees(spin));
            }
        }*/

    }

}

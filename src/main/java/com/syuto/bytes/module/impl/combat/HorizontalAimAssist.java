package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class HorizontalAimAssist extends Module {

    public HorizontalAimAssist() {
        super("AimAssist", "Assists aiming at nearby players.", Category.COMBAT);
    }

    private final NumberSetting reach = new NumberSetting("Range", this, 6, 3, 50, 0.5);
    private final NumberSetting angle = new NumberSetting("Angle", this, 60, 0, 360, 1);
    private final NumberSetting strength = new NumberSetting("Strength", this, 0.4, 0.01, 1.0, 0.01);
    private final NumberSetting falloff = new NumberSetting("Falloff", this, 30, 1, 180, 1);
    private final BooleanSetting wep = new BooleanSetting("Only weapons", this, true);
    private final BooleanSetting moving = new BooleanSetting("Only while moving", this, true);
    private final BooleanSetting vert = new BooleanSetting("Vertical", this, false);

    private final List<Player> targets = new ArrayList<>();
    public static Player target;
    public static boolean allowBreaking = true;

    @Override
    public void onDisable() {
        target = null;
        allowBreaking = true;
    }

    @EventHandler
    public void onRotation(MouseEvent event) {
        if (target == null) return;

        double inputDeltaX = event.getCursorDeltaX();
        double inputDeltaY = event.getCursorDeltaY();
        boolean movingMouse = (Math.abs(inputDeltaX) > 0.001f || Math.abs(inputDeltaY) > 0.001f);
        if (!movingMouse && moving.getValue()) return;

        boolean weaponOk = !wep.getValue()
                || PlayerUtil.isHoldingWeapon()
                || PlayerUtil.isHoldingMace();
        if (!weaponOk || mc.player.isUsingItem()) return;

        if (PlayerUtil.getBiblicallyAccurateDistanceToCentreOfEntity(target) > reach.getValue().doubleValue()) return;
        if (!isInAngle(target)) return;

        float[] current = {mc.player.getYRot(), mc.player.getXRot()};
        float[] toTarget = RotationUtils.getCentreRotations(current, mc.player.getEyePosition(), target);

        float deltaYaw = Mth.wrapDegrees(toTarget[0] - current[0]);
        float deltaPitch = toTarget[1] - current[1];

        float angularDist = (float) Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        float falloffDeg = falloff.getValue().floatValue();
        float proximityFactor = Math.min(angularDist / falloffDeg, 1.0f);

        float s = strength.getValue().floatValue() * proximityFactor;

        event.setDeltaX(inputDeltaX + deltaYaw * s);
        if (vert.getValue()) {
            event.setDeltaY(inputDeltaY + deltaPitch * s);
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        targets.clear();
        targets.addAll(mc.level.players().stream()
                .filter(ent -> ent != mc.player
                        && PlayerUtil.getBiblicallyAccurateDistanceToCentreOfEntity(ent)
                        <= reach.getValue().doubleValue())
                .sorted(Comparator.comparingDouble(PlayerUtil::getBiblicallyAccurateDistanceToCentreOfEntity))
                .limit(10)
                .toList());

        target = targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isInAngle(Player target) {
        float playerYaw = mc.player.getYRot();
        float targetYaw = RotationUtils.getRotations(
                new float[]{playerYaw, mc.player.getXRot()},
                mc.player.getEyePosition(),
                target
        )[0];

        float diff = Mth.wrapDegrees(targetYaw - playerYaw);
        return Math.abs(diff) <= angle.getValue().floatValue() / 2f;
    }
}
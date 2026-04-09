package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.*;
import com.syuto.bytes.mixin.SendPacketMixinAccessor;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.module.impl.combat.Killaura;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static com.syuto.bytes.Byte.mc;

public class Speed extends Module {

    public ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "Verus"
    );


    public Speed() {
        super("Speed", "ZOOOOOOOOOOOM btw syuto is gay.", Category.MOVEMENT);
        setSuffix(() -> mode.getValue());
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        switch (mode.getValue()) {
            case "Verus" -> {
                if(!MovementUtil.isMoving()) return;

                MovementUtil.setSpeed(0.37);

                if(mc.player.onGround()) {
                    mc.player.jumpFromGround();
                }
            }
        }
    }
}

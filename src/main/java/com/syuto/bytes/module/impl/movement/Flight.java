package com.syuto.bytes.module.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
import com.syuto.bytes.eventbus.impl.PostMotionEvent;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import io.netty.util.internal.MathUtil;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;

@Deprecated
public class Flight extends Module {

    public ModeSetting modes = new ModeSetting("Mode",this,"Vanilla", "Spoof", "Damage");
    public NumberSetting speed = new NumberSetting("Speed", this, 1.0d, 0, 8.0d, 0.1d);

    public Flight() {
        super("Flight", "Zoom", Category.MOVEMENT);
        setSuffix(() ->
                modes.getValue()
        );
    }
    //vars
    private int jumps = 0, ticks = 0;
    private boolean damage = false;

    private final double[] jumpValues = {
            0.41999998688698,
            0.7531999805212,
            1.00133597911215,
            1.166109260938214,
            1.24918707874468,
            1.25220334025373,
            1.17675927506424,
            1.024424088213685,
            0.7967356006687,
            0.495200877005914,
            0.121296840539195
    };

    //events
    @Override
    public void onEnable() {
        this.jumps = 0;
        this.damage = false;
        this.ticks = 0;

    }

    @EventHandler
    void onPreMotion(PreMotionEvent event) {
        ticks++;

        double predictedY = MovementUtil.predictedMotion(event.posY, ticks);
        double y;

        if (mc.options.keyJump.isDown()) {
            y = speed.getValue().doubleValue();
        } else if (mc.options.keyShift.isDown()) {
            y = -speed.getValue().doubleValue();
        } else {
            y = 0;
        }

        Vec3 motion = mc.player.getDeltaMovement();

        switch(modes.getValue()) {
            case "Vanilla" -> {
                mc.player.setDeltaMovement(motion.x, y, motion.z);

                if (MovementUtil.isMoving()) {
                    MovementUtil.setSpeed(speed.getValue().doubleValue());
                } else {
                    mc.player.setDeltaMovement(0, y, 0);
                }
            }


            case "Spoof" -> {
                final InputConstants.Key attackKey = InputConstants.getKey("key.mouse.left");
                final InputConstants.Key useKey = InputConstants.getKey("key.mouse.right");
                if (mc.options.keyUse.isDown()) {
                    if (ticks % 2 == 0) {
                        KeyMapping.click(attackKey);
                    } else {
                        KeyMapping.click(useKey);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPostMotion(PostMotionEvent event) {
        switch (modes.getValue()) {
            case "Spoof" -> {
            }
        }

    }

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket s12) {
            if (s12.getId() == mc.player.getId() && modes.getValue().equals("Damage")) {
                this.damage = true;
            }
        }
    }

}

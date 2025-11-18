package com.syuto.bytes.module.impl.movement;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
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
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

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
        ticks = !mc.player.isOnGround() ? ticks + 1 : 0;

        double y;

        if (mc.options.jumpKey.isPressed()) {
            y = speed.getValue().doubleValue();
        } else if (mc.options.sneakKey.isPressed()) {
            y = -speed.getValue().doubleValue();
        } else {
            y = 0;
        }

        Vec3d motion = mc.player.getVelocity();

        switch(modes.getValue()) {
            case "Vanilla" -> {
                mc.player.setVelocity(motion.x, y, motion.z);

                if (MovementUtil.isMoving()) {
                    MovementUtil.setSpeed(speed.getValue().doubleValue());
                } else {
                    mc.player.setVelocity(0, y, 0);
                }
            }

            case "Spoof" -> {
                if (!damage) {
                    Vec3d pos = mc.player.getPos();

                    mc.player.setPosition(pos.x, pos.y + 5, pos.z);
                    damage = true;
                }

            }

            case "Damage" -> {
                if (!this.damage) {
                    mc.player.setVelocity(0, motion.y, 0);
                    if (jumps < 4) {
                        event.onGround = false;
                        if (ticks < jumpValues.length) {
                            event.posY += jumpValues[ticks];
                            ticks++;
                        } else {
                            jumps++;
                            ticks = 0;
                        }
                        if (jumps == 3 && ticks >= 2) {
                            event.onGround = true;
                        }
                    }
                } else {
                    mc.player.setVelocity(motion.x, y, motion.z);

                    if (MovementUtil.isMoving()) {
                        MovementUtil.setSpeed(speed.getValue().doubleValue());
                    } else {
                        mc.player.setVelocity(0, y, 0);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket s12) {
            if (s12.getEntityId() == mc.player.getId() && modes.getValue().equals("Damage")) {
                this.damage = true;
            }
        }
    }
}

package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class NoFall extends Module {

    public ModeSetting modes = new ModeSetting("mode",this,"Packet", "Spoof", "NoGround", "Grim");

    private boolean shouldNoFall = false, jumpNextTick = false;

    public NoFall() {
        super("NoFall", "Stops fall damage", Category.PLAYER);
        setSuffix(() -> modes.getValue());
    }

    @EventHandler
    public void onPreMotion(PreMotionEvent event) {
        boolean ground = mc.player.isOnGround();

        switch (modes.getValue()) {
            case "Packet" -> {
                if (!ground) {
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, mc.player.horizontalCollision));
                }
            }
            case "Spoof" -> {
                event.onGround = true;
            }

            case "NoGround" -> {
                event.onGround = false;
            }

            case "Grim" -> {
                //ChatUtils.print("nofall " + PlayerUtil.getFallDistance() );
                if (PlayerUtil.getFallDistance() == 10) {
                    shouldNoFall = true;
                }

                var player = mc.player;

                if (shouldNoFall) {
                    if (!jumpNextTick) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                                player,
                                ClientCommandC2SPacket.Mode.START_FALL_FLYING
                        ));

                        if (mc.player.isOnGround()) {
                            jumpNextTick = true;
                            mc.options.jumpKey.setPressed(true);
                        }
                    } else {
                        mc.options.jumpKey.setPressed(false);
                        jumpNextTick = false;
                        shouldNoFall = false;
                    }
                }
            }
        }
    }
}

package com.syuto.bytes.module.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PacketReceivedEvent;
import com.syuto.bytes.eventbus.impl.PostMotionEvent;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
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

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;


public class Flight extends Module {

    public ModeSetting mode = new ModeSetting(
            "Mode", this,
            "Vanilla", "Verus"
    );
    public NumberSetting speed = new NumberSetting(
            "Speed", this,
            1.0d, 0, 8.0d, 0.1d);

    public Flight() {
        super("Flight", "Zoom", Category.MOVEMENT);
        setSuffix(() ->
                mode.getValue()
        );
    }

    // GOD BYPASS CLIENT VALUE
    // BYPASS GOD CLIENT

    /*
    `##**********************************:...
    **###********+-:...........:=+****=.....
    ****###**+=:...................-=:...:+*
    *******+:.:.......::::.............=****
    *****+-:::::-:.....:::--:...........-+**
    ****+-:----:::::......:---:.....:::::-+*
    #***+--::----==++===---====--::..:::::=*
    ##**+---+*####%%%%%%%%%###***++=-:...:-*
    ###*+--=+**###%%%%%%%%%%###****+=-:...=#
    ###**:-=+*####%%%%%%%%%%%###****+-:..:*#
    ####*--+*###################******=:.=##
    #+-=*==*=:::::.:-=+****=--::....::-=:+*#
    =***+*+*+---=-::::-=**=-::..::-::-==-+-=
    --+##+*##**=++=+++*#%%*+++++====++++==**
    :=**#####%%%%%##%###%%#**#########*++***
    :-=:=#*#############%%#***#######*+++**#
    -=+******##########%%%##**###***++==+*##
    :+****=-+++***###+=++++--+****++=-==++=#
    **+-:----++++**###*+=====+***+=---:==---
    ----:..:::=++****#*****+++++==--:..=--.:
    .....::....-====++**=---==----::...:-:..
    ..::-:..-:.:=====-:-----::--::.......==:
    ++=-:..--::+++==+*****+++=-::::::.:..--*
    ++-..:===+-+++++=======--::::::--.:-..:=
    --:---===+=+**++===--:::::::::-=-::::.:-
    =--==*++++--***+===---:-----===+------:.`

    */

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        switch (mode.getValue()) {
            case "Verus" -> {
                if(!MovementUtil.isMoving()) {
                    return;
                }

                MovementUtil.setMotionY(-0.078400001525878);
                if(!mc.player.onGround()) {
                    MovementUtil.setSpeed(0.37d);
                }
            }
        }
    }
}

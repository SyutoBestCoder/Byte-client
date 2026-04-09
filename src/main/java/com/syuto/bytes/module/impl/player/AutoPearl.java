package com.syuto.bytes.module.impl.player;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.ModeSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import com.syuto.bytes.utils.impl.player.InventoryUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class AutoPearl extends Module {
    public AutoPearl() {
        super("AutoPearl", "Automatically throws a pearl and then a wind charge", Category.PLAYER);
    }

    public ModeSetting modes = new ModeSetting("mode",this,"2 tick", "3 tick");

    private int throwStage = 0;
    private int previousSlot = -1;




    private final InputConstants.Key useKey = InputConstants.getKey("key.mouse.right");

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.options.keyPickItem.isDown() && throwStage == 0) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
            throwStage = 1;
        }

        float pitch = mc.player.getXRot();

        switch (throwStage) {
            case -1 -> {
                throwStage--;
            }

            case 1 -> {

                if (pitch < 80.0f) {
                    boolean pearlThrown = useItem(Items.ENDER_PEARL);

                    if (!pearlThrown) {
                        reset();
                        return;
                    }
                } else {
                    boolean windchargeThrown = useItem(Items.WIND_CHARGE);
                    if (!windchargeThrown) {
                        reset();
                        return;
                    }
                }


                ItemStack offhand = mc.player.getOffhandItem();
                if (offhand.getItem() != null && offhand.getItem() == Items.WIND_CHARGE) {
                    KeyUtil.pressKey(mc.options.keyUse);
                    throwStage = -1;
                } else {
                    throwStage++;
                }
            }


            case 2 -> {
                if (modes.getValue().equals("2 tick")) {
                    if (pitch < 80.0f) {
                        useItem(Items.WIND_CHARGE);
                    } else {
                        useItem(Items.ENDER_PEARL);
                    }
                }

                throwStage++;
            }

            case 3 -> {
                if (modes.getValue().equals("2 tick")) {
                    reset();
                    return;
                } else {
                    if (pitch < 80.0f) {
                        useItem(Items.WIND_CHARGE);
                    } else {
                        useItem(Items.ENDER_PEARL);
                    }
                }
                throwStage++;
            }


            case 4, -2 -> {
                reset();
            }
        }
    }

    private boolean useItem(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && stack.getItem() == item) {
                InventoryUtil.setSlot(i);
                KeyUtil.pressKey(mc.options.keyUse);
                return true;
            }
        }
        return false;
    }

    private void reset() {
        if (previousSlot != -1) {
            InventoryUtil.setSlot(previousSlot);
        }
        previousSlot = -1;
        throwStage = 0;
    }
}
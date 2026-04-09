package com.syuto.bytes.module.impl.combat;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module {

    public AutoTotem() {
        super("AutoTotem", "Opens inventory and equips totems", Category.COMBAT);
    }

    public NumberSetting delay =
            new NumberSetting("Delay", this, 200, 0, 500, 25);

    private long lastSwap;
    private boolean waitingForInventory = false;
    private int targetSlot = -1;

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {

        if (mc.player == null || mc.gameMode == null)
            return;

        ItemStack offhand = mc.player.getOffhandItem();

        if (offhand.getItem() == Items.TOTEM_OF_UNDYING)
            return;

        if (System.currentTimeMillis() - lastSwap < delay.getValue().longValue())
            return;

        if (!(mc.screen instanceof InventoryScreen) && !waitingForInventory) {

            targetSlot = findTotemSlot();

            if (targetSlot == -1)
                return;

            mc.setScreen(new InventoryScreen(mc.player));

            waitingForInventory = true;
            return;
        }

        if (mc.screen instanceof InventoryScreen && waitingForInventory) {

            moveToOffhand(targetSlot);
            mc.player.closeContainer();

            waitingForInventory = false;
            lastSwap = System.currentTimeMillis();
        }
    }

    private int findTotemSlot() {

        for (int i = 0; i < 36; i++) {

            ItemStack stack =
                    mc.player.getInventory().getItem(i);

            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {

                // Convert hotbar
                if (i < 9)
                    i += 36;

                return i;
            }
        }

        return -1;
    }

    private void moveToOffhand(int slot) {

        int offhandSlot = 45;

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slot,
                0,
                ClickType.PICKUP,
                mc.player
        );

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                offhandSlot,
                0,
                ClickType.PICKUP,
                mc.player
        );

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slot,
                0,
                ClickType.PICKUP,
                mc.player
        );
    }
}
package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ChestStealer extends Module {
    public NumberSetting stealDelay = new NumberSetting("Delay",this,200,0,500, 25);

    public ChestStealer() {
        super("Stealer", "Steals from chests", Category.PLAYER);
    }

    private boolean shouldSteal;
    private long delay, lastTime;

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        if (mc.screen instanceof ContainerScreen e) {
            String title = e.getTitle().getString();
            ChestMenu handler = e.getMenu();
            if (title.contains("Large Chest") || title.contains("Chest") || title.contains("Barrel")) {
                steal(handler.getContainer().getContainerSize(), handler);
            }
        } else if (mc.screen instanceof ShulkerBoxScreen a) {
            String title = a.getTitle().getString();
            ShulkerBoxMenu handle = a.getMenu();
            if (title.contains("Shulker Box")) {
                steal(27, handle);
            }
        }
    }

    // bringin gout your inner romania 🇷🇴
    private void steal(int size, AbstractContainerMenu handler) {
        for (int i = 0; i < size; i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                if (System.currentTimeMillis() - this.lastTime >= delay) {
                    this.lastTime = System.currentTimeMillis();
                    mc.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 1, ClickType.QUICK_MOVE, mc.player);
                    updateDelay();
                }
            }
        }
    }

    private void updateDelay() {
        delay = stealDelay.value.longValue();
    }
}

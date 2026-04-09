package com.syuto.bytes.module.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.KeyEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderTickEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import com.syuto.bytes.utils.impl.player.InventoryUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.At;

public class Lunge extends Module {
    private int lastSlot = -1;
    private boolean swapped = false;
    private int ticks = 0;
    private int stuntick = 0;
    private boolean toggleRequested = false;

    public Lunge() {
        super("Lunge", "Swaps to mace based on weapon and chosen mace variant", Category.MOVEMENT);
    }

    private final NumberSetting swapTick = new NumberSetting("Swap tick", this, 2, 0, 5, 1);

    private final InputConstants.Key AttackKey = InputConstants.getKey("key.mouse.left");

    @EventHandler
    public void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_C) {
            if (!toggleRequested && stuntick == 0 && !swapped) {
                toggleRequested = true;
            }
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {

        if (toggleRequested) {
            if (stuntick == 0) {
                float cooldown = mc.player.getAttackStrengthScale(0.5F);

                if (cooldown < 1.0f) {
                    reset();
                    return;
                }


                lastSlot = mc.player.getInventory().getSelectedSlot();
                boolean swap = swapToSpear();

                if (!swap) {
                    reset();
                    return;
                }
                stuntick++;
            }
        }

        if (swapped && lastSlot != -1) {
            int tick = swapTick.getValue().intValue();
            ticks++;
            if (ticks >= tick) {
                reset();
            }
        }
    }

    private boolean swapToSpear() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && !stack.isEmpty() && isLungeSpear(stack)) {
                boolean actuallyUsingItem = mc.player.isUsingItem();

                if (actuallyUsingItem) {
                    reset();
                    return false;
                }

                InventoryUtil.setSlot(i);
                KeyUtil.pressKey(mc.options.keyAttack);
                return (swapped = true);
            }
        }
        return false;
    }

    private boolean isLungeSpear(ItemStack stack) {
        return stack.is(ItemTags.SPEARS) &&
                stack.getEnchantments().entrySet().stream()
                        .anyMatch(entry -> entry.getKey().is(Enchantments.LUNGE));
    }

    private void reset() {
        if (lastSlot != -1) {
            InventoryUtil.setSlot(lastSlot);
            swapped = false;
        }
        lastSlot = -1;
        stuntick = 0;
        ticks = 0;
        toggleRequested = false;
    }
}
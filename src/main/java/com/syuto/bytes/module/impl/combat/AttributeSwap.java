package com.syuto.bytes.module.impl.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.AttackEntityEvent;
import com.syuto.bytes.eventbus.impl.KeyEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.ModuleManager;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.BooleanSetting;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import com.syuto.bytes.utils.impl.player.InventoryUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;

public class AttributeSwap extends Module {
    private int lastSlot = -1;
    private boolean swapped = false;

    public static boolean swapping = false;
    private int ticks = 0;

    public enum Variant {DENSITY, BREACH}

    public static Variant preferred = Variant.DENSITY;
    private boolean toggleRequested = false;

    public AttributeSwap() {
        super("AttributeSwap", "Swaps to mace based on weapon and chosen mace variant, meow", Category.COMBAT);
        setSuffix(() -> preferred.name());
    }

    private final NumberSetting swapTick = new NumberSetting("Swap tick", this, 2, 1, 5, 1);
    private final BooleanSetting wep = new BooleanSetting("Only weapons", this, true);
    private final BooleanSetting stun = new BooleanSetting("Stun", this, true);
    private final BooleanSetting yawCheck = new BooleanSetting("Only facing", this, true);

    private final InputConstants.Key AttackKey = InputConstants.getKey("key.mouse.left");
    private Entity target;
    private AutoClicker clicker;
    private AimAssist aim;

    @EventHandler
    public void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_G) {
            toggleRequested = true;
        }
    }

    public static boolean extraClickQueued = false;

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {


        clicker = ModuleManager.getModule(AutoClicker.class);
        aim = ModuleManager.getModule(AimAssist.class);

        if (toggleRequested) {
            preferred = (preferred == Variant.DENSITY) ? Variant.BREACH : Variant.DENSITY;
            toggleRequested = false;
        }


        boolean doStun = (clicker != null && clicker.isEnabled()) || (aim != null && aim.isEnabled());

        int tick = extraClickQueued ? 3 : swapTick.getValue().intValue();

        if (target != null ) {
            if (stun.getValue() && isShielding(target) && !PlayerUtil.isHoldingMace() && !doStun) {
                if (!yawCheck.getValue() || yawDiffCheck(target)) {
                    if (ticks == 0 && swapped) {
                        KeyUtil.pressKey(mc.options.keyAttack);
                        extraClickQueued = true;
                        //ChatUtils.print("hi");
                    }
                }
            }
        }

        if (swapped && lastSlot != -1) {
            ticks++;
            if (ticks >= tick) {
                extraClickQueued = false;
                reset();
            }
        }

    }


    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        target = event.getTarget();

        if (!(target instanceof LivingEntity)) return;


        if (event.getMode() == AttackEntityEvent.Mode.Pre) {

            boolean doStun = clicker != null && clicker.isEnabled();


            if (stun.getValue() && isShielding(target) && !PlayerUtil.isHoldingMace() && !doStun && !swapped) {
                if (!yawCheck.getValue() || yawDiffCheck(target) ) {
                    swapToAxe();
                    if (swapping && swapped) {
                        swapping = false;
                    }

                    return;
                }
            }

            if (wep.getValue() && PlayerUtil.isHoldingWeapon()) {
                if (ticks == 1) {
                    extraClickQueued = true;
                }

                swapToPreferredMace();

                if (swapping && swapped) {
                    swapping = false;
                }
            } else if (!wep.getValue()) {
                if (ticks == 1) {
                    extraClickQueued = true;
                }
                swapToPreferredMace();

                if (swapping && swapped) {
                    swapping = false;
                }
            }

        }
    }

    private void swapToAxe() {
        int targetSlot = findAxeSlot();
        if (targetSlot == -1 || swapped) {
            return;
        }
        swapping = true;
        //ChatUtils.print("Swapping A");


        if (!swapped) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
        }

        //InventoryUtil.setSlot(targetSlot);
        mc.player.getInventory().setSelectedSlot(targetSlot);
        swapped = true;
        //ChatUtils.print("Swapped to axe " + ticks + " " + mc.player.tickCount);

    }

    private void swapToPreferredMace() {
        int targetSlot = findPreferredMace();
        if (targetSlot == -1) {
            return;
        }
        swapping = true;
        //ChatUtils.print("Swapping B");

        if (!swapped) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
        }

        //InventoryUtil.setSlot(targetSlot);
        mc.player.getInventory().setSelectedSlot(targetSlot);
        swapped = true;
        //ChatUtils.print("Swapped to mace " + ticks + " " + preferred.name());
    }


    private int findPreferredMace() {
        int anyMaceSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() != Items.MACE) continue;
            if (anyMaceSlot == -1) {
                anyMaceSlot = i;
            }
            if (preferred == Variant.DENSITY && isDensityMace(stack)) return i;
            if (preferred == Variant.BREACH && isBreachMace(stack)) return i;
        }
        return anyMaceSlot;
    }

    private boolean isDensityMace(ItemStack stack) {
        return stack.getItem() == Items.MACE && stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.DENSITY));
    }

    private boolean isBreachMace(ItemStack stack) {
        return stack.getItem() == Items.MACE && stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.BREACH));
    }

    private boolean isShielding(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        ItemStack active = player.getUseItem();
        return player.isUsingItem() && active.getItem() instanceof ShieldItem;
    }

    private int findAxeSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    private boolean yawDiffCheck(Entity target) {
        double dx = mc.player.getX() - target.getX();
        double dz = mc.player.getZ() - target.getZ();
        double angleToPlayer = Math.toDegrees(Math.atan2(dz, dx)) - 90;

        double yawDiff = Math.abs(target.getYRot() - angleToPlayer);
        if (yawDiff > 180) yawDiff = 360 - yawDiff;

        return yawDiff < 90;
    }

    public void reset() {

        if (swapped) {
            //ChatUtils.print("Swapping C");
            mc.player.getInventory().setSelectedSlot(lastSlot);
            swapped = false;
           // ChatUtils.print("Reset " + ticks + " " + mc.player.tickCount);
        }

        lastSlot = -1;
        ticks = 0;
    }
}
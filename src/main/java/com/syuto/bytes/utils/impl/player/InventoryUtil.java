package com.syuto.bytes.utils.impl.player;

import java.util.HashMap;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.mixin.KeyMappingAccessor;
import com.syuto.bytes.utils.impl.keyboard.KeyUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import static com.syuto.bytes.Byte.mc;

public class InventoryUtil {


    public static HashMap<ResourceKey<Enchantment>, Integer> getEnchants(
            ItemStack item
    ) {
        HashMap<ResourceKey<Enchantment>, Integer> enchantments =
                new HashMap<>();

        ItemEnchantments enchantmentsComponent =
                EnchantmentHelper.getEnchantmentsForCrafting(item);

        enchantmentsComponent
                .keySet()
                .forEach(enchant -> {
                    enchantments.put(
                            enchant.unwrapKey().get(),
                            enchantmentsComponent.getLevel(enchant)
                    );
                });

        return enchantments;
    }

    public static int getEnchantLevel(
            ItemStack item,
            ResourceKey<Enchantment> enchantment
    ) {
        return getEnchants(item).getOrDefault(enchantment, 0);
    }


    public static void setSlot(int slot) {
        KeyUtil.pressKey(mc.options.keyHotbarSlots[slot]);
    }

}

package com.syuto.bytes.utils.impl.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import com.syuto.bytes.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

import static com.syuto.bytes.Byte.mc;

public class KeyUtil {

    public static void pressKey(KeyMapping key) {
        InputConstants.Key bind = ((KeyMappingAccessor) key).getKey();
        KeyMapping.click(bind);
    }
}

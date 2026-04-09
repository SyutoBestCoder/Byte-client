package com.syuto.bytes.utils.impl.client;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;

import static com.syuto.bytes.Byte.mc;

public class ClientUtil {

    @Getter
    @Setter
    public static int rightClickDelay;

    public static void crash() {
        long freeAddr = ReflectionUtil.theUnsafe.allocateMemory(1);
        ReflectionUtil.theUnsafe.freeMemory(freeAddr);
        ReflectionUtil.theUnsafe.setMemory(
                freeAddr-10 + ThreadLocalRandom.current().nextLong(-10, 11),
                ThreadLocalRandom.current().nextLong(1, 11),
                (byte) 0
        );
    }


    public static boolean nullCheck() {
        return mc.player != null && mc.level != null;
    }



}

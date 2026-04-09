package com.syuto.bytes.utils.impl.client;

public class TimerUtil {

    private long time = System.nanoTime();

    public void reset() {
        time = System.nanoTime();
    }

    public boolean hasElapsed(long ms) {
        return System.nanoTime() - time >= ms * 1_000_000L;
    }
}

package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;

public class MouseEvent implements Event {
    private double cursorDeltaX;
    private double cursorDeltaY;
    private boolean modifiedX;
    private boolean modifiedY;

    public MouseEvent(double x, double y) {
        this.cursorDeltaX = x;
        this.cursorDeltaY = y;
    }

    public double getCursorDeltaX() { return cursorDeltaX; }
    public double getCursorDeltaY() { return cursorDeltaY; }
    public boolean isModifiedX() { return modifiedX; }
    public boolean isModifiedY() { return modifiedY; }

    public void setDeltaX(double x) { this.cursorDeltaX = x; this.modifiedX = true; }
    public void setDeltaY(double y) { this.cursorDeltaY = y; this.modifiedY = true; }
}

package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

public class MouseInputEvent implements Event {

    @Getter
    private int mouseButton;

    @Getter
    @Setter
    private boolean canceled;

    public MouseInputEvent(int mouseButton, boolean canceled) {
        this.mouseButton = mouseButton;
        this.canceled = canceled;
    }
}

package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class SetSlotEvent implements Event {

    @Getter
    @Setter
    public int slot;

    @Getter
    @Setter
    private boolean canceled;

    public SetSlotEvent(int slot) {
        this.slot = slot;
        this.canceled = false;
    }


}

package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;

public class WorldJoinEvent implements Event {

    @Getter
    public int entityId;

    public WorldJoinEvent(int entityId) {
        this.entityId = entityId;
    }
}

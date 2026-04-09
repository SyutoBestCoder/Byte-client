package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;


public class PlayerDamageEvent implements Event {
    @Getter
    int id;


    public PlayerDamageEvent(int id) {
        this.id = id;
    }
}

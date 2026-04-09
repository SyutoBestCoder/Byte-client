package com.syuto.bytes.eventbus.impl;

import com.syuto.bytes.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class AttackEntityEvent implements Event {
    @Getter
    private final AttackEntityEvent.Mode mode;
    @Getter
    private Entity target;

    @Getter
    @Setter
    private boolean cancelled;

    public AttackEntityEvent(Mode mode, Entity target) {
        this.mode = mode;
        this.target = target;
    }


    public enum Mode {
        Pre, Post
    }
}

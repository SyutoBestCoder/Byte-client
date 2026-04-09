package com.syuto.bytes.module.impl.player.scaffold;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class BlockData {
    BlockPos position;
    Vec3 hit;
    Direction facing;

    public BlockData(BlockPos position, Vec3 hit, Direction facing) {
        this.position = position;
        this.hit = hit;
        this.facing = facing;
    }

}

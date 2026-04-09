package com.syuto.bytes.utils.impl.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.syuto.bytes.Byte.mc;

public class WorldUtil {

    public static BlockPos findBlocks(BlockPos pos, int range) {
        Optional<BlockPos> block = BlockPos.findClosestMatch(
                pos, range, range,
                cock -> mc.level.getBlockState(cock).getBlock().equals(Blocks.NOTE_BLOCK)
        );
        return block.orElse(null);
    }

    public static BlockPos findBlocksAround(BlockPos pos, int range) {
        Optional<BlockPos> block = BlockPos.findClosestMatch(
                pos, range, range,
                cock -> mc.level.getBlockState(cock).getBlock() instanceof BedBlock
        );
        return block.orElse(null);
    }

    public static List<BlockPos> findAllOres(BlockPos centerPos, int range) {
        List<BlockPos> diamondOres = new ArrayList<>();

        for (int x = centerPos.getX() - range; x <= centerPos.getX() + range; x++) {
            for (int y = centerPos.getY() - range; y <= centerPos.getY() + range; y++) {
                for (int z = centerPos.getZ() - range; z <= centerPos.getZ() + range; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    Block block = mc.level.getBlockState(currentPos).getBlock();
                    //we dont talk about this lol
                    if (
                            block == Blocks.DIAMOND_ORE ||
                            block == Blocks.IRON_ORE ||
                            block == Blocks.DEEPSLATE_DIAMOND_ORE ||
                            block == Blocks.DEEPSLATE_IRON_ORE ||
                            block == Blocks.DEEPSLATE_COAL_ORE ||
                            block == Blocks.COAL_ORE ||
                            block == Blocks.LAPIS_ORE ||
                            block == Blocks.DEEPSLATE_LAPIS_ORE ||
                            block == Blocks.ANCIENT_DEBRIS ||
                            block == Blocks.EMERALD_ORE ||
                            block == Blocks.DEEPSLATE_EMERALD_ORE ||
                            block == Blocks.VAULT ||
                            block == Blocks.DEEPSLATE_GOLD_ORE ||
                            block == Blocks.GOLD_ORE ||
                            block == Blocks.GOLD_BLOCK ||
                            block == Blocks.CHEST
                    ) {
                        diamondOres.add(currentPos);
                    }
                }
            }
        }

        return diamondOres;
    }

    public static Direction getClosest(BlockPos pos) {
        double closestDistance = Double.MAX_VALUE;
        Direction closestDirection = null;

        for (Direction dir : Direction.values()) {
            BlockPos offsetPos = pos.relative(dir);

            Vec3 faceCenter = offsetPos.getCenter();
            Vec3 po = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            double distance = po.distanceToSqr(faceCenter);

            if (distance <= closestDistance) {
                closestDistance = distance;
                closestDirection = dir;
            }
        }

        return closestDirection;
    }


    public static boolean canPlace(BlockPos position) {
        BlockState state =  mc.level.getBlockState(position);
        return state != null && state.canBeReplaced();
    }



    public static boolean canBePlacedOn(BlockPos blockPos) {
        if (blockPos == null || mc.player == null || mc.level == null) return false;

        AABB blockBox = new AABB(blockPos);

        if (mc.player.getBoundingBox().intersects(blockBox)) {
            return false;
        }

        BlockState state = mc.level.getBlockState(blockPos);
        return state != null && state.isRedstoneConductor(mc.level, blockPos) && !state.isAir();
    }




    public static boolean isOnTeam(Entity ent) {
        return ent.isAlliedTo(mc.player);
    }

}

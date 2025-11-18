package com.syuto.bytes.utils.impl.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.syuto.bytes.Byte.mc;

public class WorldUtil {

    public static BlockPos findBlocks(BlockPos pos, int range) {
        Optional<BlockPos> block = BlockPos.findClosest(
                pos.down(), range, range,
                cock -> !mc.world.getBlockState(cock).isAir()
        );
        return block.orElse(null);
    }

    public static BlockPos findBlocksAround(BlockPos pos, int range) {
        Optional<BlockPos> block = BlockPos.findClosest(
                pos, range, range,
                cock -> mc.world.getBlockState(cock).getBlock() instanceof BedBlock
        );
        return block.orElse(null);
    }

    public static List<BlockPos> findAllOres(BlockPos centerPos, int range) {
        List<BlockPos> diamondOres = new ArrayList<>();

        for (int x = centerPos.getX() - range; x <= centerPos.getX() + range; x++) {
            for (int y = centerPos.getY() - range; y <= centerPos.getY() + range; y++) {
                for (int z = centerPos.getZ() - range; z <= centerPos.getZ() + range; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    Block block = mc.world.getBlockState(currentPos).getBlock();
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
            BlockPos offsetPos = pos.offset(dir);

            Vec3d faceCenter = offsetPos.toCenterPos();
            double distance = mc.player.getPos().squaredDistanceTo(faceCenter);

            if (distance <= closestDistance) {
                closestDistance = distance;
                closestDirection = dir;
            }
        }

        return closestDirection;
    }


    public static boolean canPlace(BlockPos position) {
        BlockState state =  mc.world.getBlockState(position);
        return state != null && state.isReplaceable();
    }



    public static boolean canBePlacedOn(BlockPos blockPos) {
        if (blockPos == null || mc.player == null || mc.world == null) return false;

        Box blockBox = new Box(blockPos);

        if (mc.player.getBoundingBox().intersects(blockBox)) {
            return false;
        }

        BlockState state = mc.world.getBlockState(blockPos);
        return state != null && state.isSolidBlock(mc.world, blockPos) && !state.isAir();
    }




    public static boolean isOnTeam(Entity ent) {
        return ent.isTeammate(mc.player);
    }

}

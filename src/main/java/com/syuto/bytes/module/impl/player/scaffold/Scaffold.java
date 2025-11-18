package com.syuto.bytes.module.impl.player.scaffold;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.eventbus.impl.RotationEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.player.WorldUtil;
import com.syuto.bytes.utils.impl.render.AnimationUtils;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.syuto.bytes.Byte.mc;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", "Nate Higgers", Category.PLAYER);
    }


    private float[] rotations, lastRotations, foundRots;
    private BlockData blockData, oldData;
    private int blockSlot = -1;
    private BlockHitResult result;

    private final int RADIUS = 4;

    @Override
    public void onEnable() {
        super.onEnable();
        AnimationUtils.setSpoofedItem(mc.player.getStackInHand(Hand.MAIN_HAND));
        blockSlot = getBlockSlot();

    }

    @Override
    public void onDisable() {
        AnimationUtils.setSpoofedItem(null);
    }


    @EventHandler
    public void onRotation(RotationEvent event) {
        lastRotations = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};

        if (blockData != null) { //&& blockData != oldData
            rotations = RotationUtils.getBlockRotations(
                    blockData.getPosition(),
                    blockData.getHit(),
                    blockData.getFacing()
            );

            if (rotations == lastRotations) {
                ChatUtils.print("dupe rotatiopns");
            }

            float currentYaw = RotationUtils.getLastRotationYaw();

            float target = rotations[0];
            float yaws = target - currentYaw;
            yaws %= 360.0f;
            if (yaws > 180.0f) yaws-= 360.0f;
            if (yaws < -180.0f) yaws += 360.0f;

            rotations[0] = yaws + currentYaw;


            rotations = RotationUtils.getFixedRotation(rotations, lastRotations); // gcd fix


            //oldData = this.blockData;
        }


        if (mc.player.isOnGround() && mc.options.pickItemKey.isPressed()) {
            float currentYaw = RotationUtils.getRotationYaw();
            float target = rotations[0] + 180;
            float yaws = target - currentYaw;
            yaws %= 360.0f;
            if (yaws > 180.0f) yaws-= 360.0f;
            if (yaws < -180.0f) yaws += 360.0f;

            rotations[0] = yaws + currentYaw ;
            rotations = RotationUtils.getFixedRotation(rotations, lastRotations); //gcd fix
        }

        if (rotations != null) {
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);

            this.lastRotations = new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()};
        }
    }

    @EventHandler
    public void onPreUpdate(PreUpdateEvent event) {

        blockSlot = getBlockSlot();
        if (blockSlot != -1) {
            mc.player.getInventory().setSelectedSlot(blockSlot);
        }

        this.blockData = getBlockData();

        if (rotations != null && blockData != null) {
            if (mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem() != Blocks.AIR.asItem()) {
                place(blockData);
                //raycast
            }
        }

        mc.player.setSprinting(false);
        mc.options.sprintKey.setPressed(false);

        if (mc.options.pickItemKey.isPressed()) {
            if (mc.player.isOnGround() && MovementUtil.isMoving()) {
                mc.options.jumpKey.setPressed(true);
                mc.player.setSprinting(true);
                mc.options.sprintKey.setPressed(true);
            } else {
                mc.options.jumpKey.setPressed(false);
                mc.player.setSprinting(false);
                mc.options.sprintKey.setPressed(false);
            }
        }
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent event) {
        if (blockData != null) {
            RenderUtils.drawLine(
                    event.matrixStack,
                    mc.player.getEyePos(),
                    blockData.getHit(),
                    Color.magenta.getRGB()
            );
        }
    }


    private void place(BlockData blockData) {

        BlockHitResult result = new BlockHitResult(
                blockData.getHit(),
                blockData.getFacing(),
                blockData.getPosition(),
                false
        );

        boolean keepY = mc.options.pickItemKey.isPressed();
        if (keepY && blockData.getFacing() == Direction.UP) {
           // ChatUtils.print("Skip");
            return;
        }

        //if (mc.options.pickItemKey.isPressed() && blockData.getFacing() == Direction.UP) return;

        mc.interactionManager.interactBlock(mc.player, mc.player.getActiveHand(), result);
        mc.player.swingHand(Hand.MAIN_HAND);

        //ChatUtils.print("Placed block " + blockData.getFacing().asString());

    }



    private BlockData getBlockData() {
        BlockPos playerPos = mc.player.getBlockPos();

        playerPos = new BlockPos(
                playerPos.getX(),
                playerPos.getY() - 1,
                playerPos.getZ()
        );

        if (!mc.world.getBlockState(playerPos).isReplaceable()) {
            return null;
        }

        List<BlockPos> candidates = new ArrayList<>();

        for (int x = -RADIUS; x <= RADIUS ; x++) {
            for (int y = -RADIUS; y <= 0; y++) {
                for (int z = -RADIUS ; z <= RADIUS; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (!mc.world.getBlockState(pos).isReplaceable() && !canInteract(pos)) {
                        for (Direction direction : Direction.values()) {
                            if (direction != Direction.DOWN) {
                                BlockPos blockPos = pos.offset(direction);
                                if (WorldUtil.canPlace(blockPos)) {
                                    candidates.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            ChatUtils.print("Empty list returning null!");
            return null;
        }

        BlockPos finalPlayerPos = playerPos;
        candidates.sort(Comparator.comparingDouble(pos ->
                pos.getSquaredDistance(finalPlayerPos.toCenterPos())));

        BlockPos blockPos = candidates.getFirst();
        Direction facing = getFacing(blockPos, playerPos);
        int blockFacing = facing.getId();

        /*Vec3d coord = new Vec3d(
                getCoord(blockFacing, "x"),
                getCoord(blockFacing, "y"),
                getCoord(blockFacing, "z")
        );*/

        Vec3d hit = getClosestPoint(blockPos, facing); //blockPos.toCenterPos().add(coord);
        ChatUtils.print("h" + hit);
        return new BlockData(blockPos, hit, facing);
    }


    private double getCoord(int facing, String axis) {
        return switch (axis) {
            case "x" -> (facing == 4) ? -0.5 : (facing == 5) ? 0.5 : 0;
            case "y" -> (facing == 0) ? -0.5 : (facing == 1) ? 0.5 : 0;
            case "z" -> (facing == 2) ? -0.5 : (facing == 3) ? 0.5 : 0;
            default -> 0;
        };
    }

    private Direction getFacing(BlockPos blockPos, BlockPos blockPos1) {
        Direction bestFacing = null;
        double closestDistance = Double.MAX_VALUE;

        for (Direction facing : Direction.values()) {
            if (facing == Direction.DOWN) continue;

            BlockPos pos = blockPos.offset(facing);

            if (pos.getY() <= blockPos1.getY()) {
                Vec3d center = Vec3d.ofCenter(pos);
                double distance = mc.player.getPos().distanceTo(center);

                if (distance < closestDistance || bestFacing == null) {
                    closestDistance = distance;
                    bestFacing = facing;
                }
            }
        }
        return bestFacing;
    }

    public Vec3d getClosestPoint(BlockPos pos, Direction face) {
        Vec3d eye = mc.player.getEyePos();

        double down = 0.2 + Math.random() * 0.3;;

        ChatUtils.print(down);

        double cx = MathHelper.clamp(eye.x, pos.getX(), pos.getX() + 1);
        double cy = MathHelper.clamp(eye.y, pos.getY() - down, pos.getY() + down);
        double cz = MathHelper.clamp(eye.z, pos.getZ(), pos.getZ() + 1);

        Vec3d hit = new Vec3d(cx, cy, cz);

        return hit;
    }

    private int getBlockSlot() {
        int selectedSlot = -1;
        int largestStackSize = 0;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack itemStack = mc.player.getInventory().getStack(slot);

            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem) {
                int stackSize = itemStack.getCount();
                if (stackSize > largestStackSize) {
                    largestStackSize = stackSize;
                    selectedSlot = slot;
                }
            }
        }
        return selectedSlot;
    }

    private boolean canInteract(BlockPos pos) {
        return mc.player.getPos().distanceTo(pos.toCenterPos()) > mc.player.getBlockInteractionRange();
    }
}

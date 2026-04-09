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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
        AnimationUtils.setSpoofedItem(mc.player.getItemInHand(InteractionHand.MAIN_HAND));
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
            if (yaws > 180.0f) yaws -= 360.0f;
            if (yaws < -180.0f) yaws += 360.0f;

            rotations[0] = yaws + currentYaw;
            // sweep 20 degrees around
            float step = 1f;
            float yawStep = 180/10;// try every 2 degrees


            /*if (result != null
                    && result.getType() == HitResult.Type.BLOCK
                    && result.getSide() != blockData.getFacing()) {

                boolean found = false;

                for (float yOffset = -180; yOffset <= 180 && !found; yOffset += yawStep) {
                    for (float pitchOffset = 0; pitchOffset <= 90; pitchOffset += step) {

                        float yaw = rotations[0] + yOffset;
                        float pitch = rotations[1] - pitchOffset;

                        ChatUtils.print("P " + pitchOffset + " " + pitch);

                        result = PlayerUtil.raycastBlocks(
                                yaw,
                                pitch,
                                5,
                                mc.getRenderTickCounter().getTickDelta(false),
                                false
                        );

                        if (result  != null
                                && result.getType() == HitResult.Type.BLOCK
                                && result.getSide() == blockData.getFacing()) {

                            ChatUtils.print("loop");
                            found = true;   // break BOTH loops
                            break;          // break inner loop
                        }
                    }
                }
            }*/



            rotations = RotationUtils.getFixedRotation(rotations, lastRotations); // gcd fix


            //oldData = this.blockData;
        }


        if (mc.player.onGround() && mc.options.keyPickItem.isDown()) {
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

            /*result = PlayerUtil.raycastBlocks(
                    RotationUtils.getRotationYaw(),
                    RotationUtils.getRotationPitch(),
                    4,
                    mc.getRenderTickCounter().getTickDelta(false),
                    true
            );*/
            if (mc.player.getInventory().getItem(mc.player.getInventory().getSelectedSlot()).getItem() != Blocks.AIR.asItem()) {

               /* if (result != null) {
                    ChatUtils.print("Result Not null");
                    if (result.getType() == HitResult.Type.BLOCK) {
                        ChatUtils.print("Hit Block");
                        if (result.getBlockPos().equals(blockData.getPosition())) {
                            ChatUtils.print("Hit Pos");
                            if (result.getSide() == blockData.getFacing()) {
                                ChatUtils.print("Hit Side");
                                place(blockData);
                            }
                        }
                    }
                }
                */
                place(blockData);

                //raycast
            }
        }

        mc.player.setSprinting(false);
        mc.options.keySprint.setDown(false);

        if (mc.options.keyPickItem.isDown()) {
            if (mc.player.onGround() && MovementUtil.isMoving()) {
                mc.options.keyJump.setDown(true);
                mc.player.setSprinting(true);
                mc.options.keySprint.setDown(true);
            } else {
                mc.options.keyJump.setDown(false);
                mc.player.setSprinting(false);
                mc.options.keySprint.setDown(false);
            }
        }
    }

    @EventHandler
    public void onRenderWorld(RenderWorldEvent event) {
        if (blockData != null) {
            RenderUtils.drawLine(
                    event.matrixStack,
                    mc.player.getEyePosition(),
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

        boolean keepY = mc.options.keyPickItem.isDown();
        if (keepY && blockData.getFacing() == Direction.UP) {
           // ChatUtils.print("Skip");
            return;
        }

        //if (mc.options.pickItemKey.isPressed() && blockData.getFacing() == Direction.UP) return;

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result);
        mc.player.swing(InteractionHand.MAIN_HAND);

        //ChatUtils.print("Placed block " + blockData.getFacing().asString());

    }



    private BlockData getBlockData() {
        BlockPos playerPos = mc.player.blockPosition();

        playerPos = new BlockPos(
                playerPos.getX(),
                playerPos.getY() - 1,
                playerPos.getZ()
        );

        if (!mc.level.getBlockState(playerPos).canBeReplaced()) {
            return null;
        }

        List<BlockPos> candidates = new ArrayList<>();

        for (int x = -RADIUS; x <= RADIUS ; x++) {
            for (int y = -RADIUS; y <= 0; y++) {
                for (int z = -RADIUS ; z <= RADIUS; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);

                    if (!mc.level.getBlockState(pos).canBeReplaced() && !canInteract(pos)) {
                        for (Direction direction : Direction.values()) {
                            if (direction != Direction.DOWN) {
                                BlockPos blockPos = pos.relative(direction);
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
                pos.distToCenterSqr(finalPlayerPos.getCenter())));

        BlockPos blockPos = candidates.getFirst();
        Direction facing = getFacing(blockPos, playerPos);
        int blockFacing = facing.get3DDataValue();

        /*Vec3d coord = new Vec3d(
                getCoord(blockFacing, "x"),
                getCoord(blockFacing, "y"),
                getCoord(blockFacing, "z")
        );*/

        Vec3 hit = getClosestPoint(blockPos, facing); //blockPos.toCenterPos().add(coord);
       // ChatUtils.print("h" + hit);
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

            BlockPos pos = blockPos.relative(facing);

            if (pos.getY() <= blockPos1.getY()) {
                Vec3 center = Vec3.atCenterOf(pos);

                Vec3 posssss = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                double distance = posssss.distanceTo(center);

                if (distance < closestDistance || bestFacing == null) {
                    closestDistance = distance;
                    bestFacing = facing;
                }
            }
        }
        return bestFacing;
    }

    public Vec3 getClosestPoint(BlockPos pos, Direction face) {
        Vec3 eye = mc.player.getEyePosition();

        double ma = Math.max(Math.random() * 0.3, 0.5);
        double down = 0.2 + ma;

        ChatUtils.print(down);

        double cx = Mth.clamp(eye.x, pos.getX(), pos.getX() + 1);
        double cy = Mth.clamp(eye.y, pos.getY() - down, pos.getY() + down);
        double cz = Mth.clamp(eye.z, pos.getZ(), pos.getZ() + 1);

        Vec3 hit = new Vec3(cx, cy, cz);

        return hit;
    }

    private int getBlockSlot() {
        int selectedSlot = -1;
        int largestStackSize = 0;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack itemStack = mc.player.getInventory().getItem(slot);

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
        Vec3 po = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        return po.distanceTo(pos.getCenter()) > mc.player.blockInteractionRange();
    }
}

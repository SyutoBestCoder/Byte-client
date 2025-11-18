package com.syuto.bytes.utils.impl.player;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Optional;

import static com.syuto.bytes.Byte.mc;

public class PlayerUtil {


    public static double getBiblicallyAccurateDistanceToEntity(Entity target) {
        return mc.player.getEyePos().distanceTo(getClosestPoint(target));
    }

    public static Vec3d getClosestPoint(Entity target) {
        Box hb = target.getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();

        double cx = MathHelper.clamp(eyePos.x, hb.minX, hb.maxX);
        double cy = MathHelper.clamp(eyePos.y, hb.minY, hb.maxY);
        double cz = MathHelper.clamp(eyePos.z, hb.minZ, hb.maxZ);

        return new Vec3d(cx, cy, cz);
    }

    public static HitResult raycast(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d startPos = mc.player.getCameraPosVec(tickDelta);
        Vec3d direction = mc.player.getRotationVector(pitch, yaw);
        Vec3d endPos = startPos.add(direction.multiply(maxDistance));

        HitResult blockHit = mc.world.raycast(new RaycastContext(
                startPos, endPos,
                RaycastContext.ShapeType.COLLIDER,
                includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        EntityHitResult entityHit = raycastEntities(mc.world, mc.player, startPos, endPos, maxDistance);

        if (entityHit != null && (blockHit == null || entityHit.getPos().squaredDistanceTo(startPos) < blockHit.getPos().squaredDistanceTo(startPos))) {
            return entityHit;
        }

        return null;
    }

    public static BlockHitResult raycastBlocks(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d startPos = mc.player.getCameraPosVec(tickDelta);
        Vec3d direction = mc.player.getRotationVector(pitch, yaw);
        Vec3d endPos = startPos.add(direction.multiply(maxDistance));

        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.OUTLINE,
                includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        return blockHit;
    }

    // Entity raycasting method (manual)
    private static EntityHitResult raycastEntities(World world, PlayerEntity player, Vec3d startPos, Vec3d endPos, double maxDistance) {
        EntityHitResult closestEntityHit = null;
        double closestDistanceSq = maxDistance * maxDistance;

        Box searchBox = new Box(startPos, endPos).expand(1.0);

        for (Entity entity : world.getOtherEntities(player, searchBox)) {
            if (!entity.isAlive() || entity.isSpectator()) continue;

            Box entityBox = entity.getBoundingBox().expand(0.1);
            Optional<Vec3d> intersection = entityBox.raycast(startPos, endPos);
            if (intersection.isPresent()) {
                double distanceSq = startPos.squaredDistanceTo(intersection.get());
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntityHit = new EntityHitResult(entity, intersection.get());
                }
            }
        }

        return closestEntityHit;
    }



    public static boolean isHoldingWeapon() {
        if (mc.player.getMainHandStack() != null) {
            Item item = mc.player.getMainHandStack().getItem();
            return item instanceof SwordItem || item instanceof AxeItem;
        }
        return false;
    }




    public static float calcBlockBreakingDelta(BlockPos pos, ItemStack slot) {
        BlockState blockState = mc.world.getBlockState(pos);
        float hardness = blockState.getHardness(mc.world, pos);
        if (hardness == -1.0F) {
            return 0.0F;
        } else {
            //int i = player.canHarvest(state) ? 30 : 100;
            int i = (!blockState.isToolRequired() || slot.isSuitableFor(blockState)) ? 30 : 100;

            float blockBreakingSpeed = slot.getMiningSpeedMultiplier(blockState);
            if (blockBreakingSpeed > 1.0F) {
                float efficiencyMulti = (float) (Math.pow(InventoryUtil.getEnchantLevel(slot, Enchantments.EFFICIENCY), 2) + 1);
                blockBreakingSpeed += efficiencyMulti == 1 ? 0 : efficiencyMulti;
            }

            if (StatusEffectUtil.hasHaste( mc.player)) {
                blockBreakingSpeed *=
                        1.0F +
                                (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
            }

            if ( mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                float g =
                        switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                            case 0 -> 0.3F;
                            case 1 -> 0.09F;
                            case 2 -> 0.0027F;
                            default -> 8.1E-4F;
                        };

                blockBreakingSpeed *= g;
            }

            blockBreakingSpeed *=
                    (float)  mc.player
                            .getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);
            if ( mc.player.isSubmergedIn(FluidTags.WATER)) {
                blockBreakingSpeed *=
                        (float) mc.player
                                .getAttributeInstance(
                                        EntityAttributes.SUBMERGED_MINING_SPEED
                                )
                                .getValue();
            }

            if (!mc.player.isOnGround()) {
                blockBreakingSpeed /= 5.0F;
            }

            return blockBreakingSpeed / hardness / (float) i;
        }
    }

    public static int getFallDistance() {
        var player = mc.player;
        var world = mc.world;

        int startY = player.getBlockY();
        if (player.getY() % 1.0 == 0) startY--;

        int fallDistance = 0;

        for (int y = startY; y >= 0; y--) {
            BlockState block = world.getBlockState(new BlockPos(player.getBlockX(), y, player.getBlockZ()));
            if (!block.isAir() && !block.getBlock().toString().toLowerCase().contains("sign")) {
                fallDistance = startY - y;
                break;
            }
        }

        return fallDistance;
    }
}

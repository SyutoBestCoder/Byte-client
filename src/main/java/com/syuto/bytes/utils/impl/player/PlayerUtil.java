package com.syuto.bytes.utils.impl.player;

import com.syuto.bytes.utils.impl.client.ChatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

import static com.syuto.bytes.Byte.mc;

public class PlayerUtil {

    public static int jumpAge;
    public static int hurtAge;
    public static long lastModTime;

    public static double getBiblicallyAccurateDistanceToEntity(Entity target) {
        return mc.player.getEyePosition().distanceTo(getClosestPoint(target));
    }

    public static Vec3 getClosestPoint(Entity target) {
        AABB hb = target.getBoundingBox();
        Vec3 eyePos = mc.player.getEyePosition();


        double cx = Mth.clamp(eyePos.x, hb.minX, hb.maxX);
        double cy = Mth.clamp(eyePos.y, hb.minY, hb.maxY);
        double cz = Mth.clamp(eyePos.z, hb.minZ, hb.maxZ);

        return new Vec3(cx, cy, cz);
    }

    public static double getBiblicallyAccurateDistanceToCentreOfEntity(Entity target) {
        return mc.player.getEyePosition().distanceTo(getCentrePoint(target));
    }

    public static Vec3 getCentrePoint(Entity target) {
        AABB hb = target.getBoundingBox();
        return hb.getCenter();
    }

    public static HitResult raycast(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 startPos = mc.player.getEyePosition(tickDelta);
        Vec3 direction = mc.player.calculateViewVector(pitch, yaw);
        Vec3 endPos = startPos.add(direction.scale(maxDistance));

        HitResult blockHit = mc.level.clip(new ClipContext(
                startPos, endPos,
                ClipContext.Block.COLLIDER,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        ));

        EntityHitResult entityHit = raycastEntities(mc.level, mc.player, startPos, endPos, maxDistance);

        if (entityHit != null && (blockHit == null || entityHit.getLocation().distanceToSqr(startPos) < blockHit.getLocation().distanceToSqr(startPos))) {
            return entityHit;
        }

        return null;
    }

    public static BlockHitResult raycastBlocks(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 startPos = mc.player.getEyePosition(tickDelta);
        Vec3 direction = mc.player.calculateViewVector(pitch, yaw);
        Vec3 endPos = startPos.add(direction.scale(maxDistance));

        BlockHitResult blockHit = mc.level.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.OUTLINE,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        ));

        return blockHit;
    }

    // Entity raycasting method (manual)
    private static EntityHitResult raycastEntities(Level world, Player player, Vec3 startPos, Vec3 endPos, double maxDistance) {
        EntityHitResult closestEntityHit = null;
        double closestDistanceSq = maxDistance * maxDistance;

        AABB searchBox = new AABB(startPos, endPos).inflate(1.0);

        for (Entity entity : world.getEntities(player, searchBox)) {
            if (!entity.isAlive() || entity.isSpectator()) continue;

            AABB entityBox = entity.getBoundingBox();
            Optional<Vec3> intersection = entityBox.clip(startPos, endPos);
            if (intersection.isPresent()) {
                double distanceSq = startPos.distanceToSqr(intersection.get());
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntityHit = new EntityHitResult(entity, intersection.get());
                }
            }
        }

        return closestEntityHit;
    }


    public static boolean isHoldingWeapon() {
        if (mc.player.getMainHandItem() != null) {
            ItemStack t = mc.player.getMainHandItem();
            Item item = mc.player.getMainHandItem().getItem();
            return t.is(ItemTags.SWORDS) || item instanceof AxeItem;
        }
        return false;
    }

    public static boolean isHoldingMace() {
        if (mc.player.getMainHandItem() != null) {
            Item item = mc.player.getMainHandItem().getItem();
            return item instanceof MaceItem;
        }
        return false;
    }


    public static float calcBlockBreakingDelta(BlockPos pos, ItemStack slot) {
        BlockState blockState = mc.level.getBlockState(pos);
        float hardness = blockState.getDestroySpeed(mc.level, pos);
        if (hardness == -1.0F) {
            return 0.0F;
        } else {
            //int i = player.canHarvest(state) ? 30 : 100;
            int i = (!blockState.requiresCorrectToolForDrops() || slot.isCorrectToolForDrops(blockState)) ? 30 : 100;

            float blockBreakingSpeed = slot.getDestroySpeed(blockState);
            if (blockBreakingSpeed > 1.0F) {
                float efficiencyMulti = (float) (Math.pow(InventoryUtil.getEnchantLevel(slot, Enchantments.EFFICIENCY), 2) + 1);
                blockBreakingSpeed += efficiencyMulti == 1 ? 0 : efficiencyMulti;
            }

            if (MobEffectUtil.hasDigSpeed(mc.player)) {
                blockBreakingSpeed *=
                        1.0F +
                                (float) (MobEffectUtil.getDigSpeedAmplification(mc.player) + 1) * 0.2F;
            }

            if (mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
                float g =
                        switch (mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                            case 0 -> 0.3F;
                            case 1 -> 0.09F;
                            case 2 -> 0.0027F;
                            default -> 8.1E-4F;
                        };

                blockBreakingSpeed *= g;
            }

            blockBreakingSpeed *=
                    (float) mc.player
                            .getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
            if (mc.player.isEyeInFluid(FluidTags.WATER)) {
                blockBreakingSpeed *=
                        (float) mc.player
                                .getAttribute(
                                        Attributes.SUBMERGED_MINING_SPEED
                                )
                                .getValue();
            }

            if (!mc.player.onGround()) {
                blockBreakingSpeed /= 5.0F;
            }

            return blockBreakingSpeed / hardness / (float) i;
        }
    }

    public static int getFallDistance() {
        var player = mc.player;
        var world = mc.level;

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

    public static double getFallDistanceDouble() {
        var player = mc.player;
        var world = mc.level;

        double startY = player.getY();
        double motionY = player.getDeltaMovement().y;

        if (startY % 1.0 == 0) startY -= 1e-5;

        double fallDistance = 0;

        double minY = world.getMinY();

        for (double y = startY; y >= minY; y -= 0.01) {
            BlockState block = world.getBlockState(new BlockPos(player.getBlockX(), (int)Math.floor(y), player.getBlockZ()));
            if (!block.isAir() && !block.getBlock().toString().toLowerCase().contains("sign")) {
                fallDistance = startY - y;
                break;
            }
        }

        if (motionY < 0) fallDistance = -fallDistance;


        return fallDistance;
    }


    public static int getLungeEnchantmentLevel(ItemStack stack, Player player) {

        return 0;
    }

    public static int checkEnchantmentLevelByString(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);

        if (enchantments == null) {
            return 0;
        }

        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            String enchantName = enchantment.getRegisteredName();
            if (enchantName != null && enchantName.contains("lunge")) {
                return enchantments.getLevel(enchantment);
            }
        }

        return 0;
    }


    public static void performLunge(Player player, int lungeLevel) {
        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);

        if (player.isPassenger() || player.isFallFlying() || player.isUnderWater()) {
            return;
        }

        Vec3 lookVec = player.getViewVector(1.0F);


        stack.interactLivingEntity(mc.player, mc.player, InteractionHand.MAIN_HAND);


        float impulse = 0.458f * lungeLevel;

        //player.addVelocity(lookVec.x * impulse, 0, lookVec.z * impulse);


        player.playSound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0f, 1.0f);

    }
}

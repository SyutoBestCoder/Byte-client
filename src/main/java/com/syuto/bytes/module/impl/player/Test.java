package com.syuto.bytes.module.impl.player;

import com.syuto.bytes.eventbus.EventHandler;
import com.syuto.bytes.eventbus.impl.PreMotionEvent;
import com.syuto.bytes.eventbus.impl.PreUpdateEvent;
import com.syuto.bytes.eventbus.impl.RenderWorldEvent;
import com.syuto.bytes.module.Module;
import com.syuto.bytes.module.api.Category;
import com.syuto.bytes.setting.impl.NumberSetting;
import com.syuto.bytes.utils.impl.client.ChatUtils;
import com.syuto.bytes.utils.impl.player.MovementUtil;
import com.syuto.bytes.utils.impl.player.PlayerUtil;
import com.syuto.bytes.utils.impl.player.WorldUtil;
import com.syuto.bytes.utils.impl.render.RenderUtils;
import com.syuto.bytes.utils.impl.rotation.RotationUtils;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import static com.syuto.bytes.Byte.mc;


public class Test extends Module {

    public NumberSetting range = new NumberSetting("range", this, 3, 3, 50, 1);

    public Test() {
        super("Test", "Testing module", Category.PLAYER);
    }


    private BlockPos block, placePos;
    private Direction facing;
    private float[] rots;
    private int ticks;
    List<BlockPos> pos;

    @EventHandler
    void onRenderWorld(RenderWorldEvent event) {
        if (pos != null && !pos.isEmpty()) {
            for (BlockPos po : pos) {
                Block e = mc.level.getBlockState(po).getBlock();
                if (e == Blocks.DIAMOND_ORE || e == Blocks.DEEPSLATE_DIAMOND_ORE) {
                    RenderUtils.renderBlock(po, event, Color.CYAN);
                } else if (e == Blocks.IRON_ORE || e == Blocks.DEEPSLATE_IRON_ORE) {
                    RenderUtils.renderBlock(po, event, Color.WHITE);
                } else if (e == Blocks.COAL_ORE || e == Blocks.DEEPSLATE_COAL_ORE) {
                    RenderUtils.renderBlock(po, event, Color.BLACK);
                } else if (e == Blocks.LAPIS_ORE || e == Blocks.DEEPSLATE_LAPIS_ORE) {
                    RenderUtils.renderBlock(po, event, Color.BLUE);
                } else if (e == Blocks.ANCIENT_DEBRIS) {
                    RenderUtils.renderBlock(po, event, Color.MAGENTA);
                } else if (e == Blocks.EMERALD_ORE || e == Blocks.DEEPSLATE_EMERALD_ORE){
                    RenderUtils.renderBlock(po, event, Color.GREEN);
                } else if (e == Blocks.GOLD_ORE || e == Blocks.DEEPSLATE_GOLD_ORE || e == Blocks.GOLD_BLOCK) {
                    RenderUtils.renderBlock(po, event, Color.YELLOW);
                } else {
                    RenderUtils.renderBlock(po, event, Color.PINK);
                }
            }
        }
    }

    @EventHandler
    void onPreUpdate(PreUpdateEvent event) {
        pos = WorldUtil.findAllOres(mc.player.blockPosition(), range.getValue().intValue());

        /*ticks = mc.player.isOnGround() ? 0 : ticks + 1;

        this.block = WorldUtil.findBlocks(
                mc.player.getBlockPos(),
                3

        );

        if (this.block != null && !mc.player.isOnGround()) {
            this.facing = WorldUtil.getClosest(this.block);
            if (this.facing != null && WorldUtil.canBePlacedOn(this.block)) {
                if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir() && ticks >= 9) {
                    mc.options.sprintKey.setPressed(false);
                    mc.player.setSprinting(false);
                    this.place(block, facing);
                    if (this.block != null) {
                        //ChatUtils.print(this.facing.asString());
                        this.rots = RotationUtils.getBlockRotations(this.block, this.facing);
                    }
                } else {
                    mc.player.setSprinting(true);
                    mc.options.sprintKey.setPressed(true);
                    rots = null;
                }
            }
        }*/
    }


    @EventHandler
    void onPreMotion(PreMotionEvent event) {


        if (this.rots != null) {
           // event.yaw = rots[0];
           // event.pitch = rots[1];
           // RotationUtils.turnHead(event.yaw);
        }
    }

    private void place(BlockPos pos, Direction direction) {
        //ChatUtils.print(direction.asString());
        BlockHitResult result = new BlockHitResult(
                pos.getCenter(),
                direction,
                pos,
                false
        );

        mc.gameMode.useItemOn(mc.player, mc.player.getUsedItemHand(), result);
        mc.player.swing(mc.player.getUsedItemHand());

    }

}
package net.bogdanvalentin.floatater.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FloatatoItem extends BlockItem {
    private static final double FLOATING_PLACE_DISTANCE = 3.0;

    public FloatatoItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F);
        Vec3 reach = eye.add(view.scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(eye, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        ItemStack stack = player.getItemInHand(hand);
        if (hit.getType() == HitResult.Type.MISS) {
            Vec3 target = eye.add(view.scale(FLOATING_PLACE_DISTANCE));
            BlockHitResult floating = new BlockHitResult(target, player.getDirection(), BlockPos.containing(target), false);
            return this.place(new BlockPlaceContext(player, hand, stack, floating));
        }

        return this.useOn(new UseOnContext(player, hand, hit));
    }
}

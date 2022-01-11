package io.github.cleanroommc.airlock.block.mixins;

import io.github.cleanroommc.airlock.api.block.AirlockBlock;
import io.github.cleanroommc.airlock.api.entity.EntityCollisionContext;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(Block.class)
public class BlockMixin implements AirlockBlock {

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos, EntityCollisionContext ctx) {
		return state.getBoundingBox(worldIn, pos);
	}

}

package io.github.cleanroommc.airlock.api.block;

import io.github.cleanroommc.airlock.api.entity.EntityCollisionContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public interface AirlockBlock {

	@Nullable
	AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos, EntityCollisionContext ctx);

}

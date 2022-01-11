package io.github.cleanroommc.airlock.block.mixins;

import io.github.cleanroommc.airlock.api.block.AirlockBlock;
import io.github.cleanroommc.airlock.api.block.AirlockBlockState;
import io.github.cleanroommc.airlock.api.entity.EntityCollisionContext;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockStateContainer.class)
public class BlockStateContainerMixin implements AirlockBlockState {

	@Shadow @Final private Block block;

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos, EntityCollisionContext ctx) {
		return ((AirlockBlock) this.block).getCollisionBoundingBox((IBlockState) this, worldIn, pos, ctx);
	}

}

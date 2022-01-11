package io.github.cleanroommc.airlock.block.mixins;

import io.github.cleanroommc.airlock.api.block.AirlockBlock;
import io.github.cleanroommc.airlock.api.entity.EntityCollisionContext;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Block.class)
public class BlockMixin implements AirlockBlock {

	@Shadow protected static void addCollisionBoxToList(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable AxisAlignedBB blockBox) { }

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos, EntityCollisionContext ctx) {
		return state.getCollisionBoundingBox(worldIn, pos);
	}

	/**
	 * @author Rongmario
	 * @reason Redirect call from state#getCollisionBoundingBox => AirlockBlock#getCollisionBoundingBox
	 */
	@Overwrite
	@Deprecated
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, getCollisionBoundingBox(state, worldIn, pos, EntityCollisionContext.of(entityIn)));
	}

}

package com.cleanroommc.airlock.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * No... this has nothing to do with HotSpot JVM...
 *
 * This utility class provides low-overhead variants of very frequently called methods.
 * Behaviours MAY NOT BE THE SAME. Caveats are listed in their respective javadoc.
 */
public class HotspotUtil {

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    public static IBlockState server$getBlockState(World world, BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return AIR;
        }
        return ((WorldServer) world).getChunkProvider().loadedChunks
                .get((pos.getX() >> 4) & 4294967295L | ((long) (pos.getZ() >> 4) & 4294967295L) << 32)
                .getBlockStorageArray()[pos.getY() << 4].get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    private HotspotUtil() { }

}

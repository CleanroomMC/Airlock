package io.github.cleanroommc.airlock.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;

public class PointOfInterest extends IForgeRegistryEntry.Impl<PointOfInterest> {

	private final Set<IBlockState> matchingStates;
	private final int maxTickets, validRange;

	public PointOfInterest(Set<IBlockState> matchingStates, int maxTickets, int validRange) {
		this.matchingStates = matchingStates;
		this.maxTickets = maxTickets;
		this.validRange = validRange;
	}

	public Set<IBlockState> getMatchingStates() {
		return matchingStates;
	}

	public int getMaxTickets() {
		return maxTickets;
	}

	public int getValidRange() {
		return validRange;
	}

	public boolean matches(IBlockState state) {
		return this.matchingStates.contains(state);
	}

}

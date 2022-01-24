package io.github.cleanroommc.airlock.common.item;

import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

	final List<Pair<ResourceLocation, IItemPropertyGetter>> properties = new ArrayList<>();

	int maxStackSize = 64;
	String translationKey = "";

	public ItemBuilder property(ResourceLocation location, IItemPropertyGetter getter) {
		this.properties.add(Pair.of(location, getter));
		return this;
	}

	public ItemBuilder maxStackSize(int maxStackSize) {
		this.maxStackSize = maxStackSize;
		return this;
	}

	public ItemBuilder translationKey(String translationKey) {
		this.translationKey = translationKey;
		return this;
	}

}

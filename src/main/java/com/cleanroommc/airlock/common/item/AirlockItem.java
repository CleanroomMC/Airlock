package com.cleanroommc.airlock.common.item;

import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

public class AirlockItem extends Item {

	public AirlockItem(ItemBuilder builder) {
		for (Pair<ResourceLocation, IItemPropertyGetter> property : builder.properties) {
			this.addPropertyOverride(property.getLeft(), property.getRight());
		}
		setMaxStackSize(builder.maxStackSize);
		if (!builder.translationKey.isEmpty()) {
			setTranslationKey(builder.translationKey);
		}
	}

}

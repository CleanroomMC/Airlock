package com.cleanroommc.airlock.common.item.metaitem.value;

import com.cleanroommc.airlock.common.item.metaitem.StandardMetaItem;
import net.minecraft.item.ItemStack;

public class StandardMetaValueItem extends MetaValueItem<StandardMetaValueItem, StandardMetaItem> {

    public StandardMetaValueItem(StandardMetaItem item, int meta, String name) {
        super(item, meta, name);
    }

    @Override
    public ItemStack getStack(int amount) {
        return new ItemStack(item, amount, meta);
    }

}

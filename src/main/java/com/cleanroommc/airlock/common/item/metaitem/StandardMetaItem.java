package com.cleanroommc.airlock.common.item.metaitem;

import com.cleanroommc.airlock.common.item.metaitem.value.StandardMetaValueItem;

public abstract class StandardMetaItem extends MetaItem<StandardMetaItem, StandardMetaValueItem> {

    public StandardMetaItem(String domain, String baseId) {
        super(domain, baseId);
    }

    @Override
    protected StandardMetaValueItem constructValueItem(short meta, String name) {
        return new StandardMetaValueItem(this, meta, name);
    }

}

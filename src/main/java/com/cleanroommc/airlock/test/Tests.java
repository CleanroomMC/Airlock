package com.cleanroommc.airlock.test;

import com.cleanroommc.airlock.Airlock;
import com.cleanroommc.airlock.common.item.metaitem.StandardMetaItem;
import com.cleanroommc.airlock.common.item.metaitem.value.StandardMetaValueItem;

import java.util.ArrayList;
import java.util.List;

public class Tests {

    static TestMetaItem testMetaItem;

    public static void preInit() {
        testMetaItem = new TestMetaItem();
    }

    private static class TestMetaItem extends StandardMetaItem {

        List<StandardMetaValueItem> metaValueItems;

        public TestMetaItem() {
            super(Airlock.ID, "test");
        }

        @Override
        public void addValueItems() {
            metaValueItems = new ArrayList<>(Short.MAX_VALUE);
            for (short i = 0; i < Short.MAX_VALUE; i++) {
                metaValueItems.add(addValueItem(i, "id_" + i));
            }
        }

    }

}

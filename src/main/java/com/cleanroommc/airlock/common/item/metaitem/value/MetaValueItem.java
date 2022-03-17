package com.cleanroommc.airlock.common.item.metaitem.value;

import com.cleanroommc.airlock.common.item.metaitem.MetaItem;
import com.cleanroommc.airlock.common.item.metaitem.MetaItemDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class MetaValueItem<S extends MetaValueItem<S, T>, T extends MetaItem<T, S>> {

    protected static final CreativeTabs[] defaultCreativeTabs = new CreativeTabs[] { CreativeTabs.MISC };

    public final T item;
    public final int meta;
    public final String name;

    protected int maxStackSize = 64;
    protected CreativeTabs[] creativeTabs = defaultCreativeTabs;
    protected boolean hidden = false;
    protected MetaItemDefinition metaItemDefinition = MetaItemDefinition.DEFAULT;
    protected String oreDict;
    protected int models = 1;
    protected Map<ResourceLocation, IItemPropertyGetter> propertyGetters;

    public MetaValueItem(T item, int meta, String name) {
        this.item = item;
        this.meta = meta;
        this.name = name;
    }

    public abstract ItemStack getStack(int amount);


    public ItemStack getStack() {
        return getStack(1);
    }

    public void onAdded() {
        if (this.propertyGetters != null) {
            this.propertyGetters.forEach(item::addPropertyOverride);
        }
    }

    public MetaValueItem<S, T> maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    public MetaValueItem<S, T> creativeTab(CreativeTabs creativeTab) {
        if (this.creativeTabs == defaultCreativeTabs) {
            this.creativeTabs = new CreativeTabs[] { creativeTab };
        } else {
            this.creativeTabs = ArrayUtils.add(this.creativeTabs, creativeTab);
        }
        return this;
    }

    public MetaValueItem<S, T> hide() {
        this.hidden = true;
        return this;
    }

    public MetaValueItem<S, T> define(MetaItemDefinition metaItemDefinition) {
        this.metaItemDefinition = metaItemDefinition;
        return this;
    }

    public MetaValueItem<S, T> oreDict(String oreDict) {
        this.oreDict = oreDict;
        return this;
    }

    public MetaValueItem<S, T> models(int models) {
        this.models = models;
        return this;
    }

    public MetaValueItem<S, T> property(ResourceLocation location, IItemPropertyGetter getter) {
        if (this.propertyGetters == null) {
            this.propertyGetters = new Object2ObjectArrayMap<>(1);
        }
        this.propertyGetters.put(location, (stack, world, entity) -> {
            if (stack.getMetadata() == this.meta) {
                return getter.apply(stack, world, entity);
            }
            return 0.0F;
        });
        return this;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public CreativeTabs[] getCreativeTabs() {
        return creativeTabs;
    }

    public boolean isHidden() {
        return hidden;
    }

    public MetaItemDefinition getMetaItemDefinition() {
        return metaItemDefinition;
    }

    @Nullable
    public String getOreDict() {
        return oreDict;
    }

    public int getModelAmount() {
        return models;
    }

}

package com.cleanroommc.airlock.common.util;

import com.cleanroommc.airlock.common.exposer.IStackCapabilityExposer;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class HashUtil {

    public static ItemStackHashStrategy.Builder buildItemStackStrategy() {
        return new ItemStackHashStrategy.Builder();
    }

    private HashUtil() { }

    public static class ItemStackHashStrategy implements Hash.Strategy<ItemStack> {

        private final boolean item, count, metadata, damage, nbtTag, capability;

        private ItemStackHashStrategy(boolean item, boolean count, boolean metadata, boolean damage, boolean nbtTag, boolean capability) {
            this.item = item;
            this.count = count;
            this.metadata = metadata;
            this.damage = damage;
            this.nbtTag = nbtTag;
            this.capability = capability;
        }

        @Override
        public int hashCode(ItemStack stack) {
            int hash = 1;
            if (item) {
                hash = 31 * hash + stack.getItem().hashCode();
            }
            if (count) {
                hash = 31 * hash + stack.getCount();
            }
            if (metadata) {
                hash = 31 * hash + stack.getMetadata();
            }
            if (damage) {
                hash = 31 * hash + stack.getItemDamage();
            }
            if (nbtTag) {
                hash = 31 * hash + (stack.getTagCompound() == null ? 0 : stack.getTagCompound().hashCode());
            }
            if (capability) {
                IStackCapabilityExposer exposer = (IStackCapabilityExposer) (Object) stack;
                hash = 31 * hash + (exposer.airlock$getTag() == null ? 0 : exposer.airlock$getTag().hashCode());
            }
            return hash;
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            boolean equals = false;
            if (item) {
                equals = a.getItem() == b.getItem();
            }
            if (count) {
                equals = a.getCount() == b.getCount();
            }
            if (metadata) {
                equals = a.getMetadata() == b.getMetadata();
            }
            if (damage) {
                equals = a.getItemDamage() == b.getItemDamage();
            }
            if (nbtTag) {
                equals = Objects.equals(a.getTagCompound(), b.getTagCompound());
            }
            if (capability) {
                equals = Objects.equals(((IStackCapabilityExposer) (Object) a).airlock$getTag(), ((IStackCapabilityExposer) (Object) b).airlock$getTag());
            }
            return equals;
        }

        public static class Builder {

            private boolean item = true;
            private boolean count, metadata, damage, nbtTag, capability;

            public Builder avoidItem() {
                this.item = false;
                return this;
            }

            public Builder hashCount() {
                this.count = true;
                return this;
            }

            public Builder hashMetadata() {
                this.metadata = true;
                return this;
            }

            public Builder hashDamage() {
                this.damage = true;
                return this;
            }

            public Builder hashNBTTag() {
                this.nbtTag = true;
                return this;
            }

            public Builder hashCapability() {
                this.capability = true;
                return this;
            }

            public ItemStackHashStrategy build() {
                Preconditions.checkArgument(item || count || metadata || damage || nbtTag || capability, "At least one property of ItemStackHashStrategy needs to be true.");
                return new ItemStackHashStrategy(item, count, metadata, damage, nbtTag, capability);
            }

        }

    }

}

package com.cleanroommc.airlock.common.capability;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

/**
 * Specialized EnergyStorage for items to use when {@link Item#initCapabilities(ItemStack, NBTTagCompound)}
 *
 * Auto-syncing is supported.
 */
public class ItemEnergyStorage extends EnergyStorage {

    private final ItemStack stack;

    private String tagName = "Energy";

    public ItemEnergyStorage(ItemStack stack, int capacity) {
        super(capacity);
        this.stack = stack;
    }

    public ItemEnergyStorage(ItemStack stack, int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
        this.stack = stack;
    }

    public ItemEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
        this.stack = stack;
    }

    public ItemEnergyStorage(ItemStack stack, int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
        this.stack = stack;
    }

    public ItemEnergyStorage setSaveString(String tagName) {
        this.tagName = tagName;
        return this;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (canReceive()) {
            int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate) {
                energy += energyReceived;
                getOrCreateTag().setInteger(this.tagName, energy);
            }
            return energyReceived;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (canExtract()) {
            int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
            if (!simulate) {
                energy -= energyExtracted;
                getOrCreateTag().setInteger(this.tagName, energy);
            }
            return energyExtracted;
        }
        return 0;
    }

    private NBTTagCompound getOrCreateTag() {
        NBTTagCompound tag = this.stack.getTagCompound();
        if (tag == null) {
            stack.setTagCompound(tag = new NBTTagCompound());
        }
        if (!tag.hasKey(this.tagName)) {
            tag.setInteger(this.tagName, this.energy);
        }
        return tag;
    }

}

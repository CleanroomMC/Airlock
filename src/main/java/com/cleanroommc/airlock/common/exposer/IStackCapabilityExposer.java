package com.cleanroommc.airlock.common.exposer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

import javax.annotation.Nullable;

/**
 * Cast this to {@link net.minecraft.item.ItemStack} to grab its capability information
 */
public interface IStackCapabilityExposer {

    @Nullable
    CapabilityDispatcher airlock$getDispatcher();

    @Nullable
    NBTTagCompound airlock$getTag();

}

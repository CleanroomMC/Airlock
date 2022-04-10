package com.cleanroommc.airlock.core.mixins;

import com.cleanroommc.airlock.common.exposer.IStackCapabilityExposer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IStackCapabilityExposer {

    @Shadow private CapabilityDispatcher capabilities;
    @Shadow private NBTTagCompound capNBT;

    @Nullable
    @Override
    public CapabilityDispatcher airlock$getDispatcher() {
        return this.capabilities;
    }

    @Nullable
    @Override
    public NBTTagCompound airlock$getTag() {
        return this.capNBT;
    }

}

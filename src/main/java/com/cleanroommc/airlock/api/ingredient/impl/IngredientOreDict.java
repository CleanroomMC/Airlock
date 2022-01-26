package com.cleanroommc.airlock.api.ingredient.impl;

import com.cleanroommc.airlock.api.ingredient.IIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class IngredientOreDict implements IIngredient<ItemStack> {
    private final String oreName;
    private final int amount;

    public IngredientOreDict(@Nonnull String oreName) {
        this(oreName, 1);
    }

    public IngredientOreDict(@Nonnull String oreName, int amount) {
        this.oreName = oreName;
        this.amount = amount;
    }

    @Override
    public NonNullList<ItemStack> getValidIngredients() {
        NonNullList<ItemStack> output = NonNullList.create();
        for (ItemStack out : OreDictionary.getOres(oreName)) {
            ItemStack addToList = out.copy(); // Don't tamper with forge's ore dictionary list
            addToList.setCount(amount);
            output.add(addToList);
        }
        return output;
    }

    @Override
    public boolean test(ItemStack stack) {
        return testIgnoreCount(stack) && stack.getCount() >= amount;
    }

    @Override
    public boolean testIgnoreCount(ItemStack stack) {
        return stack != null && !stack.isEmpty() &&
                Arrays.asList(Arrays.stream(OreDictionary.getOreIDs(stack)).boxed().toArray(Integer[]::new))
                .contains(OreDictionary.getOreID(oreName)); // See if the item has the oredict
    }

    @Override
    @Nonnull
    public ItemStack consume(ItemStack input) {
        input.shrink(amount);
        return input;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Nonnull
    public String getOreName() {
        return oreName;
    }
}

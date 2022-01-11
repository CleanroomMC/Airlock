package io.github.cleanroommc.airlock;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AirlockAPI {

	public static <T extends IForgeRegistryEntry<T>> T setRegistryName(T type, String modid, String name) {
		return type.setRegistryName(new ResourceLocation(modid, name));
	}

	private static List<Block> blocksToRegister = new ArrayList<>();
	private static List<Item> itemsToRegister = new ArrayList<>();

	public static <T extends Block> T createBlock(T block, @Nullable ItemBlock itemBlock) {
		blocksToRegister.add(block);
		if (itemBlock != null) {
			itemsToRegister.add(itemBlock);
		}
		return block;
	}

	public static <T extends Block> T createBlock(T block, boolean withItemBlock) {
		return withItemBlock ? createBlock(block, new ItemBlock(block)) : createBlock(block, null);
	}

	public static <T extends Block> T createBlock(T block) {
		return createBlock(block, true);
	}

	@SubscribeEvent
	public static void onBlocksRegister(RegistryEvent.Register<Block> event) {
		blocksToRegister.forEach(event.getRegistry()::register);
		blocksToRegister = null;
	}

	@SubscribeEvent
	public static void onItemsRegister(RegistryEvent.Register<Item> event) {
		itemsToRegister.forEach(event.getRegistry()::register);
		itemsToRegister = null;
	}

}

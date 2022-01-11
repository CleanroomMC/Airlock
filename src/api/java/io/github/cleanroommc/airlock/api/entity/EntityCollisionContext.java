package io.github.cleanroommc.airlock.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public final class EntityCollisionContext {

	public static final EntityCollisionContext EMPTY = new EntityCollisionContext(null, false, ItemStack.EMPTY);

	public static EntityCollisionContext of(Entity entity) {
		return entity == null ? EMPTY : new EntityCollisionContext(entity);
	}

	@Nullable private final Entity entity;
	private final boolean descending;
	private final Item heldItem;

	protected EntityCollisionContext(@Nullable Entity entity, boolean descending, ItemStack stack) {
		this.entity = entity;
		this.descending = descending;
		this.heldItem = stack.getItem();
	}

	protected EntityCollisionContext(Entity entity) {
		this.entity = entity;
		this.descending = entity.isSneaking();
		this.heldItem = (entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHeldItemMainhand() : ItemStack.EMPTY).getItem();
	}

	@Nullable
	public Entity getEntity() {
		return entity;
	}

	public boolean isPlayer() {
		return entity instanceof EntityPlayer;
	}

	public boolean isDescending() {
		return descending;
	}

	public boolean isHeldItem(Item item) {
		return heldItem == item;
	}

}

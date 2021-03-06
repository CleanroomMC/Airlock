package com.cleanroommc.airlock.common.item.metaitem;

import com.cleanroommc.airlock.common.item.metaitem.value.MetaValueItem;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

public abstract class MetaItem<S extends MetaItem<S, V>, V extends MetaValueItem<V, S>> extends Item {

    protected static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    private static final List<MetaItem<?, ?>> META_ITEMS = new ArrayList<>();

    static {
        MinecraftForge.EVENT_BUS.register(MetaItem.class);
    }

    public static List<MetaItem<?, ?>> getMetaItems() {
        return Collections.unmodifiableList(META_ITEMS);
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event) {
        getMetaItems().forEach(ami -> {
            ami.addValueItems();
            ami.trimAndFreeze();
            event.getRegistry().register(ami);
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        getMetaItems().forEach(ami -> {
            ami.registerModels();
            ami.registerTextureMesh();
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerColours(ColorHandlerEvent.Item event) {
        getMetaItems().forEach(ami -> ami.registerColour(event));
    }

    protected final String domain;
    protected final String baseId;

    protected V[] metaItems = (V[]) new MetaValueItem[Short.MAX_VALUE];
    protected String[] metaNames = new String[Short.MAX_VALUE];
    protected ModelResourceLocation[][] metaItemsModels = new ModelResourceLocation[Short.MAX_VALUE][0];

    protected boolean frozen = false;

    public MetaItem(String domain, String baseId) {
        this.domain = domain;
        this.baseId = baseId;
        setTranslationKey(baseId);
        setHasSubtypes(true);
        setRegistryName(domain, baseId);
        META_ITEMS.add(this);
    }

    public String getDomain() {
        return domain;
    }

    public String getBaseId() {
        return baseId;
    }

    protected abstract V constructValueItem(short meta, String name);

    public abstract void addValueItems();

    protected final V addValueItem(short meta, String name) {
        Validate.isTrue(!frozen, "MetaItem " + this.getRegistryName() + " is frozen! Not accepting anymore meta values!");
        Validate.inclusiveBetween(0, Short.MAX_VALUE - 1, meta, "MetaItem ID should be in range from 0 to Short.MAX_VALUE-1");
        if (metaItems.length > meta) {
            V currentValueItem = metaItems[meta];
            if (currentValueItem != null) {
                throw new IllegalArgumentException(String.format("MetaItem Value %d is already occupied by MetaItem Value %s (requested by %s)", meta, currentValueItem.name, name));
            }
        }
        V valueItem = constructValueItem(meta, name);
        valueItem.onAdded();
        metaItems[meta] = valueItem;
        metaNames[meta] = name;
        return valueItem;
    }

    @Nullable
    public final V getValueItem(short metaValue) {
        if (metaValue < 0 || metaValue >= metaItems.length) {
            return null;
        }
        return metaItems[metaValue];
    }

    @Nullable
    public final V getValueItem(int metaValue) {
        if (metaValue < 0 || metaValue >= metaItems.length) {
            return null;
        }
        return metaItems[metaValue];
    }

    @Nullable
    public final V getValueItem(ItemStack stack) {
        int metaValue = stack.getMetadata();
        if (metaValue < 0 || metaValue >= metaItems.length) {
            return null;
        }
        return metaItems[metaValue];
    }

    public void registerOreDicts() {
        for (V value : metaItems) {
            if (value.getOreDict() != null) {
                OreDictionary.registerOre(value.getOreDict(), value.getStack());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (short meta = 0; meta < metaItems.length; meta++) {
            V value = metaItems[meta];
            int numberOfModels = value.getModelAmount();
            ModelResourceLocation[] mrls = new ModelResourceLocation[numberOfModels];
            if (numberOfModels > 1) {
                for (int i = 0; i < mrls.length; i++) {
                    ResourceLocation resourceLocation = createItemModelPath(value, "/" + (i + 1));
                    ModelBakery.registerItemVariants(this, resourceLocation);
                    mrls[i] = new ModelResourceLocation(resourceLocation, "inventory");
                }
            } else if (numberOfModels == 1) {
                ResourceLocation mrl = createItemModelPath(value, "");
                ModelBakery.registerItemVariants(this, mrl);
                mrls[0] = new ModelResourceLocation(mrl, "inventory");
            } else {
                throw new IllegalStateException(String.format("%s's MetaItem with the meta %d has no models!", domain, meta));
            }
            metaItemsModels[meta] = mrls;
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerTextureMesh() {
        ModelLoader.setCustomMeshDefinition(this, stack -> {
            int value = stack.getItemDamage();
            if (metaItemsModels.length < value) {
                return MISSING_LOCATION;
            }
            ModelResourceLocation[] mrls = metaItemsModels[stack.getItemDamage()];
            if (mrls == null) {
                return MISSING_LOCATION;
            }
            return mrls[getModelIndex(stack)];
        });
    }

    @SideOnly(Side.CLIENT)
    public void registerColour(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler(this::getColorForItemStack, this);
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation createItemModelPath(V value, String postfix) {
        return new ResourceLocation(domain, formatModelPath(value) + postfix);
    }

    @SideOnly(Side.CLIENT)
    protected String formatModelPath(V value) {
        return "metaitems/" + value.name;
    }

    @SideOnly(Side.CLIENT)
    protected int getModelIndex(ItemStack stack) {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        return 0xFFFFFF;
    }

    // Implementations
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().getDestroySpeed(stack, state);
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onItemRightClick(stack, world, player, hand);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onItemUseFinish(stack, world, entityLiving);
        }
        return super.onItemUseFinish(stack, world, entityLiving);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().hitEntity(stack, target, attacker);
        }
        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onBlockDestroyed(stack, world, state, pos, entityLiving);
        }
        return super.onBlockDestroyed(stack, world, state, pos, entityLiving);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().itemInteractionForEntity(stack, playerIn, target, hand);
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return "item." + valueItem.name;
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onUpdate(stack, world, entity, itemSlot, isSelected);
        } else {
            super.onUpdate(stack, world, entity, itemSlot, isSelected);
        }
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onCreated(stack, world, player);
        } else {
            super.onCreated(stack, world, player);
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getItemUseAction(stack);
        }
        return super.getItemUseAction(stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getMaxItemUseDuration(stack);
        }
        return super.getMaxItemUseDuration(stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onPlayerStoppedUsing(stack, world, entityLiving, timeLeft);
        } else {
            super.onPlayerStoppedUsing(stack, world, entityLiving, timeLeft);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().addInformation(stack, world, tooltip, flag);
        } else {
            super.addInformation(stack, world, tooltip, flag);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().hasEffect(stack);
        }
        return super.hasEffect(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().isEnchantable(stack);
        }
        return super.isEnchantable(stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (V metaItem : metaItems) {
            if (tab == CreativeTabs.SEARCH) {
                items.add(new ItemStack(this, 1, metaItem.meta));
            }
            for (CreativeTabs wantedTab : metaItem.getCreativeTabs()) {
                if (wantedTab == tab) {
                    items.add(new ItemStack(this, 1, metaItem.meta));
                }
            }
        }
    }

    /**
     * Return whether this item is repairable in an anvil.
     *
     * @param toRepair the {@code ItemStack} being repaired
     * @param repair the {@code ItemStack} being used to perform the repair
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        V valueItem = getValueItem(toRepair.getItemDamage());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getIsRepairable(toRepair, repair);
        }
        return super.getIsRepairable(toRepair, repair);
    }

    /* ======================================== FORGE START =====================================*/
    /**
     * ItemStack sensitive version of getItemAttributeModifiers
     */
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getAttributeModifiers(slot, stack);
        }
        return super.getAttributeModifiers(slot, stack);
    }

    /**
     * Called when a player drops the item into the world,
     * returning false from this will prevent the item from
     * being removed from the players inventory and spawning
     * in the world
     *
     * @param player The player that dropped the item
     * @param item The item stack, before the item is removed.
     */
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        V valueItem = getValueItem(item.getItemDamage());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onDroppedByPlayer(item, player);
        }
        return super.onDroppedByPlayer(item, player);
    }

    /**
     * Allow the item one last chance to modify its name used for the
     * tool highlight useful for adding something extra that can't be removed
     * by a user in the displayed name, such as a mode of operation.
     *
     * @param item the ItemStack for the item.
     * @param displayName the name that will be displayed unless it is changed in this method.
     */
    public String getHighlightTip(ItemStack item, String displayName) {
        V valueItem = getValueItem(item.getItemDamage());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getHighlightTip(item, displayName);
        }
        return super.getHighlightTip(item, displayName);
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @param stack The Item Stack
     * @param player The Player that used the item
     * @param world The Current World
     * @param pos Target position
     * @param side The side of the target hit
     * @param hand Which hand the item is being held in.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    /**
     * Determines the amount of durability the mending enchantment
     * will repair, on average, per point of experience.
     */
    public float getXpRepairRatio(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getXpRepairRatio(stack);
        }
        return super.getXpRepairRatio(stack);
    }

    /**
     * Override this method to change the NBT data being sent to the client.
     * You should ONLY override this when you have no other choice, as this might change behavior client side!
     *
     * Note that this will sometimes be applied multiple times, the following MUST be supported:
     * Item item = stack.getItem();
     * NBTTagCompound nbtShare1 = item.getNBTShareTag(stack);
     * stack.setTagCompound(nbtShare1);
     * NBTTagCompound nbtShare2 = item.getNBTShareTag(stack);
     * assert nbtShare1.equals(nbtShare2);
     *
     * @param stack The stack to send the NBT tag for
     * @return The NBT tag
     */
    @Nullable
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getNBTShareTag(stack);
        }
        return super.getNBTShareTag(stack);
    }

    /**
     * Override this method to decide what to do with the NBT data received from getNBTShareTag().
     *
     * @param stack The stack that received NBT
     * @param nbt Received NBT, can be null
     */
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().readNBTShareTag(stack, nbt);
        } else {
            super.readNBTShareTag(stack, nbt);
        }
    }

    /**
     * Called before a block is broken.  Return true to prevent public block harvesting.
     *
     * Note: In SMP, this is called on both client and server sides!
     *
     * @param itemstack The current ItemStack
     * @param pos Block's position in world
     * @param player The Player that is wielding the item
     * @return True to prevent harvesting, false to continue as normal
     */
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        V valueItem = getValueItem(itemstack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onBlockStartBreak(itemstack, pos, player);
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    /**
     * Called each tick while using an item.
     * @param stack The Item being used
     * @param player The Player using the item
     * @param count The amount of time in tick the item has been used for continuously
     */
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            valueItem.getMetaItemDefinition().onUsingTick(stack, player, count);
        } else {
            super.onUsingTick(stack, player, count);
        }
    }

    /**
     * Called when the player Left Clicks (attacks) an entity.
     * Processed before damage is done, if return value is true further processing is canceled
     * and the entity is not attacked.
     *
     * @param stack The Item being used
     * @param player The player that is attacking
     * @param entity The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onLeftClickEntity(stack, player, entity);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    /**
     * ItemStack sensitive version of getContainerItem.
     * Returns a full ItemStack instance of the result.
     *
     * @param itemStack The current ItemStack
     * @return The resulting ItemStack
     */
    public ItemStack getContainerItem(ItemStack itemStack) {
        V valueItem = getValueItem(itemStack.getItemDamage());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getContainerItem(itemStack);
        }
        return super.getContainerItem(itemStack);
    }

    /**
     * ItemStack sensitive version of hasContainerItem
     * @param stack The current item stack
     * @return True if this item has a 'container'
     */
    public boolean hasContainerItem(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().hasContainerItem(stack);
        }
        return super.hasContainerItem(stack);
    }

    /**
     * Retrieves the normal 'lifespan' of this item when it is dropped on the ground as a EntityItem.
     * This is in ticks, standard result is 6000, or 5 mins.
     *
     * @param itemStack The current ItemStack
     * @param world The world the entity is in
     * @return The normal lifespan in ticks.
     */
    public int getEntityLifespan(ItemStack itemStack, World world) {
        V valueItem = getValueItem(itemStack.getItemDamage());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getEntityLifespan(itemStack, world);
        }
        return super.getEntityLifespan(itemStack, world);
    }

    /**
     * Determines if this Item has a special entity for when they are in the world.
     * Is called when a EntityItem is spawned in the world, if true and Item#createCustomEntity
     * returns non null, the EntityItem will be destroyed and the new Entity will be added to the world.
     *
     * @param stack The current item stack
     * @return True of the item has a custom entity, If true, Item#createCustomEntity will be called
     */
    public boolean hasCustomEntity(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().hasCustomEntity(stack);
        }
        return super.hasCustomEntity(stack);
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the EntityItem and will leave it to function normally.
     * Called when the item it placed in a world.
     *
     * @param world The world object
     * @param location The EntityItem object, useful for getting the position of the entity
     * @param itemstack The current item stack
     * @return A new Entity object to spawn or null
     */
    @Nullable
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        V valueItem = getValueItem(itemstack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().createEntity(world, location, itemstack);
        }
        return super.createEntity(world, location, itemstack);
    }

    /**
     * Called by the public implemetation of EntityItem's onUpdate method, allowing for cleaner
     * control over the update of the item without having to write a subclass.
     *
     * @param entityItem The entity Item
     * @return Return true to skip any further update code.
     */
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        V valueItem = getValueItem(entityItem.getItem());
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onEntityItemUpdate(entityItem);
        }
        return super.onEntityItemUpdate(entityItem);
    }

    /**
     * Determines the base experience for a player when they remove this item from a furnace slot.
     * This number must be between 0 and 1 for it to be valid.
     * This number will be multiplied by the stack size to get the total experience.
     *
     * @param item The item stack the player is picking up.
     * @return The amount to award for each item.
     */
    public float getSmeltingExperience(ItemStack item) {
        V valueItem = getValueItem(item);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getSmeltingExperience(item);
        }
        return super.getSmeltingExperience(item);
    }

    /**
     *
     * Should this item, when held, allow sneak-clicks to pass through to the underlying block?
     *
     * @param world The world
     * @param pos Block position in world
     * @param player The Player that is wielding the item
     * @return
     */
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().doesSneakBypassUse(stack, world, pos, player);
        }
        return super.doesSneakBypassUse(stack, world, pos, player);
    }

    /**
     * Allow or forbid the specific book/item combination as an anvil enchant
     *
     * @param stack The item
     * @param book The book
     * @return if the enchantment is allowed
     */
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().isBookEnchantable(stack, book);
        }
        return super.isBookEnchantable(stack, book);
    }

    /**
     * Returns the font renderer used to render tooltips and overlays for this item.
     * Returning null will use the standard font renderer.
     *
     * @param stack The current item stack
     * @return A instance of FontRenderer or null to use default
     */
    @SideOnly(Side.CLIENT)
    @Nullable
    public FontRenderer getFontRenderer(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getFontRenderer(stack);
        }
        return super.getFontRenderer(stack);
    }

    /**
     * Called when a entity tries to play the 'swing' animation.
     *
     * @param entityLiving The entity swinging the item.
     * @param stack The Item stack
     * @return True to cancel any further processing by EntityLiving
     */
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().onEntitySwing(entityLiving, stack);
        }
        return super.onEntitySwing(entityLiving, stack);
    }

    /**
     * Return the itemDamage represented by this ItemStack. Defaults to the itemDamage field on ItemStack, but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    public int getDamage(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getDamage(stack, () -> super.getDamage(stack));
        }
        return super.getDamage(stack);
    }

    /**
     * Determines if the durability bar should be rendered for this item.
     * Defaults to vanilla stack.isDamaged behavior.
     * But modders can use this for any data they wish.
     *
     * @param stack The current Item Stack
     * @return True if it should render the 'durability' bar.
     */
    public boolean showDurabilityBar(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().showDurabilityBar(stack);
        }
        return super.showDurabilityBar(stack);
    }

    /**
     * Queries the percentage of the 'Durability' bar that should be drawn.
     *
     * @param stack The current ItemStack
     * @return 0.0 for 100% (no damage / full bar), 1.0 for 0% (fully damaged / empty bar)
     */
    public double getDurabilityForDisplay(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getDurabilityForDisplay(stack);
        }
        return super.getDurabilityForDisplay(stack);
    }

    /**
     * Returns the packed int RGB value used to render the durability bar in the GUI.
     * Defaults to a value based on the hue scaled based on {@link #getDurabilityForDisplay}, but can be overriden.
     *
     * @param stack Stack to get durability from
     * @return A packed RGB value for the durability colour (0x00RRGGBB)
     */
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getRGBDurabilityForDisplay(stack);
        }
        return super.getRGBDurabilityForDisplay(stack);
    }

    /**
     * Checked from {@link net.minecraft.client.multiplayer.PlayerControllerMP#onPlayerDestroyBlock(BlockPos pos) PlayerControllerMP.onPlayerDestroyBlock()}
     * when a creative player left-clicks a block with this item.
     * Also checked from {@link net.minecraftforge.common.ForgeHooks#onBlockBreakEvent(World, GameType, EntityPlayerMP, BlockPos)}  ForgeHooks.onBlockBreakEvent()}
     * to prevent sending an event.
     * @return true if the given player can destroy specified block in creative mode with this item
     */
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().canDestroyBlockInCreative(world, pos, stack, player);
        }
        return super.canDestroyBlockInCreative(world, pos, stack, player);
    }

    /**
     * ItemStack sensitive version of {@link #canHarvestBlock(IBlockState)}
     * @param state The block trying to harvest
     * @param stack The itemstack used to harvest the block
     * @return true if can harvest the block
     */
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().canHarvestBlock(state, stack);
        }
        return super.canHarvestBlock(state, stack);
    }

    /**
     * Gets the maximum number of items that this stack should be able to hold.
     * This is a ItemStack (and thus NBT) sensitive version of Item.getItemStackLimit()
     *
     * @param stack The ItemStack
     * @return The maximum number this item can be stacked to
     */
    public int getItemStackLimit(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            if (valueItem.getMaxStackSize() == 64) {
                return valueItem.getMetaItemDefinition().getItemStackLimit(stack);
            }
            return valueItem.getMaxStackSize();
        }
        return super.getItemStackLimit(stack);
    }

    public Set<String> getToolClasses(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getToolClasses(stack);
        }
        return super.getToolClasses(stack);
    }

    /**
     * Queries the harvest level of this item stack for the specified tool class,
     * Returns -1 if this tool is not of the specified type
     *
     * @param stack This item stack instance
     * @param toolClass Tool Class
     * @param player The player trying to harvest the given blockstate
     * @param blockState The block to harvest
     * @return Harvest level, or -1 if not the specified tool type.
     */
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getHarvestLevel(stack, toolClass, player, blockState);
        }
        return super.getHarvestLevel(stack, toolClass, player, blockState);
    }

    /**
     * ItemStack sensitive version of getItemEnchantability
     *
     * @param stack The ItemStack
     * @return the item echantability value
     */
    public int getItemEnchantability(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getItemEnchantability(stack);
        }
        return super.getItemEnchantability(stack);
    }

    /**
     * Checks whether an item can be enchanted with a certain enchantment. This applies specifically to enchanting an item in the enchanting table and is called when retrieving the list of possible enchantments for an item.
     * Enchantments may additionally (or exclusively) be doing their own checks in {@link Enchantment#canApplyAtEnchantingTable(ItemStack)}; check the individual implementation for reference.
     * By public this will check if the enchantment type is valid for this item type.
     * @param stack the item stack to be enchanted
     * @param enchantment the enchantment to be applied
     * @return true if the enchantment can be applied to this item
     */
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().canApplyAtEnchantingTable(stack, enchantment);
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    /**
     * Whether this Item can be used as a payment to activate the vanilla beacon.
     * @param stack the ItemStack
     * @return true if this Item can be used
     */
    public boolean isBeaconPayment(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().isBeaconPayment(stack);
        }
        return super.isBeaconPayment(stack);
    }

    /**
     * Determine if the player switching between these two item stacks
     * @param oldStack The old stack that was equipped
     * @param newStack The new stack
     * @param slotChanged If the current equipped slot was changed,
     *                    Vanilla does not play the animation if you switch between two
     *                    slots that hold the exact same item.
     * @return True to play the item change animation
     */
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        V valueItem = getValueItem(oldStack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    /**
     * Called when the player is mining a block and the item in his hand changes.
     * Allows to not reset blockbreaking if only NBT or similar changes.
     * @param oldStack The old stack that was used for mining. Item in players main hand
     * @param newStack The new stack
     * @return True to reset block break progress
     */
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        V valueItem = getValueItem(oldStack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().shouldCauseBlockBreakReset(oldStack, newStack);
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    /**
     * Called while an item is in 'active' use to determine if usage should continue.
     * Allows items to continue being used while sustaining damage, for example.
     *
     * @param oldStack the previous 'active' stack
     * @param newStack the stack currently in the active hand
     * @return true to set the new stack to active and continue using it
     */
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        V valueItem = getValueItem(oldStack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().canContinueUsing(oldStack, newStack);
        }
        return super.canContinueUsing(oldStack, newStack);
    }

    /**
     * Called to get the Mod ID of the mod that *created* the ItemStack,
     * instead of the real Mod ID that *registered* it.
     *
     * For example the Forge Universal Bucket creates a subitem for each modded fluid,
     * and it returns the modded fluid's Mod ID here.
     *
     * Mods that register subitems for other mods can override this.
     * Informational mods can call it to show the mod that created the item.
     *
     * @param itemStack the ItemStack to check
     * @return the Mod ID for the ItemStack, or
     *         null when there is no specially associated mod and {@link #getRegistryName()} would return null.
     */
    @Nullable
    public String getCreatorModId(ItemStack itemStack) {
        V valueItem = getValueItem(itemStack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getCreatorModId(itemStack);
        }
        return super.getCreatorModId(itemStack);
    }

    /**
     * Called from ItemStack.setItem, will hold extra data for the life of this ItemStack.
     * Can be retrieved from stack.getCapabilities()
     * The NBT can be null if this is not called from readNBT or if the item the stack is
     * changing FROM is different then this item, or the previous item had no capabilities.
     *
     * This is called BEFORE the stacks item is set so you can use stack.getItem() to see the OLD item.
     * Remember that getItem CAN return null.
     *
     * @param stack The ItemStack
     * @param nbt NBT of this item serialized, or null.
     * @return A holder instance associated with this ItemStack where you can hold capabilities for the life of this item.
     */
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().initCapabilities(stack, nbt);
        }
        return super.initCapabilities(stack, nbt);
    }

    /**
     * Can this Item disable a shield
     * @param stack The ItemStack
     * @param shield The shield in question
     * @param entity The EntityLivingBase holding the shield
     * @param attacker The EntityLivingBase holding the ItemStack
     * @retrun True if this ItemStack can disable the shield in question.
     */
    public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().canDisableShield(stack, shield, entity, attacker);
        }
        return super.canDisableShield(stack, shield, entity, attacker);
    }

    /**
     * Is this Item a shield
     * @param stack The ItemStack
     * @param entity The Entity holding the ItemStack
     * @return True if the ItemStack is considered a shield
     */
    public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().isShield(stack, entity);
        }
        return super.isShield(stack, entity);
    }

    /**
     * @return the fuel burn time for this itemStack in a furnace.
     * Return 0 to make it not act as a fuel.
     * Return -1 to let the public vanilla logic decide.
     */
    public int getItemBurnTime(ItemStack itemStack) {
        V valueItem = getValueItem(itemStack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getItemBurnTime(itemStack);
        }
        return super.getItemBurnTime(itemStack);
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        V valueItem = getValueItem(stack);
        if (valueItem != null) {
            return valueItem.getMetaItemDefinition().getForgeRarity(stack);
        }
        return super.getForgeRarity(stack);
    }

    private void trimAndFreeze() {
        int stripFrom = -1;
        for (int i = this.metaItems.length - 1; i > 0; i--) {
            V metaItem = this.metaItems[i];
            if (metaItem != null) {
                break;
            }
            stripFrom = i;
        }
        if (stripFrom != -1) {
            this.metaItems = Arrays.copyOf(this.metaItems, stripFrom);
            this.metaNames = Arrays.copyOf(this.metaNames, stripFrom);
            this.metaItemsModels = Arrays.copyOf(this.metaItemsModels, stripFrom);
        }
        this.frozen = true;
    }


}

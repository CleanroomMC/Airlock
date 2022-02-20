package com.cleanroommc.airlock.common.item.metaitem;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AirlockMetaItem<T extends AirlockMetaItem.MetaValue> extends Item {

    protected static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    private static final List<AirlockMetaItem<?>> META_ITEMS = new ArrayList<>();

    public static List<AirlockMetaItem<?>> getMetaItems() {
        return Collections.unmodifiableList(META_ITEMS);
    }

    protected final String domain;
    protected final short metaItemOffset;
    protected final ArrayList<T> metaItems = new ArrayList<>(Short.MAX_VALUE);
    protected final ArrayList<String> metaNames = new ArrayList<>(Short.MAX_VALUE);
    private final ArrayList<ModelResourceLocation[]> metaItemsModels = new ArrayList<>(Short.MAX_VALUE);

    public AirlockMetaItem(String domain, short metaItemOffset) {
        this.domain = domain;
        this.metaItemOffset = metaItemOffset;
        setTranslationKey("meta_item");
        setHasSubtypes(true);
        META_ITEMS.add(this);
    }

    protected abstract T constructValueItem(short meta, String name);

    public final T addValueItem(short meta, String name) {
        Validate.inclusiveBetween(0, Short.MAX_VALUE - 1, meta + metaItemOffset, "MetaItem ID should be in range from 0 to Short.MAX_VALUE-1");
        T currentValueItem = metaItems.get(meta);
        if (currentValueItem != null) {
            throw new IllegalArgumentException(String.format("MetaItem Value %d is already occupied by MetaItem Value %s (requested by %s)", meta, currentValueItem.name, name));
        }
        T valueItem = constructValueItem(meta, name);
        metaItems.set(meta, valueItem);
        metaNames.set(meta, name);
        return valueItem;
    }

    public final T getValueItem(short metaValue) {
        return metaItems.get(metaValue);
    }

    public final void trim() {
        this.metaItems.trimToSize();
        this.metaItemsModels.trimToSize();
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (short meta = 0; meta < metaItems.size(); meta++) {
            T value = metaItems.get(meta);
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
            metaItemsModels.set(metaItemOffset + meta, mrls);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerTextureMesh() {
        ModelLoader.setCustomMeshDefinition(this, stack -> {
            int value = stack.getItemDamage();
            if (metaItemsModels.size() < value) {
                return MISSING_LOCATION;
            }
            ModelResourceLocation[] mrls = metaItemsModels.get(stack.getItemDamage());
            if (mrls == null) {
                return MISSING_LOCATION;
            }
            return mrls[getModelIndex(stack)];
        });
    }

    @SideOnly(Side.CLIENT)
    public void registerColor() {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(this::getColorForItemStack, this);
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation createItemModelPath(T value, String postfix) {
        return new ResourceLocation(domain, formatModelPath(value) + postfix);
    }

    @SideOnly(Side.CLIENT)
    protected String formatModelPath(T value) {
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

    public static class MetaValue {

        protected String name;

        public int getModelAmount() {
            return 1;
        }

    }


}

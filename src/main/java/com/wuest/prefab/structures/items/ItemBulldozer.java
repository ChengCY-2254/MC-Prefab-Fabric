package com.wuest.prefab.structures.items;

import com.wuest.prefab.ClientModRegistry;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Utils;
import com.wuest.prefab.gui.GuiLangKeys;
import com.wuest.prefab.structures.gui.GuiBulldozer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author WuestMan
 */
public class ItemBulldozer extends StructureItem {

    private boolean creativePowered = false;

    /**
     * Initializes a new instance of the {@link ItemBulldozer} class.
     */
    public ItemBulldozer() {
        super(new Item.Properties()
                .durability(4));
    }

    /**
     * Initializes a new instance of the {@link ItemBulldozer} class
     *
     * @param creativePowered - Set this to true to create an always powered bulldozer.
     */
    public ItemBulldozer(boolean creativePowered) {
        super(new Item.Properties());

        this.creativePowered = creativePowered;
    }

    /**
     * Does something when the item is right-clicked.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            if (context.getClickedFace() == Direction.UP && this.getPoweredValue(context.getPlayer(), context.getHand())) {
                // Open the client side gui to determine the house options.
                ClientModRegistry.openGuiForItem(context);
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.FAIL;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext tooltipContext, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, tooltipContext, tooltip, flagIn);

        boolean advancedKeyDown = Screen.hasShiftDown();

        if (!advancedKeyDown) {
            tooltip.add(GuiLangKeys.translateToComponent(GuiLangKeys.SHIFT_TOOLTIP));
        } else {
            if (this.getPoweredValue(stack)) {
                tooltip.addAll(Utils.WrapStringToLiterals(GuiLangKeys.translateString(GuiLangKeys.BULLDOZER_POWERED_TOOLTIP)));
            } else {
                tooltip.addAll(Utils.WrapStringToLiterals(GuiLangKeys.translateString(GuiLangKeys.BULLDOZER_UNPOWERED_TOOLTIP)));
            }
        }
    }

    /**
     * Returns true if this item has an enchantment glint. By default, this returns
     * <code>stack.isItemEnchanted()</code>, but other items can override it (for instance, written books always return
     * true).
     * <p>
     * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
     */
    @Environment(EnvType.CLIENT)
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return this.getPoweredValue(stack) || super.isFoil(stack);
    }

    /**
     * Initializes common fields/properties for this structure item.
     */
    @Override
    protected void Initialize() {
        ModRegistry.guiRegistrations.add(x -> this.RegisterGui(GuiBulldozer.class));
    }

    private boolean getPoweredValue(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        return this.getPoweredValue(stack);
    }

    private boolean getPoweredValue(ItemStack stack) {
        if (this.creativePowered) {
            return true;
        }

        if (stack.getItem() == ModRegistry.Bulldozer) {
            if (stack.getComponents().isEmpty()) {
                CompoundTag baseTag = new CompoundTag();
                CompoundTag prefabTag = new CompoundTag();
                baseTag.put("prefab", prefabTag);
                prefabTag.putBoolean("powered", false);

                CustomData customData = CustomData.of(baseTag);
                stack.set(DataComponents.CUSTOM_DATA, customData);
            } else {
                CustomData customData = stack.getComponents().get(DataComponents.CUSTOM_DATA);

                if (customData != null) {
                    CompoundTag tag = customData.copyTag();

                    if (tag.contains("prefab")) {
                        CompoundTag prefabTag = tag.getCompound("prefab");

                        if (prefabTag.contains("powered")) {
                            return prefabTag.getBoolean("powered");
                        }
                    }
                }
            }
        }

        return false;
    }

    public void setPoweredValue(ItemStack stack, boolean value) {
        CompoundTag baseTag = null;
        CompoundTag prefabTag = null;
        CustomData customData = null;

        if (stack.getComponents().isEmpty()) {
            baseTag = new CompoundTag();
            customData = CustomData.of(baseTag);
        } else {
            customData = stack.getComponents().get(DataComponents.CUSTOM_DATA);
        }

        prefabTag = new CompoundTag();
        baseTag.put("prefab", prefabTag);
        prefabTag.putBoolean("powered", value);
        stack.set(DataComponents.CUSTOM_DATA, customData);
    }
}
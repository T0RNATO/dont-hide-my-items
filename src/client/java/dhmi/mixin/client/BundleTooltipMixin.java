package dhmi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.List;

@Mixin(ClientBundleTooltip.class)
public abstract class BundleTooltipMixin {
	@Unique	private static final int slotSize = 16;
	@Unique	private static final int columns = 8;
	@Unique	private static final DecimalFormat df = new DecimalFormat("#.##");

	@Final
	@Shadow	private BundleContents contents;
	@Shadow	private List<ItemStack> getShownItems(int i) {return null;}
	@Shadow	private int getContentXOffset(int i) {return 0;}
	@Shadow	private void drawSelectedItemTooltip(Font font, GuiGraphics guiGraphics, int i, int j, int k) {}
	@Shadow	private void drawProgressbar(int i, int j, Font font, GuiGraphics guiGraphics) {}
	@Shadow	private int gridSizeY() {return 0;}
	@Shadow	private int itemGridHeight() {return 0;}
	@Shadow private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_back");
	@Shadow private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_front");

	@Inject(method = "slotCount", at = @At("HEAD"), cancellable = true)
	private void modifySlotCount(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(this.contents.size());
	}

	@ModifyConstant(method = {"itemGridHeight"}, constant = @Constant(intValue = 24))
	private int slotHeight(int original) {
		return BundleTooltipMixin.slotSize;
	}

	@ModifyConstant(method = "gridSizeY", constant = @Constant(intValue = 4))
	private int numColumns(int original) {
		return BundleTooltipMixin.columns;
	}

	@ModifyConstant(method = {"getWidth", "getContentXOffset", "drawProgressbar"}, constant = @Constant(intValue = 96))
	private int tooltipWidth(int original) {
		return BundleTooltipMixin.slotSize * BundleTooltipMixin.columns;
	}

	@ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
	private int barProgress(int original) {
		return BundleTooltipMixin.columns * BundleTooltipMixin.slotSize - 2;
	}

	@ModifyConstant(method = "drawProgressbar", constant = @Constant(intValue = 48))
	private int fillText(int original) {
		return BundleTooltipMixin.columns * BundleTooltipMixin.slotSize / 2;
	}

	@ModifyReturnValue(method = "getProgressBarFillText", at = @At("RETURN"))
	private Component addFillText(Component original) {
		if (original == null) {
			var frac = this.contents.weight();
			double scaling = frac.getDenominator() / 64.;
			if (scaling == 0) {
				return null;
			}
			String scaled = df.format(frac.getNumerator() / scaling);
			return Component.literal(scaled + "/64");
		} else {
			return original;
		}
	}

	/**
	 * @author TorNato
	 * @reason easier this way
	 */
	@Overwrite
	private void renderBundleWithItemsTooltip(Font font, int i, int j, int k, int l, GuiGraphics guiGraphics) {
		List<ItemStack> list = this.getShownItems(this.contents.getNumberOfItemsToShow());
		int index = 1;

		for(int row = 0; row < this.gridSizeY(); ++row) {
			for(int col = 0; col < BundleTooltipMixin.columns; ++col) {
				if (index > list.size()) {
					break;
				}
				int x = i + col * BundleTooltipMixin.slotSize;
				int y = j + row * BundleTooltipMixin.slotSize;
				this.renderSlot(index, x, y, list, index, font, guiGraphics);
				++index;
			}
		}

		this.drawSelectedItemTooltip(font, guiGraphics, i, j, k);
		this.drawProgressbar(i + this.getContentXOffset(k), j + this.itemGridHeight() + 4, font, guiGraphics);
	}

	/**
	 * @author TorNato
	 * @reason easier this way
	 */
	@Overwrite
	private void renderSlot(int i, int j, int k, List<ItemStack> list, int l, Font font, GuiGraphics guiGraphics) {
		int index = list.size() - i;
		boolean selected = index == this.contents.getSelectedItem();
		ItemStack itemStack = list.get(index);
		if (selected) {
			guiGraphics.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_SPRITE, j - 4, k - 4, 24, 24);
		}

		guiGraphics.renderItem(itemStack, j, k, l);
		guiGraphics.renderItemDecorations(font, itemStack, j, k);

		if (selected) {
			guiGraphics.blitSprite(RenderType::guiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_SPRITE, j - 4, k - 4, 24, 24);
		}
	}
}
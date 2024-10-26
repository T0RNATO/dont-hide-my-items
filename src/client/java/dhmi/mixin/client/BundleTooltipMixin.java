package dhmi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.List;

@Mixin(ClientBundleTooltip.class)
public abstract class BundleTooltipMixin {
	@Unique
	private static final int slotSize = 16;
	@Unique
	private static final int columns = 8;
	@Unique
	private static final DecimalFormat df = new DecimalFormat("#.##");

	@Final
	@Shadow
	private BundleContents contents;
	@Shadow
	private List<ItemStack> getShownItems(int i) {return null;}
	@Shadow
	private int getContentXOffset(int i) {return 0;}
	@Shadow
	private void drawSelectedItemTooltip(Font font, GuiGraphics guiGraphics, int i, int j, int k) {}
	@Shadow
	private void drawProgressbar(int i, int j, Font font, GuiGraphics guiGraphics) {}
	@Shadow
	private int gridSizeY() {return 0;}
	@Shadow
	private int itemGridHeight() {return 0;}
	@Shadow
	private void renderSlot(int o, int r, int s, List<ItemStack> list, int o1, Font font, GuiGraphics guiGraphics) {}

	@Inject(method = "slotCount", at = @At("HEAD"), cancellable = true)
	private void modifySlotCount(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(this.contents.size());
	}

	@ModifyConstant(method = {"itemGridHeight", "renderSlot"}, constant = @Constant(intValue = 24))
	private int slotHeight(int original) {
		return BundleTooltipMixin.slotSize;
	}

	@ModifyConstant(method = "renderSlot", constant = @Constant(intValue = 4))
	private int modifyPadding(int original) {
		return 0;
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

	/***/
	@Overwrite
	private void renderBundleWithItemsTooltip(Font font, int i, int j, int k, int l, GuiGraphics guiGraphics) {
		List<ItemStack> list = this.getShownItems(this.contents.getNumberOfItemsToShow());
		int o = 1;

		for(int p = 0; p < this.gridSizeY(); ++p) {
			for(int q = 0; q < BundleTooltipMixin.columns; ++q) {
				if (o > list.size()) {
					break;
				}
				int r = i + q * BundleTooltipMixin.slotSize;
				int s = j + p * BundleTooltipMixin.slotSize;
				this.renderSlot(o, r, s, list, o, font, guiGraphics);
				++o;
			}
		}

		this.drawSelectedItemTooltip(font, guiGraphics, i, j, k);
		this.drawProgressbar(i + this.getContentXOffset(k), j + this.itemGridHeight() + 4, font, guiGraphics);
	}
}
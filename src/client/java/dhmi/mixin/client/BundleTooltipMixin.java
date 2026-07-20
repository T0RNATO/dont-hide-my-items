package dhmi.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ClientBundleTooltip.class)
public abstract class BundleTooltipMixin {
	@Unique	private static final int slotSize = 17;
	@Unique	private static final int columns = 8;
	@Unique	private static final DecimalFormat df = new DecimalFormat("#.##");

	@Final @Shadow private BundleContents contents;

	@Inject(method = "slotCount", at = @At("HEAD"), cancellable = true)
	private void modifySlotCount(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(this.contents.size());
	}

	@ModifyReturnValue(method = "getProgressBarFillText", at = @At("RETURN"))
	private static Component addFillText(Component original, Fraction weight) {
		if (original == null) {
			double scaling = weight.getDenominator() / 64.;
			if (scaling == 0) {
				return null;
			}
			String scaled = df.format(weight.getNumerator() / scaling);
			return Component.literal(scaled + "/64");
		} else {
			return original;
		}
	}

	@ModifyExpressionValue(method = "extractBundleWithItemsTooltip", at = @At(value = "CONSTANT", args = "intValue=4", ordinal = 0))
	private int getColumns(int original) {
		return columns;
	}

	@ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
	private int modifyWidth(int original) {
		return slotSize * columns;
	}

	@ModifyExpressionValue(method = "getProgressBarFill", at = @At(value = "CONSTANT", args = "intValue=94"))
	private static int barProgress(int original) {
		return columns * slotSize - 2;
	}

	@ModifyExpressionValue(method = "extractProgressbar", at = @At(value = "CONSTANT", args = "intValue=48"))
	private static int fillText(int original) {
		return columns * slotSize / 2;
	}

	@ModifyExpressionValue(method = "extractBundleWithItemsTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BundleContents;size()I"))
	private int modifySize(int original) {
		return 0;
	}

	@ModifyExpressionValue(method = "extractProgressbar", at = @At(value = "CONSTANT", args = "intValue=96"))
	private static int progressBarWidth(int original) {
		return columns * slotSize;
	}

	@ModifyExpressionValue(method = "gridSizeY", at = @At(value = "CONSTANT", args = "intValue=4"))
	private static int gridSizeY(int original) {
		return columns;
	}

	@ModifyExpressionValue(method = "itemGridHeight", at = @At(value = "CONSTANT", args = "intValue=24"))
	private static int gridHeight(int original) {
		return slotSize;
	}

	@Definition(id = "drawX", local = @Local(type = int.class, name = "drawX"))
	@Expression("drawX = @(?)")
	@ModifyExpressionValue(method = "extractBundleWithItemsTooltip", at = @At("MIXINEXTRAS:EXPRESSION"))
	private int modifyDrawX(int value, final Font font, final int x, @Local(name = "columnNumber") int columnNumber) {
		return x + (columnNumber - 1) * slotSize - 4;
	}

	@Definition(id = "drawY", local = @Local(type = int.class, name = "drawY"))
	@Expression("drawY = @(?)")
	@ModifyExpressionValue(method = "extractBundleWithItemsTooltip", at = @At("MIXINEXTRAS:EXPRESSION"))
	private int modifyDrawY(int value, final Font font, final int x, final int y, @Local(name = "rowNumber") int rowNumber) {
		return y + (rowNumber - 1) * slotSize - 4;
	}

	@Definition(id = "blitSprite", method = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")
	@Definition(id = "SLOT_BACKGROUND_SPRITE", field = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientBundleTooltip;SLOT_BACKGROUND_SPRITE:Lnet/minecraft/resources/Identifier;")
	@Expression("?.blitSprite(?, SLOT_BACKGROUND_SPRITE, ?, ?, ?, ?)")
	@Redirect(method = "extractSlot", at = @At("MIXINEXTRAS:EXPRESSION"))
	private void foo(GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height) {}

	@Overwrite
	private static int getContentXOffset(int x) {
		return 0;
	}
}
package dhmi.mixin.client;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleMouseActions.class)
public abstract class BundleMouseActionsMixin {
    @Shadow protected abstract void toggleSelectedBundleItem(ItemStack bundleItem, int slotIndex, int selectedItem);

    // Invert scrolling direction because items are rendered in the opposite order now
    @ModifyVariable(method = "onMouseScrolled", at = @At(value = "STORE", ordinal = 0), name = "wheel")
    private int modifyScrollAmount(int original, @Share("wheel") LocalIntRef wheel) {
        wheel.set(original);
        return original * -1;
    }

    // jump lines using shift
    @Inject(method = "onMouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BundleItem;getSelectedItemIndex(Lnet/minecraft/world/item/ItemStack;)I"), cancellable = true)
    private void handleShiftScroll(double scrollX, double scrollY, int slotIndex, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, @Share("wheel") LocalIntRef wheel) {
        int selectedItem = BundleItem.getSelectedItemIndex(itemStack);
        if (selectedItem == -1 || !Minecraft.getInstance().hasShiftDown()) return;
        int numItems = BundleItem.getNumberOfItemsToShow(itemStack);

        int updated = Mth.clamp(selectedItem + (wheel.get() * 8), 0, numItems - 1);

        if (updated != selectedItem) this.toggleSelectedBundleItem(itemStack, slotIndex, updated);
        cir.setReturnValue(true);
    }
}
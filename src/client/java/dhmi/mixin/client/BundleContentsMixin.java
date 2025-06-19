package dhmi.mixin.client;

import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BundleContents.class)
public class BundleContentsMixin {
    @Redirect(method = "getNumberOfItemsToShow", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    public int getNumberOfItemsToShow(int a, int b) {
        return a;
    }
}

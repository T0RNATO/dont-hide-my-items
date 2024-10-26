package dhmi.mixin.client;

import net.minecraft.client.gui.BundleMouseActions;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Debug(export = true)
@Mixin(BundleMouseActions.class)
public class BundleMouseActionsMixin {
    // Invert scrolling direction because items are rendered in the opposite order now
    @ModifyVariable(method = "onMouseScrolled", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    private int modifyScrollAmount(int original) {
        return original * -1;
    }
}
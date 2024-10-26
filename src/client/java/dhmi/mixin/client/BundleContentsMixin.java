package dhmi.mixin.client;

import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BundleContents.class)
public class BundleContentsMixin {
    @Shadow
    public int size() { return 0; }

    // Make the bundle always show all items (probably shouldn't use Overwrite but oh well)
    /***/
    @Overwrite
    public int getNumberOfItemsToShow() {
        return this.size();
    }
}

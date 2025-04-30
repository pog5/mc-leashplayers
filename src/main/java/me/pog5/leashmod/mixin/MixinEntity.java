package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import me.pog5.leashmod.LeashProxyEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(at = @At("HEAD"), method = "canUsePortals", cancellable = true)
    private void canUsePortals(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LeashProxyEntity) {
            cir.setReturnValue(false);
            cir.cancel();
        } else if ((Object) this instanceof LeashImpl impl) {
            if (impl.leashplayers$getHolder() != null) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}

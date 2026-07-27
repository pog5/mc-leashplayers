package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAttachedEntity.class)
public class MixinBlockAttachedEntity {
    @Inject(at = @At("HEAD"), method = "skipAttackInteraction", cancellable = true)
    private void leashplayers$onInteractBlockDefaultTriger(Entity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (attacker instanceof LeashImpl impl && impl.leashplayers$shouldCancel()) {
            cir.setReturnValue(true);
        }
    }
}

package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashFenceKnotEntity.class)
public class MixinLeashKnotEntity {
    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    private void leashplayers$onInteract(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof LeashImpl impl && impl.leashplayers$shouldCancel()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}

package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayerEntity {
    @Inject(method = "interactOn", at = @At("RETURN"), cancellable = true)
    private void leashplayers$onInteract(Entity entity, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> info) {
        if (info.getReturnValue() != InteractionResult.PASS) return;

        if ((Object) this instanceof ServerPlayer player && entity instanceof LeashImpl impl) {
            info.setReturnValue(impl.leashplayers$interact(player, hand));
        }
    }
}

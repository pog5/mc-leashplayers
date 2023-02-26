package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    @Inject(method = "interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"), cancellable = true)
    private void leashplayers$onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (info.getReturnValue() != ActionResult.PASS) return;
        if ((Object) this instanceof ServerPlayerEntity && entity instanceof LeashImpl) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            LeashImpl impl = (LeashImpl) entity;
            info.setReturnValue(impl.leashplayers$interact(player, hand));
            info.cancel();
        }
    }
}

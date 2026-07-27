package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashMod;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Register custom rules at the end of GameRules class-init — while the game_rule registry
// is still writable (it freezes after bootstrap).
@Mixin(GameRules.class)
public class MixinGameRules {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void leashmod$registerCustomRules(CallbackInfo ci) {
        LeashMod.registerRules();
    }
}

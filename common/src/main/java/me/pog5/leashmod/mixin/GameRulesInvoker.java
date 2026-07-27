package me.pog5.leashmod.mixin;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the private static rule-registration helpers so custom rules can be added on any loader. */
@Mixin(GameRules.class)
public interface GameRulesInvoker {
    @Invoker("registerBoolean")
    static GameRule<Boolean> leashmod$registerBoolean(String name, GameRuleCategory category, boolean defaultValue) {
        throw new AssertionError();
    }

    @Invoker("registerInteger")
    static GameRule<Integer> leashmod$registerInteger(String name, GameRuleCategory category, int defaultValue, int minValue) {
        throw new AssertionError();
    }
}

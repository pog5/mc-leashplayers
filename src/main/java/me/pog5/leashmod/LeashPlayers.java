package me.pog5.leashmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public final class LeashPlayers implements ModInitializer {
    private static GameRules.Key<GameRules.BooleanRule> ruleEnabled;
    private static GameRules.Key<DoubleRule> ruleDistanceMin;
    private static GameRules.Key<DoubleRule> ruleDistanceMax;

    public static LeashSettings getSettings(World world) {
        return new LeashSettings() {
            private GameRules getGameRules() {
                return world.getGameRules();
            }

            @Override
            public boolean isEnabled() {
                return getGameRules().getBoolean(ruleEnabled);
            }

            @Override
            public double getDistanceMin() {
                return getGameRules().get(ruleDistanceMin).get();
            }

            @Override
            public double getDistanceMax() {
                return getGameRules().get(ruleDistanceMax).get();
            }
        };
    }

    @Override
    public void onInitialize() {
        ruleEnabled = GameRuleRegistry.register("leashPlayersEnabled", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ruleDistanceMin = GameRuleRegistry.register("leashPlayersDistanceMin", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(4.0D));
        ruleDistanceMax = GameRuleRegistry.register("leashPlayersDistanceMax", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(10.0D));
    }
}

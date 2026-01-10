package me.pog5.leashmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LeashPlayers implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("leashmod");

    private static GameRule<Boolean> ruleEnabled;
    private static GameRule<Double> ruleDistanceMin;
    private static GameRule<Double> ruleDistanceMax;
    private static GameRule<Boolean> ruleAllowLeashedRemoveFenceKnot;

    private static final Identifier ruleEnabledIdentifier =
        Identifier.of("leashmod", "enabled");
    private static final Identifier ruleDistanceMinIdentifier =
        Identifier.of("leashmod", "distance_min");
    private static final Identifier ruleDistanceMaxIdentifier =
        Identifier.of("leashmod", "distance_max");
    private static final Identifier ruleAllowLeashedRemoveFenceKnotIdentifier =
        Identifier.of("leashmod", "allow_leashed_remove_fence_knot");

    public static LeashSettings getSettings(ServerWorld world) {
        return new LeashSettings() {
            private GameRules getGameRules() {
                return world.getGameRules();
            }

            @Override
            public boolean isEnabled() {
                return getGameRules().getValue(ruleEnabled);
            }

            @Override
            public double getDistanceMin() {
                return getGameRules().getValue(ruleDistanceMin);
            }

            @Override
            public double getDistanceMax() {
                return getGameRules().getValue(ruleDistanceMax);
            }

            @Override
            public boolean allowLeashedRemoveFenceKnot() {
                return getGameRules().getValue(ruleAllowLeashedRemoveFenceKnot);
            }
        };
    }

    @Override
    public void onInitialize() {
        ruleEnabled = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(ruleEnabledIdentifier);
        ruleDistanceMin = GameRuleBuilder
            .forDouble(4.0D)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(ruleDistanceMinIdentifier);
        ruleDistanceMax = GameRuleBuilder
            .forDouble(10.0D)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(ruleDistanceMaxIdentifier);
        ruleAllowLeashedRemoveFenceKnot = GameRuleBuilder
            .forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(ruleAllowLeashedRemoveFenceKnotIdentifier);
        LOGGER.info("Initialized LeashPlayers");
    }
}

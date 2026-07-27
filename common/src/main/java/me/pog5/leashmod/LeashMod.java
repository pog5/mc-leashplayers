package me.pog5.leashmod;

import me.pog5.leashmod.mixin.GameRulesInvoker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LeashMod {
    public static final String MOD_ID = "leashmod";
    public static final Logger LOGGER = LoggerFactory.getLogger("leashmod");

    // Assigned by registerRules(), invoked from a mixin on GameRules.<clinit> TAIL. In 1.21.11+
    // game rules live in a registry that freezes after bootstrap, so they must be added during
    // GameRules class-init (before freeze), NOT at mod-init.
    public static GameRule<Boolean> RULE_ENABLED;
    public static GameRule<Integer> RULE_DISTANCE_MIN;
    public static GameRule<Integer> RULE_DISTANCE_MAX;
    public static GameRule<Boolean> RULE_ALLOW_REMOVE_KNOT;

    public static void registerRules() {
        RULE_ENABLED = GameRulesInvoker.leashmod$registerBoolean(
            "leash_players_enabled", GameRuleCategory.PLAYER, true);
        RULE_DISTANCE_MIN = GameRulesInvoker.leashmod$registerInteger(
            "leash_players_distance_min", GameRuleCategory.PLAYER, 4, 0);
        RULE_DISTANCE_MAX = GameRulesInvoker.leashmod$registerInteger(
            "leash_players_distance_max", GameRuleCategory.PLAYER, 10, 1);
        RULE_ALLOW_REMOVE_KNOT = GameRulesInvoker.leashmod$registerBoolean(
            "leash_players_allow_leashed_remove_fence_knot", GameRuleCategory.PLAYER, false);
    }

    /** Loader entrypoints call this (rules register separately, from GameRules.<clinit>). */
    public static void init() {
        LOGGER.info("Initialized LeashPlayers");
    }

    public static LeashSettings getSettings(ServerLevel level) {
        final GameRules rules = level.getGameRules();
        return new LeashSettings() {
            @Override
            public boolean isEnabled() {
                return rules.get(RULE_ENABLED);
            }

            @Override
            public int getDistanceMin() {
                return rules.get(RULE_DISTANCE_MIN);
            }

            @Override
            public int getDistanceMax() {
                return rules.get(RULE_DISTANCE_MAX);
            }

            @Override
            public boolean allowLeashedRemoveFenceKnot() {
                return rules.get(RULE_ALLOW_REMOVE_KNOT);
            }
        };
    }
}

package me.pog5.leashmod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface LeashImpl {
    Entity leashplayers$getHolder();
    boolean leashplayers$shouldCancel();
    InteractionResult leashplayers$interact(Player player, InteractionHand hand);
}

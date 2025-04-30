package me.pog5.leashmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public interface LeashImpl {
    Entity leashplayers$getHolder();
    boolean leashplayers$shouldCancel();
    ActionResult leashplayers$interact(PlayerEntity player, Hand hand);
}

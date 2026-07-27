package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashProxyEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Turtle.class)
public abstract class MixinTurtleEntity {
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void leashplayers$onReadCustomData(CallbackInfo info) {
        Turtle self = (Turtle) (Object) this;

        MinecraftServer server = self.level().getServer();
        if (server == null) return;

        PlayerTeam team = server.getScoreboard().getPlayerTeam(self.getScoreboardName());
        if (team != null && Objects.equals(team.getName(), LeashProxyEntity.TEAM_NAME)) {
            self.setInvulnerable(false);
            self.setHealth(0.0F);
            self.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}

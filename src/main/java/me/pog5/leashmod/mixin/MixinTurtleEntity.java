package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashProxyEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TurtleEntity.class)
public abstract class MixinTurtleEntity {
    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void leashplayers$onReadCustomDataFromNbt(CallbackInfo info) {
        TurtleEntity self = (TurtleEntity) (Object) this;

        MinecraftServer server = self.getServer();
        if (server == null) return;

        Team team = server.getScoreboard().getPlayerTeam(self.getEntityName());
        if (team != null && Objects.equals(team.getName(), LeashProxyEntity.TEAM_NAME)) {
            self.setInvulnerable(false);
            self.setHealth(0.0F);
        }
    }
}

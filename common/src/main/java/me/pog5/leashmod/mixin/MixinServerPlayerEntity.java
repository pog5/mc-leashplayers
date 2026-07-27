package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import me.pog5.leashmod.LeashMod;
import me.pog5.leashmod.LeashProxyEntity;
import me.pog5.leashmod.LeashSettings;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity implements LeashImpl {
    @Unique
    private ServerPlayer getSelf() {
        return (ServerPlayer) (Object) this;
    }

    @Shadow
    public abstract boolean hasDisconnected();

    @Unique
    private final LeashSettings leashplayers$settings = LeashMod.getSettings(getSelf().level());

    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;

    @Override
    public Entity leashplayers$getHolder() {
        return this.leashplayers$holder;
    }

    @Override
    public boolean leashplayers$shouldCancel() {
        return leashplayers$getHolder() != null && !leashplayers$settings.allowLeashedRemoveFenceKnot();
    }

    @Unique
    private int leashplayers$lastage;

    @Unique
    private boolean leashplayers$disabled() {
        return !leashplayers$settings.isEnabled();
    }

    @Unique
    private void leashplayers$update() {
        if (
            leashplayers$holder != null && (
                leashplayers$disabled()
                || !leashplayers$holder.isAlive()
                || hasDisconnected()
                || getSelf().isPassenger()
            )
        ) {
            leashplayers$detach();
            leashplayers$drop();
        }

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy = null;
            } else {
                leashplayers$proxy.setInvisible(true);
                Entity holderActual = leashplayers$holder;
                Entity holderTarget = leashplayers$proxy.getLeashHolder();

                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach();
                    leashplayers$drop();
                } else if (holderTarget != holderActual) {
                    leashplayers$attach(holderTarget);
                }
            }
        }

        leashplayers$apply();
    }

    @Unique
    private void leashplayers$apply() {
        ServerPlayer player = getSelf();
        Entity holder = leashplayers$holder;
        if (holder == null) return;
        if (holder.level() != player.level()) return;

        float distance = player.distanceTo(holder);
        if (distance < leashplayers$settings.getDistanceMin()) {
            return;
        }
        if (distance > leashplayers$settings.getDistanceMax()) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        double dx = (holder.getX() - player.getX()) / (double) distance;
        double dy = (holder.getY() - player.getY()) / (double) distance;
        double dz = (holder.getZ() - player.getZ()) / (double) distance;

        player.push(
            Math.copySign(dx * dx * 0.4D, dx),
            Math.copySign(dy * dy * 0.4D, dy),
            Math.copySign(dz * dz * 0.4D, dz)
        );

        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.hurtMarked = false;
    }

    @Unique
    private void leashplayers$attach(Entity holder) {
        leashplayers$holder = holder;
        ServerPlayer leashed = getSelf();

        if (leashplayers$proxy == null) {
            leashplayers$proxy = new LeashProxyEntity(leashed);
            leashed.level().addFreshEntity(leashplayers$proxy);
            leashplayers$proxy.snapTo(leashed.getX(), leashed.getY(), leashed.getZ(), 0.0F, 0.0F);
            leashplayers$proxy.snapTo(new Vec3(leashed.getX(), leashed.getY(), leashed.getZ()));
        }
        leashplayers$proxy.setLeashedTo(leashplayers$holder, true);
        leashplayers$lastage = leashed.tickCount;
    }

    @Unique
    private void leashplayers$detach() {
        leashplayers$holder = null;

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.isAlive() || !leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy.proxyRemove();
            }
            leashplayers$proxy = null;
        }
    }

    @Unique
    private void leashplayers$drop() {
        getSelf().spawnAtLocation(getSelf().level(), Items.LEAD);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Override
    public InteractionResult leashplayers$interact(Player player, InteractionHand hand) {
        if (leashplayers$disabled()) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.LEAD) && leashplayers$holder == null) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            leashplayers$attach(player);
            return InteractionResult.SUCCESS;
        }

        if (leashplayers$holder == player && leashplayers$lastage + 20 < getSelf().tickCount) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Inject(method = "disconnect", at = @At("RETURN"))
    private void leashplayers$disconnect(CallbackInfo ci) {
        if (leashplayers$holder != null) {
            leashplayers$detach();
            leashplayers$drop();
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void leashplayers$onDeath(CallbackInfo ci) {
        if (leashplayers$holder != null) {
            leashplayers$detach();
            leashplayers$drop();
        }
    }
}

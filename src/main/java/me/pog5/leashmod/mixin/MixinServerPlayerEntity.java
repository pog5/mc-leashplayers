package me.pog5.leashmod.mixin;

import me.pog5.leashmod.LeashImpl;
import me.pog5.leashmod.LeashPlayers;
import me.pog5.leashmod.LeashProxyEntity;
import me.pog5.leashmod.LeashSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity implements LeashImpl {
    @Shadow
    public abstract ServerWorld getServerWorld();

    @Unique
    private ServerPlayerEntity getSelf() {
        return (ServerPlayerEntity)(Object)this;
    }

    @Shadow
    public abstract boolean isDisconnected();

    @Unique
    private final LeashSettings leashplayers$settings = LeashPlayers.getSettings(this.getServerWorld());

    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;

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
                || !getSelf().isAlive()
                || isDisconnected()
                || getSelf().hasVehicle()
            )
        ) {
            leashplayers$detach();
            leashplayers$drop();
        }

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy = null;
            }
            else {
                Entity holderActual = leashplayers$holder;
                Entity holderTarget = leashplayers$proxy.getLeashHolder();

                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach();
                    leashplayers$drop();
                }
                else if (holderTarget != holderActual) {
                    leashplayers$attach(holderTarget);
                }
            }
        }

        leashplayers$apply();
    }

    @Unique
    private void leashplayers$apply() {
        ServerPlayerEntity player = getSelf();
        Entity holder = leashplayers$holder;
        if (holder == null) return;
        if (holder.getWorld() != player.getWorld()) return;

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

        player.addVelocity(
            Math.copySign(dx * dx * 0.4D, dx),
            Math.copySign(dy * dy * 0.4D, dy),
            Math.copySign(dz * dz * 0.4D, dz)
        );

        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
        player.velocityDirty = false;
    }

    @Unique
    private void leashplayers$attach(Entity entity) {
//        LeashPlayers.LOGGER.info("LeashPlayers$attach");
        leashplayers$holder = entity;
        Entity player = getSelf();

        if (leashplayers$proxy == null) {
            leashplayers$proxy = new LeashProxyEntity((LivingEntity) player);
            player.getWorld().spawnEntity(leashplayers$proxy);
            leashplayers$proxy.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
            final Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
            leashplayers$proxy.setInvisible(!leashplayers$proxy.isInvisible());
            leashplayers$proxy.refreshPositionAfterTeleport(pos);
            leashplayers$proxy.setInvisible(!leashplayers$proxy.isInvisible());
        }
        leashplayers$proxy.attachLeash(leashplayers$holder, true);

        leashplayers$lastage = player.age;
    }

    @Unique
    private void leashplayers$detach() {
//        LeashPlayers.LOGGER.info("LeashPlayers$detach");
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
        getSelf().dropItem(getServerWorld(), Items.LEAD);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Override
    public ActionResult leashplayers$interact(PlayerEntity player, Hand hand) {
        if (leashplayers$disabled()) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == Items.LEAD && leashplayers$holder == null) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            leashplayers$attach(player);
//            LeashPlayers.LOGGER.info("LeashPlayers$interact: success: attached");
            return ActionResult.SUCCESS;
        }

        if (leashplayers$holder == player && leashplayers$lastage + 20 < getSelf().age) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
//            LeashPlayers.LOGGER.info("LeashPlayers$interact: success: detatched");
            return ActionResult.SUCCESS;
        }

//        LeashPlayers.LOGGER.info("LeashPlayers$interact: pass");
        return ActionResult.PASS;
    }

    @Inject(method = "onDisconnect", at = @At("RETURN"))
    private void leashplayers$disconnect(CallbackInfo ci) {
        leashplayers$detach();
        leashplayers$drop();
    }
}

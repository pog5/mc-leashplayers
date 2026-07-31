package me.pog5.leashmod.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Players can't be leashed directly — {@code Player} is not {@code Leashable} server-side, so
 * {@link LivingEntity#setLeashHolder(Entity)} refuses them. Same trick the mod uses: leash an
 * invisible proxy mob to the holder and glue that proxy to the leashed player every tick, so the
 * client draws the lead to the player's neck.
 */
public final class LeashPlugin extends JavaPlugin implements Listener {

    /** Marks our proxy mobs so we never confuse them with a real turtle. */
    private static final String PROXY_KEY = "leashplayers_proxy";

    /** Ticks a holder must wait before the same right-click can detach again. */
    private static final int DETACH_GRACE_TICKS = 20;

    private final Map<UUID, Leash> leashes = new HashMap<>();

    private static final class Leash {
        final UUID holder;
        final LivingEntity proxy;
        final long attachedAt;

        Leash(UUID holder, LivingEntity proxy, long attachedAt) {
            this.holder = holder;
            this.proxy = proxy;
            this.attachedAt = attachedAt;
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
        // Stale proxies can survive a hard crash; clear them so they don't accumulate.
        getServer().getWorlds().forEach(w -> w.getEntitiesByClass(Turtle.class).stream()
                .filter(this::isProxy).forEach(Entity::remove));
        getLogger().info("Initialized LeashPlayers");
    }

    @Override
    public void onDisable() {
        for (UUID leashed : new HashMap<>(leashes).keySet()) {
            detach(leashed, false);
        }
    }

    private boolean enabled() {
        return getConfig().getBoolean("enabled", true);
    }

    private boolean isProxy(Entity entity) {
        return entity.hasMetadata(PROXY_KEY);
    }

    // ---- interaction ------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Never let anyone fiddle with the invisible proxy itself.
        if (isProxy(event.getRightClicked())) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getRightClicked() instanceof Player target)) return;
        if (!enabled()) return;

        Player holder = event.getPlayer();
        Leash existing = leashes.get(target.getUniqueId());

        if (existing == null) {
            ItemStack hand = holder.getInventory().getItemInMainHand();
            if (hand.getType() != Material.LEAD) return;

            attach(target, holder);
            if (holder.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                hand.setAmount(hand.getAmount() - 1);
            }
            event.setCancelled(true);
            return;
        }

        // Right-clicking your own leashed player takes the lead back.
        boolean sameHolder = existing.holder.equals(holder.getUniqueId());
        boolean pastGrace = target.getWorld().getFullTime() - existing.attachedAt >= DETACH_GRACE_TICKS;
        if (sameHolder && pastGrace) {
            detach(target.getUniqueId(), holder.getGameMode() != org.bukkit.GameMode.CREATIVE);
            event.setCancelled(true);
        }
    }

    /** A leashed player may not rip leash knots off fences unless the config allows it. */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onKnotDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LeashHitch)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (shouldBlockKnotRemoval(player)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onKnotInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LeashHitch)) return;
        if (shouldBlockKnotRemoval(event.getPlayer())) event.setCancelled(true);
    }

    private boolean shouldBlockKnotRemoval(Player player) {
        return leashes.containsKey(player.getUniqueId())
                && !getConfig().getBoolean("allow-leashed-remove-fence-knot", false);
    }

    /** The proxy is invulnerable, but cancel anyway so nothing can nudge it. */
    @EventHandler(ignoreCancelled = true)
    public void onProxyDamage(EntityDamageEvent event) {
        if (isProxy(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (leashes.containsKey(id)) detach(id, true);
        // If the holder leaves, everything they were holding drops.
        leashes.entrySet().removeIf(e -> {
            if (!e.getValue().holder.equals(id)) return false;
            dropAndClean(e.getValue(), e.getKey());
            return true;
        });
    }

    // ---- per-tick ---------------------------------------------------------

    private void tick() {
        if (leashes.isEmpty()) return;

        for (Iterator<Map.Entry<UUID, Leash>> it = leashes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Leash> entry = it.next();
            Leash leash = entry.getValue();
            Player target = getServer().getPlayer(entry.getKey());
            Player holder = getServer().getPlayer(leash.holder);

            boolean broken = !enabled()
                    || target == null || !target.isValid()
                    || holder == null || !holder.isValid()
                    || !leash.proxy.isValid()
                    || !leash.proxy.isLeashed()
                    || target.isInsideVehicle();

            if (broken) {
                dropAndClean(leash, entry.getKey());
                it.remove();
                continue;
            }

            // Glue the proxy to the player's neck so the lead renders in the right place.
            leash.proxy.teleport(neckOf(target));

            if (!target.getWorld().equals(holder.getWorld())) continue;

            double distance = target.getLocation().distance(holder.getLocation());
            if (distance < getConfig().getDouble("distance-min", 4.0D)) continue;

            if (distance > getConfig().getDouble("distance-max", 10.0D)) {
                dropAndClean(leash, entry.getKey());
                it.remove();
                continue;
            }

            target.setVelocity(target.getVelocity().add(pullToward(target, holder, distance)));
        }
    }

    /** Same easing the mod uses: per-axis, scaled by the square of the normalised offset. */
    private Vector pullToward(Player target, Player holder, double distance) {
        double dx = (holder.getLocation().getX() - target.getLocation().getX()) / distance;
        double dy = (holder.getLocation().getY() - target.getLocation().getY()) / distance;
        double dz = (holder.getLocation().getZ() - target.getLocation().getZ()) / distance;
        return new Vector(
                Math.copySign(dx * dx * 0.4D, dx),
                Math.copySign(dy * dy * 0.4D, dy),
                Math.copySign(dz * dz * 0.4D, dz));
    }

    private Location neckOf(Player target) {
        double y = target.getLocation().getPitch() > 31 ? 1.9D : 1.3D;
        if (target.isSneaking()) y -= 0.5D;
        return target.getLocation().clone().add(0.0D, y, -0.15D);
    }

    // ---- attach / detach --------------------------------------------------

    private void attach(Player target, Player holder) {
        Turtle proxy = target.getWorld().spawn(neckOf(target), Turtle.class, spawned -> {
            spawned.setMetadata(PROXY_KEY, new FixedMetadataValue(this, true));
            spawned.setInvisible(true);
            spawned.setSilent(true);
            spawned.setInvulnerable(true);
            spawned.setAI(false);
            spawned.setGravity(false);
            spawned.setCollidable(false);
            spawned.setRemoveWhenFarAway(false);
            spawned.setBaby();
        });

        proxy.setLeashHolder(holder);
        leashes.put(target.getUniqueId(), new Leash(holder.getUniqueId(), proxy, target.getWorld().getFullTime()));
    }

    private void detach(UUID leashed, boolean dropLead) {
        Leash leash = leashes.remove(leashed);
        if (leash == null) return;
        if (dropLead) dropLead(leashed, leash);
        leash.proxy.remove();
    }

    /** Break a leash that ended for any reason: always drop the lead, always kill the proxy. */
    private void dropAndClean(Leash leash, UUID leashed) {
        dropLead(leashed, leash);
        leash.proxy.remove();
    }

    private void dropLead(UUID leashed, Leash leash) {
        Player target = getServer().getPlayer(leashed);
        Location where = target != null && target.isValid()
                ? target.getLocation()
                : (leash.proxy.isValid() ? leash.proxy.getLocation() : null);
        if (where != null && where.getWorld() != null) {
            where.getWorld().dropItemNaturally(where, new ItemStack(Material.LEAD));
        }
    }
}

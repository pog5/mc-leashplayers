package me.pog5.leashmod;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class LeashProxyEntity extends TurtleEntity implements Leashable {
    private final LivingEntity target;

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;

        if (target == null) return true;
        if (target.getEntityWorld() != getEntityWorld() || !target.isAlive()) return true;

        Vec3d posActual = getEntityPos();
        Vec3d posTarget;
        double y = 1.0D;
        if (target.getPitch() > 31) {
            y = 1.9D;
        } else {
            y = 1.3D;
        }
        if (target.isSneaking()) {
            y -= 0.5D;
        }
        posTarget = target.getEntityPos().add(0.0D, y, -0.15D);

        if (!Objects.equals(posActual, posTarget)) {
            setRotation(0.0F, 0.0F);
            setPos(posTarget.getX(), posTarget.getY(), posTarget.getZ());
            setBoundingBox(getDimensions(EntityPose.DYING).getBoxAt(posTarget));
        }

        return false;
    }

    @Override
    public void tick() {
        if (this.getEntityWorld().isClient()) return;
        if (proxyUpdate() && !proxyIsRemoved()) {
            proxyRemove();
        }
    }

    public boolean proxyIsRemoved() {
        return this.isRemoved();
    }

    public void proxyRemove() {
        MinecraftServer server = getEntityWorld().getServer();
        if (server == null) {
            return;
        }
        ServerScoreboard scoreboard = server.getScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            return;
        }

        scoreboard.removeScoreHolderFromTeam(getNameForScoreboard(), team);
        teleport(0, -100, 0, false);
        super.discard();
    }

    public static final String TEAM_NAME = "leashplayersimpl";

    public LeashProxyEntity(LivingEntity target) {
        super(EntityType.TURTLE, target.getEntityWorld());

        this.target = target;

        setHealth(1.0F);
        setInvulnerable(true);

        setAiDisabled(true);
        setBaby(true);
        setInvisible(true);
        noClip = true;

        MinecraftServer server = getEntityWorld().getServer();
        if (server != null) {
            ServerScoreboard scoreboard = server.getScoreboard();

            Team team = scoreboard.getTeam(TEAM_NAME);
            if (team == null) {
                team = scoreboard.addTeam(TEAM_NAME);
            }
            if (team.getCollisionRule() != AbstractTeam.CollisionRule.NEVER) {
                team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
            }

            scoreboard.addScoreHolderToTeam(getNameForScoreboard(), team);
        }
    }

    @Override
    public float getHealth() {
        return 1.0F;
    }

    @Override
    protected void initGoals() {
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }
}

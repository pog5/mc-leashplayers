package me.pog5.leashmod.forge;

import me.pog5.leashmod.LeashMod;
import net.minecraftforge.fml.common.Mod;

@Mod(LeashMod.MOD_ID)
public final class LeashModForge {
    public LeashModForge() {
        LeashMod.init();
    }
}

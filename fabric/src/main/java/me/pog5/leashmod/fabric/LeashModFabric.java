package me.pog5.leashmod.fabric;

import me.pog5.leashmod.LeashMod;
import net.fabricmc.api.ModInitializer;

public final class LeashModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LeashMod.init();
    }
}

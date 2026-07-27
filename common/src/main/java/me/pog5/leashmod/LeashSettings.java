package me.pog5.leashmod;

public interface LeashSettings {
    boolean isEnabled();
    int getDistanceMin();
    int getDistanceMax();
    boolean allowLeashedRemoveFenceKnot();
}

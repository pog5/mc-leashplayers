# Leashable Players Mod

Leash other players with a lead. Runs on **Fabric**, **NeoForge** and **Forge**, plus a
**Bukkit/Spigot/Paper** plugin build.

## Setup

Drop the `.jar` for your loader into your mods folder:
- **Fabric**: `leashmod-fabric-<version>.jar`
- **NeoForge**: `leashmod-neoforge-<version>.jar`
- **Forge**: `leashmod-forge-<version>.jar` (1.20.1)

Server owners running Bukkit/Spigot/Paper/Purpur instead want `leashmod-bukkit-<version>.jar`
in `plugins/`. One plugin jar covers 1.20.1 through 26.2.

If there is no release then please get a Jar from Modrinth.

## Building

This is an [Architectury](https://docs.architectury.dev/) multiloader project:
- `common/` — shared logic and mixins (Mojang mappings)
- `fabric/` — Fabric entrypoint + manifest
- `neoforge/` — NeoForge entrypoint + manifest

Build every loader with `./gradlew build`; the loader jars land in `fabric/build/libs`
and `neoforge/build/libs`. `gradle.properties` pins one Minecraft version at a time —
each target needs its own toolchain (26.x is unobfuscated and uses Loom's no-remap
variant on JDK 25; 1.21.11 and 1.20.1 are obfuscated and use Mojang mappings on JDK
21 / 17), so switch it there and rebuild per version.

The `bukkit/` plugin is a separate standalone Gradle build — `cd bukkit && ./gradlew build`.

## Gamerules

Names are snake_case on 1.21.11 and newer, because game rules moved into a registry and
were renamed to resource locations:

- `leash_players_enabled` (bool) — master switch
- `leash_players_distance_min` (int) — distance before knockback pull kicks in
- `leash_players_distance_max` (int) — distance before the leash snaps
- `leash_players_allow_leashed_remove_fence_knot` (bool) — may a leashed player remove fence knots

On **1.20.1** the same rules use the old camelCase names (`leashPlayersEnabled`,
`leashPlayersDistanceMin`, `leashPlayersDistanceMax`,
`leashPlayersAllowLeashedRemoveFenceKnot`).

The **plugin** has no game rules — plugins cannot register them. It reads the same four
settings from `plugins/LeashablePlayers/config.yml` as `enabled`, `distance-min`,
`distance-max` and `allow-leashed-remove-fence-knot`.

## Incompatabilities

This mod does not work with Origins for some reason.

## License

This mod is available under the MIT license. Feel free to learn from it and incorporate it in your own projects.

## Credit

I would like to thank luavixen for the original idea and most of the code for the mod.
Also credits to Nvidium for the config system.

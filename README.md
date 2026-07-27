# Leashable Players Mod

Leash other players with a lead, configurable via gamerules. Runs on **Fabric** and **NeoForge**.

## Setup

Drop the `.jar` for your loader into your mods folder:
- **Fabric**: `leashmod-fabric-<version>.jar`
- **NeoForge**: `leashmod-neoforge-<version>.jar`

If there is no release then please get a Jar from Modrinth.

## Building

This is an [Architectury](https://docs.architectury.dev/) multiloader project:
- `common/` — shared logic and mixins (Mojang mappings)
- `fabric/` — Fabric entrypoint + manifest
- `neoforge/` — NeoForge entrypoint + manifest

Build every loader with `./gradlew build`; the loader jars land in `fabric/build/libs`
and `neoforge/build/libs`.

> **Minecraft 26.x is unobfuscated.** Starting with 26.1, Minecraft ships with official
> (Mojang) names, so there are no mappings to apply. This project uses Architectury Loom's
> **no-remap** variant (`dev.architectury.loom-no-remap`) with no `mappings` line, and
> requires **JDK 25**. To target 26.1.2 instead (covers 26.1/26.1.1/26.1.2 with a stabler
> NeoForge), set `minecraft_version=26.1.2` and `neoforge_version=26.1.2.78` in
> `gradle.properties`.

## Gamerules

- `leashPlayersEnabled` (bool) — master switch
- `leashPlayersDistanceMin` (int) — distance before knockback pull kicks in
- `leashPlayersDistanceMax` (int) — distance before the leash snaps
- `leashPlayersAllowLeashedRemoveFenceKnot` (bool) — may a leashed player remove fence knots

## Incompatabilities

This mod does not work with Origins for some reason.

## License

This mod is available under the MIT license. Feel free to learn from it and incorporate it in your own projects.

## Credit

I would like to thank luavixen for the original idea and most of the code for the mod.
Also credits to Nvidium for the config system.

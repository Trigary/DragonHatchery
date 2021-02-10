# Dragon Hatchery

A Minecraft Bukkit ([Paper](https://github.com/PaperMC/Paper)) plugin aims to fulfill all
your dragon egg spawning related needs.

In vanilla Minecraft the dragon egg only spawns when the dragon is killed for the first
time. This plugin adds configurable chance-based controls for the spawning behaviour. It
also lets server owners define custom "loot" (spawned block) instead of dragon eggs.

**IMPORTANT!** This plugin only runs on Paper and its forks. It doesn't support Spigot.

## Downloads

TODO

## Commands

Some commands exist, they are all subcommands of the `dragonhatchery` command. Enter it to
see the list of available subcommands! The `dragonhatchery` command requires
the `dragonhatchery` permission.

## Configuration

The configuration is explained in the configuration file. This file gets generated when
the plugin is loaded for the first time. Whenever the server starts up (and whenever you
reload the config via the reload command) be sure to monitor the console for any errors!

If something goes wrong (due to an invalid config), then no blocks (dragon eggs) will
spawn. But don't worry, the players who were around when this happened are logged.

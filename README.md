# Dragon Hatchery

A Minecraft Bukkit ([Paper](https://github.com/PaperMC/Paper)) plugin that aims to fulfill
all your dragon egg spawning related needs.

In vanilla Minecraft the dragon egg only spawns when the dragon is killed for the first
time. This plugin adds configurable chance-based controls for the spawning behaviour. It
also lets server owners define custom "loot" (spawned block) instead of dragon eggs.

**IMPORTANT!** This plugin only runs on Paper and its forks. It doesn't support Spigot and
it has no plans to do so in the future.

Links:
[ [GitHub](https://github.com/Trigary/DragonHatchery) ]
[ [bStats](https://bstats.org/plugin/bukkit/DragonHatchery/10368) ]

## Downloads

The plugin is built via GitHub Actions artifacts. You have different download options
depending on whether you are currently logged in to GitHub.

For users who are not logged in to GitHub:

- Navigate to
  [this project's nightly.link](https://nightly.link/Trigary/DragonHatchery/workflows/build/master)
  page. This service is a free wrapper around GitHub Actions that lets guest users bypass
  the permission restrictions.
- Download the `.zip` file and unzip it to get the `.jar` file.
- Congratulations! You successfully download this plugin.

For users who are logged in to GitHub:

- Navigate to the [GitHub Actions](https://github.com/Trigary/DragonHatchery/actions) tab.
- Open the latest "workflow run".
- Click on the `.jar` file at the bottom, in the "Artifacts" section.
- This will download a `.zip` file, unzip it to get the `.jar` file.
- Congratulations! You successfully download this plugin.

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

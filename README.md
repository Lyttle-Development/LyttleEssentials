# LyttleEssentials

LyttleEssentials is a Minecraft plugin that provides essential commands and features for server administrators and players. It is built with Java and Maven, and is compatible with Bukkit, Spigot, and Paper servers.

## Features

- **Permission Support**: Each command and feature has its own permission node, allowing for granular control over who can use what.
- **Configurable**: All messages and settings can be adjusted in the configuration file.
- **Warp System**: Set and teleport to custom warp locations.
- **Teleportation**: Send teleport requests to other players and accept or deny incoming requests.
- **Spawn Management**: Set and teleport to the server's spawn location.
- **Home Management**: Set and teleport to the player's home location.
- **Admin Teleportation**: Immediately teleport to other players without requiring acceptance.

## Commands

- `/lyttleessentials`: Displays the plugin version and provides a command to reload the configuration.
- `/setwarp <warpName> [player]`: Sets a warp at the player's current location. If a player name is provided, sets the warp for that player.
- `/delwarp <warpName> [player]`: Deletes a warp. If a player name is provided, deletes the warp for that player.
- `/warp <warpName>`: Teleports the player to a warp.
- `/tp <playerName>`: Sends a teleport request to another player.
- `/tpaccept`: Accepts a teleport request from another player.
- `/tpdeny`: Denies a teleport request from another player.
- `/setspawn`: Sets the spawn location to the player's current location.
- `/spawn`: Teleports the player to the spawn location.
- `/sethome`: Sets the player's home location to their current location.
- `/home`: Teleports the player to their home location.
- `/admintp <playerName>`: Immediately teleports the player to another player without requiring acceptance.

## Permissions

- `lyttleessentials.warp.others`: Allows setting and deleting warps for other players.
- `lyttleessentials.tp.others`: Allows sending teleport requests to other players.
- `lyttleessentials.spawn.set`: Allows setting the spawn location.
- `lyttleessentials.home.set`: Allows setting the home location.
- `lyttleessentials.admintp`: Allows immediate teleportation to other players.
- `lyttleessentials.lyttleessentials`: Allows access to the base command of the plugin.

## Installation

1. Download the latest version of the plugin from the [releases page](https://github.com/Lyttle-Development/LyttleEssentials/releases).
2. Place the downloaded `.jar` file into your server's `plugins` folder.
3. Restart your server or use a plugin manager to load the plugin.

## Configuration

After the first run, a `config.yml` file will be created in the `plugins/LyttleEssentials` directory. This file can be modified to change the plugin's settings and messages.

## Support

If you encounter any issues or have any questions, please [open an issue](https://github.com/Lyttle-Development/LyttleEssentials/issues) on GitHub.

## Contributing

Contributions are welcome! Please read the [contributing guidelines](CONTRIBUTING.md) before getting started.

# License

All rights reserved. Before using or distributing this software, you must first obtain permission from the author. Please contact the author (Stualyttle Kirry) at [Discord](https://discord.com/invite/QfqFFPFFQZ) to request permission.
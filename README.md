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
- `/admintp <playerName>`: Immediately teleports the player to another player without requiring acceptance.
- `/fly <playerName>`: Toggles flight mode for the player.
- `/gamemode <playerName> <gamemode>`: Changes the gamemode of the player.
- `/gm<c / s / a / sp>`: Shortcut commands for changing the player's gamemode.
- `/heal <playerName>`: Heals the player.
- `/home`: Teleports the player to their home location.
- `/sethome`: Sets the player's home location to their current location.
- `/repair <playerName>`: Repairs the item in the player's hand.
- `/spawn`: Teleports the player to the spawn location.
- `/setspawn`: Sets the spawn location to the player's current location.
- `/tp <playerName>`: Sends a teleport request to another player.
- `/tpaccept`: Accepts a teleport request from another player.
- `/tpdeny`: Denies a teleport request from another player.
- `/top`: Teleports the player to the highest block at their current location.
- `/warp <warpName>`: Teleports the player to a warp.
- `/setwarp <warpName> [player]`: Sets a warp at the player's current location. If a player name is provided, sets the warp for that player.
- `/delwarp <warpName> [player]`: Deletes a warp. If a player name is provided, deletes the warp for that player.

## Permissions

- `lyttleessentials.lyttleessentials`: Allows access to the base command of the plugin.
- `lyttleessentials.admintp`: Allows immediate teleportation to other players.
- `lyttleessentials.fly`: Allows toggling flight mode for the player.
- `lyttleessentials.fly.self`: Allows toggling flight mode for oneself.
- `lyttleessentials.fly.other`: Allows toggling flight mode for other players
- `lyttleessentials.gamemode`: Allows changing the gamemode of the player.
- `lyttleessentials.gamemode.self`: Allows changing the gamemode for oneself.
- `lyttleessentials.gamemode.other`: Allows changing the gamemode for other players.
- `lyttleessentials.heal`: Allows healing the player.
- `lyttleessentials.heal.self`: Allows healing oneself.
- `lyttleessentials.heal.other`: Allows healing other players.
- `lyttleessentials.home`: Allows setting the home location for other players.
- `lyttleessentials.home.set`: Allows setting the home location for other players.
- `lyttleessentials.home.set.other`: Allows setting the home location for other players.
- `lyttleessentials.home.del`: Allows deleting the home location for other players.
- `lyttleessentials.home.del.other`: Allows deleting the home location for other players.
- `lyttleessentials.repair`: Allows repairing the item in the player's hand.
- `lyttleessentials.repair.self`: Allows repairing the item in one's hand.
- `lyttleessentials.repair.other`: Allows repairing the item in other players' hands.
- `lyttleessentials.spawn.set`: Allows setting the spawn location.
- `lyttleessentials.tp.others`: Allows sending teleport requests to other players.
- `lyttleessentials.top`: Allows teleporting to the highest block at the player's current location.
- `lyttleessentials.warp.others`: Allows setting and deleting warps for other players.

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
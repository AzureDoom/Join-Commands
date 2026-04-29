# Join Commands

A lightweight Hytale server plugin that runs a configurable list of commands every time a player joins the server. Useful for granting starter items, sending welcome messages, teleporting players to a spawn world, or running any other command-driven setup that needs to happen on connection.

## Features

- Runs any number of commands on player join, in the order they appear in the config.
- Supports running commands as the joining player **or** as the console, so you can mix elevated-permission setup with ordinary player actions.
- Supports per-player command lists, keyed by UUID, that run in addition to the global list. Per-player lists also support both player-executed and console-executed commands.
- Skips empty entries, so leaving blank lines in your config will not cause errors.
- Accepts commands with or without a leading slash. `/give coins --quantity=100` and `give coins --quantity=100` both work.
- Logs each dispatched command for easy debugging, including which sender (player or console) ran it.
- Catches and reports failures per command, so one broken entry will not stop the rest from running.
- Optional integration with PlaceholderAPI. If the plugin is installed, placeholders such as `%player_username%`, `%luckperms_prefix%`, or any other registered expansion will be replaced before the command runs. Placeholders resolve against the joining player even when the command runs as the console.

## Configuration

The config file is generated at `mods/com.azuredoom_joincommands/joincommands.json` on first startup. By default, it contains an empty command list, meaning nothing will run on join until you add entries.

Example:

```json
{
  "Commands": [
    "coins --quantity=100",
    "say Welcome to the server!",
    "tp 0 64 0"
  ],
  "ConsoleCommands": [
    "echo %player_username% just joined!"
  ]
}
```

Each entry is run as if the joining player had typed it as a command. The player is the executor, so permissions are required to run commands.

### Player commands vs. console commands

The plugin supports two execution contexts at every level of the config:

- **`Commands`** – run as the joining player. The player is the executor, and the player's permissions are required for the command to succeed. Use this for actions you want the player to "perform" themselves.
- **`ConsoleCommands`** – run as the server console (`ConsoleSender`). These bypass the player's permission set and run with full server authority. Use this for administrative actions like granting items, modifying ranks, or broadcasting messages, where the joining player should not need (or have) permission to run the command directly.

In both cases the joining player is still the placeholder context, so something like `%player_username%` will resolve to the joining player even inside a `ConsoleCommands` entry.

For each player, global commands run first (`Commands`, then `ConsoleCommands`), followed by any matching per-player entries.

### Per-player commands

In addition to the global `Commands` list, you can define commands that run only for specific players, keyed by their UUID. These run **after** the global commands, so they layer on top rather than replacing them.

```json
{
  "Commands": [
    "say Welcome to the server!"
  ],
  "ConsoleCommands": [
    "echo %player_username% just joined!"
  ],
  "PlayerCommands": [
    {
      "Uuid": "550e8400-e29b-41d4-a716-446655440000",
      "Commands": [
        "coins --quantity=500",
        "echo Welcome back, admin."
      ],
      "ConsoleCommands": [
        "coins --quantity=500 --player=%player_username%",
        "lp user %player_username% parent set admin"
      ]
    },
    {
      "Uuid": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
      "Commands": [
        "tp 100 64 100"
      ]
    }
  ]
}
```

Notes:

- UUIDs are matched exactly. Use the standard dashed format shown above.
- If a UUID is not found in the `PlayerCommands` list, only the global commands run for that player.
- You can have multiple entries for the same UUID; all matching entries will run in the order they appear.
- Both `Commands` and `ConsoleCommands` are optional inside a per-player entry. Include only the lists you need.
- The `PlayerCommands` field itself is optional. Omit it entirely if you only need global commands.

### Using placeholders

If PlaceholderAPI is installed alongside Join Commands, any placeholder registered with PlaceholderAPI can be used inside a command string, in any of the four lists (global `Commands`, global `ConsoleCommands`, per-player `Commands`, per-player `ConsoleCommands`). The plugin detects PlaceholderAPI automatically at startup and logs a confirmation when it does.

Placeholders always resolve against the joining player, even when the command itself is dispatched as the console. This means you can write console commands that target the joining player by name or UUID without giving the player permission to run those commands themselves.

Example using placeholders:

```json
{
  "Commands": [
    "echo Welcome back, %player_username%!"
  ],
  "ConsoleCommands": [
    "say %player_username% just joined with rank %luckperms_prefix%",
    "give %player_username% starter_kit"
  ]
}
```

If PlaceholderAPI is not installed, placeholder strings are left untouched and passed through to the command as-is.

## Behavior notes

- Commands run during the `PlayerReadyEvent`, which fires once the player has fully connected and is ready to receive game state. They will not run earlier in the connection process.
- Execution order for any given player is: global `Commands` → global `ConsoleCommands` → per-player `Commands` → per-player `ConsoleCommands`, for each matching `PlayerCommands` entry.
- Commands run asynchronously through Hytale's command manager. Failures are logged with a warning and do not interrupt subsequent commands in the list.
- Each dispatched command is logged with its sender label (`console` or the player's username), making it easy to tell at a glance which context a command ran in.
- Global commands run before per-player commands. Per-player commands do not replace the global lists, they extend them.

## Dependencies

- PlaceholderAPI by HelpChat (optional, soft dependency).


### Hosting Partner

Looking for a reliable server to run **LevelingCore** and other Hytale mods?

**BisectHosting** offers pre-configured game servers, fast setup, and solid performance for modded environments.

Use code **azuredoom** for **25% off your first month**.

[![BisectHosting](https://www.bisecthosting.com/images/CF/LEVELINGCORE/MP_LEVELINGCORE_Promo.webp)](https://url-shortener.curseforge.com/z2g8c)
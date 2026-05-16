package com.azuredoom.joincommands;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.azuredoom.joincommands.compat.HStats;
import com.azuredoom.joincommands.compat.PlaceholderAPICompat;

public class JoinCommandsMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<JoinCommandsConfig> config;

    private boolean placeholderApiAvailable;

    public JoinCommandsMod(JavaPluginInit init) {
        super(init);
        config = this.withConfig("joincommands", JoinCommandsConfig.CODEC);
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("Starting Join Commands!");
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("Setting up Join Commands!");
        config.save();

        placeholderApiAvailable = PluginManager.get()
            .getPlugin(new PluginIdentifier("HelpChat", "PlaceholderAPI")) != null;
        if (placeholderApiAvailable) {
            LOGGER.atInfo().log("PlaceholderAPI detected, placeholders will be replaced in join commands");
        }
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        new HStats("398f3a4c-2e1d-4d49-bcba-59d0fa7a9009", "1.0.1", LOGGER);
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("Shutting down Join Commands!");
    }

    /**
     * Handles the {@link PlayerReadyEvent} which is fired when a player has fully connected and is ready to interact
     * with the server.
     * <p>
     * This method retrieves the player's {@link PlayerRef} and executes any configured join commands. It will:
     * <ul>
     * <li>Run global player commands defined in the config</li>
     * <li>Run global console commands defined in the config</li>
     * <li>Run player-specific commands if the player's UUID matches an entry</li>
     * <li>Run player-specific console commands if the player's UUID matches an entry</li>
     * </ul>
     * <p>
     * If required player data cannot be retrieved (such as {@link PlayerRef} or its component), execution is aborted
     * and a log entry is created.
     *
     * @param event the player ready event containing the connecting player
     */
    private void onPlayerReady(PlayerReadyEvent event) {
        var player = event.getPlayer();

        var playerRef = player.getReference();
        if (playerRef == null) {
            LOGGER.atInfo().log("Player reference is null on connection");
            return;
        }

        var playerRefComponent = playerRef.getStore()
            .getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComponent == null) {
            LOGGER.atInfo().log("Player ref component is null on connection");
            return;
        }

        var playerUuid = playerRefComponent.getUuid();
        var cfg = config.get();
        var commandManager = CommandManager.get();

        runPlayerCommands(cfg.getCommands(), playerRefComponent, commandManager);
        runConsoleCommands(cfg.getConsoleCommands(), playerRefComponent, commandManager);

        var playerEntries = cfg.getPlayerCommands();
        if (playerEntries != null) {
            for (var entry : playerEntries) {
                if (entry == null || !playerUuid.equals(entry.getUuid()))
                    continue;
                runPlayerCommands(entry.getCommands(), playerRefComponent, commandManager);
                runConsoleCommands(entry.getConsoleCommands(), playerRefComponent, commandManager);
            }
        }
    }

    /**
     * Executes a list of commands as the joining player.
     *
     * @param commands           the array of commands to execute
     * @param playerRefComponent the player reference used to execute commands and resolve placeholders
     * @param commandManager     the command manager responsible for handling execution
     */
    private void runPlayerCommands(String[] commands, PlayerRef playerRefComponent, CommandManager commandManager) {
        if (commands == null)
            return;
        for (var command : commands) {
            var finalCommand = prepareCommand(command, playerRefComponent);
            if (finalCommand == null)
                continue;
            executeCommand(
                playerRefComponent.getUsername(),
                playerRefComponent,
                finalCommand,
                commandManager.handleCommand(playerRefComponent, finalCommand)
            );
        }
    }

    /**
     * Executes a list of commands as the console, while still using the joining player as the placeholder context.
     * <p>
     * This allows commands to run with elevated permissions (bypassing the player's own permission set) while still
     * resolving placeholders like {@code %player_name%} against the joining player.
     *
     * @param commands           the array of commands to execute
     * @param playerRefComponent the player reference used for placeholder resolution and logging
     * @param commandManager     the command manager responsible for handling execution
     */
    private void runConsoleCommands(String[] commands, PlayerRef playerRefComponent, CommandManager commandManager) {
        if (commands == null)
            return;
        for (var command : commands) {
            var finalCommand = prepareCommand(command, playerRefComponent);
            if (finalCommand == null)
                continue;
            executeCommand(
                "console",
                playerRefComponent,
                finalCommand,
                commandManager.handleCommand(ConsoleSender.INSTANCE, finalCommand)
            );
        }
    }

    /**
     * Normalizes a single command line by stripping whitespace, removing a leading '/', and applying PlaceholderAPI
     * substitutions when available.
     *
     * @param command          the raw command string
     * @param placeholderOwner the player used as context for placeholder resolution
     * @return the prepared command, or {@code null} if it should be skipped
     */
    private String prepareCommand(String command, PlayerRef placeholderOwner) {
        if (command == null || command.isBlank())
            return null;
        var trimmed = command.strip();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (placeholderApiAvailable) {
            trimmed = PlaceholderAPICompat.replacePlaceholders(trimmed, placeholderOwner);
        }
        return trimmed;
    }

    /**
     * Logs the dispatch and attaches a failure handler to the command future. Synchronous exceptions are caught by the
     * caller's surrounding try/catch indirectly via the future's {@code exceptionally} stage.
     */
    private void executeCommand(
        String senderLabel,
        PlayerRef playerRefComponent,
        String finalCommand,
        CompletableFuture<Void> future
    ) {
        try {
            LOGGER.atInfo()
                .log(
                    "Running join command as " + senderLabel + " for "
                        + playerRefComponent.getUsername() + ": " + finalCommand
                );
            future.exceptionally(e -> {
                LOGGER.atWarning().withCause(e).log("Failed to run join command: " + finalCommand);
                return null;
            });
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to run join command: " + finalCommand);
        }
    }
}

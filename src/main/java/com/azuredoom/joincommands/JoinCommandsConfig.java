package com.azuredoom.joincommands;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import com.azuredoom.joincommands.util.PlayerCommandEntry;

public class JoinCommandsConfig {

    private static final ArrayCodec<PlayerCommandEntry> PLAYER_COMMAND_ARRAY =
        new ArrayCodec<>(PlayerCommandEntry.CODEC, PlayerCommandEntry[]::new);

    public static final BuilderCodec<JoinCommandsConfig> CODEC = BuilderCodec.builder(
        JoinCommandsConfig.class,
        JoinCommandsConfig::new
    )
        .append(
            new KeyedCodec<>("Commands", Codec.STRING_ARRAY),
            (exConfig, aStringArray, _) -> exConfig.commands = aStringArray,
            (exConfig, _) -> exConfig.commands
        )
        .add()
        .append(
            new KeyedCodec<>("ConsoleCommands", Codec.STRING_ARRAY),
            (exConfig, aStringArray, _) -> exConfig.consoleCommands = aStringArray,
            (exConfig, _) -> exConfig.consoleCommands
        )
        .add()
        .append(
            new KeyedCodec<>("PlayerCommands", PLAYER_COMMAND_ARRAY),
            (exConfig, entries, _) -> exConfig.playerCommands = entries,
            (exConfig, _) -> exConfig.playerCommands
        )
        .add()
        .build();

    private String[] commands = {};

    private String[] consoleCommands = {};

    private PlayerCommandEntry[] playerCommands = {};

    private JoinCommandsConfig() {}

    /**
     * Gets the list of global commands executed for all players when they join.
     *
     * @return an array of command strings
     */
    public String[] getCommands() {
        return this.commands;
    }

    /**
     * Gets the list of global commands executed as the console when any player joins.
     * <p>
     * These commands are run with elevated permissions via {@code ConsoleSender.INSTANCE}, allowing operations the
     * joining player would not otherwise have access to.
     *
     * @return an array of command strings
     */
    public String[] getConsoleCommands() {
        return this.consoleCommands;
    }

    /**
     * Gets the list of player-specific command entries.
     * <p>
     * Each entry contains a UUID and a set of commands that will only be executed when the matching player joins.
     *
     * @return an array of {@link PlayerCommandEntry}
     */
    public PlayerCommandEntry[] getPlayerCommands() {
        return this.playerCommands;
    }
}

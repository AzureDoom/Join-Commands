package com.azuredoom.joincommands.util;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

/**
 * Represents a configuration entry for player-specific join commands.
 * <p>
 * Each entry associates a player's UUID with a set of commands that should be executed when that player joins the
 * server.
 */
public class PlayerCommandEntry {

    public static final BuilderCodec<PlayerCommandEntry> CODEC = BuilderCodec.builder(
        PlayerCommandEntry.class,
        PlayerCommandEntry::new
    )
        .append(
            new KeyedCodec<>("Uuid", Codec.UUID_STRING),
            (entry, uuid, _) -> entry.uuid = uuid,
            (entry, _) -> entry.uuid
        )
        .add()
        .append(
            new KeyedCodec<>("Commands", Codec.STRING_ARRAY),
            (entry, commands, _) -> entry.commands = commands,
            (entry, _) -> entry.commands
        )
        .add()
        .append(
            new KeyedCodec<>("ConsoleCommands", Codec.STRING_ARRAY),
            (entry, commands, _) -> entry.consoleCommands = commands,
            (entry, _) -> entry.consoleCommands
        )
        .add()
        .build();

    private UUID uuid;

    private String[] commands = {};

    private String[] consoleCommands = {};

    private PlayerCommandEntry() {}

    /**
     * Gets the UUID associated with this entry.
     *
     * @return the player's UUID
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Gets the commands that should be executed for the associated player.
     *
     * @return an array of command strings
     */
    public String[] getCommands() {
        return this.commands;
    }

    /**
     * Gets the commands that should be executed when this player joins, run as the console.
     * <p>
     * These commands are run with elevated permissions via {@code ConsoleSender.INSTANCE}, allowing operations the
     * joining player would not otherwise have access to.
     *
     * @return an array of command strings
     */
    public String[] getConsoleCommands() {
        return this.consoleCommands;
    }
}

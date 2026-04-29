package com.azuredoom.joincommands.compat;

import at.helpch.placeholderapi.PlaceholderAPI;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Utility class for integrating PlaceholderAPI with the JoinCommands plugin.
 * <p>
 * This class provides a simple wrapper around PlaceholderAPI to replace placeholders in command strings using a
 * {@link PlayerRef}.
 */
public class PlaceholderAPICompat {

    private PlaceholderAPICompat() {}

    /**
     * Replaces placeholders in the given input string using PlaceholderAPI.
     *
     * @param input  the string containing placeholders
     * @param player the player context used for placeholder resolution
     * @return the input string with placeholders replaced
     */
    public static String replacePlaceholders(String input, PlayerRef player) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}

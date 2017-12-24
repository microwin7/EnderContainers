package fr.utarwyn.endercontainers.util;

import fr.utarwyn.endercontainers.Config;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class used to send various messages for players.
 * @since 1.0.0
 * @author Utarwyn
 */
public class PluginMsg {

	/**
	 * Utility class!
	 */
	private PluginMsg() {

	}

	/**
	 * Send a given error message to a specific sender
	 * @param sender The sender
	 * @param message Error message to send
	 */
	public static void errorMessage(CommandSender sender, String message) {
		sender.sendMessage(Config.PREFIX + ChatColor.RED + message);
	}

	/**
	 * Inform a sender that the access is denied for him
	 * @param sender The sender
	 */
	public static void accessDenied(CommandSender sender) {
		if (sender instanceof Player)
			errorMessage(sender, Locale.nopermPlayer);
		else
			errorMessage(sender, Locale.nopermConsole);
	}

	/**
	 * Send the plugin header bar to a given sender
	 * @param sender Sender to process
	 */
	public static void pluginBar(CommandSender sender) {
		String pBar = "§5§m" + StringUtils.repeat("-", 5);
		String sBar = "§d§m" + StringUtils.repeat("-", 11);

		sender.sendMessage("§8++" + pBar + sBar + "§r§d( §6EnderContainers §d)" + sBar + pBar + "§8++");
	}

	/**
	 * Send the plugin footer bar to a given sender
	 * @param sender Sender to process
	 */
	public static void endBar(CommandSender sender) {
		String pBar = "§5§m" + StringUtils.repeat("-", 5);
		sender.sendMessage("§8++" + pBar + "§d§m" + StringUtils.repeat("-", 39) + pBar + "§8++");
	}

}
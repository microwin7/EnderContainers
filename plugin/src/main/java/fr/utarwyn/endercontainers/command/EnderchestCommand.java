package fr.utarwyn.endercontainers.command;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.MiscUtil;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderchestCommand extends AbstractCommand {

    private EnderChestManager manager;

    public EnderchestCommand() {
        super("enderchest", "ec", "endchest");

        this.manager = Managers.get(EnderChestManager.class);

        this.addParameter(Parameter.integer().optional());
    }

    @Override
    public void performPlayer(Player player) {
        if (Files.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            PluginMsg.errorMessage(player, Files.getLocale().getPluginWorldDisabled());
            return;
        }

        Integer chestNumber = this.readArgOrDefault(null);

        if (chestNumber != null) {
            if (chestNumber > 0 && chestNumber <= Files.getConfiguration().getMaxEnderchests()) {
                this.openChestMenu(player, chestNumber - 1);
            } else {
                PluginMsg.accessDenied(player);
            }
        } else {
            this.openHubMenu(player);
        }
    }

    @Override
    public void performConsole(CommandSender sender) {
        PluginMsg.errorMessage(sender, Files.getLocale().getNopermConsole());
    }

    private void openHubMenu(Player player) {
        if (MiscUtil.playerHasPerm(player, "cmd.enderchests")) {
            this.manager.loadPlayerContext(player.getUniqueId(), context -> context.openHubMenuFor(player));
        } else {
            PluginMsg.accessDenied(player);
        }
    }

    private void openChestMenu(Player player, int num) {
        if (MiscUtil.playerHasPerm(player, "cmd.enderchests") || MiscUtil.playerHasPerm(player, "cmd.enderchest." + num)) {
            this.manager.loadPlayerContext(player.getUniqueId(), context -> {
                if (!context.openEnderchestFor(player, num)) {
                    this.sendTo(player, ChatColor.RED + Files.getLocale().getNopermOpenChest());
                }
            });
        } else {
            PluginMsg.accessDenied(player);
        }
    }

}

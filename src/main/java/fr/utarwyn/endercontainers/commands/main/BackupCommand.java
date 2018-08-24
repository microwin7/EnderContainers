package fr.utarwyn.endercontainers.commands.main;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.AbstractCommand;
import fr.utarwyn.endercontainers.commands.backup.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackupCommand extends AbstractCommand {

	public BackupCommand() {
		super("backup");

		BackupManager manager = EnderContainers.getInstance().getInstance(BackupManager.class);

		this.addSubCommand(new ListCommand(manager));
		this.addSubCommand(new InfoCommand(manager));
		this.addSubCommand(new CreateCommand(manager));
		this.addSubCommand(new LoadCommand(manager));
		this.addSubCommand(new RemoveCommand(manager));
	}

	@Override
	public void perform(CommandSender sender) {
		this.sendTo(sender, ChatColor.RED + "Sub-commands available: " + ChatColor.GOLD + "list,info,create,load,remove");
	}

	@Override
	public void performPlayer(Player player) {

	}

	@Override
	public void performConsole(CommandSender sender) {

	}
}

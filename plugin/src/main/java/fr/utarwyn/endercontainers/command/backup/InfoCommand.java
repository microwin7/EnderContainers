package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.Parameter;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.DateFormat;
import java.util.Optional;

public class InfoCommand extends AbstractBackupCommand {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public InfoCommand(BackupManager manager) {
        super("info", manager);

        this.setPermission("backup.info");
        this.addParameter(Parameter.string());
    }

    @Override
    public void perform(CommandSender sender) {
        String name = this.readArg();
        Optional<Backup> optionalBackup = this.manager.getBackupByName(name);
        Locale locale = Files.getLocale();

        if (!optionalBackup.isPresent()) {
            this.sendTo(sender, ChatColor.RED + locale.getBackupUnknown().replace("%backup%", name));
            return;
        }

        Backup backup = optionalBackup.get();

        PluginMsg.pluginBar(sender);
        sender.sendMessage(" ");
        sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelName() + ": §r" + backup.getName() + " §7(" + backup.getType() + ")");
        sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelDate() + ": §r" + DATE_FORMAT.format(backup.getDate()));
        sender.sendMessage(" " + ChatColor.GRAY + "  " + locale.getBackupLabelBy() + ": §e" + backup.getCreatedBy());
        sender.sendMessage(" ");
        sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + locale.getBackupLabelLoadCmd() + ": §d/ecp backup load " + name);
        sender.sendMessage(" " + ChatColor.DARK_GRAY + "  " + locale.getBackupLabelRmCmd() + ": §c/ecp backup remove " + name);
        sender.sendMessage(" ");
        PluginMsg.endBar(sender);
    }

}

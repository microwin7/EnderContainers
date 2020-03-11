package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.database.DatabaseManager;
import fr.utarwyn.endercontainers.database.DatabaseSet;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Storage wrapper for backups (MySQL)
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public class BackupsSQLData extends BackupsData {

    /**
     * The database manager
     */
    private DatabaseManager databaseManager;

    /**
     * Construct a new backup storage wrapper with a SQL database.
     *
     * @param logger plugin logger
     */
    public BackupsSQLData(Logger logger) {
        super(logger);

        this.databaseManager = Managers.get(DatabaseManager.class);
        this.backups = new ArrayList<>();

        this.load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void load() {
        try {
            this.databaseManager.getBackups().forEach(set -> this.backups.add(
                    new Backup(set.getString("name"), set.getTimestamp("date"),
                            set.getString("type"), set.getString("created_by"))));
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot retrieve backups list from the database", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void save() {
        // There is no file to save when using SQL
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveNewBackup(Backup backup) {
        try {
            this.databaseManager.saveBackup(
                    backup.getName(), backup.getDate().getTime(), backup.getType(),
                    this.getEnderchestsStringData(), backup.getCreatedBy()
            );
            return true;
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot save the backup " + backup.getName(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeStorage(Backup backup) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applyBackup(Backup backup) {
        Optional<DatabaseSet> backupSet = Optional.empty();

        try {
            backupSet = this.databaseManager.getBackup(backup.getName());
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot retrieve the backup "
                    + backup.getName() + " from the database", e);
        }

        if (backupSet.isPresent()) {
            try {
                String backupData = backupSet.get().getString("data");
                this.databaseManager.replaceEnderchests(this.getEnderchestsFromString(backupData));
                return true;
            } catch (SQLException e) {
                this.logger.log(Level.SEVERE, "Cannot replace enderchests in the database", e);
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeBackup(Backup backup) {
        try {
            return this.databaseManager.removeBackup(backup.getName());
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot delete backup " + backup.getName() + " from the database", e);
            return false;
        }
    }

    /**
     * Get all saved enderchests in the database as a string.
     *
     * @return all enderchests as a string
     */
    private String getEnderchestsStringData() {
        List<DatabaseSet> sets;

        try {
            sets = this.databaseManager.getAllEnderchests();
        } catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Cannot retrieve all enderchests from the database", e);
            return "";
        }

        List<String> dataElementList = sets.stream()
                .map(set -> set.getInteger("id") + ":"
                        + set.getInteger("num") + ":"
                        + Base64Coder.encodeString(set.getString("owner")) + ":"
                        + Base64Coder.encodeString(set.getString("contents")) + ":"
                        + set.getInteger("rows"))
                .collect(Collectors.toList());

        return StringUtils.join(dataElementList, ";");
    }

    /**
     * Create a list of enderchests from a string.
     *
     * @param data string to use to generate enderchest datasets
     * @return list of generated datasets
     */
    private List<DatabaseSet> getEnderchestsFromString(String data) {
        List<DatabaseSet> sets = new ArrayList<>();
        DatabaseSet set;

        for (String backupData : data.split(";")) {
            String[] info = backupData.split(":");

            int id = Integer.parseInt(info[0]);
            int num = Integer.parseInt(info[1]);
            String owner = Base64Coder.decodeString(info[2]);
            String contents = Base64Coder.decodeString(info[3]);
            int rows = Integer.parseInt(info[4]);

            set = new DatabaseSet();
            set.setObject("id", id);
            set.setObject("num", num);
            set.setObject("owner", owner);
            set.setObject("contents", contents);
            set.setObject("rows", rows);

            sets.add(set);
        }

        return sets;
    }

}

package fr.utarwyn.endercontainers.compatibility;

import org.bukkit.Bukkit;

/**
 * Utility object used to get the current server version.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public enum ServerVersion {

    V1_8,
    V1_9,
    V1_10,
    V1_11,
    V1_12,
    V1_13,
    V1_14,
    V1_15;

    private static ServerVersion currentVersion;

    private static String bukkitVersion;

    static {
        // Getting the bukkit Server version!
        String path = Bukkit.getServer().getClass().getPackage().getName();
        bukkitVersion = path.substring(path.lastIndexOf('.') + 1);

        for (ServerVersion version : values()) {
            if (bukkitVersion.startsWith(version.name().toLowerCase())) {
                currentVersion = version;
                break;
            }
        }
    }

    public static ServerVersion get() {
        return currentVersion;
    }

    public static String getBukkitVersion() {
        return bukkitVersion;
    }

    public static boolean is(ServerVersion version) {
        return currentVersion.equals(version);
    }

    public static boolean isOlderThan(ServerVersion version) {
        return get().ordinal() < version.ordinal();
    }

    public static boolean isNewerThan(ServerVersion version) {
        return get().ordinal() > version.ordinal();
    }

}

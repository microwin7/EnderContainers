package fr.utarwyn.endercontainers.dependency;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import fr.utarwyn.endercontainers.api.dependency.dependency.Dependency;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Dependency used to interact with the WorldGuard V6+ plugin
 *
 * @author Utarwyn
 * @since 2.1.0
 */
public class WorldGuard6Dependency extends Dependency {

    private WorldGuardPlugin worldGuardPlugin;

    WorldGuard6Dependency(String name, Plugin plugin) {
        super(name, plugin);
        this.worldGuardPlugin = (WorldGuardPlugin) plugin;
    }

    @Override
    public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
        // OP? Ok, you can do whatever you want...
        if (player.isOp()) return true;

        // Retrieve the WorldGuard Player instance and create a region query.
        LocalPlayer localPlayer = this.worldGuardPlugin.wrapPlayer(player);
        RegionQuery query = this.worldGuardPlugin.getRegionContainer().createQuery();

        // Check for denied flags at the chest's location!
        return query.testBuild(block.getLocation(), localPlayer, DefaultFlag.INTERACT, DefaultFlag.USE);
    }

}

package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.dependencies.DependenciesManager;
import fr.utarwyn.endercontainers.menu.OfflineEnderChestMenu;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Updater;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

/**
 * Class used to intercept events of player
 * @since 2.0.0
 * @author Utarwyn
 */
public class EnderChestListener implements Listener {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * The dependencies manager
	 */
	private DependenciesManager dependenciesManager;

	/**
	 * Construct the listener
	 * @param manager The chest manager associated with this listener
	 */
	EnderChestListener(EnderChestManager manager) {
		this.manager = manager;
		this.dependenciesManager = manager.getPlugin().getInstance(DependenciesManager.class);
	}

	/**
	 * Method called when a player interacts with something in the world
	 * @param e The interact event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();

		if (block == null || player.isSneaking()) return;

		// Right click on an ender chest?
		if (block.getType().equals(Material.ENDER_CHEST)) {

			// Plugin not enabled or world disabled in config?
			if (!Config.enabled || Config.disabledWorlds.contains(player.getWorld().getName()))
				return;

			e.setCancelled(true);

			if (!this.dependenciesManager.onBlockChestOpened(block, player))
				return;

			EUtil.runAsync(() -> {
				this.manager.openHubMenuFor(player);

				if (Config.globalSound)
					EUtil.playSound(block.getLocation(), "CHEST_OPEN", "BLOCK_CHEST_OPEN");
				else
					EUtil.playSound(player, "CHEST_OPEN", "BLOCK_CHEST_OPEN");
			});
		}
	}

	/**
	 * Method called when a player joins the server
	 * @param event The join event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		// Send update message to the player is he has the permission.
		if (EUtil.playerHasPerm(player, "update") && !Updater.getInstance().isUpToDate()) {
			player.sendMessage(Config.PREFIX+ "§aThere is a newer version available: §2§l" + Updater.getInstance().getNewestVersion() + "§a.");
			player.sendMessage(Config.PREFIX + "Download it here: §f§n" + Config.DOWNLOAD_LINK);
			EUtil.playSound(player, "NOTE_PLING", "BLOCK_NOTE_PLING");
		}
	}

	/**
	 * Method called when a player quits the server
	 * @param event The quit event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		// When a player quits, clear all of its data from memory.
		// (force method, instead of waiting the automatic purge task)
		this.manager.deleteChestsOf(player);
		StorageWrapper.unload(PlayerData.class, player.getUniqueId());
	}

	/**
	 * Method called when a player closes an inventory
	 * @param event The inventory close event
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) return;

		Player player = (Player) event.getPlayer();
		Inventory inventory = event.getInventory();

		/*
		 * To save a vanilla enderchest of an offline player,
		 * we need to force the writing of owner data on the disk
		 * so we have to check when the inventory closes to
		 * start the custom method.
		 */
		EUtil.runAsync(() -> {
			if (inventory.getType() == InventoryType.ENDER_CHEST) {
				EnderChest chest = OfflineEnderChestMenu.getOpenedChestFor(player);

				if (chest != null)
					EUtil.saveVanillaEnderchestOf(chest.getOwner(), inventory);
			}
		});
	}

}
package fr.utarwyn.endercontainers.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to convert a list of items into string and vice-versa.
 * @since 1.0.5
 * @author Utarwyn
 */
public class ItemSerializer {

	private ItemSerializer() {}

	/**
	 * Build a string with a map of ItemStacks.
	 * @param items Map of ItemStacks to convert.
	 * @return The ItemStacks formatted to string.
	 */
	public static String itemsToString(Map<Integer, ItemStack> items) {
		StringBuilder serialization = new StringBuilder(new String((items.size() + ";").getBytes(), Charset.forName("UTF-8")));

		for (Integer slot : items.keySet()) {
			ItemStack is = items.get(slot);

			if (is != null) {
				StringBuilder serializedItemStack = new StringBuilder(new String("".getBytes(), Charset.forName("UTF-8")));

				// Item type
				String isType = String.valueOf(is.getType().getId());
				serializedItemStack.append("t@").append(isType);

				// Item durability
				if (is.getDurability() != 0) {
					String isDurability = String.valueOf(is.getDurability());
					serializedItemStack.append(":d@").append(isDurability);
				}

				// Amount of the itemstack
				if (is.getAmount() != 1) {
					String isAmount = String.valueOf(is.getAmount());
					serializedItemStack.append(":a@").append(isAmount);
				}

				// Enchantments
				Map<Enchantment, Integer> isEnch = is.getEnchantments();
				if (isEnch.size() > 0)
					for (Map.Entry<Enchantment, Integer> ench : isEnch.entrySet())
						serializedItemStack.append(":e@").append(ench.getKey().getId()).append("@").append(ench.getValue());

				// Display name
				if (is.getItemMeta().getDisplayName() != null) {
					String[] itemDisplayName = new String(is.getItemMeta().getDisplayName().getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8")).split(" ");
					serializedItemStack.append(":n@");

					for (String anItemDisplayName : itemDisplayName)
						serializedItemStack.append(escapeItemDisplayName(anItemDisplayName)).append("=");

				}

				// Item descriptions
				if (is.getItemMeta().getLore() != null) {
					List<String> itemLores = is.getItemMeta().getLore();
					serializedItemStack.append(":l@");

					for (String itemLore : itemLores)
						serializedItemStack.append(escapeItemDisplayName(itemLore)).append("=");

				}

				// Slot where the itemstack is stored
				serialization.append(slot).append("#").append(serializedItemStack).append(";");
			}
		}

		return serialization.toString();
	}

	/**
	 * Restore the map of ItemStacks from a formatted string.
	 * @param invString String to parse.
	 * @return Generated map of Itemstacks.
	 */
	public static HashMap<Integer, ItemStack> stringToItems(String invString) {
		String[] serializedBlocks = invString.split("(?<!\\\\);");
		HashMap<Integer, ItemStack> items = new HashMap<>();

		// Parse every item in the string
		for (int i = 1; i < serializedBlocks.length; i++) {
			String[] serializedBlock = serializedBlocks[i].split("(?<!\\\\)#");
			int stackPosition = Integer.valueOf(serializedBlock[0]);

			ItemStack is = null;
			Boolean createdItemStack = false;

			String[] serializedItemStack = serializedBlock[1].split("(?<!\\\\):");
			for (String itemInfo : serializedItemStack) {
				String[] itemAttribute = itemInfo.split("(?<!\\\\)@");
				// Item type
				if (itemAttribute[0].equals("t")) {
					Material mat = Material.getMaterial(Integer.valueOf(itemAttribute[1]));
					if (mat == null) continue;

					is = new ItemStack(mat);
					createdItemStack = true;
				} else
					// Item durability
					if (itemAttribute[0].equals("d") && createdItemStack) {
						is.setDurability(Short.valueOf(itemAttribute[1]));
					} else
						// Itemstack amount
						if (itemAttribute[0].equals("a") && createdItemStack) {
							is.setAmount(Integer.valueOf(itemAttribute[1]));
						} else
							// Enchantments
							if (itemAttribute[0].equals("e") && createdItemStack) {
								Enchantment enchantment = Enchantment.getById(Integer.valueOf(itemAttribute[1]));
								Integer level = Integer.valueOf(itemAttribute[2]);

								is.addUnsafeEnchantment(enchantment, level);
							} else
								// Itemstack display name
								if (itemAttribute[0].equals("n") && createdItemStack) {
									ItemMeta meta = is.getItemMeta();
									String[] displayName = itemAttribute[1].split("(?<!\\\\)=");
									StringBuilder finalName = new StringBuilder();

									for (int m = 0; m < displayName.length; m++) {
										if (m == displayName.length - 1)
											finalName.append(displayName[m]);
										else
											finalName.append(displayName[m]).append(" ");
									}

									finalName = new StringBuilder(finalName.toString().replaceAll("\\\\", ""));

									meta.setDisplayName(finalName.toString());
									is.setItemMeta(meta);
								} else
									// Item description
									if (itemAttribute[0].equals("l") && createdItemStack) {
										ItemMeta meta = is.getItemMeta();
										String[] lore = itemAttribute[1].split("(?<!\\\\)=");
										List<String> itemLore = new ArrayList<>();

										for (String l : lore)
											itemLore.add(l.replaceAll("\\\\", ""));

										meta.setLore(itemLore);
										is.setItemMeta(meta);
									}
			}

			// Put the created itemstack in the map
			items.put(stackPosition, is);
		}

		return items;
	}

	/**
	 * Escape specials characters from the display name to avoid bug.
	 * @param displayName The display name to securize.
	 * @return The formatted ItemStack display name.
	 */
	private static String escapeItemDisplayName(String displayName) {
		StringBuilder sb = new StringBuilder();

		for (char c : displayName.toCharArray()) {
			switch (c) {
				case ';':
				case ':':
				case '@':
				case '=':
				case '#':
				case '\\':
					sb.append("\\\\");
				default:
					sb.append(c);
			}
		}

		return sb.toString();
	}

}

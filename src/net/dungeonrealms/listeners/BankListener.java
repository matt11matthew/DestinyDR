/**
 *
 */
package net.dungeonrealms.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankListener implements Listener {
	/**
	 * @param event
	 * @since 1.0 Bank Inventory. When a player moves items
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnderChestRightClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
			Block b = e.getClickedBlock();
			ItemStack stack = new ItemStack(b.getType(), 1);
			NBTTagCompound nbt = CraftItemStack.asNMSCopy(stack).getTag();
			// if (nbt.hasKey("type") &&
			// nbt.getString("type").equalsIgnoreCase("bank")) {
			e.setCancelled(true);
			e.getPlayer().openInventory(getBank(e.getPlayer().getUniqueId()));
			e.getPlayer().playSound(e.getPlayer().getLocation(), "random.chestopen", 1, 1);
			// }
			}
		}
	}

	/**
	 * @param event
	 * @since 1.0 Bank inventorys clicked.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBankClicked(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		if (e.getInventory().getTitle().equalsIgnoreCase("Bank Chest")) {
			if (e.getCursor() != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(e.getCursor());
			if (e.getRawSlot() < 9) {
				if (e.getRawSlot() == 8) {
					e.setCancelled(true);
					if (e.getCursor() != null) {
						if (e.getRawSlot() == 8) {
						if (e.getClick() == ClickType.LEFT) {
							openHowManyGems(player.getUniqueId());
						} else if (e.getClick() == ClickType.RIGHT) {
							openHowMuch(player.getUniqueId());
						}
						}
					}
				} else {
					e.setCancelled(true);
					if (nms.hasTag() && e.getCursor().getType() == Material.EMERALD
						|| nms.hasTag() && e.getCursor().getType() == Material.PAPER) {
						if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
						Utils.log.info("Added Gem");
						int size = 0;
						if (e.isLeftClick()) {
							if (e.getCursor().getType() == Material.EMERALD)
								size = e.getCursor().getAmount();
							else
								size = e.getCursor().getAmount() * nms.getTag().getInt("worth");
							e.setCursor(null);
							e.setCurrentItem(null);
						} else if (e.isRightClick()) {
							e.getCursor().setAmount(e.getCursor().getAmount() - 1);
							if (e.getCursor().getType() == Material.EMERALD)
								size = 1;
							else
								size = 1 * nms.getTag().getInt("worth");
						}
						BankMechanics.addGemsToPlayer(player.getUniqueId(), size);
						ItemStack bankItem = new ItemStack(Material.EMERALD);
						ItemMeta meta = bankItem.getItemMeta();
						meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
								+ ChatColor.GREEN + " Gem(s)");
						ArrayList<String> lore = new ArrayList<>();
						lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
						lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
						meta.setLore(lore);
						bankItem.setItemMeta(meta);
						net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
						nmsBank.getTag().setString("type", "bank");
						e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
						// checkOtherBankSlots(e.getInventory(),
						// player.getUniqueId());
						player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
						}
					}
				}
			} else {
				if (e.isShiftClick()) {
					nms = CraftItemStack.asNMSCopy(e.getCurrentItem());
					int size = 0;
					if (e.getCurrentItem().getType() == Material.EMERALD)
						size = e.getCurrentItem().getAmount();
					else if (e.getCurrentItem().getType() == Material.PAPER) {
						size = nms.getTag().getInt("worth");
					}
					if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("money")) {
						e.setCancelled(true);
						Utils.log.info("Added Gem");
						BankMechanics.addGemsToPlayer(player.getUniqueId(), size);
						e.setCurrentItem(null);
						ItemStack bankItem = new ItemStack(Material.EMERALD);
						ItemMeta meta = bankItem.getItemMeta();
						meta.setDisplayName(getPlayerGems(player.getUniqueId()) + size + ChatColor.BOLD.toString()
							+ ChatColor.GREEN + " Gem(s)");
						ArrayList<String> lore = new ArrayList<>();
						lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
						lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
						meta.setLore(lore);
						bankItem.setItemMeta(meta);
						net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(bankItem);
						nmsBank.getTag().setString("type", "bank");
						e.getInventory().setItem(8, CraftItemStack.asBukkitCopy(nmsBank));
						player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
					}
				}
			}
			}
		} else if (e.getInventory().getTitle().equalsIgnoreCase("How Many?")) {
			e.setCancelled(true);
			if (e.getRawSlot() < 27) {
			ItemStack current = e.getCurrentItem();
			if (current != null) {
				if (current.getType() == Material.STAINED_GLASS_PANE) {
					int number = getAmmount(e.getRawSlot());
					int currentWith = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
					int finalNum = 0;
					finalNum = currentWith + number;
					if (finalNum < 0)
						finalNum = 0;
					ItemStack item = new ItemStack(Material.EMERALD, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName("Withdraw " + finalNum + " Gems");
					item.setItemMeta(meta);
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					nms.getTag().setInt("withdraw", finalNum);
					e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
				} else if (current.getType() == Material.INK_SACK) {
					int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
					if (number == 0) {
						return;
					}
					int currentGems = getPlayerGems(player.getUniqueId());
					try {
						if (number < 0) {
						player.getPlayer().sendMessage("You can't ask for negative money!");
						} else if (number > currentGems) {
						player.getPlayer().sendMessage("You only have " + currentGems);
						} else {
						ItemStack stack = BankMechanics.gem.clone();
						if (hasSpaceInInventory(player.getUniqueId(), number)) {
							Player p = player.getPlayer();
							DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
									"info.gems", -number);
							while (number > 0) {
								while (number > 64) {
									ItemStack item = stack.clone();
									item.setAmount(64);
									p.getInventory().setItem(p.getInventory().firstEmpty(), item);
									number -= 64;
								}
								ItemStack item = stack.clone();
								item.setAmount(number);
								p.getInventory().setItem(p.getInventory().firstEmpty(), item);
								number = 0;
							}
							player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
						} else {
							player.getPlayer().sendMessage("You do not have space for all those gems");
						}
						}
						player.closeInventory();
					} catch (Exception exc) {
						exc.printStackTrace();
					}

				}
			}
			}
		} else if (e.getInventory().getTitle().equalsIgnoreCase("How much?")) {
			e.setCancelled(true);
			if (e.getRawSlot() < 27) {
			ItemStack current = e.getCurrentItem();
			if (current != null) {
				if (current.getType() == Material.STAINED_GLASS_PANE) {
					int number = getAmmount(e.getRawSlot());
					int currentWith = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
					int finalNum = 0;
					finalNum = currentWith + number;
					if (finalNum < 0)
						finalNum = 0;
					ItemStack item = new ItemStack(Material.PAPER, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName("Withdraw " + finalNum + " Gems");
					item.setItemMeta(meta);
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					nms.getTag().setInt("withdraw", finalNum);
					e.getInventory().setItem(4, CraftItemStack.asBukkitCopy(nms));
				} else if (current.getType() == Material.INK_SACK) {
					int number = CraftItemStack.asNMSCopy(e.getInventory().getItem(4)).getTag().getInt("withdraw");
					if (number == 0) {
						return;
					}
					int currentGems = getPlayerGems(player.getUniqueId());
					try {
						if (number < 0) {
						player.getPlayer().sendMessage("You can't ask for negative money!");
						} else if (number > currentGems) {
						player.getPlayer().sendMessage("You only have " + currentGems);
						} else {
						ItemStack stack = BankMechanics.banknote.clone();
						ItemMeta meta = stack.getItemMeta();
						ArrayList<String> lore = new ArrayList<String>();
						lore.add(ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString() + number);
						meta.setLore(lore);
						stack.setItemMeta(meta);
						net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
						nms.getTag().setInt("worth", number);
						Player p = player.getPlayer();
						p.getInventory().addItem(CraftItemStack.asBukkitCopy(nms));
						DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
								"info.gems", -number);
						player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
						}
						player.closeInventory();
					} catch (Exception exc) {
						exc.printStackTrace();
					}

				}
			}
			}
		}
	}

	/**
	 * Gets ammount to add, or subtract for each slot clicked in How Many?
	 * Inventory.
	 *
	 * @param slot
	 * @since 1.0
	 */
	private int getAmmount(int slot) {
		switch (slot) {
		case 0:
			return -1000;
		case 1:
			return -100;
		case 2:
			return -10;
		case 3:
			return -1;
		case 5:
			return 1;
		case 6:
			return 10;
		case 7:
			return 100;
		case 8:
			return 1000;
		}
		return 0;
	}

	/**
	 * Checks if player has room in inventory for ammount of gems to withdraw.
	 * 
	 * @param uuid
	 * @param gems
	 *           being added
	 * @since 1.0
	 */
	public boolean hasSpaceInInventory(UUID uuid, int Gems_worth) {
		if (Gems_worth > 64) {
			int space_needed = Math.round(Gems_worth / 64) + 1;
			int count = 0;
			ItemStack[] contents = Bukkit.getPlayer(uuid).getInventory().getContents();
			for (ItemStack content : contents) {
			if (content == null || content.getType() == Material.AIR) {
				count++;
			}
			}
			int empty_slots = count;

			if (space_needed > empty_slots) {
			Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED
					+ "You do not have enough space in your inventory to withdraw " + Gems_worth + " GEM(s).");
			Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + space_needed + " slots");
			return false;
			} else
			return true;
		}
		if (Bukkit.getPlayer(uuid).getInventory().firstEmpty() == -1)
			return false;
		return true;
	}

	/**
	 * Opens a GUI asking how many gems the player would like to withdraw.
	 *
	 * @param uuid
	 * @since 1.0
	 */
	public void openHowManyGems(UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 27, "How Many?");
		ItemStack item0 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1000", null,
			DyeColor.RED.getWoolData());
		ItemStack item1 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-100", null,
			DyeColor.RED.getWoolData());
		ItemStack item2 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-10", null,
			DyeColor.RED.getWoolData());
		ItemStack item3 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1", null,
			DyeColor.RED.getWoolData());
		ItemStack item4 = ItemManager.createItem(Material.EMERALD, "Withdraw 0 Gems", null);
		ItemStack item5 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1", null,
			DyeColor.LIME.getWoolData());
		ItemStack item6 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "10", null,
			DyeColor.LIME.getWoolData());
		ItemStack item7 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "100", null,
			DyeColor.LIME.getWoolData());
		ItemStack item8 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1000", null,
			DyeColor.LIME.getWoolData());
		ItemStack confimItem = ItemManager.createItemWithData(Material.INK_SACK, "Confirm", null,
			DyeColor.LIME.getDyeData());

		inv.setItem(0, item0);
		inv.setItem(1, item1);
		inv.setItem(2, item2);
		inv.setItem(3, item3);
		inv.setItem(4, item4);
		inv.setItem(5, item5);
		inv.setItem(6, item6);
		inv.setItem(7, item7);
		inv.setItem(8, item8);
		inv.setItem(26, confimItem);
		Bukkit.getPlayer(uuid).openInventory(inv);
	}

	/**
	 * Opens a GUI asking how many gems the player would like to withdraw.
	 *
	 * @param uuid
	 * @since 1.0
	 */
	public void openHowMuch(UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 27, "How Much?");
		ItemStack item0 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1000", null,
			DyeColor.RED.getWoolData());
		ItemStack item1 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-100", null,
			DyeColor.RED.getWoolData());
		ItemStack item2 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-10", null,
			DyeColor.RED.getWoolData());
		ItemStack item3 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "-1", null,
			DyeColor.RED.getWoolData());
		ItemStack item4 = ItemManager.createItem(Material.PAPER, "Withdraw 0", null);
		ItemStack item5 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1", null,
			DyeColor.LIME.getWoolData());
		ItemStack item6 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "10", null,
			DyeColor.LIME.getWoolData());
		ItemStack item7 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "100", null,
			DyeColor.LIME.getWoolData());
		ItemStack item8 = ItemManager.createItemWithData(Material.STAINED_GLASS_PANE, "1000", null,
			DyeColor.LIME.getWoolData());
		ItemStack confimItem = ItemManager.createItemWithData(Material.INK_SACK, "Confirm", null,
			DyeColor.LIME.getDyeData());

		inv.setItem(0, item0);
		inv.setItem(1, item1);
		inv.setItem(2, item2);
		inv.setItem(3, item3);
		inv.setItem(4, item4);
		inv.setItem(5, item5);
		inv.setItem(6, item6);
		inv.setItem(7, item7);
		inv.setItem(8, item8);
		inv.setItem(26, confimItem);
		Bukkit.getPlayer(uuid).openInventory(inv);
	}

	/**
	 * Gets an Inventory specific for player.
	 *
	 * @param uuid
	 * @since 1.0
	 */
	private Inventory getBank(UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
		ItemStack bankItem = new ItemStack(Material.EMERALD);
		ItemStack storage = new ItemStack(Material.CHEST, 1);
		ItemMeta storagetMeta = storage.getItemMeta();
		storagetMeta.setDisplayName(ChatColor.RED.toString() + "Storage");
		ArrayList<String> storelore = new ArrayList<>();
		storelore.add(ChatColor.GREEN.toString() + "Left Click to open your storage.");
		storelore.add(ChatColor.GREEN.toString() + "Right Click to upgrade your storage!");
		storagetMeta.setLore(storelore);
		storage.setItemMeta(storagetMeta);
		net.minecraft.server.v1_8_R3.ItemStack storagenms = CraftItemStack.asNMSCopy(storage);
		storagenms.getTag().setString("type", "storage");
		inv.setItem(0, CraftItemStack.asBukkitCopy(storagenms));

		
		
		ItemMeta meta = bankItem.getItemMeta();
		meta.setDisplayName(getPlayerGems(uuid) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.GREEN.toString() + "Left Click to withdraw Raw Gems.");
		lore.add(ChatColor.GREEN.toString() + "Right Click to create a Bank Note.");
		meta.setLore(lore);
		bankItem.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(bankItem);
		nms.getTag().setString("type", "bank");
		inv.setItem(8, CraftItemStack.asBukkitCopy(nms));
		return inv;
	}

	/**
	 * Get Player Gems.
	 *
	 * @param uuid
	 * @since 1.0
	 */
	private int getPlayerGems(UUID uuid) {
		return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
	}

}

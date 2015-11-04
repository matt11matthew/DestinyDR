package net.dungeonrealms.listeners;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mastery.RealmManager;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.LootManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.LootSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Random;

/**
 * Created by Nick on 9/18/2015.
 */
public class BlockListener implements Listener {

    /**
     * Disables the placement of core items that have NBTData of `important` in
     * `type` field.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand() == null) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemInHand());
        if (nmsItem == null) return;
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
        event.setCancelled(true);
    }

    /**
     * Handles breaking a shop
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void blockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        Block block = e.getBlock();
        if (block == null) return;
        if (block.getType() == Material.CHEST) {
            Shop shop = ShopMechanics.getShop(block);
            if (shop != null) {
                e.setCancelled(true);
                if (e.getPlayer().isOp()) {
                    shop.deleteShop();
                }
            }
        } else if (block.getType() == Material.ARMOR_STAND) {
            SpawningMechanics.getSpawners().stream().filter(spawner -> spawner.loc == block.getLocation()).forEach(SpawningMechanics::remove);
        }
    }

    /**
     * Handles breaking ore
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();	
        if (block == null) return;
        if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
        	e.setCancelled(true);
            ItemStack stackInHand = e.getPlayer().getItemInHand();
            if (Mining.isDRPickaxe(stackInHand)) {
                Player p = e.getPlayer();
                Material type = block.getType();
                int tier = Mining.getBlockTier(type);
                int pickTier = Mining.getPickTier(stackInHand);
                if (pickTier < tier) {
                    p.sendMessage(ChatColor.RED + "Your pick not strong enough to mine this ore!");
                    e.setCancelled(true);
                    return;
                }
                int experienceGain = Mining.getExperienceGain(stackInHand, type);
                Mining.addExperience(stackInHand, experienceGain, p);
                p.getItemInHand().setDurability((short) (stackInHand.getDurability() + tier));
                if (new Random().nextInt(100) <= 75)//TODO INCORPORATE CHANCE INTO PICKS
                    p.getInventory().addItem(new ItemStack(type));
                e.getBlock().setType(Material.STONE);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> e.getBlock().setType(type), (Mining.getOreRespawnTime(type) * 20L));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.ANVIL) return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR){ event.setCancelled(true); return;}
        ItemStack item = event.getPlayer().getItemInHand();
        if (RepairAPI.isItemArmorOrWeapon(item)) {
            if (RepairAPI.canItemBeRepaired(item)) {
                int cost = RepairAPI.getItemRepairCost(item);
                Player player = event.getPlayer();
                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
                    if (e.getSlot() == AnvilSlot.OUTPUT) {
                        String text = e.getName();
                        if (text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("y")) {
                        	boolean tookGems = BankMechanics.getInstance().takeGemsFromInventory(cost, player);
                            if (tookGems) {
                                RepairAPI.setCustomItemDurability(player.getItemInHand(), 1499);
                                player.updateInventory();
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have " + cost + "g");
                            }
                        } else {
                            e.destroy();
                            e.setWillClose(true);
                        }
                    }
                });
                Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName("Repair for " + cost + "g ?");
                    stack.setItemMeta(meta);
                    gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                    gui.open();
                });
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "This item is already repaired all the way!");
            }
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Handling Shops being Right clicked.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickChest(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        for (LootSpawner loot : LootManager.LOOT_SPAWNERS) {
            if (loot.location.getBlockX() == block.getX() && loot.location.getBlockY() == block.getY() && loot.location.getBlockZ() == block.getLocation().getZ()) {
                Collection<Entity> list = API.getNearbyMonsters(loot.location, 10);
                if (list.isEmpty()) {
                    Action actionType = e.getAction();
                    switch (actionType) {
                        case RIGHT_CLICK_BLOCK:
                            e.setCancelled(true);
                            e.getPlayer().openInventory(loot.inv);
                            break;
                        case LEFT_CLICK_BLOCK:
                            e.setCancelled(true);
                            for (ItemStack stack : loot.inv.getContents()) {
                                if (stack == null)
                                    continue;
                                loot.inv.remove(stack);
                                if (stack.getType() != Material.AIR)
                                    e.getPlayer().getWorld().dropItemNaturally(loot.location, stack);
                            }
                            loot.update();
                            break;
                    }
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "You can't open this while monsters are around!");
                    e.setCancelled(true);
                }
            }
        }

        Shop shop = ShopMechanics.getShop(block);
        if (shop == null)
            return;
        Action actionType = e.getAction();
        switch (actionType) {
            case RIGHT_CLICK_BLOCK:
                if (shop.isopen || shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
                    e.setCancelled(true);
                    e.getPlayer().openInventory(shop.getInv());
                } else {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "This shop is closed!");
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
                    e.setCancelled(true);
                    shop.deleteShop();
                }
                break;
            default:
        }
    }

    /**
     * Handling setting up shops.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamaged(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) return;
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItem());
            if (nmsItem == null) return;
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
            event.setCancelled(true);
            if (event.getPlayer().isSneaking()) {
                ItemStack item = event.getPlayer().getItemInHand();
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                if (nms.getTag().hasKey("usage") && nms.getTag().getString("usage").equalsIgnoreCase("profile")) {
                    /*if (ShopMechanics.PLAYER_SHOPS.get(event.getPlayer().getUniqueId()) != null) {
                        event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You already have an active shop");
                        return;
                    }*/
                    //ShopMechanics.setupShop(event.getClickedBlock(), event.getPlayer().getUniqueId());
                	if(event.getPlayer().isOp()){
                    RealmManager.getInstance().tryToOpenRealm(event.getPlayer(), event.getClickedBlock().getLocation());
                	}else{
                		event.getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "COMING SOON..");
                	}
                }
            } else {
                if (event.getClickedBlock().getType() == Material.PORTAL) {
                    if (RealmManager.getInstance().getPlayerRealm(event.getPlayer()).isRealmPortalOpen()) {
                        if (RealmManager.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()).getRealmOwner().equals(event.getPlayer().getUniqueId())) {
                            RealmManager.getInstance().removeRealm(RealmManager.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes snow that snowmen pets
     * create after 3 seconds.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void snowmanMakeSnow(EntityBlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlock().setType(Material.AIR), 60L);
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels Portals changing to Air if
     * they are not surrounded by obsidian.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysicsChange(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.PORTAL && event.getChangedType() == Material.AIR) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles a player entering a portal,
     * teleports them to wherever they should
     * be, or cancels it if they're in combat
     * etc.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }
            if (!CombatLog.isInCombat(event.getPlayer())) {
                if (RealmManager.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()) != null) {
                    String locationAsString = event.getFrom().getX() + "," + event.getFrom().getY() + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
                    DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
                    event.setTo(RealmManager.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()));
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Sorry, you've tried to enter a null realm. Attempting to remove it!");
                    RealmManager.getInstance().removeRealmViaPortalLocation(event.getFrom());
                    event.getFrom().getBlock().setType(Material.AIR);
                    if (event.getFrom().subtract(0, 1, 0).getBlock().getType() == Material.PORTAL) {
                        event.getFrom().getBlock().setType(Material.AIR);
                    }
                    if (event.getFrom().add(0, 2, 0).getBlock().getType() == Material.PORTAL) {
                        event.getFrom().getBlock().setType(Material.AIR);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId()).equals("")) {
                String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId())).split(",");
                event.setTo(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getPlayerList().remove(event.getPlayer());
            } else {
                Location realmPortalLocation = RealmManager.getInstance().getPortalLocationFromRealmWorld(event.getPlayer());
                event.setTo(realmPortalLocation.clone().add(0, 2, 0));
            }
            event.getPlayer().setFlying(false);
        }
    }

    /**
     * Handles a player breaking a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBreakBlockInRealm(BlockBreakEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getBlock().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED +  "You cannot break Portal blocks!");
        }
        if (!RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
            event.setCancelled(true);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks in this realm, please ask the owner to add you to the builders list!");
        }
    }

    /**
     * Handles a player placing a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPlaceBlockInRealm(BlockPlaceEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getBlockPlaced().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED +  "You cannot place Portal blocks!");
        }
        if (event.getBlockAgainst().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED +  "You cannot place blocks ontop of Portal blocks!");
        }
        if (!RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to the builders list!");
        }
    }
}

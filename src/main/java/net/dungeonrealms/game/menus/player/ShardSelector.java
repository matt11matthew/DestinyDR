package net.dungeonrealms.game.menus.player;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.menus.AbstractMenu;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.game.network.bungeecord.BungeeServerTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/18/2016
 */
public class ShardSelector extends AbstractMenu {

    public ShardSelector() {
        super("DungeonRealms Shards", 18);

        setDestroyOnExit(true);
        // DISPLAY AVAILABLE SHARDS //
        for (Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {

            String bungeeName = e.getKey();
            String shardID = DungeonRealms.getInstance().DR_SHARDS_NAMES.get(bungeeName);
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || shardID.equals(DungeonRealms.getInstance().shardid)) continue;

            GUIButton button = new GUIButton(Material.END_CRYSTAL) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    player.closeInventory();

                    BountifulAPI.sendTitle(player, 1, 60, 1, ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shardID + ChatColor.YELLOW + " ...", ChatColor.GRAY.toString() + "Do not disconnect");

                    player.sendMessage(ChatColor.GRAY + "Retrieving relevant server information...");
                    player.sendMessage(" ");
                    player.sendMessage("                     " + ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shardID + ChatColor.YELLOW + " ...");
                    player.sendMessage(ChatColor.GRAY + "Your current game session has been paused while you are transferred.");

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LAST_SHARD_TRANSFER, System.currentTimeMillis(), true);
                    API.handleLogout(player.getUniqueId());

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                            () -> {
                                NetworkAPI.getInstance().sendToServer(player.getName(), bungeeName);
                            }, 60);
                }
            };

            button.setDisplayName(ChatColor.GREEN + shardID);
            button.setLore(Arrays.asList(ChatColor.GREEN + "This shard is online!", " ", ChatColor.GRAY + "Online: " + info.getOnlinePlayers() + "/" + info.getMaxPlayers()));

            set(getSize(), button);
        }
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available shard for you");
            return;
        }

        long lastShardTransfer = (long) DatabaseAPI.getInstance().getData(EnumData.LAST_SHARD_TRANSFER, player.getUniqueId());

        if (lastShardTransfer != 0 && (System.currentTimeMillis() - lastShardTransfer) < 300000) {
            player.sendMessage(ChatColor.RED + "You have recently transferred your shard. You must wait 5 minutes");
            return;
        }

        player.openInventory(inventory);
    }
}

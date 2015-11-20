package net.dungeonrealms.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.json.JSONMessage;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

    static Chat instance = null;

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    List<String> TERRIBLE_WORDS = Arrays.asList(
            "shit",
            "nigger",
            "bitch",
            "xwaffle",
            "xwaffle the developer",
            "kayaba",
            "dungeonrealms.us",
            "minecade",
            "xwaffle the br",
            "FUCK THE BRS",
            "ITS A BR",
            "RUN ITS A BR",
            "chewedmarkers"
    );

    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        if (event.getMessage().startsWith("@") && !event.getMessage().contains("@i@")) {
            String playerName = event.getMessage().replace("@", "").split(" ")[0];
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).limit(1).forEach(player1 -> {
                player1.sendMessage(ChatColor.GRAY + event.getPlayer().getName() + ": " + event.getMessage().replace("@" + playerName, ""));
                event.getPlayer().sendMessage((ChatColor.GRAY + playerName + " -> " + event.getMessage().replace("@" + playerName, "")));
            });
            event.setCancelled(true);
            return;
        }


        UUID uuid = event.getPlayer().getUniqueId();

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        if (gChat) {
           	if(event.getMessage().contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR){
                String message = event.getMessage();
               final Player p = event.getPlayer();
               String aprefix = GameChat.getPreMessage(p);
               String[] split = message.split("@i@");
               String after = "";
               String before = "";
               if (split.length > 0)
                   before = split[0];
               if (split.length > 1)
                   after = split[1];
               
               final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
               normal.addText(before + "");
               normal.addItem(p.getItemInHand(), ChatColor.GREEN + ChatColor.BOLD.toString() + "SHOW" + ChatColor.WHITE, ChatColor.UNDERLINE);
               normal.addText(after);
               Bukkit.getOnlinePlayers().stream().forEach(player ->{
             	  if((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId())){
             		  normal.sendToPlayer(player);
             	  }
               });
               event.setCancelled(true);
               return;
         	}
        	
        	
            if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, event.getPlayer().getUniqueId())) {
                event.setFormat(GameChat.getPreMessage(event.getPlayer()) + event.getMessage());
            }
        } else {
            if (API.getNearbyPlayers(event.getPlayer().getLocation(), 75).size() >= 2) {
               	if(event.getMessage().contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR){
                    String message = event.getMessage();
                   final Player p = event.getPlayer();
                   String aprefix = GameChat.getPreMessage(p);
                   String[] split = message.split("@i@");
                   String after = "";
                   String before = "";
                   if (split.length > 0)
                       before = split[0];
                   if (split.length > 1)
                       after = split[1];
                   
                   final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
                   normal.addText(before + "");
                   normal.addItem(p.getItemInHand(), ChatColor.AQUA + ChatColor.BOLD.toString() + "SHOW" + ChatColor.WHITE, ChatColor.UNDERLINE);
                   normal.addText(after);
                   API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player ->{
                 	  if((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId())){
                 		  normal.sendToPlayer(player);
                 	  }
                   });
                   event.setCancelled(true);
                   return;
             	}
                event.setCancelled(true);
                API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(player)+ event.getMessage()));
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "No one heard you...");
            }

        }
    }

}

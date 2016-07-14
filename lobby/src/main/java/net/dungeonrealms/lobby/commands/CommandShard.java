package net.dungeonrealms.lobby.commands;

import net.dungeonrealms.game.commands.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.lobby.Lobby;
import net.dungeonrealms.lobby.ShardSelector;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/13/2016
 */
public class CommandShard extends BasicCommand {

    public CommandShard(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0 || !Rank.isGM(player)) {
            new ShardSelector(player).open(player);
            return true;
        }


        if (args.length > 0) {
            player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.YELLOW + "...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(Lobby.getInstance(),
                    () -> BungeeUtils.sendToServer(player.getName(), args[0]), 10);
        }

        return true;
    }

}
package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Chase on Nov 6, 2015
 */
public class CommandStop extends BasicCommand {
    public CommandStop(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof Player) {
            Player player = (Player) s;
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
                return false;
            }
        }
        DungeonRealms.getInstance().setFinishedSetup(false);
        DungeonRealms.getInstance().saveConfig();
        ShopMechanics.deleteAllShops();
        API.logoutAllPlayers(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            DungeonRealms.getInstance().mm.stopInvocation();
            Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN in 5s");
            AsyncUtils.pool.shutdown();
            Database.mongoClient.close();
        }, 200);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Bukkit::shutdown, 15 * 20L);
        return false;
    }
}
package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */
public class CommandAsk extends BaseCommand {

    public CommandAsk(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player p = (Player) sender;

        if (PunishAPI.isMuted(p.getUniqueId())) {
            p.sendMessage(PunishAPI.getMutedMessage(p.getUniqueId()));
            return true;
        }

        StringBuilder message;
        if (args.length > 0) {
            message = new StringBuilder(args[0]);

            for (int arg = 1; arg < args.length; arg++)
                message.append(" ").append(args[arg]);

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Your question has been sent to an online staff member."));
            GameAPI.sendNetworkMessage("StaffMessage", "&e<QUESTION> &6(" + DungeonRealms.getInstance().shardid + ") " + PlayerWrapper.getWrapper(p).getChatName() + "&e" + message);

        } else sender.sendMessage("/ask|a [message]");

        return false;
    }

    public String[] getAliases() {
        return new String[]{"a"};
    }
}

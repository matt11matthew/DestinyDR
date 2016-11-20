package net.dungeonrealms.control.party;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.packet.party.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evoltr on 11/17/2016.
 */
public class PartyManager {

    private DRControl control;

    private long partyUptime;

    private List<Party> parties = new ArrayList<>();

    public PartyManager(DRControl control) {
        this.control = control;
    }

    public List<Party> getParties() {
        return parties;
    }

    public Party getParty(DRPlayer player) {
        for (Party party : getParties()) {
            if (party.containsPlayer(player)) {
                return party;
            }
        }
        return null;
    }

    public long getPartyUptime() {
        return partyUptime;
    }

    public Party createParty(DRPlayer player) {
        Party party = new Party(player);

        parties.add(party);
        partyUptime = System.currentTimeMillis();
        return party;
    }

    public void removeParty(Party party) {
        parties.remove(party);
    }

    public void handleInvite(PacketPartyInvite invite) {
        DRPlayer sender = DRControl.getInstance().getPlayerManager().getPlayerByName(invite.getSender());
        DRPlayer receiver = DRControl.getInstance().getPlayerManager().getPlayerByName(invite.getReceiver());

        Party party = getParty(sender);

        // Check if sender and receiver are a different player.
        if (sender == receiver) {
            sender.sendMessage("&cYou can not invite yourself", true);
            return;
        }

        // Check if the receiver is online.
        if (receiver == null || !receiver.isOnline()) {
            sender.sendMessage("&c&l" + receiver + " &cis not online", true);
            return;
        }

        // Don't allow devs or owners to be invited.
        if ((receiver.getRank() == Rank.DEV || receiver.getRank().getID() >= Rank.DEV.getID()) && sender.getRank().getID() < Rank.DEV.getID()) {
            sender.sendMessage("&cYou can't invite this player to your party!", true);
            return;
        }

        // If sender is not already in a party, create one.
        if (party == null) {
            party = createParty(sender);
        }

        if (!party.isOwner(sender)) {
            sender.sendMessage("&cYou need to be the party leader to invite players", true);
            return;
        }

        // Check if the receiver isnt already in the party.
        if (party.containsPlayer(receiver)) {
            sender.sendMessage("&c&l" + receiver + " &cis already in the party", true);
        }

        // Check they haven't already been invited.
        if (party.isInvited(receiver)) {
            if (receiver.getRank() == Rank.DEFAULT) {
                sender.sendMessage(receiver.getRank().getColor() + receiver.getName() + " &chas already been invited", true);
            } else {
                sender.sendMessage(receiver.getRank().getColor() + receiver.getRank().getName() + " " + receiver.getName() + " &chas already been invited", true);
            }
            return;
        }

        receiver.sendMessage(" ", true);
        receiver.sendMessage("&aYou have been invited to join &a&l" + sender.getName() + "&a's party", true);
        receiver.sendMessage("&7Type &n/party accept&7 to join the party", true);

        party.broadcast("&a&l" + receiver.getName() + " &ahas joined the party!", true);
        party.invitePlayer(receiver);
    }

    public void handleAccept(PacketPartyAccept accept) {
        DRPlayer sender = DRControl.getInstance().getPlayerManager().getPlayerByName(accept.getSender());
        DRPlayer receiver = DRControl.getInstance().getPlayerManager().getPlayerByName(accept.getReceiver());

        Party party = getParty(receiver);

        if (receiver == null) {
            sender.sendMessage("&c&l" + receiver.getName() + " &cis not online", true);
            return;
        }

        // Check if there is a pending invite.
        if (party == null || !party.isInvited(sender)) {
            sender.sendMessage("&cYou are not invited to &c&l" + receiver.getName() + "&c's party", true);
            return;
        }

        // Check if they are not already in a party
        if (getParty(sender) != null) {
            sender.sendMessage("&cYou are already in a party", true);
            sender.sendMessage("&7Type &n/party leave&7 to leave the party", true);
            return;
        }
        party.addPlayer(sender);
        party.broadcast(sender.getRank().getColor() + sender.getName() + " &ahas joined the party!", true);
    }

    public void handleLeave(PacketPartyLeave leave) {
        DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(leave.getPlayer());
        Party party = getParty(player);

        // Check if player is in a party.
        if (party == null) {
            player.sendMessage("&cYou are not in a party", true);
        }

        party.broadcast(player.getRank().getColor() + player.getName() + " &ahas left the party", true);
    }

    public void handleChat(PacketPartyChat chat) {
        DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(chat.getSender());

        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("&cYou are not in a party", true);
            return;
        }
        party.broadcast("&d&lPARTY &9> &7" + chat.getSender() + ": &f" + chat.getMessage(), false);
    }

    public void handleWarp(PacketPartyWarp packet) {
        DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(packet.getPlayer());

        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("&cYou are not in a party", true);
            return;
        }

        if (!party.isOwner(player)) {
            player.sendMessage("&cYou need to be the party leader to use this command", true);
            return;
        }

        GameServer server = control.getServerManager().getGameServer(packet.getServer());

        if (server == null) {
            player.sendMessage("&cFailed to warp to shard: &c&l" + server.getName().toUpperCase(), true);
            return;
        }

        party.warp(server);
        for (DRPlayer partyPlayers : party.getPlayers()) {
            partyPlayers.sendMessage("&dYou are being warped to &e&l" + server.getName(), true);
        }
    }

    public void handleDisband(PacketPartyDisband disband) {
        DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(disband.getPlayer());

        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("&cYou are not in a party", true);
            return;
        }

        if (!party.isOwner(player)) {
            player.sendMessage("&cYou need to be the party leader to use this command", true);
            return;
        }

        party.broadcast("&cThe party you were in has been disbanded by &c&l" + party.getOwner().getName(), true);

        parties.remove(party);
    }

}

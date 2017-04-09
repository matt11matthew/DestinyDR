package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;

public class ItemHearthstone extends FunctionalItem {

	private Player player;
	
	public ItemHearthstone(Player player) {
		super(ItemType.HEARTHSTONE);
		this.player = player;
		setUndroppable(true);
	}
	
	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Hearthstone";
	}

	@Override
	protected String[] getLore() {
		return new String[]{
                ChatColor.DARK_GRAY + "Home location",
                "",
                "Use: Returns you to " + ChatColor.YELLOW + TeleportAPI.getLocationFromDatabase(player.getUniqueId()),
                "",
                ChatColor.YELLOW + "Speak to an Innkeeper to change location."};
	}

	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		evt.setCancelled(true);
		evt.closeInventory();
		if (!CombatLog.isInCombat(evt.getPlayer())) {
			
            if (TeleportAPI.isPlayerCurrentlyTeleporting(evt.getPlayer().getUniqueId())) {
            	evt.getPlayer().sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            
            if (TeleportAPI.canUseHearthstone(evt.getPlayer()))
                Teleportation.getInstance().teleportPlayer(evt.getPlayer().getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, null);
        
		} else {
			evt.getPlayer().sendMessage(ChatColor.RED + "You are in combat! Please wait (" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
		}
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INVENTORY_PICKUP;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.QUARTZ);
	}
}

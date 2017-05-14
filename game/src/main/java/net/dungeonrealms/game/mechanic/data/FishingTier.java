package net.dungeonrealms.game.mechanic.data;

import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.world.item.Item.ItemTier;

import org.bukkit.ChatColor;

@AllArgsConstructor
public enum FishingTier {

	TIER_1(0, 20, 10, 250, EnumAchievements.FISHINGROD_LEVEL_I, "Basic", "wood and thread"),
	TIER_2(20, 25, 20, 430, EnumAchievements.FISHINGROD_LEVEL_II,  "Advanced", "oak wood and thread"),
	TIER_3(40, 33, 30, 820, EnumAchievements.FISHINGROD_LEVEL_III, "Expert", "ancient oak wood and spider silk"),
	TIER_4(60, 33, 40, 1050, EnumAchievements.FISHINGROD_LEVEL_IV, "Supreme", "jungle bamboo and spider silk"),
	TIER_5(80, 45, 50, 1230, EnumAchievements.FISHINGROD_LEVEL_IV, "Master", "rich mahogany and enchanted silk");
	
	@Getter private int level;
	@Getter private int buffChance;
	@Getter private int hungerAmount;
	private int xpInc;
	@Getter private EnumAchievements achievement;
	
	private String name;
	private String description;
	
	public int getTier() { 
		return ordinal() + 1;
	}
	
	public String getItemName(ItemFishingPole rod) {
		return getColor() + (rod.getLevel() == 100 ? "Grand " : "") + this.name + " Rod";
	}
	
	public String getDescription() {
		return ChatColor.ITALIC + "A fishing rod made of " + this.description + ".";
	}
	
	public ChatColor getColor() {
		return ItemTier.getByTier(getTier()).getColor();
	}
	
	public int getXP() {
		return (int) (2D * (this.xpInc + new Random().nextInt((int) (this.xpInc * 0.3D))));
	}
	
	public static FishingTier getTierByLevel(int level) {
		for (int i = values().length; i >= 0; i--)
			if (MiningTier.values()[i].getLevel() <= level)
				return FishingTier.values()[i];
		return FishingTier.TIER_1;
	}
}
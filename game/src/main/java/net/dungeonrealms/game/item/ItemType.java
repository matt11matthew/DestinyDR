package net.dungeonrealms.game.item;

import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.ecash.*;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import lombok.Getter;

/**
 * A registry of all persistent items.
 * If an item is not registered here, it will NOT be constructable via PersistentItem.constructItem
 * @author Kneesnap
 */
public enum ItemType {

	//  COMBAT  //
	SWORD("sword", ItemWeaponSword.class, GeneratedItemType.SWORD),
	AXE("axe", ItemWeaponAxe.class, GeneratedItemType.AXE),
	POLEARM("polearm", ItemWeaponPolearm.class, GeneratedItemType.POLEARM),
	STAFF("staff", ItemWeaponStaff.class, GeneratedItemType.STAFF),
	BOW("bow", ItemWeaponBow.class, GeneratedItemType.BOW),
	
	HELMET("helmet", ItemArmorHelmet.class, GeneratedItemType.HELMET),
	CHESTPLATE("chestplate", ItemArmorChestplate.class, GeneratedItemType.CHESTPLATE),
	LEGGINGS("leggings", ItemArmorLeggings.class, GeneratedItemType.LEGGINGS),
	BOOTS("boots", ItemArmorBoots.class, GeneratedItemType.BOOTS),
	
	//  PROFESSION TOOLS  //
	PICKAXE("pick", ItemPickaxe.class, GeneratedItemType.PICKAXE),
	FISHING_POLE("rod", ItemFishingPole.class, GeneratedItemType.FISHING_ROD),
	
	//  FUNCTIONAL ITEMS  //
	SCRAP("scrap", ItemScrap.class),
	TELEPORT_BOOK("teleport", ItemTeleportBook.class),
	FISH("fish", ItemFish.class),
	FOOD("food", ItemHealingFood.class),
	POTION("potion", PotionItem.class),
	ENCHANT_WEAPON("enchantWeapon", ItemEnchantWeapon.class),
	ENCHANT_ARMOR("enchantArmor", ItemEnchantArmor.class),
	ENCHANT_PICKAXE("enchantPickaxe", ItemEnchantPickaxe.class),
	ENCHANT_FISHING_ROD("enchantFishingRod", ItemEnchantFishingRod.class),
	PROTECTION_SCROLL("protScroll", ItemProtectionScroll.class),
	REALM_CHEST("realmChest", ItemRealmChest.class),
	GUILD_BANNER("guildBanner", ItemGuildBanner.class),
	SHOP("shopItem", ShopItem.class),
	MOUNT_SELECTOR("mountSelect", ItemMountSelector.class),
	MOUNT_SKIN_SELECTOR("mountSkinSel", ItemMountSkin.class),
	
	//  ORBS  //
	ORB_OF_ALTERATION("orb", ItemOrb.class),
	ORB_OF_FLIGHT("flightOrb", ItemFlightOrb.class),
	ORB_OF_PEACE("peaceOrb", ItemPeaceOrb.class),
	
	//  MONEY  //
	GEM("gem", ItemGem.class),
	GEM_POUCH("gemPouch", ItemGemPouch.class),
	GEM_NOTE("gemNote", ItemGemNote.class),
	
	//  ECASH  //
	GLOBAL_MESSAGER("globalMessager", ItemGlobalMessager.class),
	RETRAINING_BOOK("retrainingBook", ItemRetrainingBook.class),
	MOUNT("mount", ItemMount.class),
	MULE("mule", ItemMuleMount.class),
	MULE_UPGRADE("muleUpgrade", ItemMuleUpgrade.class),
	PARTICLE_TRAIL("trail", ItemParticleTrail.class),
	PET("pet", ItemPet.class),
	PET_SELECTOR("petSelector", ItemPetSelector.class),
	PARTICLE_SELECTOR("particleSelector", ItemParticleSelector.class),
	HEARTHSTONE_RELOCATE("hearthstoneRelocator", ItemHearthStoneRelocator.class),
	
	//  DONATION  //
	BUFF("buff", ItemBuff.class),
	
	//  MENU ITEMS  //
	PLAYER_JOURNAL("journal", ItemPlayerJournal.class),
	PORTAL_RUNE("portalRune", ItemPortalRune.class),
	HEARTHSTONE("hearthstone", ItemHearthstone.class),
	OPEN_PROFILE("profile", ItemPlayerProfile.class);
	
	@Getter private final String NBT;
	@Getter private final Class<? extends PersistentItem> itemClass;
	@Getter private GeneratedItemType type;
	
	ItemType(String nbt, Class<? extends PersistentItem> cls) {
		this(nbt, cls, null);
	}
	
	ItemType(String nbt, Class<? extends PersistentItem> cls, GeneratedItemType gType) {
		this.NBT = nbt;
		this.itemClass = cls;
		this.type = gType;
	}
	
	public static ItemType getByName(String name) {
		for (ItemType type : values())
			if (name.equalsIgnoreCase(name))
				return type;
		return null;
	}
	
	public static ItemType getType(String type) {
		for (ItemType it : ItemType.values())
			if (it.getNBT().equals(type))
				return it;
		return null;
	}
}

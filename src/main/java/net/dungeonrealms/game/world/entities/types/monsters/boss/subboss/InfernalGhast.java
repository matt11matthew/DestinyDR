package net.dungeonrealms.game.world.entities.types.monsters.boss.subboss;

import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.InfernalAbyss;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.minecraft.server.v1_9_R2.EntityGhast;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalGhast extends EntityGhast implements Boss {

	private InfernalAbyss boss;

	
	/**
	 * @param infernalAbyss
	 */
	public InfernalGhast(InfernalAbyss infernalAbyss) {
		super(infernalAbyss.getWorld());
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		this.boss = infernalAbyss;
		int health = boss.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
		int maxHealth = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
		this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), health));
		this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHealth));
	}

	public void init() {
		int health = boss.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
		int maxHealth = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
		this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), health));
//		HealthHandler.getInstance().setMonsterHPLive((LivingEntity) this, health);
		this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHealth));
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.InfernalGhast;
	}

	@Override
	public void onBossDeath() {
		say(this.getBukkitEntity(), "Guuuards!");
		this.getWorld().addEntity(boss.guard, SpawnReason.CUSTOM);
		boss.guard.setLocation(locX, locY, locZ, 1, 1);
	}

	/**
	 */
	public void setArmor(ItemStack[] armor, ItemStack weapon) {
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(armor[3]));
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		livingEntity.getEquipment().setItemInMainHand(weapon);
		livingEntity.getEquipment().setBoots(armor[0]);
		livingEntity.getEquipment().setLeggings(armor[1]);
		livingEntity.getEquipment().setChestplate(armor[2]);
		livingEntity.getEquipment().setHelmet(armor[3]);
	}

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		//LivingEntity en = (LivingEntity) event.getEntity();
	}

}

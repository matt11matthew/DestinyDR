package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.BasicEntitySkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Oct 18, 2015
 */
public class Mayel extends BasicEntitySkeleton implements Boss {

	/**
	 * @param world
	 */
	public Mayel(World world) {
		super(world);
	}

	public Location loc;

	public Mayel(World world, Location loc) {
		super(world);
		this.loc = loc;
		this.setSkeletonType(1);
		setArmor(getEnumBoss().tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 100;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().name));
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}

	}

	@Override
	public void setArmor(int tier) {
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getWeapon();
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("mayelboot")));
		this.setEquipment(2, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("mayelpants")));
		this.setEquipment(3, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("mayelchest")));
		//this.setEquipment(4, getHead());
	}

	/**
	 * @return
	 */
	private ItemStack getWeapon() {
		return ItemGenerator.getNamedItem("mayelbow");
	}

	/**
	 * Called when entity fires a projectile.
	 */
	@Override
	public void a(EntityLiving entityliving, float f) {
		/*EntityArrow entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, 14 - 2 * 4);
		entityarrow.b(f * 2.0F + this.random.nextGaussian() * 0.25D + 2 * 0.11F);
		Projectile arrowProjectile = (Projectile) entityarrow.getBukkitEntity();
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
		NBTTagCompound tag = nmsItem.getTag();
		MetadataUtils.registerProjectileMetadata(tag, arrowProjectile, 2);
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entityarrow);*/

		net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
		NBTTagCompound tag = nmsItem.getTag();
		DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());
	}

	@Override
	public void onBossDeath() {
		say(this.getBukkitEntity(), getEnumBoss().death);
		int droppedGems = 64 * this.getBukkitEntity().getWorld().getPlayers().size();
		for(int i = 0; i < droppedGems; i++){
			this.getBukkitEntity().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 4, 0), BankMechanics.createGems(1));
		}
		
	}

	public boolean canSpawn = true;

	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();
		if (canSpawn) {
			for (int i = 0; i < 5; i++) {
				Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.MayelPirate);
				int level = Utils.getRandomFromTier(1, "high");
				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, level);
				EntityStats.setMonsterRandomStats(entity, level, 1);
				
				if (entity == null)
					return;
				entity.setCustomName("Mayel Pirate");
					ArmorStand stand = entity.getBukkitEntity().getLocation().getWorld()
				            .spawn(entity.getBukkitEntity().getLocation(), ArmorStand.class);
					stand.setRemoveWhenFarAway(false);
					stand.setVisible(false);
					stand.setSmall(true);
					stand.setBasePlate(false);
					stand.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "nametag"));
					stand.setGravity(false);
					stand.setArms(false);
					stand.setCustomNameVisible(true);
					stand.setCustomName("Mayel Pirate");
					stand.setRemoveWhenFarAway(false);
					entity.getBukkitEntity().setPassenger(stand);
					EntityStats.setMonsterElite(entity, level + 10, 1);
					stand.setCustomName(entity.getCustomName());
				Location location = new Location(world.getWorld(),
				        this.getBukkitEntity().getLocation().getX() + new Random().nextInt(3),
				        this.getBukkitEntity().getLocation().getY(),
				        this.getBukkitEntity().getLocation().getZ() + new Random().nextInt(3));
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
				world.addEntity(entity, SpawnReason.CUSTOM);
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
				canSpawn = false;
			}
			say(this.getBukkitEntity(), "Come to my call, brothers!");
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> canSpawn = true, 20 * 5);
		}

	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.Mayel;
	}
}

package net.dungeonrealms.game.mechanic;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by Kieran on 9/20/2015.
 */
public class ParticleAPI {

	@Getter
    public enum ParticleEffect {
        FIREWORKS_SPARK(Material.FIREWORK, "Fireworks"),
        WATER_BUBBLE(Material.WATER_BUCKET, "Bubble", -1),
        TOWN_AURA(Material.SULPHUR, "Stubble"),
        CRIT(Material.NETHER_STAR, "Light Stars"),
        CRIT_MAGIC(Material.FIREWORK_CHARGE, "Dark Stars"),
        SPELL_WITCH(Material.CAULDRON_ITEM, "Magic"),
        NOTE(Material.NOTE_BLOCK, "Notes", 1250),
        PORTAL(Material.EYE_OF_ENDER, "Portal", 1250),
        ENCHANTMENT_TABLE(Material.ENCHANTMENT_TABLE, "Enchantment"),
        FLAME(Material.FIREBALL, "Flames", 1250),
        LAVA(Material.LAVA_BUCKET, "Lava", -1),
        WATER_SPLASH(Material.WATER_BUCKET, "Splash"),
        SMOKE_LARGE(Material.MELON, "Thick Smoke", -1),
        REDSTONE(Material.CAKE, "Birthday", ChatColor.RED, 1250),
        SNOWBALL(Material.SNOW_BALL, "Snowball"),
        SMOKE_NORMAL(Material.SUGAR, "Thin Smoke", 1250),
        CLOUD(Material.BEACON, "Cloudy", 1250),
        VILLAGER_HAPPY(Material.SPIDER_EYE, "Poison", ChatColor.DARK_GREEN, 650),
        SPELL(Material.BLAZE_POWDER, "Potion", -1),
        SNOW_SHOVEL(Material.SNOW, "Snowfall"),
        HEART(Material.APPLE, "Hearts", -1);

        private ItemStack selectionItem;
        private String displayName;
        private ChatColor color;
        private int price;
        
        
        ParticleEffect(Material mat, String displayName) {
        	this(mat, displayName, 650);
        }
        
        ParticleEffect(Material mat, String displayName, int price) {
        	this(mat, displayName, ChatColor.WHITE, price);
        }

        ParticleEffect(Material mat, String displayName, ChatColor color, int price) {
            this.selectionItem = new ItemStack(mat);
            this.displayName = displayName;
            this.color = color;
            this.price = price;
        }
        
        public EnumParticle getParticle() {
        	return EnumParticle.valueOf(name());
        }
        
        public int getId() {
        	return ordinal();
        }

        public static ParticleEffect getById(int id) {
            for (ParticleEffect particleEffect : values())
                if (particleEffect.getId() == id)
                    return particleEffect;
            return null;
        }

        public static ParticleEffect getByName(String rawName) {
            for (ParticleEffect particleEffect : values())
                if (particleEffect.name().equalsIgnoreCase(rawName))
                    return particleEffect;
            return null;
        }
    }


    public static void sendParticleToEntityLocation(final ParticleEffect particleEffect, Entity entity, final float xOffset, final float yOffset, final float zOffset, final float particleSpeed, final int particleCount) {
        sendParticleToEntityLocation(particleEffect, ((CraftEntity) entity), xOffset, yOffset, zOffset, particleSpeed, particleCount);
    }

    public static void sendParticleToEntityLocation(final ParticleEffect particleEffect, CraftEntity entity, final float xOffset, final float yOffset, final float zOffset, final float particleSpeed, final int particleCount) {
        Object packet = null;
        try {
            packet = newPacket(particleEffect, entity.getLocation(), xOffset, yOffset, zOffset, particleSpeed, particleCount);
        } catch (Exception e) {
            Utils.log.info("Something went wrong creating a packet");
        }

        for (Player player : GameAPI.getNearbyPlayers(entity, 25)) {
            try {
                sendPacketToPlayer(player, packet);
            } catch (Exception e) {
                Utils.log.info("Unable to send particle packet to player " + player.getName());
            }
        }
    }


    /**
     * Sends a particle to a location so that every player within 25 blocks can see it
     *
     * @param particleEffect
     * @param location
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param particleSpeed
     * @param particleCount
     * @since 1.0
     */
    public static void sendParticleToLocation(final ParticleEffect particleEffect, final Location location, final float xOffset, final float yOffset, final float zOffset, final float particleSpeed, final int particleCount) {
        Object packet = null;
        try {
            packet = newPacket(particleEffect, location, xOffset, yOffset, zOffset, particleSpeed, particleCount);
        } catch (Exception e) {
            Utils.log.info("Something went wrong creating a packet");
        }

        for (Player player : GameAPI.getNearbyPlayers(location, 25)) {
            try {
                sendPacketToPlayer(player, packet);
            } catch (Exception e) {
                Utils.log.info("Unable to send particle packet to player " + player.getName());
            }
        }
    }

    /**
     * Sends a particle to a location so that every player within 25 blocks can see it
     *
     * @param particleEffect
     * @param location
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param particleSpeed
     * @param particleCount
     * @since 1.0
     */
    public static void sendParticleToLocationAsync(final ParticleEffect particleEffect, final Location location, final float xOffset, final float yOffset, final float zOffset, final float particleSpeed, final int particleCount) {
        Object packet = null;
        try {
            packet = newPacket(particleEffect, location, xOffset, yOffset, zOffset, particleSpeed, particleCount);
        } catch (Exception e) {
            Utils.log.info("Something went wrong creating a packet");
        }

        for (Player player : GameAPI.getNearbyPlayersAsync(location, 25)) {
            try {
                sendPacketToPlayer(player, packet);
            } catch (Exception e) {
                Utils.log.info("Unable to send particle packet to player " + player.getName());
            }
        }
    }
    
    public static void spawnParticle(Particle p, Location loc, int count, float speed) {
    	Random r = new Random();
    	spawnParticle(p, loc, r.nextFloat(), r.nextFloat(), r.nextFloat(), count, speed);
    }
    
    public static void spawnParticle(Particle p, Location loc, double xOff, double yOff, double zOff, int count, float speed) {
    	GameAPI.getNearbyPlayers(loc, 30).forEach(pl -> pl.spawnParticle(p, loc, count, xOff, yOff, zOff, speed));
    }

    /**
     * Creates a new packet to send to players with given parameters
     *
     * @param particleEffect
     * @param location
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param particleSpeed
     * @param particleCount
     * @since 1.0
     */
    private static Object newPacket(ParticleEffect particleEffect, Location location, float xOffset, float yOffset, float zOffset, float particleSpeed, int particleCount) throws Exception {
        Object packet = new PacketPlayOutWorldParticles();
        setPacketValue(packet, "a", particleEffect.getParticle());
        setPacketValue(packet, "b", (float) location.getX());
        setPacketValue(packet, "c", (float) location.getY());
        setPacketValue(packet, "d", (float) location.getZ());
        setPacketValue(packet, "e", xOffset);
        setPacketValue(packet, "f", zOffset);
        setPacketValue(packet, "g", yOffset);
        setPacketValue(packet, "h", particleSpeed);
        setPacketValue(packet, "i", particleCount);
        return packet;
    }

    /**
     * Sets the packets value so that the location etc registers correctly
     *
     * @param instance
     * @param fieldName
     * @param value
     * @since 1.0
     */
    private static void setPacketValue(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Sends the packet to a player
     *
     * @param player
     * @param packet
     * @since 1.0
     */
    private static void sendPacketToPlayer(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
}

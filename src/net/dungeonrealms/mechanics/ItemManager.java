package net.dungeonrealms.mechanics;

import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Nick on 9/18/2015.
 */
public class ItemManager {
    /**
     * returns hearthstone
     *
     * @param name
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createHearthStone(String name, String[] lore) {
        ItemStack rawStack = new ItemStack(Material.QUARTZ);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("hearthstone"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a random Teleport book
     *
     * @param name
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createRandomTeleportBook(String name) {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        String teleportLocation = TeleportAPI.getRandomTeleportString();
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + teleportLocation));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(teleportLocation));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a scrap piece based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createArmorScrap(int tier) {
        ItemStack rawStack = null;
        String name = "";
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.LEATHER);
                name = ChatColor.BOLD + "Leather";
                break;
            case 2:
                rawStack = new ItemStack(Material.IRON_FENCE);
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Chain";
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_INGOT);
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Iron";
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND);
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Diamond";
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_INGOT);
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Gold";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name + " Scrap");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Repairs 3% durability on " + name + ChatColor.GRAY + " items."));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("scrap"));
            tag.setInt("itemTier", tier);
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }
    /**
     * Creates a pickaxe based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createPickaxe(int tier){
        ItemStack rawStack = null;
        String name = "";
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.WOOD_PICKAXE);
                name = ChatColor.BOLD + "Weak Pick";
                break;
            case 2:
                rawStack = new ItemStack(Material.STONE_PICKAXE);
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Basic Pick";
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_PICKAXE);
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Intermediate Pick";
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND_PICKAXE);
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Strong Pick";
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_PICKAXE);
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Master Pick";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
    		String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
            meta.setLore(Collections.singletonList(ChatColor.RED.toString() + expBar));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("pickaxe"));
            tag.setInt("itemTier", tier);
            tag.setInt("XP", 0);
            tag.setInt("maxXP", Mining.getMaxXP(tier));
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }
    
    
    public static ItemStack createFishingPole(int tier){
        ItemStack rawStack = new ItemStack(Material.FISHING_ROD);
        String name = "";
        switch (tier) {
            case 1:
                name = ChatColor.BOLD + "Weak Rod";
                break;
            case 2:
                name = ChatColor.GREEN.toString() + ChatColor.BOLD + "Basic Rod";
                break;
            case 3:
                name = ChatColor.AQUA.toString() + ChatColor.BOLD + "Intermediate Rod";
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Strong Rod";
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Master Rod";
                break;
            default:
                break;
        }
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        meta.setLore(Collections.singletonList(ChatColor.RED.toString() + expBar));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("rod"));
        tag.setInt("itemTier", tier);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getMaxXP(tier));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
    
    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItem(Material m, String name, String[] lore) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItemWithData(Material m, String name, String[] lore, short i) {
        ItemStack is = new ItemStack(m, 1, i);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * returns playerProfile
     *
     * @param player
     * @param displayName
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack getPlayerProfile(Player player, String displayName, String[] lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        skull.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(skull);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("profile"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
}

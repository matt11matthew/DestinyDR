package net.dungeonrealms.game.item.items.functional.ecash.jukebox;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.LocationUtils;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

public class ItemJukebox extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    @Getter
    private static Map<Block, MobileJukebox> mobileJukeboxes = new ConcurrentHashMap<>();
    private static Map<UUID, Long> jukeboxCooldowns = new ConcurrentHashMap<>();

    private static int taskID = -1;

    private static final int SPACE_REQUIRED = 6 * 6;

    public ItemJukebox(ItemStack item) {
        this();
    }

    public ItemJukebox() {
        super(ItemType.JUKE_BOX);
        setUndroppable(true);
        setSoulbound(true);
    }

    public static MobileJukebox getPlacedJukebox(UUID uuid) {
        return mobileJukeboxes.values().stream().filter(box -> box.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public static MobileJukebox getJukebox(Block block) {
        return mobileJukeboxes.get(block);
    }

    public static MobileJukebox getNearbyJukebox(Location location) {
        return mobileJukeboxes.entrySet().stream().filter(juke -> LocationUtils.distanceSquared(juke.getKey().getLocation(), location) <= SPACE_REQUIRED).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.JUKEBOX);
    }

    public static void attemptTaskStart() {
        if (taskID == -1) {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (mobileJukeboxes.size() == 0) {
                    //Cancel this.
                    Bukkit.getScheduler().cancelTask(taskID);
                    taskID = -1;
                } else {
                    mobileJukeboxes.forEach((block, juke) -> {
                        if (!block.getChunk().isLoaded() || block.getType() != Material.JUKEBOX || juke.getTimeoutTime() > 0 && juke.getTimeoutTime() <= System.currentTimeMillis()) {
                            //Remove to not keep processing..
                            juke.breakJukebox();
                            return;
                        }

                        CompletableFuture.runAsync(() -> {
                            ParticleAPI.spawnParticle(Particle.NOTE, block.getLocation().add(.5, .95, .5), 40, .5F);
                            ParticleAPI.spawnParticle(Particle.NOTE, block.getLocation().add(.5, 1, 1.5), 2, .02F);
                            ParticleAPI.spawnParticle(Particle.NOTE, block.getLocation().add(.5, 1, -1.5), 2, .02F);
                            ParticleAPI.spawnParticle(Particle.NOTE, block.getLocation().add(1.5, 1, .5), 2, .02F);
                            ParticleAPI.spawnParticle(Particle.NOTE, block.getLocation().add(-1.5, 1, .5), 2, .02F);
                        }, ForkJoinPool.commonPool());
                    });
                }
            }, 20, 5);
        }
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        Player player = evt.getPlayer();
        evt.setCancelled(true);
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (Purchaseables.JUKEBOX.getNumberOwned(wrapper) <= 0) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "(!) " + ChatColor.RED + "You do NOT own a Mobile Musicbox!");
            player.sendMessage(ChatColor.GRAY + "You can unlock one at " + ChatColor.UNDERLINE + Constants.SHOP_URL);
            evt.setResultItem(null);
            return;
        }
        if (evt.hasBlock()) {
            //Trying to place?
            Block clicked = evt.getClickedBlock();
            Block block = clicked.getRelative(BlockFace.UP);
            if (block.getType() == Material.AIR && clicked.getType().isSolid()) {

                MobileJukebox box = getPlacedJukebox(player.getUniqueId());
                if (box != null) {
                    Location l = box.getJukebox().getLocation();
                    player.sendMessage(ChatColor.RED + "It seems you already have a Mobile Musicbox summoned.");
                    player.sendMessage(ChatColor.GRAY + "You can find it at these coordinates: " + ChatColor.BOLD + l.getBlockX() + "x " + l.getBlockY() + "y " + l.getBlockZ() + "z");
                    return;
                }

                if (!GameAPI.isMainWorld(player)) {
                    player.sendMessage(ChatColor.RED + "You can only use this in Andalucia!");
                    return;
                }
                //can be placed?
                //Open menu?
                MobileJukebox closest = getNearbyJukebox(block.getLocation());
                if (closest != null) {
                    player.sendMessage(ChatColor.RED + "You cannot place a Mobile Musicbox so close to another one!");
                    player.sendMessage(ChatColor.GRAY + "Closest Jukebox: " + ChatColor.BOLD + closest.getOwner());
                    return;
                }

                Long cooldown = jukeboxCooldowns.get(player.getUniqueId());

                //Cooldown?
                if (cooldown != null && cooldown > System.currentTimeMillis()) {
                    player.sendMessage(ChatColor.RED + "Please wait before trying to place another Mobile Musicbox!");
                    return;
                }


                jukeboxCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 5_000);
                MobileJukebox jukebox = new MobileJukebox(block, player.getUniqueId(), player.getName(), null);
                new MobileJukeboxGUI(player, jukebox).open(player, null);

                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1, .5F);
            } else {
                player.sendMessage(ChatColor.RED + "You cannot place your Mobile Musicbox here!");
            }
        }
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Mobile Musicbox";
    }

    @Override
    protected String[] getLore() {
        return new String[]{"", ChatColor.GRAY + "Place down to play some", ChatColor.GRAY + "tunes for nearby players!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT;
    }


}

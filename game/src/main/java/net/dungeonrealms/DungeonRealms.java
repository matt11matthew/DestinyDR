package net.dungeonrealms;

import com.esotericsoftware.minlog.Log;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.Database;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.game.updater.UpdateTask;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.anticheat.PacketLogger;
import net.dungeonrealms.game.anticheat.PacketModifier;
import net.dungeonrealms.game.command.*;
import net.dungeonrealms.game.command.dungeon.*;
import net.dungeonrealms.game.command.friend.AcceptCommand;
import net.dungeonrealms.game.command.friend.AddCommand;
import net.dungeonrealms.game.command.friend.FriendsCommand;
import net.dungeonrealms.game.command.friend.RemoveCommand;
import net.dungeonrealms.game.command.gameplay.CommandGraveyard;
import net.dungeonrealms.game.command.guild.*;
import net.dungeonrealms.game.command.menu.*;
import net.dungeonrealms.game.command.moderation.*;
import net.dungeonrealms.game.command.party.*;
import net.dungeonrealms.game.command.punish.*;
import net.dungeonrealms.game.command.support.CommandSupport;
import net.dungeonrealms.game.command.test.*;
import net.dungeonrealms.game.command.toggle.*;
import net.dungeonrealms.game.commands.quests.CommandQuestEditor;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.*;
import net.dungeonrealms.game.listener.MainListener;
import net.dungeonrealms.game.listener.TabCompleteCommands;
import net.dungeonrealms.game.listener.combat.DamageListener;
import net.dungeonrealms.game.listener.combat.PvEListener;
import net.dungeonrealms.game.listener.combat.PvPListener;
import net.dungeonrealms.game.listener.inventory.*;
import net.dungeonrealms.game.listener.mechanic.BankListener;
import net.dungeonrealms.game.listener.mechanic.BossListener;
import net.dungeonrealms.game.listener.mechanic.EnergyListener;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.listener.network.BungeeChannelListener;
import net.dungeonrealms.game.listener.network.NetworkClientListener;
import net.dungeonrealms.game.listener.world.BlockListener;
import net.dungeonrealms.game.listener.world.DungeonListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.GraveyardMechanic;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.generic.MechanicManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.TabbedChatListener;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.combat.ForceField;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.tab.TabMechanics;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.packet.type.ServerListPacket;
import net.dungeonrealms.tool.PatchTools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonRealms extends JavaPlugin {

    private static long SERVER_START_TIME;

    private static long rebootTime;

    @Getter
    private static ShardInfo shard;

    @Getter
    private static GameClient client;

    private static DungeonRealms instance = null;
    private static CraftingMenu cm;
    private static TabCompleteCommands tcc;

    @Getter
    @Setter
    private boolean almostRestarting = false;

    // Shard Config
    public MechanicManager mm = null;
    public boolean isInstanceServer = false;
    public String bungeeName = "Lobby";
    public int realmnumber = -1;
    public int realmport = -1;
    public int realmmax = 0;
    public int realmpmax = 0;
    public String shardid = "US-666";
    public boolean isMasterShard = false; // Master shard (US-0) - handles rollout / editable / etc.
    public boolean isSubscriberShard = false; // Subscriber shard - only allow subsribers.
    public boolean isSupportShard = false; // Custom support shard - should we enable support commands?
    public boolean isYouTubeShard = false; // YouTuber shard - only YTers / staff allowed.
    public boolean isBrazilianShard = false; // Brazilian shard - eventually create DR localization, etc.
    public boolean isRoleplayShard = false; // Role playing shard - prompt user its a RP shard.
    public boolean isBetaShard = false; // Beta shard - enable extended capabilities / alert user about bugs.
    public boolean isEventShard = false; // Event shard - Used for any events.
    public boolean isGMExtendedPermissions = false; // Does the GM have extended permissions (events / spawning / etc).
    // End of Shard Config

    private volatile boolean acceptPlayers = false;

    public boolean isDrStopAll;

    @Getter
    private Set<UUID> loggingIn = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    @Getter
    private List<String> loggingOut = new ArrayList<>();

    public static DungeonRealms getInstance() {
        return instance;
    }

    public static long getServerStart() {
        return SERVER_START_TIME;
    }


    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
        instance = this;
    }

    public static int rebooterID;

    public List<String> getDevelopers() {
        return Arrays.asList(Constants.DEVELOPERS);
    }

    public boolean canAcceptPlayers() {
        return acceptPlayers;
    }

    public void setAcceptPlayers(boolean bool) {
        acceptPlayers = bool;
    }

    public void onEnable() {
        Constants.build();
        SERVER_START_TIME = System.currentTimeMillis();

        Utils.log.info("DungeonRealms - Hello World. Starting up.");
        saveDefaultConfig();

        // RANDOMIZE REBOOT TIME //
        Random random = new Random();
        long min = Constants.MIN_GAME_TIME + SERVER_START_TIME;
        long max = Constants.MAX_GAME_TIME + SERVER_START_TIME;

        setRebootTime(min + (long) (random.nextDouble() * (max - min)));

        Utils.log.info("DungeonRealms - Reading Shard Config");
        
        Ini ini = new Ini();
        try {
            ini.load(new FileReader("shardconfig.ini"));
            // Main
            isInstanceServer = ini.get("Main", "instanced", Boolean.class);
            shardid = ini.get("Main", "shardid", String.class);
            bungeeName = ini.get("Bungee", "name", String.class);

            realmnumber = ini.get("RealmInstance", "number", int.class);
            realmport = ini.get("RealmInstance", "port", int.class);
            realmmax = ini.get("RealmInstance", "maxrealms", int.class);
            realmpmax = ini.get("RealmInstance", "maxplayers", int.class);
            // Shard Settings
            isMasterShard = ini.get("Settings", "master_shard", Boolean.class);
            isSubscriberShard = ini.get("Settings", "subscriber_shard", Boolean.class);
            isSupportShard = ini.get("Settings", "support_shard", Boolean.class);
            isYouTubeShard = ini.get("Settings", "youtube_shard", Boolean.class);
            isBrazilianShard = ini.get("Settings", "brazilian_shard", Boolean.class);
            isRoleplayShard = ini.get("Settings", "roleplay_shard", Boolean.class);
            isBetaShard = ini.get("Settings", "beta_shard", Boolean.class);
            if (ini.get("Settings").containsKey("event_shard"))
                isEventShard = ini.get("Settings", "event_shard", Boolean.class);
            else
                ini.add("Settings", "event_shard", false);
        } catch (InvalidFileFormatException e1) {
            Utils.log.info("InvalidFileFormat in shard config!");
        } catch (FileNotFoundException e1) {
            Utils.log.info("Shard Config not found!");
        } catch (IOException e1) {
            Utils.log.info("IOException in shard config!");
        }
        shard = ShardInfo.getByShardID(shardid);
        BungeeUtils.setPlugin(this);
        BungeeUtils.fetchServers();
        
        Utils.log.info("DungeonRealms - Discovered Identity as " + shard.getShardID());
        Utils.log.info("DungeonRealms - Connecting to Master Server");
        
        client = new GameClient();

        try {
            client.connect();
            Log.set(Log.LEVEL_INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //These have to load seperately since they are part of dr-common and therefor cannot implement GenericMechanic
        DatabaseInstance.getInstance().startInitialization(true);
        DatabaseAPI.getInstance().startInitialization(bungeeName);
        
        ItemGenerator.loadModifiers();
        PowerMove.registerPowerMoves();
        ItemGenerator.loadModifiers();

        
        Utils.log.info("DungeonRealms - Loading Mechanics");

        mm = new MechanicManager();
        mm.registerMechanic(NetworkClientListener.getInstance());
        mm.registerMechanic(DungeonManager.getInstance());
        mm.registerMechanic(AntiDuplication.getInstance());
        mm.registerMechanic(TipHandler.getInstance());
        mm.registerMechanic(PetUtils.getInstance());
        mm.registerMechanic(CombatLog.getInstance());
        mm.registerMechanic(EnergyHandler.getInstance());
        mm.registerMechanic(DonationEffects.getInstance());
        mm.registerMechanic(HealthHandler.getInstance());
        mm.registerMechanic(KarmaHandler.getInstance());
        mm.registerMechanic(BankMechanics.getInstance());
        mm.registerMechanic(BungeeChannelListener.getInstance());
        mm.registerMechanic(NetworkClientListener.getInstance());
        mm.registerMechanic(ScoreboardHandler.getInstance());
        mm.registerMechanic(new ShopMechanics());
        mm.registerMechanic(new EntityMechanics());
        mm.registerMechanic(PatchTools.getInstance());
        mm.registerMechanic(Mining.getInstance());
        mm.registerMechanic(Realms.getInstance());
        mm.registerMechanic(Affair.getInstance());
        mm.registerMechanic(AchievementManager.getInstance());
        mm.registerMechanic(new LootManager());
        mm.registerMechanic(Quests.getInstance());
        mm.registerMechanic(new PacketModifier());
        mm.registerMechanic(Fishing.getInstance());
        mm.registerMechanic(TutorialIsland.getInstance());
        mm.registerMechanic(new TradeManager());
        
        if (!isInstanceServer) {
            mm.registerMechanic(Teleportation.getInstance());;
            mm.registerMechanic(new ForceField());
            mm.registerMechanic(CrashDetector.getInstance());
            mm.registerMechanic(SpawningMechanics.getInstance());
            mm.registerMechanic(TabMechanics.getInstance());
            mm.registerMechanic(BuffManager.getInstance());
            mm.registerMechanic(new GraveyardMechanic());
        }
        
        mm.loadMechanics();

        // START UPDATER TASK //
        new UpdateTask(this);

        PluginManager pm = Bukkit.getPluginManager();
        Utils.log.info("DungeonRealms - Registering Events");

        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new ItemListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new EnergyListener(), this);
        pm.registerEvents(new AntiCheatListener(), this);
        pm.registerEvents(new AchievementManager(), this);
        pm.registerEvents(new TabbedChatListener(), this);
        pm.registerEvents(new RestrictionListener(), this);
        pm.registerEvents(new DungeonListener(), this);
        pm.registerEvents(new BossListener(), this);
        pm.registerEvents(new PvPListener(), this);
        pm.registerEvents(new PvEListener(), this);
        pm.registerEvents(new PacketLogger(), this);
        pm.registerEvents(new CurrencyTabListener(), this);
        
        if (!isInstanceServer) {
            pm.registerEvents(new MainListener(), this);
            pm.registerEvents(new BankListener(), this);
            pm.registerEvents(new TitleAPI(), this);
            pm.registerEvents(TutorialIsland.getInstance(), this);
            //TODO: Fix.
            pm.registerEvents(new ShopListener(), this);
            pm.registerEvents(new PassiveEntityListener(), this);
            cm = new CraftingMenu();
            tcc = new TabCompleteCommands();
            cm.onEnable();
            tcc.onEnable();
        }
        
        Utils.log.info("DungeonRealms - Registering Commands");

        CommandManager cm = new CommandManager();

        cm.registerCommand(new CommandGraveyard());
        // Commands always registered regardless of server.
        cm.registerCommand(new CommandDevDebug());
        cm.registerCommand(new CommandCloseShop());
        cm.registerCommand(new CommandListInstance());

        cm.registerCommand(new CommandInvsee());
        cm.registerCommand(new CommandBanksee());
        cm.registerCommand(new CommandArmorsee());
        cm.registerCommand(new CommandGemsee());
        cm.registerCommand(new CommandBinsee());
        cm.registerCommand(new CommandMuleSee());
        cm.registerCommand(new CommandRealmsee());
        cm.registerCommand(new CommandScrapTabSee());

        cm.registerCommand(new CommandLag());
        cm.registerCommand(new CommandSet());
        cm.registerCommand(new CommandEss());
        cm.registerCommand(new CommandFix());
        cm.registerCommand(new CommandCountdown());
        cm.registerCommand(new CommandCountdownStop());
        cm.registerCommand(new CommandVote());
        cm.registerCommand(new CommandSend());
        cm.registerCommand(new CommandInterface());
        cm.registerCommand(new CommandTell());
        cm.registerCommand(new CommandISay());
        cm.registerCommand(new CommandModeration());
        cm.registerCommand(new CommandStaffChat());
        cm.registerCommand(new CommandBroadcast());
        cm.registerCommand(new CommandGm());
        cm.registerCommand(new CommandHeadGm());
        cm.registerCommand(new CommandPlayerFix());
        cm.registerCommand(new CommandPlayerRetention());
        cm.registerCommand(new CommandSudo());
        cm.registerCommand(new CommandSudoChat());
        cm.registerCommand(new CommandLookup());
        cm.registerCommand(new CommandAnnounce());
//        cm.registerCommand(new CommandWatchList());

        cm.registerCommand(new CommandPAccept("paccept", "/<command> [args]", "Accept a party invitation."));
        cm.registerCommand(new CommandPLoot());
        cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party.", Collections.singletonList("pkick")));
        cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party.", Collections.singletonList("pquit")));
        cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat.", Collections.singletonList("p")));
        cm.registerCommand(new CommandPl("pinvite", "/<command> [args]", "Will invite a player to a party, creating one if it doesn't exist."));
        cm.registerCommand(new CommandPDecline("pdecline", "/<command> [args]", "Decline a party invitation."));

        cm.registerCommand(new CommandTestDupe("testdupe", "/<command> [args]", "Command test dupe."));
        cm.registerCommand(new CommandClearChat("clearchat", "/<command> [args]", "Command clear chat."));

        cm.registerCommand(new CommandRoll("roll", "/<command> [args]", "Rolls a random number between 1 and the supplied argument."));
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "This command will allow the user to change shards.", Collections.singletonList("connect")));

        cm.registerCommand(new CommandToggle("toggles", "/<command> [args]", "View and manage your profile toggles.", Collections.singletonList("toggle")));
        cm.registerCommand(new CommandToggleDebug("toggledebug", "/<command> [args]", "Toggles displaying combat debug messages.", Collections.singletonList("debug")));
        cm.registerCommand(new CommandToggleChaos("togglechaos", "/<command> [args]", "Toggles killing blows on lawful players (anti-chaotic)."));
        cm.registerCommand(new CommandToggleDamageIndicators("togglefloatdamage", "/<command> [args]", "Toggles Floating Damage Indicators."));
        cm.registerCommand(new CommandToggleGlobalChat("toggleglobalchat", "/<command> [args]", "Toggles talking only in global chat."));
        cm.registerCommand(new CommandTogglePvp("togglepvp", "/<command> [args]", "Toggles all outgoing PvP damage (anti-neutral)."));
        cm.registerCommand(new CommandToggleTells("toggletells", "/<command> [args]", "Toggles receiving NON-BUD /tell.", Collections.singletonList("dnd")));
        cm.registerCommand(new CommandToggleTrade("toggletrade", "/<command> [args]", "Toggles trading requests."));
        cm.registerCommand(new CommandToggleTradeChat("toggletradechat", "/<command> [args]", "Toggles receiving <T>rade chat."));
        cm.registerCommand(new CommandToggleDuel("toggleduel", "/<command> [args]", "Toggles dueling requests."));
        cm.registerCommand(new CommandToggleTips("toggletips", "/<command> [args]", "Toggles tip messages."));
        cm.registerCommand(new CommandToggleGlow());

        cm.registerCommand(new CommandCheck("check", "/<command> [args]", "Checks the identity of a Dungeon Realms signed item."));
        cm.registerCommand(new CommandStats("stat", "/<command> [args]", "Allows you to view and manage your stat points.", Collections.singletonList("stats")));
        cm.registerCommand(new CommandStop("shutdown", "/<command> [args]", "This will stop Dungeon Realms safely following safe shutdown procedures.", Collections.singletonList("drstop")));

        cm.registerCommand(new CommandWarp());
        cm.registerCommand(new DungeonSpawn("dspawn", "/<command> [args]", "Spawn dungeon monsters."));
        cm.registerCommand(new CommandMonSpawn("monspawn", "/<command> [args]", "Spawn monsters"));
        cm.registerCommand(new ReplaceNear("drreplacenear", "/<command> [args]", "Replaces nearby blocks"));
        cm.registerCommand(new BossTeleport("bosstp", "/<command> [args]", "Dungeon Boss Teleporation"));
        cm.registerCommand(new BossSpawn("bspawn", "/<command> [args]", "Spawn a dungeon boss."));
        cm.registerCommand(new CommandTips("tips", "/<command>", "Tips command"));
        cm.registerCommand(new DungeonJoin("djoin", "/<command>", "Dungeon Join command"));
        cm.registerCommand(new DRLightning("drlightning", "/<command>", "Spawns lightning at an area"));
        cm.registerCommand(new DebuffCrystal("debuffcrystal", "/<command>", "Spawns a debuff crystal"));
        cm.registerCommand(new CommandMessage("message", "/<command>", "Messages a player", Arrays.asList("msg", "tell", "t", "whisper", "w", "m")));
        cm.registerCommand(new CommandReply("reply", "/<command>", "Messages a player", Collections.singletonList("r")));
        cm.registerCommand(new CommandPlayed("played", "/<command>", "Checks your playtime"));
        cm.registerCommand(new CommandSessionID("sessions", "/<command> [args]", "Session ID fixer", Arrays.asList("session", "fixsession")));
        cm.registerCommand(new CommandSpawner());
        // Commands only registered for an instance server (including the always registered commands).
        if (isInstanceServer) {
            // cm.registerCommand(new CommandGuild("guild", "/<command> [args]", "Opens the guild menus!"));
        }
        // Commands only registered for live servers (including always registered).
        else {

            //GUILD STUFF
            cm.registerCommand(new CommandGInfo("ginfo", "/<command>", "Guild info command."));
            cm.registerCommand(new CommandG("g", "/<command> [msg]", "Guild chat command."));
            cm.registerCommand(new CommandGQuit("gquit", "/<command>", "Guild quit command.", Arrays.asList("gleave", "gdisband")));
            cm.registerCommand(new CommandGAccept("gaccept", "/<command>", "Guild accept invitation command."));
            cm.registerCommand(new CommandGKick("gkick", "/<command> [args]", "Guild kick command."));
            cm.registerCommand(new CommandGInvite("ginvite", "/<command> [args]", "Guild invitation command."));
            cm.registerCommand(new CommandGPromote("gpromote", "/<command> [args]", "Guild promote command."));
            cm.registerCommand(new CommandGDemote("gdemote", "/<command> [args]", "Guild demote command."));
            cm.registerCommand(new CommandGMotd("gmotd", "/<command> [args]", "Guild motd command."));
            cm.registerCommand(new CommandGDeny("gdecline", "/<command>", "Guild decline invitation command.", Collections.singletonList("gdeny")));

            cm.registerCommand(new CommandSpawn("spawn", "/<command> [args]", "This will teleport a Game Master to their spawn point."));
            cm.registerCommand(new CommandTeleport("teleport", "/<command> [args]", "This will allow a Gamer Master to teleport across the lands of Andalucia."));
            cm.registerCommand(new CommandAdd("ad", "/<command> [args]", "This will spawn a Dungeon Realms item.", Collections.singletonList("ad")));
            cm.registerCommand(new CommandList("list", "/<command> [args]", "Displays a list of online players."));
            cm.registerCommand(new CommandSetRank("setrank", "/<command> [args]", "Sets the rank of a player."));
            cm.registerCommand(new CommandArmorSee("armorsee", "/<command> [args]", "Shows the armor of a player or entity."));
            cm.registerCommand(new CommandWhois("whois", "/<command> [args]", "Get which shard a player is playing on if any."));
            cm.registerCommand(new CommandPacketLog("packetlog", "/<command> [args]", "Log all data a user sends"));
            cm.registerCommand(new CommandMail("mailbox", "/<command> [args]", "Manage your received mail and send your own mail."));
            cm.registerCommand(new CommandReboot("reboot", "/<command>", "Displays the time until the shard will next reboot."));
            cm.registerCommand(new CommandInvoke("invoke", "/<command> [args]", "The invoke command."));
            cm.registerCommand(new CommandHead("head", "/<command> [args]", "Spawn a player's Minecraft head."));
            cm.registerCommand(new CommandStore("drstore", "/<command> [args]", "This command will issue store items to users."));

            cm.registerCommand(new CommandGlobalChat("gl", "/<command> [args]", "Sends a message to global chat."));
            cm.registerCommand(new CommandLocalChat("l", "/<command> [args]", "Sendsa message to local chat."));

            cm.registerCommand(new CommandAsk("ask", "/<command> [args]", "Ask command", Collections.singletonList("help")));
            //cm.registerCommand(new CommandWelcome("welcome", "/<command> [args]", "Welcome command for ecash"));
            cm.registerCommand(new CommandAnswer("answer", "/<command> [args]", "Answer command"));
            //cm.registerCommand(new CommandStuck("stuck", "/<command> [args]", "Will help remove you if you're stuck in a block."));
            cm.registerCommand(new CommandSuicide("suicide", "/<command>", "Kills your player.", Collections.singletonList("drsuicide")));

            cm.registerCommand(new CommandRealm("realm", "/<command> [args]", "Realm command"));
            cm.registerCommand(new CommandResetRealm("resetrealm", "/<command>", "Realm reset command"));
            cm.registerCommand(new CommandRealmFix("realmfix", "/<command> [args]", "Realm fix command"));
            cm.registerCommand(new CommandRealmWipe("realmwipe", "/<command> [args]", "Realm wipe command"));

            cm.registerCommand(new CommandBan("ban", "/ban <player> <duration | eg. 15m / 5d> [reason ...]", "Ban command", "drban"));
            cm.registerCommand(new CommandUnban("unban", "/<command> [args]", "Unban command", "drunban", "drpardon"));
            cm.registerCommand(new CommandMute("mute", "/mute <player> <duration | eg. 15m / 5d> [reason ...]", "Mute command", "drmute"));
            cm.registerCommand(new CommandUnmute("unmute", "/<command> [args]", "Unmute command", "drunmute"));
            cm.registerCommand(new CommandJail("jail", "/<command> [args]", "Jail command"));
            cm.registerCommand(new CommandUnjail("unjail", "/<command> [args]", "Unjail command"));

            //cm.registerCommand(new CommandSkip("skip", "/<command> [args]", "Skips the tutorial island."));
            cm.registerCommand(new CommandPurchase("purchase", "/<command> [args]", "Will announce a purchase messages."));

            cm.registerCommand(new CommandMount("mount", "/<command> [args]", "Opens the player mounts menu.", Collections.singletonList("mounts")));
            cm.registerCommand(new CommandPet("pet", "/<command> [args]", "Opens the player pets menu.", Collections.singletonList("pets")));
            cm.registerCommand(new CommandTrail("trail", "/<command> [args]", "Opens the player trails menu.", Arrays.asList("trails", "effect", "effects")));
            cm.registerCommand(new CommandAchievements("achievements", "/<command> [args]", "Opens the player achievements menu.", Collections.singletonList("achievement")));
            cm.registerCommand(new CommandProfile("profile", "/<command> [args]", "Opens the player profile menu."));
            cm.registerCommand(new CommandEcash("ecash", "/<command> [args]", "Opens the E-Cash vendor menu.", Arrays.asList("shop", "buy")));
            cm.registerCommand(new CommandPatchNotes("patchnotes", "/<command>", "Shows patch for current build", Collections.singletonList("patch")));

            cm.registerCommand(new CommandTestRank("testrank", "/<command> [args]", "This is a test command."));

            cm.registerCommand(new StarterCommand("givestarter", "/<command> [args]", "Provides a player with the starter kit."));
            cm.registerCommand(new KickAllCommand("kickall", "/<command> [args]", "Kicks all players from the server."));

            //FRIENDS
            cm.registerCommand(new AddCommand("add", "/<command> [args]", "Send Friend request!", Collections.singletonList("friend")));
            cm.registerCommand(new RemoveCommand("unfriend", "/<command> [args]", "Remove friend from list!", Collections.singletonList("rem")));
            cm.registerCommand(new AcceptCommand("accept", "/<command> [args]", "Accept Friend request!", Collections.singletonList("draccept")));
            cm.registerCommand(new AcceptCommand("deny", "/<command> [args]", "Deny Friend request!", Collections.singletonList("drdeny")));

            cm.registerCommand(new FriendsCommand("friends", "/<command> [args]", "Open friends list!", Arrays.asList("buddy", "buddys")));
            cm.registerCommand(new CommandPlayed("played", "/<command>", "Checks your playtime"));
            cm.registerCommand(new CommandIgnore());
        }

        //Good command to have to set peoples gems all together?
        cm.registerCommand(new CommandSupport("support", "/<command> [args]", "The main command for accessing all support features and tools."));
        // Commands exclusive to support agents on their special server.
        if (isMasterShard || isSupportShard ) {
            cm.registerCommand(new CommandQuestEditor("quests", "/<command>", "Edit quests"));
        }

        try {
            FileUtils.deleteDirectory(new File("world" + File.separator + "playerdata"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().dispatchCommand(getServer().getConsoleSender(), "save-off");

        Bukkit.getServer().setWhitelist(false);

        // FIX PLAYERS //
        UpdateResult playerFixResult = DatabaseInstance.playerData.updateMany(Filters.eq("info.current", shard.getPseudoName()),
                new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false)));

        if (playerFixResult.wasAcknowledged())
            Constants.log.info("DungeonRealms - Set " + playerFixResult.getModifiedCount() + " players' " +
                    "statuses to offline from " +
                    "shard " + shard);
        else Constants.log.info("DungeonRealms - Operation failed: database error.");

        Utils.log.info("DungeonRealms - Startup Complete. Took " + ((System.currentTimeMillis() / 1000L) / SERVER_START_TIME) + "/s");

        try {
            Constants.log.info("DungeonRealms - Process ID = " + Utils.getPid());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            Constants.log.info("DungeonRealms - Now Accepting Players.");

            this.acceptPlayers = true;
            Bukkit.getServer().setWhitelist(false);
        }, 240L);


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameAPI.GAMEPLAYERS.values().forEach(gp -> gp.getPlayerStatistics().setTimePlayed(gp.getPlayerStatistics().getTimePlayed() + 1));
            }
        }, 0L, 1000);

        if (!isSupportShard) {
            // SEND SERVER INFO TO MASTER SERVER REPEATEDLY //
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ServerListPacket packet = new ServerListPacket();

                    final Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);

                    packet.target = shard;
                    packet.tokens = new PlayerToken[onlinePlayers.length];


                    for (int i = 0; i < onlinePlayers.length; i++) {
                        Player player = onlinePlayers[i];
                        packet.tokens[i] = new PlayerToken(player.getUniqueId().toString(), player.getName());
                    }

                    getClient().sendTCP(packet);
                }
            }, 0L, 3000);
        }

        // run backup every ten minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, GameAPI::backupDatabase, 0L, 12000L);
    }

    public void setRebootTime(long nextReboot) {
        rebootTime = nextReboot;
        if (rebooterID != 0)
            Bukkit.getScheduler().cancelTask(rebooterID);
        rebooterID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if (System.currentTimeMillis() >= (rebootTime - 300000L)) {
                scheduleRestartTask();
                Bukkit.getScheduler().cancelTask(rebooterID);
            }
        }, 0, 100);
    }

    public long getRebootTime() {
        return rebootTime;
    }

    private void scheduleRestartTask() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> Bukkit.getOnlinePlayers().forEach(player -> TitleAPI.sendTitle(player, 1, 60, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 5 MINUTES")));

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> Realms.getInstance().removeAllRealms(true));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setAcceptPlayers(false);
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(),
                        () -> Bukkit.getOnlinePlayers().forEach(player -> TitleAPI.sendTitle(player, 1, 60, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 1 MINUTE")));
            }
        }, 240000L);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), GameAPI::stopGame);
            }
        }, 300000L);
    }

    public void onDisable() {
    	if(!isAlmostRestarting() && !CrashDetector.crashDetected) {
    		GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Shard " + ChatColor.GOLD + "{SERVER}" + ChatColor.WHITE + " failed to load.");
    		Utils.log.info("DungeonRealms - Failed to load.");
    		//Shutdown?
    	} else {
    		cm.onDisable();
        	tcc.onDisable();
        	if (!mm.isShutdown())
        		mm.stopInvocation();
        	
        	DatabaseAPI.getInstance().stopInvocation();
        	
        	Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
    	}
    }

    public FTPClient getFTPClient() {
        FTPClient ftpClient = new FTPClient();

        try{
            Ini ini = new Ini();
            ini.load(new FileReader("credentials.ini"));
            ftpClient.connect(ini.get("FTP", "ftp_host", String.class));
            ftpClient.login(ini.get("FTP", "ftp_username", String.class), ini.get("FTP", "ftp_password", String.class));
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        }catch(Exception e){
            Bukkit.getLogger().info("Failed to load FTP credentials from credentials.ini");
            e.printStackTrace();
        }
        return ftpClient;
    }

}

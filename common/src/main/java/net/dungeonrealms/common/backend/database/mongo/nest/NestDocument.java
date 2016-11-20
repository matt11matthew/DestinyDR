package net.dungeonrealms.common.backend.database.mongo.nest;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.backend.player.DataPlayer;
import net.dungeonrealms.common.backend.player.data.enumeration.EnumKeyShardTier;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NestDocument
{
    // TODO realm stuff & more collectible data

    @Getter
    private EnumNestType nestType;

    public NestDocument(EnumNestType nestType)
    {
        this.nestType = nestType;
    }

    public Document generate(UUID uuid)
    {
        Document document = null;
        switch (this.nestType)
        {
            case PLAYER:
                document = new Document("genericData",
                        new Document("uniqueId", uuid.toString()).append("hasDeadLogger", false)
                                .append("hearthstoneLocation", "CYRENNICA").append("currentAlignment", "LAWFUL").append("currentLocation", "")
                                .append("level", 1).append("experience", 0.0).append("gems", 0).append("currentFoodLevel", 20).append("ecash", 0).append("health", 50))
                        // Guild data
                        .append("guildData", new Document("guild", "").append("guildInvitations", Lists.newArrayList()))
                        // Friend data
                        .append("friendData", new Document("friends", Lists.newArrayList()).append("friendRequests", Lists.newArrayList()))
                        // Inventory data
                        .append("inventoryData", new Document("inventory", "").append("storageLevel", 1).append("collectionBin", "").append("armorContents", Lists.newArrayList())
                                .append("mule", "empty").append("muleLevel", 0).append("storage", "").append("hasOpenShop", false).append("shopLevel", 0))
                        // Collection data & Keyshard data
                        .append("collectionData", new Document("achievements", Lists.newArrayList()))
                        .append("keyShardData", new Document("tier1", 0).append("tier2", 0).append("tier3", 0).append("tier4", 0).append("tier5", 0))
                        // Rank data
                        .append("rankData", new Document("currentRank", "DEFAULT"))
                        // Settings data
                        .append("settingsData", new Document("enabledGlobalChat", true).append("enabledTradeChat", true).append("tradingEnabled", false).append("receiveMessages", true)
                                .append("pvp", false).append("enabledChaoticPrevention", true).append("duels", true).append("tips", true));
                break;
            default:
                break;
        }
        return document;
    }

    public Document generateExistent(DataPlayer dataPlayer)
    {
        Document document = new Document("genericData",
                new Document("uniqueId", dataPlayer.getUniqueId().toString()).append("hasDeadLogger", dataPlayer.getGameData().isHasDeadLogger())
                        .append("hearthstoneLocation", dataPlayer.getGameData().getHearthstoneLocationBlob())
                        .append("currentAlignment", dataPlayer.getGameData().getAlignmentBlob()).
                        append("currentLocation", dataPlayer.getGameData().getCurrentLocationBlob())
                        .append("level", dataPlayer.getGameData().getLevel()).append("experience", dataPlayer.getGameData().getExp())
                        .append("gems", dataPlayer.getGameData().getGems()).append("currentFoodLevel", dataPlayer.getGameData().getCurrentFood())
                        .append("ecash", dataPlayer.getGameData().getEcash()).append("health", dataPlayer.getGameData().getHealth()))
                // Guild data
                .append("guildData", new Document("guild", dataPlayer.getGuildData().getGuild())
                        .append("guildInvitations", dataPlayer.getGuildData().getGuildInvitations()))
                // Friend data
                .append("friendData", new Document("friends", dataPlayer.getFriendData().getFriends())
                        .append("friendRequests", dataPlayer.getFriendData().getFriendRequesters()))
                // Inventory data
                .append("inventoryData", new Document("inventory", dataPlayer.getInventoryData().getGameInventoryBlob())
                        .append("storageLevel", dataPlayer.getInventoryData().getStorageLevel())
                        .append("collectionBin", dataPlayer.getInventoryData().getCollectionBinBlob())
                        .append("armorContents", dataPlayer.getInventoryData().getArmorContents())
                        .append("mule", dataPlayer.getInventoryData().getMuleBlob()).append("muleLevel", dataPlayer.getInventoryData().getMuleLevel())
                        .append("storage", dataPlayer.getInventoryData().getStorageBlob()).append("hasOpenShop", dataPlayer.getInventoryData().isHasShop())
                        .append("shopLevel", dataPlayer.getInventoryData().getShopLevel()))
                // Collection data & Keyshard data
                .append("collectionData", new Document("achievements", dataPlayer.getCollectionData().getAchievements()))
                .append("keyShardData", new Document("tier1", dataPlayer.getCollectionData().getKeyShards(EnumKeyShardTier.T1))
                        .append("tier2", dataPlayer.getCollectionData().getKeyShards(EnumKeyShardTier.T2))
                        .append("tier3", dataPlayer.getCollectionData().getKeyShards(EnumKeyShardTier.T3))
                        .append("tier4", dataPlayer.getCollectionData().getKeyShards(EnumKeyShardTier.T4))
                        .append("tier5", dataPlayer.getCollectionData().getKeyShards(EnumKeyShardTier.T5)))
                // Rank data
                .append("rankData", new Document("currentRank", dataPlayer.getRankData().getRank().name()))
                // Settings data
                .append("settingsData", new Document("enabledGlobalChat", dataPlayer.getSettingsData().isGlobalChatEnabled())
                        .append("enabledTradeChat", dataPlayer.getSettingsData().isTradeChatEnabled())
                        .append("tradingEnabled", dataPlayer.getSettingsData().isTradingEnabled())
                        .append("receiveMessages", dataPlayer.getSettingsData().isAllowReceiveMessages())
                        .append("pvp", dataPlayer.getSettingsData().isAllowPvP())
                        .append("enabledChaoticPrevention", dataPlayer.getSettingsData().isEnabledChaoticPrevention())
                        .append("duels", dataPlayer.getSettingsData().isAllowDuels()).append("tips", dataPlayer.getSettingsData().isAllowTips()));
        return document;
    }
}

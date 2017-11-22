package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemOrcTooth extends ItemGeneric {
    protected String name = CC.WhiteB + "Orc Tooth";
    protected String lore = CC.Gray + "A string durable tooth from a dead Orc.";

    public ItemOrcTooth() {
        super(new ItemStack(Material.GHAST_TEAR));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "orc tooth");
    }

    @Override
    protected ItemStack getStack() { return this.item;}
}

package com.runicrealms.runicitems.weaponskin.ui;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.weaponskin.WeaponSkin;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WeaponAppearancesUI implements InventoryHolder {
    private static List<Pair<CharacterClass, List<WeaponSkin>>> pages = null;

    private final Inventory inventory;
    private final Player player;
    private final int pageNumber;

    public WeaponAppearancesUI(Player player, int pageNumber) {
        this.player = player;
        this.pageNumber = pageNumber;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&eWeapon Appearances"));
        generateMenu();
    }


    public WeaponAppearancesUI(Player player) {
        this(player, getDefaultPageNumber(player));
    }

    private static ItemStack arrow(Material material) {
        ItemStack arrow = new ItemStack(material);
        ItemMeta meta = arrow.getItemMeta();
        if (meta == null) return arrow;
        meta.setDisplayName(ChatColor.GRAY + "");
        arrow.setItemMeta(meta);
        return arrow;
    }

    private static String getClassName(WeaponSkin skin) {
        return switch (skin.material()) {
            case BOW -> "Archer";
            case WOODEN_SHOVEL -> "Cleric";
            case WOODEN_HOE -> "Mage";
            case WOODEN_SWORD -> "Rogue";
            case WOODEN_AXE -> "Warrior";
            default -> "";
        };
    }

    private static int getDefaultPageNumber(Player player) {
        CharacterClass characterClass;
        RunicItemTemplate template = RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand());
        if (template instanceof RunicItemArtifactTemplate) {
            characterClass = ((RunicItemArtifactTemplate) template).getRunicClass().toCharacterClass();
        } else if (template instanceof RunicItemWeaponTemplate) {
            characterClass = ((RunicItemWeaponTemplate) template).getRunicClass().toCharacterClass();
        } else {
            throw new IllegalStateException("Could not find character class for item player is holding");
        }
        int index = 0;
        for (Pair<CharacterClass, List<WeaponSkin>> entry : getPages()) {
            if (entry.first == characterClass) return index;
            index++;
        }
        throw new IllegalStateException("Could not find character class for player");
    }

    private static List<Pair<CharacterClass, List<WeaponSkin>>> getPages() {
        if (pages == null) {
            pages = new ArrayList<>();
            for (CharacterClass characterClass : CharacterClass.values()) {
                if (characterClass == CharacterClass.ANY) continue;
                pages.add(new Pair<>(characterClass, RunicItems.getWeaponSkinAPI().getAllSkins().stream()
                        .filter((skin) -> skin.classType() == characterClass).toList()));
            }
        }
        return pages;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    private ItemStack generateWeaponSkinIcon(WeaponSkin skin) {
        ItemStack item = new ItemStack(skin.material());
        Damageable damageable = (Damageable) item.getItemMeta();
        damageable.setDamage(skin.damage());
        item.setItemMeta((ItemMeta) damageable);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setDisplayName(ColorUtil.format("&e" + skin.name()));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.format("&7" + skin.classType().getName()));
        lore.add("");
        if (!RunicItems.getWeaponSkinAPI().canActivateSkin(player, skin)) {
            lore.add(ColorUtil.format("&l&4You do not have permission to use this skin!"));
            lore.add(ColorUtil.format("&cPurchase a rank from our store for access to certain skins."));
        } else {
            lore.add(ColorUtil.format("&2Left click to enable skin"));
            lore.add(ColorUtil.format("&cRight click to disable skin"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("weapon-skin-menu", skin.id());
        return nbtItem.getItem();
    }

    private void generateMenu() {
        ItemStack topElement = new ItemStack(player.getInventory().getItemInMainHand().getType());
        if (player.getInventory().getItemInMainHand().getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) topElement.getItemMeta();
            damageable.setDamage(((Damageable) player.getInventory().getItemInMainHand().getItemMeta()).getDamage());
            topElement.setItemMeta((ItemMeta) damageable);
        }
        ItemMeta meta = topElement.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&eWeapon Appearances"));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.format("&7" + getPages().get(pageNumber).first.getName()));
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&7Weapon skins only modify the appearance of your weapons and artifacts. Donor ranks grant you access to more weapon skins!"));
        meta.setLore(lore);
        topElement.setItemMeta(meta);

        this.inventory.clear();
        for (int i = 0; i < 9; i++) {
            if (i != 4) this.inventory.setItem(i, GUIUtil.BORDER_ITEM);
        }
        this.inventory.setItem(4, topElement);
        if (pageNumber > 0) {
            this.inventory.setItem(0, arrow(Material.GRAY_STAINED_GLASS_PANE));
        }
        if (pageNumber < 4) {
            this.inventory.setItem(8, arrow(Material.BROWN_STAINED_GLASS_PANE));
        }
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            int index = 9;
            for (WeaponSkin skin : getPages().get(pageNumber).second) {
                this.inventory.setItem(index++, generateWeaponSkinIcon(skin));
            }
        });
    }

}

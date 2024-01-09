package xyz.steffq.itemrent.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SerializeUtils {

    public static String serializeItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }

        StringBuilder serializedItem = new StringBuilder();


        serializedItem.append(itemStack.getType().name()).append(":");
        serializedItem.append(itemStack.getAmount()).append(":");
        serializedItem.append(meta.getDisplayName()).append(":");


        Map<Enchantment, Integer> enchantments = meta.getEnchants();
        if (!enchantments.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                serializedItem.append(entry.getKey().getKey().getKey()).append("#").append(entry.getValue()).append("|");
            }
            serializedItem.deleteCharAt(serializedItem.length() - 1);
        }


        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            for (String loreLine : lore) {
                serializedItem.append(ChatColor.translateAlternateColorCodes('&', loreLine)).append("|");
            }
            serializedItem.deleteCharAt(serializedItem.length() - 1);
        } else {

            serializedItem.append("|");
        }

        return serializedItem.toString();
    }


    public static ItemStack deserializeItemStack(String serializedItem) {
        if (serializedItem == null) {
            return null;
        }

        String[] parts = serializedItem.split(":");
        if (parts.length < 3) {
            return null;
        }

        ItemStack itemStack = new ItemStack(Material.matchMaterial(parts[0]));
        itemStack.setAmount(Integer.parseInt(parts[1]));

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(parts[2]);

            if (parts.length > 3) {
                String[] enchantmentsPart = parts[3].split("\\|");
                for (String enchantmentInfo : enchantmentsPart) {
                    String[] enchantmentDetails = enchantmentInfo.split("#");
                    if (enchantmentDetails.length == 2) {
                        String enchantmentKey = enchantmentDetails[0];
                        String enchantmentValue = enchantmentDetails[1].replaceAll("[^0-9]", "");

                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey));
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, Integer.parseInt(enchantmentValue), true);
                        }
                    }
                }
            }

            if (parts.length > 4) {
                String lorePart = parts[4];
                String[] loreLines = lorePart.split("\\|");


                meta.setLore(null);


                List<String> loreList = new ArrayList<>();


                for (String loreLine : loreLines) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                }


                meta.setLore(loreList);
            } else {
                meta.setLore(Collections.emptyList());
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }
}

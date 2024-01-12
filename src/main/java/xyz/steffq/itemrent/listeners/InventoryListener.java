package xyz.steffq.itemrent.listeners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.steffq.itemrent.ItemRent;
import xyz.steffq.itemrent.database.ItemRentDatabase;
import xyz.steffq.itemrent.menus.RentMenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryListener implements Listener {

    private final ItemRent plugin;
    private final Economy economy;

    public InventoryListener(ItemRent plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory == null || clickedItem == null) {
            return;
        }

        if (clickedInventory.equals(RentMenu.getInv())) {
            int clickedSlot = event.getSlot();
            double clickedPrice = getPriceFromItem(clickedItem);

            if (itemExistsInDatabase(clickedSlot, clickedPrice, player.getName(), clickedItem.getType().toString())) {
                if (economy != null && economy.has(player, clickedPrice)) {
                    economy.withdrawPlayer(player, clickedPrice);
                    player.getInventory().addItem(clickedItem);
                    player.sendMessage(ChatColor.GREEN + "You purchased the item for " + clickedPrice + " " + economy.currencyNamePlural() + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have enough money to purchase this item!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "This item doesn't exist in the database!");
            }
        }

        event.setCancelled(true);
    }

    private double getPriceFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                // Using a regular expression to match the numeric part of the lore
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+\\.\\d+").matcher(lore);
                if (matcher.find()) {
                    try {
                        return Double.parseDouble(matcher.group());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Default value if the price is not found or cannot be parsed
        return 0.0;
    }

    private boolean itemExistsInDatabase(int slot, double price, String owner, String itemName) {
        try (Connection connection = ItemRentDatabase.getConnection()) {
            System.out.println("Is connection closed: " + connection.isClosed());
            String sql = "SELECT * FROM rented_items WHERE slot = ? AND price = ? AND owner = ? AND item_name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, slot);
                statement.setDouble(2, price);
                statement.setString(3, owner);
                statement.setString(4, itemName);

                System.out.println("Executing SQL: " + sql);
                System.out.println("Parameters: slot=" + slot + ", price=" + price + ", owner=" + owner + ", itemName=" + itemName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    boolean result = resultSet.next();
                    System.out.println("Result: " + result);
                    return result;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}

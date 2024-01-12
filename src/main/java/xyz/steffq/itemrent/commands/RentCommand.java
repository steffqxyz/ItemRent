package xyz.steffq.itemrent.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.steffq.itemrent.ItemRent;
import xyz.steffq.itemrent.database.ItemRentDatabase;
import xyz.steffq.itemrent.menus.RentMenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RentCommand implements CommandExecutor, TabCompleter {

    private List<String> lores;

    public void reload() {
        lores = ItemRent.getConfigYml().options().getStringList("lore");

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Inventory inv = RentMenu.getInv();
            player.openInventory(inv);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCleared the menu!"));
            RentMenu.getInv().clear();
            return true;
        }


        if (command.getName().equalsIgnoreCase("itemrent")) {



            if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
                try {
                    int slot = Integer.parseInt(args[1]);
                    double price = Double.parseDouble(args[2]);

                    ItemStack item = createRentalItem(player, price);


                    addItemToMenu(player, slot, item, price);

                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    player.sendMessage("Item added to the menu!");
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid arguments. Please use /itemrent add <slot> <price>");
                }
                return true;
            }
        }
        return false;
    }

    private ItemStack createRentalItem(Player player, double price) {

        reload();

        ItemStack item = player.getInventory().getItemInMainHand().clone();
        ItemMeta meta = item.getItemMeta();

        List<String> updatedLores = new ArrayList<>();

        for (String lore : lores) {
            updatedLores.add(ChatColor.translateAlternateColorCodes('&', lore.
                    replace("{OWNER}", player.getName()).
                    replace("{PRICE}", String.valueOf(price))));
        }

        meta.setLore(updatedLores);

        item.setItemMeta(meta);
        return item;
    }

    public void addItemToMenu(Player player, int slot, ItemStack item, double price) {
        Inventory inv = RentMenu.getInv();

        if (slot >= 0 && slot < inv.getSize()) {
            inv.setItem(slot, item);

            try (Connection connection = ItemRentDatabase.getConnection()) {

                String insertItemSQL = "INSERT INTO rented_items (slot, price, owner, item_name) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertItemStatement = connection.prepareStatement(insertItemSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertItemStatement.setInt(1, slot);
                    insertItemStatement.setDouble(2, price);
                    insertItemStatement.setString(3, player.getName());
                    insertItemStatement.setString(4, item.getType().toString());
                    insertItemStatement.executeUpdate();

                    try (ResultSet generatedKeys = insertItemStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int itemId = generatedKeys.getInt(1);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "Item added to the menu with ID: " + itemId));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cInvalid slot. Please provide a slot between 0 and " + (inv.getSize() - 1) + "."));
        }

        player.closeInventory();
        player.openInventory(inv);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("itemrent")) {
            if (args.length == 1) {
                // Suggestions for the first argument (sub-command)
                completions.add("add");
                completions.add("clear");
                // Add more sub-commands as needed
            } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
                // Suggestions for the second argument (slot)
                // You may want to customize this based on your requirements
                for (int i = 0; i < 9; i++) {
                    completions.add(String.valueOf(i));
                }
            }
            // Add more tab completions based on your specific needs
        }

        return completions;
    }
}

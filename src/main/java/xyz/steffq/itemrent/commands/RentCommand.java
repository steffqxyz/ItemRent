package xyz.steffq.itemrent.menus;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.steffq.itemrent.ItemRent;

public class RentMenu implements CommandExecutor {

    private final ItemRent plugin;
    private int slots;
    private String menuName;
    private Inventory openedInventory;

    public RentMenu(ItemRent plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        menuName = plugin.getConfigYml().options().getString("menu-name");
        slots = plugin.getConfigYml().options().getInt("menu-size");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // /itemrent add <slot> <price>

        if (args.length == 0) {
            openedInventory = inventory();  // Store the opened inventory
            player.openInventory(openedInventory);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            // Check if the correct number of arguments is provided
            if (args.length == 3) {
                try {
                    int slot = Integer.parseInt(args[1]);
                    double price = Double.parseDouble(args[2]);

                    if (price <= 0) {
                        player.sendMessage("Price must be greater than zero.");
                        return true;
                    }

                    addItemToMenu(player, slot, price);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid arguments. Please provide a valid slot and price for the item.");
                }
            } else {
                player.sendMessage("Usage: /itemrent add <slot> <price>");
            }
            return true;
        }

        return false;
    }

    private void addItemToMenu(Player player, int slot, double price) {
        // Check if Vault is enabled
        if (plugin.getEconomy() == null) {
            player.sendMessage("Vault not found. Economy features are disabled.");
            return;
        }

        // Use the previously opened inventory
        if (openedInventory != null) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (!itemInHand.getType().isAir()) {
                ItemStack newItem = new ItemStack(itemInHand);

                // You can access and copy the enchantments and item meta
                newItem.addUnsafeEnchantments(itemInHand.getEnchantments());
                newItem.setItemMeta(itemInHand.getItemMeta());

                // For simplicity, assume slots are identified by item names
                openedInventory.setItem(slot, newItem);

                player.sendMessage("Item in hand added to the menu with enchantments and meta!");
            } else {
                player.sendMessage("You must be holding an item in your hand to use this command.");
            }
        }
    }
}

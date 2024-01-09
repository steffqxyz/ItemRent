package xyz.steffq.itemrent.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.steffq.itemrent.ItemRent;
import xyz.steffq.itemrent.menus.RentMenu;

import java.util.ArrayList;
import java.util.List;

public class RentCommand implements CommandExecutor {

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


        if (command.getName().equalsIgnoreCase("itemrent")) {


            if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
                try {
                    int slot = Integer.parseInt(args[1]);
                    double price = Double.parseDouble(args[2]);

                    ItemStack item = createRentalItem(player, price);


                    addItemToMenu(player, slot, item);

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

    public void addItemToMenu (Player player, int slot, ItemStack item) {

        Inventory inv = RentMenu.getInv();

        if (slot >= 0 && slot < inv.getSize()) {
            inv.setItem(slot, item);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid slot. Please provide a slot between 0 and " + (inv.getSize() - 1) + "."));
        }

        player.closeInventory();
        player.openInventory(inv);

    }
}

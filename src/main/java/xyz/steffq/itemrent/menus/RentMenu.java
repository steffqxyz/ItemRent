package xyz.steffq.itemrent.menus;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import xyz.steffq.itemrent.ItemRent;

public class RentMenu {

    private static String menuName;
    private static int slots;
    private static Inventory inventory;

    static {
        menuName = ItemRent.getConfigYml().options().getString("menu-name");
        slots = ItemRent.getConfigYml().options().getInt("menu-size");

        if (slots != 9 && slots != 18 && slots != 27 && slots != 36 && slots != 45 && slots != 54) {
            Bukkit.getLogger().warning("Invalid menu size. Defaulting to 9.");
            slots = 9;
        }

        if (menuName == null) {
            Bukkit.getLogger().warning("Invalid menu name. Defaulting to 'Rent Menu'.");
            menuName = ChatColor.translateAlternateColorCodes('&', "&cRent Menu");
        }



        inventory = Bukkit.createInventory(null, slots, ChatColor.translateAlternateColorCodes('&', menuName));
    }
    public static Inventory getInv() {
        return inventory;
    }

}

package xyz.steffq.itemrent;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.steffq.itemrent.database.ItemRentDatabase;
import xyz.steffq.itemrent.files.Config;
import xyz.steffq.itemrent.commands.RentCommand;
import xyz.steffq.itemrent.listeners.InventoryListener;

import java.sql.SQLException;

public final class ItemRent extends JavaPlugin {

    private static Config configYml;

    private Economy economy;
    private ItemRentDatabase database;


    @Override
    public void onEnable() {
        // Plugin startup logic

        database = new ItemRentDatabase(getDataFolder().getAbsolutePath() + "/database.db");

        this.configYml = new Config(this, "config");


        if (!setupEconomy()) {
            getLogger().warning("Vault not found. Economy features are disabled.");
        } else {
            getLogger().info("Vault found. Economy features are enabled.");
        }


        getCommand("itemrent").setExecutor(new RentCommand());
        getServer().getPluginManager().registerEvents(new InventoryListener(this, economy), this);

    }

    @Override
    public void onDisable() {

        // Disconnect from the database
        try {
            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfigYml() {
        return configYml;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }



}

package xyz.steffq.itemrent;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.steffq.itemrent.database.ItemRentDatabase;
import xyz.steffq.itemrent.files.Config;
import xyz.steffq.itemrent.commands.RentCommand;
import xyz.steffq.itemrent.menus.RentMenu;
import xyz.steffq.itemrent.utils.SerializeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ItemRent extends JavaPlugin {

    private static Config configYml;

    private Economy economy;
    private ItemRentDatabase database;


    @Override
    public void onEnable() {
        // Plugin startup logic
        this.configYml = new Config(this, "config");

        database = new ItemRentDatabase(getDataFolder().getAbsolutePath() + "/database.db");

        loadItems();

        if (!setupEconomy()) {
            getLogger().warning("Vault not found. Economy features are disabled.");
        } else {
            getLogger().info("Vault found. Economy features are enabled.");
        }

        getCommand("itemrent").setExecutor(new RentCommand());
        getServer().getPluginManager().registerEvents(new RentMenu(), this);

    }

    @Override
    public void onDisable() {
        // Save items to the database
        saveItems();

        // Disconnect from the database
        try {
            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveItems() {
        Connection connection = database.getConnection();
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO items (slot, item_data) VALUES (?, ?)")) {

                for (int slot = 0; slot < RentMenu.getInv().getSize(); slot++) {

                    String itemData = SerializeUtils.serializeItemStack(RentMenu.getInv().getItem(slot));

                    statement.setInt(1, slot);
                    statement.setString(2, itemData != null ? itemData : "");
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadItems() {
        Connection connection = database.getConnection();
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM items");
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int slot = resultSet.getInt("slot");
                    String itemData = resultSet.getString("item_data");


                    RentMenu.getInv().setItem(slot, SerializeUtils.deserializeItemStack(itemData));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

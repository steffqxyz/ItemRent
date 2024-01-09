package xyz.steffq.itemrent.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.steffq.itemrent.ItemRent;

import java.io.File;
import java.io.IOException;

public class Config {

    private final ItemRent plugin;

    private File configFile;
    private FileConfiguration fileConfiguration;
    private final String fileName;
    private final String id;

    public Config(ItemRent plugin, String id) {
        this.id = id;
        fileName = id + ".yml";
        this.plugin = plugin;

        fileConfiguration = YamlConfiguration.loadConfiguration(file());
        save();
        reload();
    }

    private File file() {
        return new File(plugin.getDataFolder(), fileName);
    }

    public FileConfiguration options() {
        return fileConfiguration;
    }

    public boolean exists() {
        return file().exists();
    }

    public void saveFile() {
        if (!((fileConfiguration == null) || (configFile == null))) {
            try { options().save(configFile); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void save() {
        if (configFile == null) configFile = file();
        if (configFile.exists()) return;
        plugin.saveResource(fileName, false);
    }

    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file());
    }

}

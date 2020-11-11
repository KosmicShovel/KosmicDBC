package me.KosmicDev.KosmicDBC;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Yamls {
    public static Yamls PlayerData;
    public FileConfiguration playerscfg;
    public File playersfile;
    private MainClass plugin;

    public Yamls() {
        this.plugin = (MainClass) MainClass.getPlugin((Class) MainClass.class);
    }

    public void setup() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }
        this.playersfile = new File(this.plugin.getDataFolder(), "PlayerData.yml");
        if (!this.playersfile.exists()) {
            try {
                this.playersfile.createNewFile();
            } catch (IOException ex) {
            }
        }
        this.playerscfg = (FileConfiguration) YamlConfiguration.loadConfiguration(this.playersfile);
    }

    public FileConfiguration getPlayerData() {
        return this.playerscfg;
    }

    public void savePlayerData() {
        try {
            this.playerscfg.save(this.playersfile);
        } catch (IOException ex) {
        }
    }

    public void reloadPlayerData() {
        this.playerscfg = (FileConfiguration) YamlConfiguration.loadConfiguration(this.playersfile);
    }
}

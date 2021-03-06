package com.platymuus.bukkit.nether;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.WorldCreator;

/**
 * Main class for Nether 2.0
 */
public class NetherPlugin extends JavaPlugin {

    private final NetherPlayerListener playerListener = new NetherPlayerListener(this);

    public static final int MODE_CLASSIC = 0;

    public static final int MODE_AGENT = 1;

    public static final int MODE_ADJUST = 2;

    public void onEnable() {
        // Write a default config if we need to
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                getDataFolder().mkdirs();

                InputStream in = this.getClassLoader().getResourceAsStream("/com/platymuus/bukkit/nether/defaultConfig.yml");
                FileOutputStream out = new FileOutputStream(configFile);
                byte[] buffer = new byte[1024];

                while (true) {
                    int amt = in.read(buffer);
                    if (amt <= 0) {
                        break;
                    }
                    out.write(buffer, 0, amt);
                }
                out.close();
                in.close();

            }
            catch (IOException ex) {
                System.out.println("[Nether] Unable to write default config file: " + ex);
            }
        }

        // Verify the mode is valid for the nether setting
        if (getServer().getAllowNether()) {
            if (getMode() == MODE_CLASSIC) {
                getConfig().set("mode", MODE_AGENT);
                System.out.println("[Nether] Allow-nether is on, using Agent mode instead of Classic");
            }
        } else {
            if (getMode() != MODE_CLASSIC) {
                int prevMode = getMode();
                getConfig().set("mode", MODE_CLASSIC);
                System.out.println("[Nether] Allow-nether is off, using Classic mode instead of " + (prevMode == MODE_ADJUST ? "Adjust" : "Agent"));
            }
        }

        getServer().getPluginManager().registerEvents(playerListener, this);

        if (getMode() == MODE_CLASSIC) {
            WorldCreator netherWorldCreator = new WorldCreator(getConfig().getString("worldName", "nether"));
            netherWorldCreator.environment(Environment.NETHER);
            getServer().createWorld(netherWorldCreator);
        }

        // Good morning
        System.out.println(this + " enabled");
    }

    public void onDisable() {
        // 'Nught
        System.out.println(this + " disabled");
    }

    // Helpers
    public World getNether() {
        return getServer().getWorld(getConfig().getString("worldName", "nether"));
    }

    public World getNormal() {
        for (World world : getServer().getWorlds()) {
            if (world.getEnvironment() != Environment.NETHER) {
                return world;
            }
        }
        return null;
    }

    public void logMessage(String message) {
        if (getConfig().getBoolean("log", false)) {
            System.out.println("[Nether] " + message);
        }
    }

    public TravelAgent adjustTravelAgent(TravelAgent agent, Player player) {
        if (getMode() == MODE_AGENT) {
            agent = new NetherTravelAgent(this, player.getName(), player.getWorld().getEnvironment());
        } else {
            boolean nether = player.getWorld().getEnvironment() == Environment.NETHER;
            logMessage(player.getName() + " is portalling to " + (nether ? "normal world" : "Nether"));
        }
        agent.setSearchRadius(getSearchRadius()).setCreationRadius(getCreationRadius()).setCanCreatePortal(getCanCreate());
        return agent;
    }

    // Config getting stuff
    public int getMode() {
        return getConfig().getInt("mode", MODE_AGENT);
    }

    public int getScale() {
        return getConfig().getInt("scale", 8);
    }

    public boolean getRespawn() {
        return getConfig().getBoolean("respawn", true);
    }

    public int getSearchRadius() {
        return getConfig().getInt("options.searchRadius", 24);
    }

    public int getCreationRadius() {
        return getConfig().getInt("options.creationRadius", 12);
    }

    public boolean getCanCreate() {
        return getConfig().getBoolean("options.canCreate", true);
    }

}

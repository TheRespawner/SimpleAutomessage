package com.carlgo11.simpleautomessage;

import com.carlgo11.simpleautomessage.commands.SimpleautomessageCommand;
import com.carlgo11.simpleautomessage.updater.Updater;
import com.carlgo11.simpleautomessage.language.*;
import com.carlgo11.simpleautomessage.metrics.Metrics;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public int tick = 1; // msg+<tick> int
    public int time = 0; // the delay
    public final static Logger logger = Logger.getLogger("Minecraft");
    public String debugmsg = null;
    public static YamlConfiguration LANG;
    public static File LANG_FILE;

    public void onEnable() {
        this.reloadConfig();
        getServer().getPluginManager().registerEvents(new Time(this), this);
        checkVersion();
        checkConfig();
        checkMetrics();
        getServer().getPluginManager().registerEvents(new loadLang(this), this);
        getServer().getPluginManager().registerEvents(new Broadcast(this), this);
        commands();
        this.getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{getDescription().getName(), getDescription().getVersion(), Lang.ENABLED});
    }

    public void onDisable() {
        this.getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{getDescription().getName(), getDescription().getVersion(), Lang.DISABLED});
    }

    public void commands() {
        getCommand("simpleautomessage").setExecutor(new SimpleautomessageCommand(this));
    }

    public void checkVersion() {
        if (getDescription().getVersion().startsWith("dev-")) {
            this.getLogger().warning("You are using a development build! Keep in mind development builds might contain bugs!");
            this.getLogger().warning("If you want a fully working version please use a recommended build!");
        }

        if (getConfig().getBoolean("auto-update") == true) {
            debugmsg = "Calling Updater.java";
            this.onDebug();
            Updater updater = new Updater(this, "simpleautomessage/", this.getFile(), Updater.UpdateType.DEFAULT, true);
        } else {
            debugmsg = "auto-update: is set to false!";
        }
    }

    public void checkMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            graphs(metrics);
            metrics.start();
        } catch (IOException e) {
            System.out.println("[" + getDescription().getName() + "] " + Lang.STATS_ERROR);
        }
    }

    public void checkConfig() {
        File config = new File(this.getDataFolder(), "config.yml");
        if (!config.exists()) {
            this.saveDefaultConfig();
            System.out.println("[" + getDescription().getName() + "] " + "No config.yml detected, config.yml created.");
        }
        if (!getConfig().getString("version").equals("1.0.5")) {
            config.renameTo(new File(this.getDataFolder(), "config.version-" + getConfig().getString("version") + ".yml"));
            this.saveDefaultConfig();
        }
    }

    public YamlConfiguration getLang() {
        return LANG;
    }

    public File getLangFile() {
        return LANG_FILE;
    }

    public void onDebug() { // Debug message method
        if (getConfig().getBoolean("debug") == true) {
            Main.logger.log(Level.INFO, "[SimpleAutoMessage] {0}", debugmsg);
        }
    }

    public void graphs(Metrics metrics) { // Custom Graphs. Sends data to mcstats.org
        try {
            //Graph1
            Metrics.Graph graph1 = metrics.createGraph("Messages"); //Sends data about how many msg strings the user has.
            int o = 0;
            for (int i = 1; getConfig().contains("msg" + i); i++) {
                o = i;
            }
            graph1.addPlotter(new SimplePlotter(o + ""));

            //graph2
            Metrics.Graph graph2 = metrics.createGraph("auto-update"); //Sends auto-update data. if auto-update: is true it returns 'enabled'.
            if (getConfig().getBoolean("auto-update") == true) {
                graph2.addPlotter(new SimplePlotter("enabled"));
            } else {
                graph2.addPlotter(new SimplePlotter("disabled"));
            }

            //Graph3
            Metrics.Graph graph3 = metrics.createGraph("language");
            if (getConfig().getString("language").equalsIgnoreCase("EN") || getConfig().getString("language").isEmpty()) {
                graph3.addPlotter(new SimplePlotter("English"));
            }
            if (getConfig().getString("language").equalsIgnoreCase("FR")) {
                graph3.addPlotter(new SimplePlotter("French"));
            }
            if (getConfig().getString("language").equalsIgnoreCase("NL")) {
                graph3.addPlotter(new SimplePlotter("Dutch"));
            }
            if (getConfig().getString("language").equalsIgnoreCase("SE")) {
                graph3.addPlotter(new SimplePlotter("Swedish"));
            }
            if (!getConfig().getString("language").equalsIgnoreCase("EN") && !getConfig().getString("language").equalsIgnoreCase("FR") && !getConfig().getString("language").equalsIgnoreCase("NL") && !getConfig().getString("language").equalsIgnoreCase("SE")) {
                graph3.addPlotter(new SimplePlotter("Other"));
            }
            debugmsg = "Metrics sent!";
            onDebug();
            metrics.start();
        } catch (Exception e) {
            Main.logger.warning(e.getMessage());
        }
    }

    public class SimplePlotter extends Metrics.Plotter {

        public SimplePlotter(final String name) {
            super(name);
        }

        @Override
        public int getValue() {
            return 1;
        }
    }
}
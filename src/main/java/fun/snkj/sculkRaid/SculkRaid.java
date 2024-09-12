package fun.snkj.sculkRaid;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SculkRaid extends JavaPlugin {
    @Override
    public void onEnable() {
        System.out.println("Successfully enabled");
        PluginManager pluginManager = Bukkit.getPluginManager();
        RaidStart raidStart = new RaidStart(this);
        getCommand("sculkraid").setExecutor(raidStart);
        pluginManager.registerEvents(raidStart, this);
        // Plugin startup logic

    }


    @Override
    public void onDisable() {
        System.out.println("Successfully disabled");
        // Plugin shutdown logic
    }
}

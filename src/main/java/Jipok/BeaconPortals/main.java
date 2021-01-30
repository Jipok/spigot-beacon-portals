package Jipok.BeaconPortals;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
    public beacons beacons = new beacons(this);

    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(beacons, this);
        if (getConfig().contains("Beacons"))
            for (Object o: getConfig().getList("Beacons"))
                beacons.locations.add((Location) o);
        getLogger().info("Loaded locations: " + String.valueOf(beacons.locations.size()));
        beacons.update_gui();
    }

}
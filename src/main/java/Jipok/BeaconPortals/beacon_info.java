package Jipok.BeaconPortals;

import java.util.*;

import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class beacon_info implements ConfigurationSerializable {
    public Location location;
    public Material icon;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("location", location);
        data.put("icon", icon.toString());
        return data;
    }

    public static beacon_info deserialize(Map<String, Object> data) {
        beacon_info result = new beacon_info();
        result.location = (Location) data.get("location");
        result.icon = Material.getMaterial((String) data.get("icon"));
        return result;
    }
}

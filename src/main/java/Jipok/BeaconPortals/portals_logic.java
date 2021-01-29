package Jipok.BeaconPortals;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;


public class portals_logic {
    private main plugin;
    public List<Location> portals = new ArrayList<>();

    portals_logic(main plugin) {
        this.plugin = plugin;
    }
    

    public boolean add_portal(Location location) {
        for (Location i: portals) {
            if (i.equals(location)) return false;
        }
        portals.add(location);
        plugin.getConfig().set("Portals", portals);
        plugin.saveConfig();
        return true;
    }


    public boolean is_beacon(Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.BEACON) return false;
        return ((Beacon)block.getState()).getTier() > 0;
    }


    public static boolean is_safe_location(Location location) {
        Block feet = location.getBlock();
        if (!feet.isPassable())
            return false;
        if (!location.clone().add(0, 1, 0).getBlock().isPassable())
            return false;
        return true;
    }


    // Teleports with horse/boat/passengers. Safe
    public void full_teleport(Entity target, Location to_pos) {
        to_pos.getWorld().loadChunk(to_pos.getBlockX(), to_pos.getBlockZ());
        while (!is_safe_location(to_pos)) { to_pos.add(0, 1, 0); }
        if (target.isInsideVehicle()) {
            full_teleport(target.getVehicle(), to_pos);
            return;
        }
        for (Entity passenger: target.getPassengers()) {
            target.removePassenger(passenger);
            full_teleport(passenger, to_pos);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                target.addPassenger(passenger);
                if (target.getType() == EntityType.PLAYER )
                    to_pos.getWorld().regenerateChunk(to_pos.getBlockX(), to_pos.getBlockZ());
            }, 6);
        }
        if (target.getType() == EntityType.PLAYER) {
            target.teleport(to_pos, TeleportCause.PLUGIN);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                target.teleport(to_pos, TeleportCause.PLUGIN);
            }, 3);
        }
    }


    public void beacon_to_beacon_teleport(Location from_pos, Location to_pos) {
        World from_world = from_pos.getWorld();
        World to_world = to_pos.getWorld();

        // Mass teleportatoin
        Vector locationOffset = to_pos.toVector().subtract(from_pos.toVector());
        Collection<Entity> entities = from_world.getNearbyEntities(from_pos, 4, 4, 4);
        for (Entity e: entities) {
            Location loc = e.getLocation();
            loc.setWorld(to_world);
            loc.add(locationOffset);
            full_teleport(e, loc);
            to_world.spawnParticle(Particle.FLASH, loc, 10); // Flash effect
        };

        // Sound and effect
        from_world.playSound(from_pos, Sound.ENTITY_ENDERMAN_TELEPORT, 100, 100);
        to_world.playSound(to_pos, Sound.ENTITY_ENDERMAN_TELEPORT, 100, 100);
        from_world.spawnParticle(Particle.FLASH, from_pos, 50);
        from_world.spawnParticle(Particle.END_ROD, from_pos, 100);
        to_world.spawnParticle(Particle.END_ROD, to_pos, 100);
    }
    

    public Location get_next_portal(Location origin, Boolean reverse) {
        Location first = null;
        Boolean was_origin = false;
        List<Location> tmp = new ArrayList<>(portals);
        if (reverse) Collections.reverse(tmp);
        for (Location loc: tmp) {
            if (!is_beacon(loc)) {
                portals.remove(loc);
                continue;
            }
            if (!loc.equals(origin)) {
                if (first == null) first = loc;
                if (was_origin) return loc;
            } else was_origin = true;
        }
        return first;
    }


    public boolean beacon_do_teleport_chain(Location from_pos, boolean reverse) {
        // Add if new beacon
        if (add_portal(from_pos.clone())) {
            from_pos.getWorld().playSound(from_pos, Sound.ENTITY_ENDER_EYE_DEATH, 100, 100);
            from_pos.getWorld().spawnParticle(Particle.SQUID_INK, from_pos.add(0.5, 1, 0.5), 100);
            return false;
        }

        // Get destination position
        Location to_pos = get_next_portal(from_pos, reverse);
        if (to_pos == null) {
            from_pos.getWorld().spawnParticle(Particle.BARRIER, from_pos.add(0.5, 1.5, 0.5), 1);
            return false;
        }

        beacon_to_beacon_teleport(from_pos, to_pos);
        return true;
    }
}


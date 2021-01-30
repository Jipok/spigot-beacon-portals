package Jipok.BeaconPortals;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;


public class teleport_logic {
    public static boolean is_safe_location(Location location) {
        Block feet = location.getBlock();
        if (!feet.isPassable())
            return false;
        if (!location.clone().add(0, 1, 0).getBlock().isPassable())
            return false;
        return true;
    }


    // Teleports with horse/boat/passengers. Safe
    public static void full_teleport(JavaPlugin plugin, Entity target, Location to_pos) {
        to_pos.getWorld().loadChunk(to_pos.getBlockX(), to_pos.getBlockZ());
        while (!is_safe_location(to_pos)) { to_pos.add(0, 1, 0); }
        if (target.isInsideVehicle()) {
            full_teleport(plugin, target.getVehicle(), to_pos);
            return;
        }
        for (Entity passenger: target.getPassengers()) {
            target.removePassenger(passenger);
            full_teleport(plugin, passenger, to_pos);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                target.addPassenger(passenger);
                //if (target.getType() == EntityType.PLAYER )
                    //to_pos.getWorld().refreshChunk(to_pos.getBlockX(), to_pos.getBlockZ());
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


    public static void mass_teleport(JavaPlugin plugin, Location from_pos, Location to_pos) {
        World from_world = from_pos.getWorld();
        World to_world = to_pos.getWorld();

        // Mass teleportatoin
        Vector locationOffset = to_pos.toVector().subtract(from_pos.toVector());
        Collection<Entity> entities = from_world.getNearbyEntities(from_pos, 4, 4, 4);
        for (Entity e: entities) {
            Location loc = e.getLocation();
            to_world.spawnParticle(Particle.FLASH, loc, 10); // Flash effect
            loc.setWorld(to_world);
            loc.add(locationOffset);
            full_teleport(plugin, e, loc);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                to_world.spawnParticle(Particle.FLASH, loc, 10); // Flash effect
            }, 1);
        };

        // Sound and effect
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            from_world.playSound(from_pos, Sound.ENTITY_ENDERMAN_TELEPORT, 100, 100);
            from_world.spawnParticle(Particle.FLASH, from_pos, 50);
            from_world.spawnParticle(Particle.END_ROD, from_pos, 100);
        }, 2);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            to_world.playSound(to_pos, Sound.ENTITY_ENDERMAN_TELEPORT, 100, 100);
            to_world.spawnParticle(Particle.END_ROD, to_pos, 100);
        }, 8);
    }
}
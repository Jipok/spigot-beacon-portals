package Jipok.BeaconPortals;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener {
    private portals_logic logic = new portals_logic(this);

    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getConfig().contains("Portals"))
            for (Object o: getConfig().getList("Portals"))
                logic.portals.add((Location) o);
        getLogger().info("Loaded portals: " + String.valueOf(logic.portals.size()));
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Only if player click on BEACON with EYE in hand
        if (!event.hasItem() || !event.hasBlock()) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        if (event.getItem().getType() != Material.ENDER_EYE) return;
        Location from_pos = event.getClickedBlock().getLocation();
        if (!logic.is_beacon(from_pos)) return;
        // Skip if sneaking(shift)
        if (player.isSneaking()) return;

        // Prevent beacon gui
        event.setCancelled(true);

        if (logic.beacon_do_teleport_chain(from_pos, event.getAction() == Action.RIGHT_CLICK_BLOCK))
            event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
}


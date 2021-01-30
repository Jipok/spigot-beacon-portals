package Jipok.BeaconPortals;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;


public class beacons implements Listener {
    private Inventory inv;
    private main plugin;
    public List<Location> locations = new ArrayList<>();


    public beacons(main plugin) {
        this.plugin = plugin;
        inv = Bukkit.createInventory(null, 54, "Teleport to:");
    }

    public boolean add_beacon(Location location) {
        for (Location i: locations) {
            if (i.equals(location)) return false;
        }
        locations.add(location);
        plugin.getConfig().set("Beacons", locations);
        plugin.saveConfig();
        return true;
    }


    public boolean is_beacon(Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.BEACON) return false;
        return ((Beacon)block.getState()).getTier() > 0;
    }

    public void update_gui() {
        inv.clear();
        for (Location loc: locations) {
            Material icon = loc.getWorld().getBlockAt(loc.clone().add(0, -2, 0)).getType();
            ItemStack item = new ItemStack(icon, 1);
            ItemMeta meta = item.getItemMeta();
            // Set the name of the item
            meta.setDisplayName(loc.getWorld().getName());
            // Set the lore of the item
            meta.setLore(Arrays.asList(loc.toVector().toString()));

            item.setItemMeta(meta);
            inv.addItem(item);
        }
    }

    public void show_gui(HumanEntity ent, Location beacon) {
        ent.setMetadata("current_beacon", new FixedMetadataValue(plugin, beacon));
        ent.openInventory(inv);
    }


    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.BEACON)
        add_beacon(event.getBlock().getLocation());
            update_gui();
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEACON)
            for (Location i: locations)
                if (i.equals(event.getBlock().getLocation())) {
                    locations.remove(i);
                    update_gui();
                    break;
                }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Only if player click on BEACON with EYE in hand
        if (!event.hasItem() || !event.hasBlock()) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        if (event.getItem().getType() != Material.ENDER_EYE) return;
        Location from_pos = event.getClickedBlock().getLocation();
        if (!is_beacon(from_pos)) return;
        // Skip if sneaking(shift)
        if (player.isSneaking()) return;

        // Prevent beacon gui
        event.setCancelled(true);

        // Fix block before plugin installation
        if (add_beacon(from_pos.clone()))
            update_gui();

        show_gui(player, from_pos);
        //if (logic.beacon_do_teleport_chain(from_pos, event.getAction() == Action.RIGHT_CLICK_BLOCK))
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
    

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != inv) return;
        e.setCancelled(true);

        final ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();
        p.closeInventory();

        Location from_pos = (Location) p.getMetadata("current_beacon").get(0).value();
        teleport_logic.mass_teleport(plugin, from_pos, locations.get(e.getSlot()));
    }


    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDragEvent(final InventoryDragEvent e) {
        if (e.getInventory() == inv) {
          e.setCancelled(true);
        }
    }

}

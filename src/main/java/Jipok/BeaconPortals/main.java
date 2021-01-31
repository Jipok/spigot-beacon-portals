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
import org.bukkit.plugin.java.JavaPlugin;

import Jipok.BeaconPortals.icons_gui.SelectIconEvent;


public class main extends JavaPlugin implements Listener {
    private Inventory beacons_inv;
    public icons_gui icons;
    public List<beacon_info> list = new ArrayList<>();

    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        icons = new icons_gui(this);
        beacons_inv = Bukkit.createInventory(null, 54, "Teleport to:");
        if (getConfig().contains("Beacons"))
            for (Object o: getConfig().getList("Beacons"))
                list.add((beacon_info) o);
        getLogger().info("Loaded beacons: " + String.valueOf(list.size()));
        update();
    }


    public void update() {
        beacons_inv.clear();
        // Update GUI
        for (beacon_info info: list) {
            ItemStack item = new ItemStack(info.icon);
            ItemMeta meta = item.getItemMeta();
            // Set the name of the item
            meta.setDisplayName(info.location.getWorld().getName());
            // Set the lore of the item
            meta.setLore(Arrays.asList(info.location.toVector().toString()));

            item.setItemMeta(meta);
            beacons_inv.addItem(item);
        }
        // Add button "Change icon"
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Change icon");
        item.setItemMeta(meta);
        beacons_inv.setItem(53, item);
        // Save beacons to config
        getConfig().set("Beacons", list);
        saveConfig();
    }


    public beacon_info add_beacon(Location location) {
        for (beacon_info info: list) {
            if (info.location.equals(location)) return info;
        }
        beacon_info beacon = new beacon_info();
        beacon.location = location.clone();
        beacon.icon = Material.BEACON;
        list.add(beacon);
        update();
        return beacon;
    }


    public boolean is_beacon(Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.BEACON) return false;
        return ((Beacon)block.getState()).getTier() > 0;
    }


    public void show_gui(HumanEntity ent, beacon_info info) {
        ent.setMetadata("current_beacon", new FixedMetadataValue(this, info));
        ent.openInventory(beacons_inv);
    }


    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.BEACON)
            add_beacon(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEACON)
            for (beacon_info info: list)
                if (info.location.equals(event.getBlock().getLocation())) {
                    list.remove(info);
                    update();
                    break;
                }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        // Only if player click(right) on BEACON with EYE in hand
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem() || !event.hasBlock()) return;
        if (event.getItem().getType() != Material.ENDER_EYE) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        Player player = event.getPlayer();
        Location from_pos = event.getClickedBlock().getLocation();
        // Prevent beacon gui
        event.setCancelled(true);
        // Also fix block before plugin installation
        beacon_info info = add_beacon(from_pos);

        show_gui(player, info);
    }


    @EventHandler
    public void onSelectIconEvent(SelectIconEvent event) {
        beacon_info info = (beacon_info) event.getWhoClicked().getMetadata("current_beacon").get(0).value();
        for (beacon_info i: list)
            if (i.location.equals(info.location))
                i.icon = event.icon;
        update();
        event.getWhoClicked().openInventory(beacons_inv);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory() != beacons_inv) return;
        event.setCancelled(true);

        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (event.getRawSlot() == 53) { // "Change icon"
            icons.show(player);
            return;
        } 
        // Only active beacon allow teleportation
        beacon_info info = (beacon_info) player.getMetadata("current_beacon").get(0).value();
        if (((Beacon) info.location.getBlock().getState()).getTier() > 0) {
            // Take payment 
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            teleport_logic.mass_teleport(this, info.location, list.get(event.getSlot()).location);
            player.closeInventory();
        } else {
            player.sendMessage(ChatColor.RED + "Only active beacon allow teleportation!");
        }
    }


    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDragEvent(final InventoryDragEvent e) {
        if (e.getInventory() == beacons_inv) {
          e.setCancelled(true);
        }
    }
}
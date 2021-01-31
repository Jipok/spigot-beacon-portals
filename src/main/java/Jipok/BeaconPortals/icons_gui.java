package Jipok.BeaconPortals;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;


public class icons_gui implements Listener {
    private List<Inventory> pages = new ArrayList<>();
    private JavaPlugin plugin;

    public class SelectIconEvent extends InventoryInteractEvent {
        public Material icon;
        public SelectIconEvent(InventoryView transaction, Material icon) { 
            super(transaction);
            this.icon = icon;
        }
    }

    private void add_page(Inventory inv) {
        ItemStack item;
        ItemMeta meta;
        // Add next arrow
        item = new ItemStack(Material.SPECTRAL_ARROW);
        meta = item.getItemMeta();
        meta.setDisplayName("Next page");
        item.setItemMeta(meta);
        inv.setItem(53, item);
        // Add prev arrow
        item = new ItemStack(Material.TIPPED_ARROW);
        meta = item.getItemMeta();
        meta.setDisplayName("Previous page");
        item.setItemMeta(meta);
        inv.setItem(45, item);
        //
        pages.add(inv);
    }

    public icons_gui(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        int counter = 0;
        Inventory inv = Bukkit.createInventory(null, 54, "Select icon:");

        for (Material m: Material.values()) {
            if (!m.isItem() || (m == Material.AIR)) continue;
            ItemStack item = new ItemStack(m);
            inv.addItem(item);
            counter++;
            if (counter == 45) {
                add_page(inv);
                inv = Bukkit.createInventory(null, 54, "Select icon:");
                counter = 0;
            }
        }
        add_page(inv);
    }


    public void show(HumanEntity ent) {
        int page = 0;
        if (ent.hasMetadata("icons_page")) {
            page = ent.getMetadata("icons_page").get(0).asInt();
        } else {
            ent.setMetadata("icons_page", new FixedMetadataValue(plugin, 0));
        }
        ent.openInventory(pages.get(page));
    }


    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!pages.contains(event.getInventory())) return;
        event.setCancelled(true);

        final ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        final Player p = (Player) event.getWhoClicked();

        int page = p.getMetadata("icons_page").get(0).asInt();
        if (event.getRawSlot() == 53) {
            page++;
            if (page == pages.size()) page = 0;
            p.setMetadata("icons_page", new FixedMetadataValue(plugin, page));
            p.openInventory(pages.get(page));
            return;
        }
        if (event.getRawSlot() == 45) {
            if (page == 0) page = pages.size();
            page--;
            p.setMetadata("icons_page", new FixedMetadataValue(plugin, page));
            p.openInventory(pages.get(page));
            return;
        }
        p.closeInventory();
        SelectIconEvent msg = new SelectIconEvent(event.getView(), item.getType());
        Bukkit.getPluginManager().callEvent(msg);
    }


    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDragEvent(final InventoryDragEvent e) {
        if (pages.contains(e.getInventory())) {
            e.setCancelled(true);
        }
    }
}

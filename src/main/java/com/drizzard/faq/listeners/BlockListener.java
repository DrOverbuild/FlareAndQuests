package com.drizzard.faq.listeners;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Created by jasper on 8/11/16.
 *
 * Handles all events under org.bukkit.event.block
 */
public class BlockListener implements Listener{
	FlareAndQuests plugin;

	public BlockListener(FlareAndQuests plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e){
		if(!e.isCancelled()){
			if(e.getItemInHand().getType().equals(Material.MOB_SPAWNER) && e.getItemInHand().getItemMeta().hasLore()){
				if(e.getItemInHand().getItemMeta().getLore().size() >= 1){
					try{
						EntityType type = EntityType.valueOf(e.getItemInHand().getItemMeta().getLore().get(0));
						CreatureSpawner cs = (CreatureSpawner) e.getBlockPlaced().getState();
						cs.setSpawnedType(type);
						cs.update();
					}catch (IllegalArgumentException ex){

					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev){
		if(ev.getBlock().getType().equals(Material.CHEST)) {
			BukkitTask task = plugin.getPartTimers().remove(ev.getBlock());
			if (task != null) {
				task.cancel();
			}
		}
	}
}

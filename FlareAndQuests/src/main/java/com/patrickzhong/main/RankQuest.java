package com.patrickzhong.main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RankQuest implements Listener {
	
	ItemStack is;
	Item im;
	Player owner;
	int timeLeft;
	boolean running = true;
	FlareAndQuests plugin;
	BukkitTask timer;
	
	public RankQuest(final ItemStack item, final Player owner, int duration, FlareAndQuests plugin){
		this.is = item;
		this.owner = owner;
		this.plugin = plugin;
		timeLeft = duration;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		ItemMeta IM = item.getItemMeta();
		if(!IM.getDisplayName().contains("(") || !IM.getDisplayName().contains(")")){
			IM.setDisplayName(IM.getDisplayName()+ChatColor.GRAY+" ("+ChatColor.YELLOW+timeLeft+ChatColor.GRAY+")");
			item.setItemMeta(IM);
			owner.updateInventory();
		}
		else {
			String str = ChatColor.stripColor(IM.getDisplayName());
			timeLeft = Integer.parseInt(str.substring(str.indexOf("(")+1, str.indexOf(")")));
		}
		
		timer = new BukkitRunnable(){
			public void run(){
				if(running){
					timeLeft--;
					ItemMeta IM = item.getItemMeta();
					String snip = IM.getDisplayName().substring(0, IM.getDisplayName().indexOf("(")+1);
					IM.setDisplayName(snip+ChatColor.YELLOW+timeLeft+ChatColor.GRAY+")");
					item.setItemMeta(IM);
					owner.updateInventory();
					
					if(timeLeft == 0){
						// Voucher
						owner.sendMessage(ChatColor.GREEN+"Rank quest completed.");
						this.cancel();
					}
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}
	
	public boolean isActive(Player player){
		return player.equals(owner) && running == true;
	}
	
	@EventHandler
	public void onDeath(final PlayerDeathEvent ev){
		if(ev.getEntity().equals(owner)){
			int am = is.getAmount();
			is.setAmount(1);
			int index = ev.getDrops().indexOf(is);
			is.setAmount(am);
			if(index > -1){
				ev.getDrops().remove(index);
				is.setAmount(1);
				im = owner.getWorld().dropItem(owner.getLocation(), is);
			}
			running = false;
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent ev){
		if(ev.getPlayer().equals(owner) && ev.getItemDrop().getItemStack().isSimilar(is)){
			running = false;
			im = ev.getItemDrop();
		}
			
	}

	@EventHandler
	public void onPick(PlayerPickupItemEvent ev){
		if(ev.getItem().equals(im))
			owner = ev.getPlayer();
	}
	
}

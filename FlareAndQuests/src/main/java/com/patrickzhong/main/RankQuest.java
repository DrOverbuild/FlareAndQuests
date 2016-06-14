package com.patrickzhong.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RankQuest implements Listener {
	
	ItemStack is;
	Item im;
	Player owner;
	int timeLeft;
	FlareAndQuests plugin;
	BukkitTask timer;
	String name;
	
	Location one;
	Location two;
	
	public RankQuest(final ItemStack item, final Player owner, int duration, final FlareAndQuests plugin, final String name){
		this.is = item;
		this.owner = owner;
		this.plugin = plugin;
		this.name = name;
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
		
		plugin.conf.load();
		one = (Location)plugin.conf.config.get("Quests."+name+".First");
		two = (Location)plugin.conf.config.get("Quests."+name+".Second");
		
		String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Start Broadcast")).replace("{player}", owner.getName());
		if(!message.toLowerCase().equals("none"))
			for(Player p : Bukkit.getOnlinePlayers())
				p.sendMessage(message);
			
		timer = new BukkitRunnable(){
			public void run(){
				timeLeft--;
				ItemMeta IM = item.getItemMeta();
				String snip = IM.getDisplayName().substring(0, IM.getDisplayName().indexOf("(")+1);
				IM.setDisplayName(snip+ChatColor.YELLOW+timeLeft+ChatColor.GRAY+")");
				item.setItemMeta(IM);
				owner.updateInventory();
				
				if(owner.getItemInHand() == null || !owner.getItemInHand().equals(is))
					plugin.sendActionBar(owner, ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("Action Bar Message")).replace("{left}", timeLeft+""));
				
				if(timeLeft == 0){
					plugin.conf.load();
					//if(is.getAmount() == 1)
						owner.getInventory().remove(is);
					/*else {
						is.setAmount(is.getAmount()-1);
						IM = is.getItemMeta();
						int index = IM.getDisplayName().indexOf("(") - 3;
						if(index >= 0){
							IM.setDisplayName(IM.getDisplayName().substring(0, index));
							is.setItemMeta(IM);
						}
					}*/
					owner.getInventory().addItem(plugin.conf.config.getItemStack("Quests."+name+".Voucher"));
					owner.updateInventory();
					
					String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Complete Broadcast")).replace("{player}", owner.getName());
					if(!message.toLowerCase().equals("none"))
						for(Player p : Bukkit.getOnlinePlayers())
							p.sendMessage(message);
					
					plugin.startLull(owner);
					kill();
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}
	
	public boolean isActive(Player player){
		return player.equals(owner);
	}
	
	public void kill(){
		timer.cancel();
		plugin.QIP.remove(owner);
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onDeath(final PlayerDeathEvent ev){
		if(ev.getEntity().equals(owner)){
			int am = is.getAmount();
			is.setAmount(1);
			int index = ev.getDrops().indexOf(is);
			is.setAmount(am);
			if(index > -1){
				plugin.conf.load();
				String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Lost Broadcast")).replace("{player}", owner.getName());
				if(!message.toLowerCase().equals("none"))
					for(Player p : Bukkit.getOnlinePlayers())
						p.sendMessage(message);
				kill();
			}
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent ev){
		if(ev.getWhoClicked().equals(owner) && ev.getCurrentItem() != null && ev.getCurrentItem().equals(is))
			ev.setCancelled(true);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent ev){
		if(ev.getPlayer().equals(owner) && !plugin.inside(ev.getTo(), one, two)){
			plugin.conf.load();
			String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Reset Broadcast")).replace("{player}", owner.getName());
			if(!message.toLowerCase().equals("none"))
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(message);
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if(index >= 0){
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			kill();
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent ev){
		if(ev.getPlayer().equals(owner)){
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if(index >= 0){
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			plugin.conf.load();
			String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Quit Broadcast")).replace("{player}", owner.getName());
			if(!message.toLowerCase().equals("none"))
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(message);
			kill();
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent ev){
		if(ev.getPlayer().equals(owner) && ev.getItemDrop().getItemStack().isSimilar(is)){
			plugin.conf.load();
			String message = ChatColor.translateAlternateColorCodes('&', plugin.conf.config.getString("RQ Lost Broadcast")).replace("{player}", owner.getName());
			if(!message.toLowerCase().equals("none"))
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(message);
			kill();
		}
	}
	
}

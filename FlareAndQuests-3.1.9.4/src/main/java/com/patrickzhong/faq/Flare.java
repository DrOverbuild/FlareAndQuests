package com.patrickzhong.faq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class Flare {
	
	FlareAndQuests plugin;
	boolean manual = false;
	double vel = 0.0;
	Random rand = new Random();
	
	public Flare(ItemStack is, Player player, final FlareAndQuests plugin, final String name){
		
		double r = plugin.conf.config.getDouble("Flare Drop Radius");
		Location loc = player.getLocation().add(0, 5, 0);
		Location rloc = randomLoc(loc, r);
		int count = 0;
		int max = plugin.conf.config.getInt("Flare Max Tries");
		
		while(!plugin.isWarzone(rloc)){
			rloc = randomLoc(loc, r);
			count++;
			if(count >= max){
				String message = ChatColor.translateAlternateColorCodes('&', plugin.trans.config.getString("Flare Drop Failed Message")).replace("{player}", player.getName());
				if(!message.toLowerCase().equals("none"))
					player.sendMessage(message);
				
					//player.sendMessage(plugin.DR+"Drop failed.");
				return;
			}
		}
		
		if(is.getAmount() == 1)
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
		else
			is.setAmount(is.getAmount()-1);
		player.updateInventory();
		
		double ar = plugin.conf.config.getDouble("Flare Alert Radius");
		String message = ChatColor.translateAlternateColorCodes('&', plugin.trans.config.getString("Flare Broadcast")).replace("{player}", player.getName());
		if(!message.toLowerCase().equals("none")){
			for(Entity ent : player.getNearbyEntities(r, r, r))
				if(ent instanceof Player && ent.getLocation().distance(loc) <= r)
					((Player)ent).sendMessage(message);
			player.sendMessage(message);
		}
		
		final ArmorStand armor = (ArmorStand)loc.getWorld().spawnEntity(rloc.clone().subtract(0, 1.5, 0), EntityType.ARMOR_STAND);
		armor.setGravity(true);
		armor.setVisible(false);
		armor.setHelmet(new ItemStack(Material.CHEST));
		armor.setHeadPose(new EulerAngle(0, Math.PI, 0));
		
		new BukkitRunnable(){
			public void run(){
				
				if(manual){
					Location head = armor.getLocation().add(0, 1.5, 0);
					if(head.getBlock().getType().isSolid()){
						armor.remove();
						final Block b = head.add(0, 1, 0).getBlock();
						b.setType(Material.CHEST);
						Chest c = (Chest)b.getState();
						plugin.conf.load();
						List<ItemStack> list = (List<ItemStack>)plugin.conf.config.getList("Flares."+name+".Contents", new ArrayList<ItemStack>());
						ItemStack[] conts = new ItemStack[27];
						int count = 0;
						
						int min = plugin.conf.config.getInt("Minimum Flare Contents");
						int max = plugin.conf.config.getInt("Maximum Flare Contents");
						
						if(list.size() < min)
							min = list.size();
						if(list.size() < max)
							max = list.size();
						
						int target = (int)(Math.random() * (max-min)) + min;
						
						for(int i = 0; i < list.size(); i++){
							ItemStack is = list.get(i);
							double percent = 100;
							if(is != null && is.hasItemMeta() && is.getItemMeta().hasLore()){
								ItemMeta im = is.getItemMeta();
								List<String> lore = im.getLore();
								percent = Double.parseDouble(ChatColor.stripColor(lore.remove(lore.size()-1)));
								im.setLore(lore);
								is.setItemMeta(im);
							}
							if(rand.nextDouble() * 100 < percent){
								conts[count] = is;
								count++;
							}
							
							if(count >= target)
								break;
						}
						
						while(count < target){
							conts[count] = list.get((int)(Math.random() * list.size()));
							count++;
						}
						
						c.getBlockInventory().setContents(conts);
						c.update(true);
						
						plugin.partTimers.put(b, new BukkitRunnable(){
							public void run(){
								plugin.spawnParticle(24, b.getLocation().clone().add(0.5, 0.5, 0.5), 1, 1, 1, 5, b.getWorld().getEntitiesByClass(Player.class));
							}
						}.runTaskTimer(plugin, 0, 1));
						
						this.cancel();
					}
					else
						armor.teleport(armor.getLocation().add(0, 5*vel, 0));
				}
				else if(armor.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()){
					double v = armor.getVelocity().getY();
					manual = true;
					armor.setGravity(false);
					vel = v;
				}
				else if(armor.getVelocity().getY() == 0){
					manual = true;
					armor.setGravity(false);
					vel = -0.0784000015258789;
				}
			}
		}.runTaskTimer(plugin, 10, 1);
	}
	
	private Location randomLoc(Location loc, double r){
		int sx; //   Variables
		int lx; //   For
		int xz; //   Warzone  
		int lz; //   Boundaries
		
		double dX = (Math.random() * 2 - 1) * r;
		double dZ = (Math.random() * 2 - 1) * Math.sqrt(Math.pow(r, 2) - Math.pow(dX, 2));
		double dY = Math.random() * Math.sqrt(Math.pow(r, 2) - Math.pow(dX, 2) - Math.pow(dZ, 2));
		
		return new Location(loc.getWorld(), (int)(dX + loc.getX())+0.5, dY + loc.getY(), (int)(dZ + loc.getZ())+0.5);
	}

}

package com.patrickzhong.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class FlareAndQuests extends JavaPlugin implements Listener {
	
	Config conf;
	
	String DR = ChatColor.DARK_RED+"";
	String R = ChatColor.RED+"";
	String G = ChatColor.GRAY+"";
	String Y = ChatColor.YELLOW+"";
	
	HashMap<Player, Location> left = new HashMap<Player, Location>();
	HashMap<Player, Location> right = new HashMap<Player, Location>();
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		HashMap<String, Object> defs = new HashMap<String, Object>();
		defs.put("Broadcast", "&e{player} &7has started a rank quest!");
		conf = new Config(this, defs);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		boolean a = cmd.getName().equalsIgnoreCase("rq");
		boolean b = cmd.getName().equalsIgnoreCase("flare");
		
		if(a || b){
			if(!(sender instanceof Player))
				sender.sendMessage(DR+"You must be a player.");
			else if(args.length < 1) // TODO help page
				sender.sendMessage(DR+"Missing arguments.");
			else if(!sender.hasPermission(cmd.getName().toLowerCase()+"."+args[0].toLowerCase()) && !sender.hasPermission("rq.*"))
				sender.sendMessage(DR+"You need the permission "+R+"rq."+args[0].toLowerCase());
			else if(args.length < 2){ // TODO check if command exists
				if(args[0].equalsIgnoreCase("wand")){
					ItemStack i = new ItemStack(Material.IRON_AXE);
					ItemMeta im = i.getItemMeta();
					im.setDisplayName(ChatColor.AQUA+""+ChatColor.BOLD+"Rank Quest Region Selector");
					i.setItemMeta(im);
					((Player)sender).getInventory().addItem(i);
					((Player)sender).updateInventory();
				}
			}
			else {
				conf.load();
				Player player = (Player)sender;
				if(args[0].equalsIgnoreCase("create")){
					if(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
						player.sendMessage(DR+"You must hold an item in your hand.");
					else if(a){
						// Start CREATE (RQ)
						if(conf.config.contains("Quests."+args[1]))
							player.sendMessage(DR+"There is already a quest named "+R+args[1]);
						else {
							conf.config.set("Quests."+args[1]+".Activate", player.getItemInHand());
							conf.save();
							player.sendMessage(G+"Successfully created a rank quest named "+Y+args[1]);
							player.sendMessage(G+"Next step: select a region using "+Y+"/rq wand"+G+" and "+Y+"/rq setregion "+args[1]);
						}
						// Successful CREATE (RQ)
					}
					else if(b){
						// Start CREATE (FLARE)
						if(conf.config.contains("Flares."+args[1]))
							player.sendMessage(DR+"There is already a flare named "+R+args[1]);
						else {
							conf.config.set("Flares."+args[1]+".Activate", player.getItemInHand());
							conf.save();
							player.sendMessage(G+"Successfully created a flare named "+Y+args[1]);
							player.sendMessage(G+"Next step: set the items using "+Y+"/flare setinventory "+args[1]);
						}
						// End CREATE (FLARE)
					}
				}
				else if(args[0].equalsIgnoreCase("delete")){
					if(a){
						// Start DELETE (RQ)
						if(!conf.config.contains("Quests."+args[1]))
							player.sendMessage(DR+"There is no quest named "+R+args[1]);
						else {
							conf.config.set("Quests."+args[1], null);
							conf.save();
							player.sendMessage(G+"Successfully deleted "+Y+args[1]);
						}
						// Successful DELETE (RQ)
					}
					else if(b){
						// Start DELETE (FLARE)
						if(!conf.config.contains("Flares."+args[1]))
							player.sendMessage(DR+"There is no flare named "+R+args[1]);
						else {
							conf.config.set("Flares."+args[1], null);
							conf.save();
							player.sendMessage(G+"Successfully deleted "+Y+args[1]);
						}
						// End DELETE (FLARE)
					}
				}
				else {
					if(a && !conf.config.contains("Quests."+args[1]))
						sender.sendMessage(DR+"There is no rank quest of the name "+R+args[1]);
					else if(a){
						if(args[0].equalsIgnoreCase("setvoucher")){
							// Start SETVOUCHER (RQ)
							if(player.getItemInHand() == null)
								player.sendMessage(DR+"You must hold an item in your hand.");
							else {
								conf.config.set("Quests."+args[1]+".Voucher", player.getItemInHand());
								conf.save();
								player.sendMessage(G+"Successfully set the voucher for "+Y+args[1]);
								player.sendMessage(G+"Next step: set the rewards using "+Y+"/rq setvitems "+args[1]);
							}
							// End SETVOUCHER (RQ)
						}
						else if(args[0].equalsIgnoreCase("setvitems")){
							// Start SETVITEMS (RQ)
							List<ItemStack> items = new ArrayList<ItemStack>();
							for(ItemStack i : player.getInventory().getContents())
								if(i != null && i.getType() != Material.AIR)
									items.add(i);
							conf.config.set("Quests."+args[1]+".Rewards", items);
							conf.save();
							player.sendMessage(G+"Successfully set the reward items for "+Y+args[1]);
							player.sendMessage(G+"Next step: add reward commands using "+Y+"/rq addvcommand "+args[1]+" <command>");
							// End SETVITEMS (RQ)
						}
						else if(args[0].equalsIgnoreCase("setregion")){
							// Start SETREGION (RQ)
							Location f = left.get(player);
							Location s = right.get(player);
							if(f == null || s == null)
								player.sendMessage(DR+"Please select a region first");
							else {
								conf.config.set("Quests."+args[1]+".First", f);
								conf.config.set("Quests."+args[1]+".Second", s);
								conf.save();
								player.sendMessage(G+"Successfully set the region for "+Y+args[1]);
								player.sendMessage(G+"Next step: set the voucher item using "+Y+"/rq setvoucher "+args[1]);
							}
							// End SETREGION (RQ)
						}
						else if(args[0].equalsIgnoreCase("addvcommand")){
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								// Start ADDVCOMMAND (RQ)
								List<String> cmds = conf.config.getStringList("Quests."+args[1]+".Commands");
								if(cmds == null)
									cmds = new ArrayList<String>();
								String newCMD = "";
								for(int i = 2; i < args.length; i++)
									newCMD += (i > 2 ? " " : "") + args[i]; 
								cmds.add(newCMD);
								conf.config.set("Quests."+args[1]+".Commands", cmds);
								conf.save();
								player.sendMessage(G+"Successfully added "+Y+"/"+newCMD+G+" to the quest "+Y+args[1]);
								player.sendMessage(G+"You're all done setting up the rank quest "+Y+args[1]+G+"!");
								// End ADDVCOMMAND (RQ)
							}
						}
					}
					else if(b && !conf.config.contains("Flares."+args[1]))
						sender.sendMessage(DR+"There is no flare of the name "+R+args[1]);
					else if(b && args[0].equalsIgnoreCase("setinventory")){
						// Start SETINVENTORY (FLARE)
						List<ItemStack> conts = new ArrayList<ItemStack>();
						for(ItemStack i : player.getInventory().getContents())
							conts.add(i);
						conf.config.set("Flares."+args[1]+".Contents", conts);
						conf.save();
						player.sendMessage(G+"Successfully set the inventory of "+Y+args[1]);
						player.sendMessage(G+"You're all done setting up the flare "+Y+args[1]+G+"!");
						// End SETINVENTORY (FLARE)
					}
				}
			}
		}
		
		return true;
	}
	
	public boolean inside(Location loc, Location one, Location two){
		boolean worlds = loc.getWorld().equals(one.getWorld()) && loc.getWorld().equals(two.getWorld());
		boolean x = Math.signum(loc.getX()-one.getX()) == -1 * Math.signum(loc.getX()-two.getX());
		boolean y = Math.signum(loc.getY()-one.getY()) == -1 * Math.signum(loc.getY()-two.getY());
		boolean z = Math.signum(loc.getZ()-one.getZ()) == -1 * Math.signum(loc.getZ()-two.getZ());
		return worlds && x && y && z;
	}
	
	private String disp(ItemStack i){
		try {
			return ChatColor.stripColor(i.getItemMeta().getDisplayName());
		}
		catch (Exception e){
			return "";
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		String disp = disp(ev.getItem());
		if(disp.equals("Rank Quest Region Selector")){
			ev.setCancelled(true);
			if(ev.getAction() == Action.LEFT_CLICK_BLOCK){
				Location loc = ev.getClickedBlock().getLocation();
				left.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G+"Set first position to "+Y+loc.getBlockX()+G+", "+Y+loc.getBlockY()+G+", "+Y+loc.getBlockZ()+G+" in "+Y+loc.getWorld().getName());
			}
			else if(ev.getAction() == Action.RIGHT_CLICK_BLOCK){
				Location loc = ev.getClickedBlock().getLocation();
				right.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G+"Set second position to "+Y+loc.getBlockX()+G+", "+Y+loc.getBlockY()+G+", "+Y+loc.getBlockZ()+G+" in "+Y+loc.getWorld().getName());
			}
		}
		else if(disp.contains("Rank Quest")){
			// TODO Match activation items & check if inside region(s)
			ev.setCancelled(true);
			new RankQuest(ev.getItem(), ev.getPlayer(), 30, this);
		}
	}

}

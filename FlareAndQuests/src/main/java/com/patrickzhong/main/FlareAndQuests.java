package com.patrickzhong.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FlareAndQuests extends JavaPlugin implements Listener {
	
	Config conf;
	
	String DR = ChatColor.DARK_RED+"";
	String R = ChatColor.RED+"";
	String G = ChatColor.GRAY+"";
	String Y = ChatColor.YELLOW+"";
	
	HashMap<Player, Location> left = new HashMap<Player, Location>();
	HashMap<Player, Location> right = new HashMap<Player, Location>();
	HashMap<Player, RankQuest> QIP = new HashMap<Player, RankQuest>();
	
	HashMap<Player, Integer> deathsLeft = new HashMap<Player, Integer>();
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		HashMap<String, Object> defs = new HashMap<String, Object>();
		defs.put("RQ Start Broadcast", "&e{player} &7has started a rank quest!");
		defs.put("RQ Complete Broadcast", "&e{player} &7has completed their rank quest!");
		defs.put("RQ Lost Broadcast", "&e{player} &7has lost their rank quest!");
		defs.put("RQ Reset Broadcast", "&e{player} &7has reset their rank quest!");
		
		defs.put("Deaths Allowed For Keep-Inv", 1);
		defs.put("Keep-Inv Duration", 60);
		
		defs.put("Rank Quest Duration", 30);
		conf = new Config(this, defs);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		boolean a = cmd.getName().equalsIgnoreCase("rq");
		boolean b = cmd.getName().equalsIgnoreCase("flare");
		
		if(a || b){
			if(!(sender instanceof Player))
				sender.sendMessage(DR+"You must be a player.");
			else if(args.length < 1) // TODO help page
				help(sender, a);
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
				else if(args[0].equalsIgnoreCase("help"))
					help(sender, a);
				else 
					sender.sendMessage(DR+"Missing arguments.");
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
						// End CREATE (RQ)
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
						// End DELETE (RQ)
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
								player.sendMessage(G+"Successfully added "+Y+"/"+ChatColor.translateAlternateColorCodes('&', newCMD)+G+" to the quest "+Y+args[1]);
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
	
	private void help(CommandSender sender, boolean rq){
		String SEP = ChatColor.WHITE+"- "+ChatColor.GRAY;
		String BEG = ChatColor.GRAY+"- "+ChatColor.AQUA;
		sender.sendMessage(ChatColor.GRAY+"==============  [ "+ChatColor.AQUA+"Flares and Quests"+ChatColor.GRAY+" ]  =============");
		if(rq){
			sender.sendMessage(BEG+"/rq help "+SEP+"Display this help page.");
			sender.sendMessage(BEG+"/flare help "+SEP+"Display flare help page.");
			sender.sendMessage(BEG+"/rq create <name> "+SEP+"Creates a rank quest with the item in your hand.");
			sender.sendMessage(BEG+"/rq delete <name> "+SEP+"Deletes a rank quest.");
			sender.sendMessage(BEG+"/rq wand "+SEP+"Gives you a selection wand.");
			sender.sendMessage(BEG+"/rq setregion <name> "+SEP+"Sets the region to your selection.");
			sender.sendMessage(BEG+"/rq setvoucher <name> "+SEP+"Sets the voucher.");
			sender.sendMessage(BEG+"/rq setvitems <name> "+SEP+"Sets the reward items.");
			sender.sendMessage(BEG+"/rq addvcommand <name> <command> "+SEP+"Adds a reward command (executed by console).");
		}
		else {
			sender.sendMessage(BEG+"/flare help "+SEP+"Display this help page.");
			sender.sendMessage(BEG+"/rq help "+SEP+"Display rank quest help page.");
			sender.sendMessage(BEG+"/flare create <name> "+SEP+"Creates a flare with the item in your hand.");
			sender.sendMessage(BEG+"/flare delete <name> "+SEP+"Deletes a flare.");
			sender.sendMessage(BEG+"/flare setinventory <name> "+SEP+"Sets the chest inventory of a flare.");
		}
	}
	
	public void startLull(final Player player){
		int dA = conf.config.getInt("Deaths Allowed For Keep-Inv");
		int dD = conf.config.getInt("Keep-Inv Duration");
		
		player.sendMessage(G+"You now have "+Y+dD+G+" seconds or "+Y+dA+G+" deaths of keep-inventory.");
		
		deathsLeft.put(player, dA);
		new BukkitRunnable(){
			public void run(){
				if(deathsLeft.containsKey(player)){
					deathsLeft.remove(player);
					player.sendMessage(G+"Your keep-inventory period has expired.");
				}
			}
		}.runTaskLater(this, dD * 20);
	}
	
	public boolean inside(Location loc, Location one, Location two){
		boolean worlds = loc.getWorld().equals(one.getWorld()) && loc.getWorld().equals(two.getWorld());
		
		/*boolean x = Math.signum(loc.getX()-one.getX()) == -1 * Math.signum(loc.getX()-two.getX());
		boolean y = Math.signum(loc.getY()-one.getY()) == -1 * Math.signum(loc.getY()-two.getY());
		boolean z = Math.signum(loc.getZ()-one.getZ()) == -1 * Math.signum(loc.getZ()-two.getZ());*/
		boolean x = inside1D(loc.getX(), one.getBlockX(), two.getBlockX());
		boolean y = inside1D(loc.getY(), one.getBlockY(), two.getBlockY());
		boolean z = inside1D(loc.getZ(), one.getBlockZ(), two.getBlockZ());
		return worlds && x && y && z;
	}
	
	private boolean inside1D(double a, double b, double c){
		return (b <= c && d(s(b - a), s(c + 1 - a))) || (b > c && d(s(b + 1 - a), s(c - a)));
	}
	
	private boolean d(double x, double y){
		return x == 0 || y == 0 || x == -1 * y;
	}
	
	private double s(double d){
		return Math.signum(d);
	}
	
	private String disp(ItemStack i){
		try {
			return ChatColor.stripColor(i.getItemMeta().getDisplayName());
		}
		catch (Exception e){
			return "";
		}
	}
	
	private boolean almost(ItemStack larger, ItemStack smaller, boolean disp){
		
		try {
			ItemMeta l = larger.getItemMeta();
			ItemMeta s = smaller.getItemMeta();
			return larger.getType() == smaller.getType() && 
					larger.getData().equals(smaller.getData()) && 
					((disp && l.getDisplayName().contains(s.getDisplayName())) || (!disp && l.getDisplayName().equals(s.getDisplayName()))) && 
					l.getLore().equals(s.getLore());
		}
		catch (Exception e){
			return false;
		}
		
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent ev){
		if(deathsLeft.containsKey(ev.getEntity())){
			int num = deathsLeft.get(ev.getEntity());
			if(num > 0)
				ev.setKeepInventory(true);
			num--;
			if(num <= 0){
				deathsLeft.remove(ev.getEntity());
				ev.getEntity().sendMessage(G+"Your keep-inventory period has expired.");
			}
			else
				deathsLeft.put(ev.getEntity(), num);
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
		else {
			if(ev.getItem() != null){
				conf.load();
				if(conf.config.contains("Flares")){
					for(String key : conf.config.getConfigurationSection("Flares").getKeys(false)){
						if(almost(ev.getItem(), conf.config.getItemStack("Flares."+key+".Activate"), false)){
							// Matched to flare
							// TODO launch flare
							// TODO check warzone
							ev.getPlayer().sendMessage(G+"Flare will be launched here.");
							ev.setCancelled(true);
							return;
						}
					}
				}
				if(conf.config.contains("Quests")){
					for(String key : conf.config.getConfigurationSection("Quests").getKeys(false)){
						ItemStack citem = conf.config.getItemStack("Quests."+key+".Activate");
						ItemStack vitem;
						
						if(almost(ev.getItem(), citem, true)){
							// Matched to Rank Quest
							ev.setCancelled(true);
							if(QIP.containsKey(ev.getPlayer()))
								ev.getPlayer().sendMessage(DR+"You are already doing a rank quest!");
							else if(!inside(ev.getPlayer().getLocation(), (Location)conf.config.get("Quests."+key+".First"), (Location)conf.config.get("Quests."+key+".Second")))
								ev.getPlayer().sendMessage(DR+"You must be inside the proper region!");
							else
								QIP.put(ev.getPlayer(), new RankQuest(ev.getItem(), ev.getPlayer(), conf.config.getInt("Rank Quest Duration"), this, key));
							return;
						}
						else if(almost(ev.getItem(), vitem = conf.config.getItemStack("Quests."+key+".Voucher"), false)){
							// Matched to Voucher
							ev.setCancelled(true);
							Inventory inv = ev.getPlayer().getInventory();
							inv.remove(ev.getItem());
							for(ItemStack i : (List<ItemStack>)conf.config.getList("Quests."+key+".Rewards", new ArrayList<ItemStack>()))
								inv.addItem(i);
							ev.getPlayer().updateInventory();
							if(conf.config.contains("Quests."+key+".Commands"))
								for(String str : conf.config.getStringList("Quests."+key+".Commands"))
									this.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", ev.getPlayer().getName()));
						}
					}
				}
			}
		}
	}

}

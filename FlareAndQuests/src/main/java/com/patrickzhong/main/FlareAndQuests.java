package com.patrickzhong.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


import org.bukkit.block.Block;
import org.apache.commons.lang.exception.ExceptionUtils;

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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

public class FlareAndQuests extends JavaPlugin implements Listener {
	
	Config conf;
	
	String DR = ChatColor.DARK_RED+"";
	String R = ChatColor.RED+"";
	String G = ChatColor.GRAY+"";
	String Y = ChatColor.YELLOW+"";
	
	HashMap<Player, Location> left = new HashMap<Player, Location>();
	HashMap<Player, Location> right = new HashMap<Player, Location>();
	HashMap<Player, RankQuest> QIP = new HashMap<Player, RankQuest>();
	
	HashMap<Block, BukkitTask> partTimers = new HashMap<Block, BukkitTask>();
	
	HashMap<Player, Integer> deathsLeft = new HashMap<Player, Integer>();
	
	String CBPATH;
	String NMSPATH;
	
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		HashMap<String, Object> defs = new HashMap<String, Object>();
		defs.put("RQ Start Broadcast", "&e{player} &7has started a rank quest!");
		defs.put("RQ Complete Broadcast", "&e{player} &7has completed their rank quest!");
		defs.put("RQ Lost Broadcast", "&e{player} &7has lost their rank quest!");
		defs.put("RQ Reset Broadcast", "&e{player} &7has reset their rank quest!");
		defs.put("RQ Quit Broadcast", "&e{player} &7left, so their rank quest was reset!");
		defs.put("Action Bar Message", "&b&lRank Quest: &e{left} &7seconds");
		
		defs.put("Not in Warzone Message", "&4You must be in a Warzone!");
		defs.put("Already Doing Quest Message", "&4You are already doing a rank quest!");
		defs.put("Not in Region Message", "&4You must be inside the proper region!");
		defs.put("Keep-Inventory Start Message", "&7You now have &e{duration} &7seconds or &e{deaths} &7deaths of keep-inventory.");
		defs.put("Keep-Inventory Expire Message", "&7Your keep-inventory period has expired.");
		defs.put("Flare Drop Failed Message", "&4Drop failed.");
		
		defs.put("Cannot Activate Stacked Rank Quests Message", "&4You cannot activate more than one rank quest at the same time!");
		
		defs.put("Flare Max Tries", 100);
		
		defs.put("Deaths Allowed For Keep-Inv", 1);
		defs.put("Keep-Inv Duration", 60);
		
		defs.put("Rank Quest Duration", 30);
		
		defs.put("Flare Drop Radius", 10.0);
		defs.put("Flare Alert Radius", 10.0);
		defs.put("Flare Broadcast", "&e{player} &7has used a flare!");
		conf = new Config(this, defs);
		
		String packageName = getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.indexOf(".v")+2);
		CBPATH = "org.bukkit.craftbukkit.v"+version+".";
		NMSPATH = "net.minecraft.server.v"+version+".";
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		boolean a = cmd.getName().equalsIgnoreCase("rq");
		boolean b = cmd.getName().equalsIgnoreCase("flare");
		boolean c = cmd.getName().equalsIgnoreCase("witem");
		
		if(a || b || c){
			if(args.length < 1)
				help(sender, cmd.getName());
			else if(!(sender instanceof Player) && !args[0].equalsIgnoreCase("give"))
				sender.sendMessage(DR+"You must be a player.");
			else if(!sender.hasPermission("faq"))
				sender.sendMessage(DR+"You need the permission "+R+"faq");
			else if(args.length < 2){ // TODO check if command exists
				if(args[0].equalsIgnoreCase("wand")){
					ItemStack i = new ItemStack(Material.IRON_AXE);
					ItemMeta im = i.getItemMeta();
					im.setDisplayName(ChatColor.AQUA+""+ChatColor.BOLD+"Rank Quest Region Selector");
					i.setItemMeta(im);
					((Player)sender).getInventory().addItem(i);
					((Player)sender).updateInventory();
				}
				else if(args[0].equalsIgnoreCase("list")){
					String msg = "";
					conf.load();
					String sec;
					if(a) sec = "Quests";
					else if(b) sec = "Flares";
					else sec = "Witems";
					for(String key : conf.config.getConfigurationSection(sec).getKeys(false))
						msg += (msg.equals("") ? "" : G+", ") + Y + key;
					sender.sendMessage(G+sec+": "+msg);
				}
				else if(args[0].equalsIgnoreCase("help"))
					help(sender, cmd.getName());
				else 
					sender.sendMessage(DR+"Missing arguments.");
			}
			else {
				conf.load();
				if(args[0].equalsIgnoreCase("create")){
					Player player = (Player)sender;
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
					else if(c){
						// Start CREATE (WITEM)
						if(conf.config.contains("Witems."+args[1]))
							player.sendMessage(DR+"There is already a witem named "+R+args[1]);
						else {
							conf.config.set("Witems."+args[1]+".Activate", player.getItemInHand());
							conf.save();
							player.sendMessage(G+"Successfully created a witem named "+Y+args[1]);
							player.sendMessage(G+"Next step: select a region using "+Y+"/rq wand"+G+" and "+Y+"/witem setregion "+args[1]);
						}
						// End CREATE (WITEM)
					}
				}
				else if(args[0].equalsIgnoreCase("delete")){
					Player player = (Player)sender;
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
					else if(c){
						// Start DELETE (WITEM)
						if(!conf.config.contains("Witems."+args[1]))
							player.sendMessage(DR+"There is no witem named "+R+args[1]);
						else {
							conf.config.set("Witems."+args[1], null);
							conf.save();
							player.sendMessage(G+"Successfully deleted "+Y+args[1]);
						}
						// End DELETE (WITEM)
					}
				}
				else if(args[0].equalsIgnoreCase("listvcommands") || args[0].equalsIgnoreCase("listcommands")){
					String sec;
					if(a) sec = "Quests";
					else if(c) sec = "Witems";
					else return true;
					
					if(!conf.config.contains(sec+"."+args[1]))
						sender.sendMessage(DR+"Could not find "+R+args[1]);
					else {
						String msg = "";
						conf.load();
						
						List<String> cmds = conf.config.getStringList(sec+"."+args[1]+".Commands");
						if(cmds == null)
							cmds = new ArrayList<String>();
						
						for(String command : cmds)
							msg += (msg.equals("") ? "" : G+", ") + Y + ChatColor.translateAlternateColorCodes('&', command);
						sender.sendMessage(ChatColor.GRAY+"Commands: "+msg);
					}
				}
				else {
					if(a && !conf.config.contains("Quests."+args[1]))
						sender.sendMessage(DR+"There is no rank quest of the name "+R+args[1]);
					else if(a){
						if(args[0].equalsIgnoreCase("setvoucher")){
							Player player = (Player)sender;
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
							Player player = (Player)sender;
							// Start SETVITEMS (RQ)
							
							List<ItemStack> items = (List<ItemStack>)conf.config.getList("Quests."+args[1]+".Rewards", new ArrayList<ItemStack>());
							Inventory inv = Bukkit.createInventory(player, 36, "Set Voucher Items For "+args[1]);
							for(int i = 0; i < items.size(); i++)
								inv.setItem(i, items.get(i));
							player.openInventory(inv);
							/*List<ItemStack> items = new ArrayList<ItemStack>();
							for(ItemStack i : player.getInventory().getContents())
								if(i != null && i.getType() != Material.AIR)
									items.add(i);
							conf.config.set("Quests."+args[1]+".Rewards", items);
							conf.save();
							player.sendMessage(G+"Successfully set the reward items for "+Y+args[1]);
							player.sendMessage(G+"Next step: add reward commands using "+Y+"/rq addvcommand "+args[1]+" <command>");
							*/
							// End SETVITEMS (RQ)
						}
						else if(args[0].equalsIgnoreCase("setregion")){
							Player player = (Player)sender;
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
							Player player = (Player)sender;
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
						else if(args[0].equalsIgnoreCase("delvcommand")){
							Player player = (Player)sender;
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								// Start DELVCOMMAND (RQ)
								List<String> cmds = conf.config.getStringList("Quests."+args[1]+".Commands");
								if(cmds == null)
									cmds = new ArrayList<String>();
								String newCMD = "";
								for(int i = 2; i < args.length; i++)
									newCMD += (i > 2 ? " " : "") + args[i]; 
								if(cmds.remove(newCMD)){
									conf.config.set("Quests."+args[1]+".Commands", cmds);
									conf.save();
									player.sendMessage(G+"Successfully removed "+Y+"/"+ChatColor.translateAlternateColorCodes('&', newCMD)+G+" from the quest "+Y+args[1]);
								}
								else
									player.sendMessage(DR+"Could not find the command "+R+"/"+ChatColor.translateAlternateColorCodes('&', newCMD));
								// End DELVCOMMAND (RQ)
							}
						}
						else if(args[0].equalsIgnoreCase("give")){
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								// Start GIVE (RQ)
								Player target = Bukkit.getPlayer(args[2]);
								if(target == null)
									sender.sendMessage(DR+"Could not find "+R+args[2]);
								else {
									target.getInventory().addItem(conf.config.getItemStack("Quests."+args[1]+".Activate"));
									target.updateInventory();
									sender.sendMessage(G+"Gave the quest "+Y+args[1]+G+" to "+Y+args[2]);
								}
								// End GIVE (RQ)
							}
						}
					}
					else if(b){
						if(!conf.config.contains("Flares."+args[1]))
							sender.sendMessage(DR+"There is no flare of the name "+R+args[1]);
						else if(args[0].equalsIgnoreCase("setinventory")){
							Player player = (Player)sender;
							// Start SETINVENTORY (FLARE)
							
							List<ItemStack> items = (List<ItemStack>)conf.config.getList("Flares."+args[1]+".Contents", new ArrayList<ItemStack>());
							Inventory inv = Bukkit.createInventory(player, 27, "Set Flare Items For "+args[1]);
							for(int i = 0; i < items.size(); i++)
								inv.setItem(i, items.get(i));
							player.openInventory(inv);
							
							/*List<ItemStack> conts = new ArrayList<ItemStack>();
							ItemStack[] iconts = player.getInventory().getStorageContents();
							for(int i = 0; i < 36; i++)
								if(iconts[i] != null && iconts[i].getType() != Material.AIR)
									conts.add(iconts[i]);
							if(conts.size() > 27)
								player.sendMessage(DR+"The chest can only hold 27 items, you have "+R+conts.size());
							else {
								conf.config.set("Flares."+args[1]+".Contents", conts);
								conf.save();
								player.sendMessage(G+"Successfully set the inventory of "+Y+args[1]);
								player.sendMessage(G+"You're all done setting up the flare "+Y+args[1]+G+"!");
							}*/
							// End SETINVENTORY (FLARE)
						}
						else if(args[0].equalsIgnoreCase("give")){
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								// Start GIVE (FLARE)
								Player target = Bukkit.getPlayer(args[2]);
								if(target == null)
									sender.sendMessage(DR+"Could not find "+R+args[2]);
								else {
									target.getInventory().addItem(conf.config.getItemStack("Flares."+args[1]+".Activate"));
									target.updateInventory();
									sender.sendMessage(G+"Gave the flare "+Y+args[1]+G+" to "+Y+args[2]);
								}
								// End GIVE (FLARE)
							}
						}
					}
					else if(c){
						if(!conf.config.contains("Witems."+args[1]))
							sender.sendMessage(DR+"There is no witem of the name "+R+args[1]);
						else if(args[0].equalsIgnoreCase("setregion")){
							Player player = (Player)sender;
							Location f = left.get(player);
							Location s = right.get(player);
							if(f == null || s == null)
								player.sendMessage(DR+"Please select a region first");
							else {
								conf.config.set("Witems."+args[1]+".First", f);
								conf.config.set("Witems."+args[1]+".Second", s);
								conf.save();
								player.sendMessage(G+"Successfully set the region for "+Y+args[1]);
								player.sendMessage(G+"Next step: add a command using "+Y+"/witem addcommand "+args[1]+" <command>");
							}
						}
						else if(args[0].equalsIgnoreCase("addcommand")){
							Player player = (Player)sender;
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								List<String> cmds = conf.config.getStringList("Witems."+args[1]+".Commands");
								if(cmds == null)
									cmds = new ArrayList<String>();
								String newCMD = "";
								for(int i = 2; i < args.length; i++)
									newCMD += (i > 2 ? " " : "") + args[i]; 
								cmds.add(newCMD);
								conf.config.set("Witems."+args[1]+".Commands", cmds);
								conf.save();
								player.sendMessage(G+"Successfully added "+Y+"/"+ChatColor.translateAlternateColorCodes('&', newCMD)+G+" to the witem "+Y+args[1]);
								player.sendMessage(G+"You're all done setting up the witem "+Y+args[1]+G+"!");
							}
						}
						else if(args[0].equalsIgnoreCase("delcommand")){
							Player player = (Player)sender;
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								List<String> cmds = conf.config.getStringList("Witems."+args[1]+".Commands");
								if(cmds == null)
									cmds = new ArrayList<String>();
								String newCMD = "";
								for(int i = 2; i < args.length; i++)
									newCMD += (i > 2 ? " " : "") + args[i]; 
								if(cmds.remove(newCMD)){
									conf.config.set("Witems."+args[1]+".Commands", cmds);
									conf.save();
									player.sendMessage(G+"Successfully removed "+Y+"/"+ChatColor.translateAlternateColorCodes('&', newCMD)+G+" from the witem "+Y+args[1]);
								}
								else
									player.sendMessage(DR+"Could not find the command "+R+"/"+ChatColor.translateAlternateColorCodes('&', newCMD));
									
							}
						}
						else if(args[0].equalsIgnoreCase("give")){
							if(args.length < 3)
								sender.sendMessage(DR+"Missing arguments.");
							else {
								Player target = Bukkit.getPlayer(args[2]);
								if(target == null)
									sender.sendMessage(DR+"Could not find "+R+args[2]);
								else {
									target.getInventory().addItem(conf.config.getItemStack("Witems."+args[1]+".Activate"));
									target.updateInventory();
									sender.sendMessage(G+"Gave the witem "+Y+args[1]+G+" to "+Y+args[2]);
								}
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public void sendActionBar(Player player, String message){
		
		try {
			Class CP = Class.forName(CBPATH+"entity.CraftPlayer");
	        Object p = CP.cast(player);
			
	        Class ICBC = Class.forName(NMSPATH+"IChatBaseComponent");
	        Class CS = ICBC.getDeclaredClasses()[0];
	        Object cbc = CS.getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}");
	        
	        Class PPOC = Class.forName(NMSPATH+"PacketPlayOutChat");
	        Object ppoc = PPOC.getConstructor(ICBC, byte.class).newInstance(cbc, (byte)2);
	        
	        Class PC = Class.forName(NMSPATH+"PlayerConnection");
	        Class EP = Class.forName(NMSPATH+"EntityPlayer");
	        Object ep = CP.getMethod("getHandle").invoke(p);
	        Object pc = EP.getField("playerConnection").get(ep);
	        Class P = Class.forName(NMSPATH+"Packet");
	        
	        PC.getMethod("sendPacket", P).invoke(pc, P.cast(ppoc));
	        
	        //CraftPlayer p = (CraftPlayer) player;
	        //IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
	        //PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
	        //((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
			}
		catch (Exception e){
			getLogger().info(ExceptionUtils.getStackTrace(e));
		}
        
    }
	
	public void spawnParticle(int part, Location loc, double offX, double offY, double offZ, int count, Collection<Player> ents){
		try {
			Class PPOP = Class.forName(NMSPATH+"PacketPlayOutWorldParticles");
			Class EPART = Class.forName(NMSPATH+"EnumParticle");
			Constructor PPOPCONSTRUCTOR = PPOP.getConstructor(EPART, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);
			Object enumParticle = EPART.getEnumConstants()[part];
			
			Class P = Class.forName(NMSPATH+"Packet");
	        Object packet = P.cast(PPOPCONSTRUCTOR.newInstance(EPART.cast(enumParticle), true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), (float)offX, (float)offY, (float)offZ, 0f, count, new int[0]));
		
			Class CP = Class.forName(CBPATH+"entity.CraftPlayer");
	        
			Class PC = Class.forName(NMSPATH+"PlayerConnection");
	        Class EP = Class.forName(NMSPATH+"EntityPlayer");
	        
	        Field field = EP.getField("playerConnection");
	        Method getHandle = CP.getMethod("getHandle");
	        Method sendPacket = PC.getMethod("sendPacket", P);
	        
	        for(Player ent : ents)
				sendPacket.invoke(field.get(getHandle.invoke(CP.cast(ent))), packet);
		}
		catch (Exception e){
			getLogger().info(ExceptionUtils.getStackTrace(e));
		}
		
		/*PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(part, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), (float)offX, (float)offY, (float)offZ, 0f, 1);
		for(Entity ent : ents)
			if(ent instanceof Player)
				((CraftPlayer)ent).getHandle().playerConnection.sendPacket(packet);*/
		
	}
	
	
	private void help(CommandSender sender, String name){
		String SEP = ChatColor.WHITE+"- "+ChatColor.GRAY;
		String BEG = ChatColor.GRAY+"- "+ChatColor.AQUA;
		sender.sendMessage(ChatColor.GRAY+"==============  [ "+ChatColor.AQUA+"Flares and Quests"+ChatColor.GRAY+" ]  =============");
		if(name.equalsIgnoreCase("rq")){
			sender.sendMessage(BEG+"/rq help "+SEP+"Display this help page.");
			sender.sendMessage(BEG+"/flare help "+SEP+"Display flare help page.");
			sender.sendMessage(BEG+"/witem help "+SEP+"Display witem help page.");
			sender.sendMessage(BEG+"/rq create <name> "+SEP+"Creates a rank quest.");
			sender.sendMessage(BEG+"/rq delete <name> "+SEP+"Deletes a rank quest.");
			sender.sendMessage(BEG+"/rq wand "+SEP+"Gives you a selection wand.");
			sender.sendMessage(BEG+"/rq setregion <name> "+SEP+"Sets the region to your selection.");
			sender.sendMessage(BEG+"/rq setvoucher <name> "+SEP+"Sets the voucher.");
			sender.sendMessage(BEG+"/rq setvitems <name> "+SEP+"Sets the reward items.");
			sender.sendMessage(BEG+"/rq addvcommand <name> <command> "+SEP+"Adds a command.");
			sender.sendMessage(BEG+"/rq listvcommands <name> <command> "+SEP+"Lists all commands.");
			sender.sendMessage(BEG+"/rq delvcommand <name> <command> "+SEP+"Deletes a command.");
			sender.sendMessage(BEG+"/rq give <name> <player> "+SEP+"Gives a player a rank quest.");
			sender.sendMessage(BEG+"/rq list "+SEP+"Lists all rank quests.");
			
		}
		else if(name.equalsIgnoreCase("flare")){
			sender.sendMessage(BEG+"/flare help "+SEP+"Display this help page.");
			sender.sendMessage(BEG+"/rq help "+SEP+"Display rank quest help page.");
			sender.sendMessage(BEG+"/witem help "+SEP+"Display witem help page.");
			sender.sendMessage(BEG+"/flare create <name> "+SEP+"Creates a flare.");
			sender.sendMessage(BEG+"/flare delete <name> "+SEP+"Deletes a flare.");
			sender.sendMessage(BEG+"/flare setinventory <name> "+SEP+"Sets the chest inventory.");
			sender.sendMessage(BEG+"/flare give <name> <player> "+SEP+"Gives a player a flare.");
			sender.sendMessage(BEG+"/flare list "+SEP+"Lists all flares.");
			
		}
		else if(name.equalsIgnoreCase("witem")){
			sender.sendMessage(BEG+"/witem help "+SEP+"Display this help page.");
			sender.sendMessage(BEG+"/rq help "+SEP+"Display rank quest help page.");
			sender.sendMessage(BEG+"/flare help "+SEP+"Display flare help page.");
			sender.sendMessage(BEG+"/witem create <name> "+SEP+"Creates a witem.");
			sender.sendMessage(BEG+"/witem delete <name> "+SEP+"Deletes a witem.");
			sender.sendMessage(BEG+"/witem setregion <name> "+SEP+"Sets the region to your selection.");
			sender.sendMessage(BEG+"/witem addcommand <name> <command> "+SEP+"Adds a command.");
			sender.sendMessage(BEG+"/witem listcommands <name> <command> "+SEP+"Lists all commands.");
			sender.sendMessage(BEG+"/witem delcommand <name> <command> "+SEP+"Deletes a command.");
			sender.sendMessage(BEG+"/witem give <name> <player> "+SEP+"Gives a player a witem.");
			sender.sendMessage(BEG+"/witem list "+SEP+"Lists all witems.");
			
		}
	}
	
	public void startLull(final Player player){
		int dA = conf.config.getInt("Deaths Allowed For Keep-Inv");
		int dD = conf.config.getInt("Keep-Inv Duration");
		
		String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Keep-Inventory Start Message")).replace("{player}", player.getName()).replace("{duration}", dD+"").replace("{deaths}", dA+"");
		if(!message.toLowerCase().equals("none"))
			player.sendMessage(message);
		
		//player.sendMessage(G+"You now have "+Y+dD+G+" seconds or "+Y+dA+G+" deaths of keep-inventory.");
		
		deathsLeft.put(player, dA);
		new BukkitRunnable(){
			public void run(){
				if(deathsLeft.containsKey(player)){
					deathsLeft.remove(player);
					
					String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Keep-Inventory Expire Message")).replace("{player}", player.getName());
					if(!message.toLowerCase().equals("none"))
						player.sendMessage(message);
					
					//player.sendMessage(G+"Your keep-inventory period has expired.");
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
			String disp = ChatColor.stripColor(i.getItemMeta().getDisplayName());
			return disp == null ? "" : disp;
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
					((!l.hasDisplayName() && !s.hasDisplayName()) || (((disp && l.getDisplayName().contains(s.getDisplayName())) || (!disp && l.getDisplayName().equals(s.getDisplayName()))))) && 
					((!l.hasLore() && !s.hasLore()) || (l.getLore().equals(s.getLore())));
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
				
				String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Keep-Inventory Expire Message")).replace("{player}", ev.getEntity().getName());
				if(!message.toLowerCase().equals("none"))
					ev.getEntity().sendMessage(message);
				
				//ev.getEntity().sendMessage(G+"Your keep-inventory period has expired.");
			}
			else
				deathsLeft.put(ev.getEntity(), num);
		}
	}
	
	public boolean isWarzone(Location loc){
		Faction f = Board.getInstance().getFactionAt(new FLocation(loc));
		return f == null ? false : f.isWarZone();
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent ev){
		String t = ev.getInventory().getTitle();
		boolean a = t.contains("Set Voucher Items For ");
		boolean b = t.contains("Set Flare Items For ");
		if(a || b){
			conf.load();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for(ItemStack i : ev.getInventory().getContents())
				if(i != null && i.getType() != Material.AIR)
					items.add(i);
			String name = t.substring(t.indexOf("For ")+4);
			conf.config.set((a ? "Quests." : "Flares.")+name+(a ? ".Rewards" : ".Contents"), items);
			conf.save();
			
			if(a){
				ev.getPlayer().sendMessage(G+"Successfully set the reward items for "+Y+name);
				ev.getPlayer().sendMessage(G+"Next step: add reward commands using "+Y+"/rq addvcommand "+name+" <command>");
			}
			else {
				ev.getPlayer().sendMessage(G+"Successfully set the inventory of "+Y+name);
				ev.getPlayer().sendMessage(G+"You're all done setting up the flare " +Y+name+G+"!");
			}
		}
	}
	
	@EventHandler
	public void onOpen(InventoryOpenEvent ev){
		if(ev.getInventory().getHolder() instanceof Block){
			BukkitTask t = partTimers.remove(((Block)ev.getInventory().getHolder()).getLocation());
			if(t != null)
				t.cancel();
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent ev){
		String disp = disp(ev.getItem());
		
		if(ev.getClickedBlock() != null){
			BukkitTask t = partTimers.remove(ev.getClickedBlock());
			if(t != null)
				t.cancel();
		}
		
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
							if(!isWarzone(ev.getPlayer().getLocation())){
								String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Not in Warzone Message")).replace("{player}", ev.getPlayer().getName());
								if(!message.toLowerCase().equals("none"))
									ev.getPlayer().sendMessage(message);
								//ev.getPlayer().sendMessage(DR+"You must be in a Warzone");
							}
							else {
								new Flare(ev.getItem(), ev.getPlayer(), this, key);
								ev.setCancelled(true);
							}
							return;
						}
					}
				}
				if(conf.config.contains("Witems")){
					for(String key : conf.config.getConfigurationSection("Witems").getKeys(false)){
						if(almost(ev.getItem(), conf.config.getItemStack("Witems."+key+".Activate"), false)){
							//Matched to Witem
							ev.setCancelled(true);
							if(!inside(ev.getPlayer().getLocation(), (Location)conf.config.get("Witems."+key+".First"), (Location)conf.config.get("Witems."+key+".Second"))){
								String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Not in Region Message")).replace("{player}", ev.getPlayer().getName());
								if(!message.toLowerCase().equals("none"))
									ev.getPlayer().sendMessage(message);
								
								//ev.getPlayer().sendMessage(DR+"You must be inside the proper region!");
							}
							else {
								if(ev.getItem().getAmount() == 1)
									ev.getPlayer().getInventory().remove(ev.getItem());
								else
									ev.getItem().setAmount(ev.getItem().getAmount()-1);
								
								ev.getPlayer().updateInventory();
								
								if(conf.config.contains("Witems."+key+".Commands"))
									for(String str : conf.config.getStringList("Witems."+key+".Commands"))
										this.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", ev.getPlayer().getName()));
							}
						}
					}
				}
				if(conf.config.contains("Quests")){
					for(String key : conf.config.getConfigurationSection("Quests").getKeys(false)){
						ItemStack citem = conf.config.getItemStack("Quests."+key+".Activate");
						
						if(almost(ev.getItem(), citem, true)){
							// Matched to Rank Quest
							ev.setCancelled(true);
							if(ev.getItem().getAmount() > 1){
								String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Cannot Activate Stacked Rank Quests Message")).replace("{player}", ev.getPlayer().getName());
								if(!message.toLowerCase().equals("none"))
									ev.getPlayer().sendMessage(message);
							}
							else if(QIP.containsKey(ev.getPlayer())){
								String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Already Doing Quest Message")).replace("{player}", ev.getPlayer().getName());
								if(!message.toLowerCase().equals("none"))
									ev.getPlayer().sendMessage(message);
								
								//ev.getPlayer().sendMessage(DR+"You are already doing a rank quest!");
							}
							else if(!inside(ev.getPlayer().getLocation(), (Location)conf.config.get("Quests."+key+".First"), (Location)conf.config.get("Quests."+key+".Second"))){
								String message = ChatColor.translateAlternateColorCodes('&', conf.config.getString("Not in Region Message")).replace("{player}", ev.getPlayer().getName());
								if(!message.toLowerCase().equals("none"))
									ev.getPlayer().sendMessage(message);
								
								//ev.getPlayer().sendMessage(DR+"You must be inside the proper region!");
							}
							else
								QIP.put(ev.getPlayer(), new RankQuest(ev.getItem(), ev.getPlayer(), conf.config.getInt("Rank Quest Duration"), this, key));
							return;
						}
						else if(almost(ev.getItem(), conf.config.getItemStack("Quests."+key+".Voucher"), false)){
							// Matched to Voucher
							ev.setCancelled(true);
							Inventory inv = ev.getPlayer().getInventory();
							
							if(ev.getItem().getAmount() == 1)
								inv.remove(ev.getItem());
							else
								ev.getItem().setAmount(ev.getItem().getAmount()-1);
							
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

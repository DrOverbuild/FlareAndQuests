package com.drizzard.faq;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.drizzard.faq.commands.*;
import com.drizzard.faq.util.*;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.block.Block;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

public class FlareAndQuests extends JavaPlugin implements Listener {

	Config conf;
	Config trans;
	Config playerData;

	public static final String G = ChatColor.GRAY + "";
	public static final String Y = ChatColor.YELLOW + "";

	public HashMap<Player, Location> left = new HashMap<Player, Location>();
	public HashMap<Player, Location> right = new HashMap<Player, Location>();
	HashMap<Player, RankQuest> QIP = new HashMap<Player, RankQuest>();
	//	HashMap<Player, WarzoneQuest> WQIP = new HashMap<>();
	HashMap<Player, String> playerFlares = new HashMap<>();


	HashMap<Block, BukkitTask> partTimers = new HashMap<Block, BukkitTask>();

	HashMap<Player, Integer> deathsLeft = new HashMap<Player, Integer>();

	HashMap<Inventory, Group<ItemStack, String>> anvils = new HashMap<Inventory, Group<ItemStack, String>>();

	public static String CBPATH;
	public static String NMSPATH;

	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);

		conf = ConfigDefaults.setConfigDefaults(this);
		trans = ConfigDefaults.setTranslationsDefaults(this);
		playerData = new Config(this, null, "players");

		String packageName = getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.indexOf(".v") + 2);
		CBPATH = "org.bukkit.craftbukkit.v" + version + ".";
		NMSPATH = "net.minecraft.server.v" + version + ".";

		registerCommands();

		RankQuest.loadTimedActions(this);
		FireworkUtil.loadFromConfig(this);
	}

	public void registerCommands() {
		getCommand("rq").setExecutor(new RQCommand(this));
		getCommand("faq").setExecutor(new FAQCommand(this));
		getCommand("flare").setExecutor(new FLARECommand(this));
		getCommand("witem").setExecutor(new WITEMCommand(this));
	}

//	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//		boolean isRQ = cmd.getName().equalsIgnoreCase("rq");
//		boolean isFLARE = cmd.getName().equalsIgnoreCase("flare");
//		boolean isWITEM = cmd.getName().equalsIgnoreCase("witem");
//		boolean isFAQ = cmd.getName().equalsIgnoreCase("faq"); // Done
//
//		if (isRQ || isFLARE || isWITEM || isFAQ) {
//			if (!sender.hasPermission("faq"))
//				sender.sendMessage(DR + "You need the permission " + R + "faq");
//			else if (args.length < 1)
//				help(sender, cmd.getName());
//			else if (!(sender instanceof Player) && !args[0].equalsIgnoreCase("give"))
//				sender.sendMessage(DR + "You must be a player.");
//			else if (args.length < 2) {
//				if (args[0].equalsIgnoreCase("wand")) {
//					ItemStack i = new ItemStack(Material.IRON_AXE);
//					ItemMeta im = i.getItemMeta();
//					im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Rank Quest Region Selector");
//					i.setItemMeta(im);
//					((Player) sender).getInventory().addItem(i);
//					((Player) sender).updateInventory();
//				} else if (args[0].equalsIgnoreCase("list")) {
//					String msg = "";
//					conf.load();
//					String sec;
//					if (isRQ) sec = "Quests";
//					else if (isFLARE) sec = "Flares";
//					else sec = "Witems";
//					if (conf.config.contains(sec))
//						for (String key : conf.config.getConfigurationSection(sec).getKeys(false))
//							msg += (msg.equals("") ? "" : G + ", ") + Y + key;
//					sender.sendMessage(G + sec + ": " + (msg.equals("") ? Y + "None" : msg));
//				} else if (args[0].equalsIgnoreCase("help"))
//					help(sender, cmd.getName());
//				else
//					sender.sendMessage(DR + "Missing arguments.");
//			} else {
//				conf.load();
//				if (args[0].equalsIgnoreCase("create")) {
//					Player player = (Player) sender;
//					if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
//						player.sendMessage(DR + "You must hold an item in your hand.");
//					else if (!player.getItemInHand().hasItemMeta())
//						player.sendMessage(DR + "You cannot use an unnamed/unlored item.");
//					else if (isRQ) {
//						// Start CREATE (RQ)
//						if (conf.config.contains("Quests." + args[1]))
//							player.sendMessage(DR + "There is already a quest named " + R + args[1]);
//						else {
//							conf.config.set("Quests." + args[1] + ".Activate", player.getItemInHand());
//							conf.save();
//							//player.getInventory().remove(player.getItemInHand());
//							player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
//							player.updateInventory();
//							player.sendMessage(G + "Successfully created a rank quest named " + Y + args[1]);
//							player.sendMessage(G + "Next step: select a region using " + Y + "/rq wand" + G + " and " + Y + "/rq setregion " + args[1]);
//						}
//						// End CREATE (RQ)
//					} else if (isFLARE) {
//						// Start CREATE (FLARE)
//						if (conf.config.contains("Flares." + args[1]))
//							player.sendMessage(DR + "There is already a flare named " + R + args[1]);
//						else {
//							conf.config.set("Flares." + args[1] + ".Activate", player.getItemInHand());
//							conf.save();
//							//player.getInventory().remove(player.getItemInHand());
//							player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
//							player.updateInventory();
//							player.sendMessage(G + "Successfully created a flare named " + Y + args[1]);
//							player.sendMessage(G + "Next step: set the items using " + Y + "/flare setinventory " + args[1]);
//						}
//						// End CREATE (FLARE)
//					} else if (isWITEM) {
//						// Start CREATE (WITEM)
//						if (conf.config.contains("Witems." + args[1]))
//							player.sendMessage(DR + "There is already a witem named " + R + args[1]);
//						else {
//							conf.config.set("Witems." + args[1] + ".Activate", player.getItemInHand());
//							conf.save();
//							//player.getInventory().remove(player.getItemInHand());
//							player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
//							player.updateInventory();
//							player.sendMessage(G + "Successfully created a witem named " + Y + args[1]);
//							player.sendMessage(G + "Next step: select a region using " + Y + "/rq wand" + G + " and " + Y + "/witem setregion " + args[1]);
//						}
//						// End CREATE (WITEM)
//					}
//				} else if (args[0].equalsIgnoreCase("delete")) {
//					Player player = (Player) sender;
//					if (isRQ) {
//						// Start DELETE (RQ)
//						if (!conf.config.contains("Quests." + args[1]))
//							player.sendMessage(DR + "There is no quest named " + R + args[1]);
//						else {
//							conf.config.set("Quests." + args[1], null);
//							conf.save();
//							player.sendMessage(G + "Successfully deleted " + Y + args[1]);
//						}
//						// End DELETE (RQ)
//					} else if (isFLARE) {
//						// Start DELETE (FLARE)
//						if (!conf.config.contains("Flares." + args[1]))
//							player.sendMessage(DR + "There is no flare named " + R + args[1]);
//						else {
//							conf.config.set("Flares." + args[1], null);
//							conf.save();
//							player.sendMessage(G + "Successfully deleted " + Y + args[1]);
//						}
//						// End DELETE (FLARE)
//					} else if (isWITEM) {
//						// Start DELETE (WITEM)
//						if (!conf.config.contains("Witems." + args[1]))
//							player.sendMessage(DR + "There is no witem named " + R + args[1]);
//						else {
//							conf.config.set("Witems." + args[1], null);
//							conf.save();
//							player.sendMessage(G + "Successfully deleted " + Y + args[1]);
//						}
//						// End DELETE (WITEM)
//					}
//				} else if (args[0].equalsIgnoreCase("listvcommands") || args[0].equalsIgnoreCase("listcommands")) {
//					String sec;
//					if (isRQ) sec = "Quests";
//					else if (isWITEM) sec = "Witems";
//					else return true;
//
//					if (!conf.config.contains(sec + "." + args[1]))
//						sender.sendMessage(DR + "Could not find " + R + args[1]);
//					else {
//						String msg = "";
//						conf.load();
//
//						List<String> cmds = conf.config.getStringList(sec + "." + args[1] + ".Commands");
//						if (cmds == null)
//							cmds = new ArrayList<String>();
//
//						for (String message : cmds)
//							msg += (msg.equals("") ? "" : G + ", ") + Y + ChatColor.translateAlternateColorCodes('&', message);
//						sender.sendMessage(ChatColor.GRAY + "Commands: " + msg);
//					}
//				} else {
//					if (isRQ && !conf.config.contains("Quests." + args[1]))
//						sender.sendMessage(DR + "There is no rank quest of the name " + R + args[1]);
//					else if (isRQ) {
//						if (args[0].equalsIgnoreCase("setvoucher")) {
//							Player player = (Player) sender;
//							// Start SETVOUCHER (RQ)
//							if (player.getItemInHand() == null)
//								player.sendMessage(DR + "You must hold an item in your hand.");
//							else {
//								conf.config.set("Quests." + args[1] + ".Voucher", player.getItemInHand());
//								conf.save();
//								player.getInventory().remove(player.getItemInHand());
//								player.updateInventory();
//								player.sendMessage(G + "Successfully set the voucher for " + Y + args[1]);
//								player.sendMessage(G + "Next step: set the rewards using " + Y + "/rq setvitems " + args[1]);
//							}
//							// End SETVOUCHER (RQ)
//						} else if (args[0].equalsIgnoreCase("setvitems")) {
//							Player player = (Player) sender;
//							// Start SETVITEMS (RQ)
//
//							List<ItemStack> items = (List<ItemStack>) conf.config.getList("Quests." + args[1] + ".Rewards", new ArrayList<ItemStack>());
//							Inventory inv = Bukkit.createInventory(player, 36, "Set Voucher Items For " + args[1]);
//							for (int i = 0; i < items.size(); i++)
//								inv.setItem(i, items.get(i));
//							player.openInventory(inv);
//							/*List<ItemStack> items = new ArrayList<ItemStack>();
//							for(ItemStack i : player.getInventory().getContents())
//								if(i != null && i.getType() != Material.AIR)
//									items.add(i);
//							conf.config.set("Quests."+args[1]+".Rewards", items);
//							conf.save();
//							player.sendMessage(G+"Successfully set the reward items for "+Y+args[1]);
//							player.sendMessage(G+"Next step: add reward commands using "+Y+"/rq addvcommand "+args[1]+" <message>");
//							*/
//							// End SETVITEMS (RQ)
//						} else if (args[0].equalsIgnoreCase("setregion")) {
//							Player player = (Player) sender;
//							// Start SETREGION (RQ)
//							Location f = left.get(player);
//							Location s = right.get(player);
//							if (f == null || s == null)
//								player.sendMessage(DR + "Please select a region first");
//							else {
//								conf.config.set("Quests." + args[1] + ".First", f);
//								conf.config.set("Quests." + args[1] + ".Second", s);
//								conf.save();
//								player.sendMessage(G + "Successfully set the region for " + Y + args[1]);
//								//38.47
//								player.sendMessage(G + "Next step: set the duration using " + Y + "/rq settime " + args[1] + " <seconds>");
//							}
//							// End SETREGION (RQ)
//						} else if (args[0].equalsIgnoreCase("settime")) {
//							Player player = (Player) sender;
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								// Start SETTIME (RQ)
//
//								int seconds = Integer.parseInt(args[2]);
//
//								conf.config.set("Quests." + args[1] + ".Duration", seconds);
//								conf.save();
//
//								player.sendMessage(G + "Successfully set the duration of " + Y + args[1] + G + " to " + Y + seconds + G + " seconds.");
//								player.sendMessage(G + "Next step: set the voucher item using " + Y + "/rq setvoucher " + args[1]);
//
//								// End SETTIME (RQ)
//							}
//						} else if (args[0].equalsIgnoreCase("addvcommand")) {
//							Player player = (Player) sender;
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								// Start ADDVCOMMAND (RQ)
//								List<String> cmds = conf.config.getStringList("Quests." + args[1] + ".Commands");
//								if (cmds == null)
//									cmds = new ArrayList<String>();
//								String newCMD = "";
//								for (int i = 2; i < args.length; i++)
//									newCMD += (i > 2 ? " " : "") + args[i];
//								cmds.add(newCMD);
//								conf.config.set("Quests." + args[1] + ".Commands", cmds);
//								conf.save();
//								player.sendMessage(G + "Successfully added " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " to the quest " + Y + args[1]);
//								player.sendMessage(G + "You're all done setting up the rank quest " + Y + args[1] + G + "!");
//								// End ADDVCOMMAND (RQ)
//							}
//						} else if (args[0].equalsIgnoreCase("delvcommand")) {
//							Player player = (Player) sender;
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								// Start DELVCOMMAND (RQ)
//								List<String> cmds = conf.config.getStringList("Quests." + args[1] + ".Commands");
//								if (cmds == null)
//									cmds = new ArrayList<String>();
//								String newCMD = "";
//								for (int i = 2; i < args.length; i++)
//									newCMD += (i > 2 ? " " : "") + args[i];
//								if (cmds.remove(newCMD)) {
//									conf.config.set("Quests." + args[1] + ".Commands", cmds);
//									conf.save();
//									player.sendMessage(G + "Successfully removed " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " from the quest " + Y + args[1]);
//								} else
//									player.sendMessage(DR + "Could not find the message " + R + "/" + ChatColor.translateAlternateColorCodes('&', newCMD));
//								// End DELVCOMMAND (RQ)
//							}
//						} else if (args[0].equalsIgnoreCase("give")) {
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								// Start GIVE (RQ)
//								Player target = Bukkit.getPlayer(args[2]);
//								if (target == null)
//									sender.sendMessage(DR + "Could not find " + R + args[2]);
//								else {
//									target.getInventory().addItem(conf.config.getItemStack("Quests." + args[1] + ".Activate"));
//									target.updateInventory();
//									sender.sendMessage(G + "Gave the quest " + Y + args[1] + G + " to " + Y + args[2]);
//								}
//								// End GIVE (RQ)
//							}
//						}
//					} else if (isFLARE) {
//						if (!conf.config.contains("Flares." + args[1]))
//							sender.sendMessage(DR + "There is no flare of the name " + R + args[1]);
//						else if (args[0].equalsIgnoreCase("setinventory")) {
//							Player player = (Player) sender;
//							// Start SETINVENTORY (FLARE)
//
//							openFlareInventory(player, args[1]);
//
//							/*List<ItemStack> conts = new ArrayList<ItemStack>();
//							ItemStack[] iconts = player.getInventory().getStorageContents();
//							for(int i = 0; i < 36; i++)
//								if(iconts[i] != null && iconts[i].getType() != Material.AIR)
//									conts.add(iconts[i]);
//							if(conts.size() > 27)
//								player.sendMessage(DR+"The chest can only hold 27 items, you have "+R+conts.size());
//							else {
//								conf.config.set("Flares."+args[1]+".Contents", conts);
//								conf.save();
//								player.sendMessage(G+"Successfully set the inventory of "+Y+args[1]);
//								player.sendMessage(G+"You're all done setting up the flare "+Y+args[1]+G+"!");
//							}*/
//							// End SETINVENTORY (FLARE)
//						} else if (args[0].equalsIgnoreCase("give")) {
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								// Start GIVE (FLARE)
//								Player target = Bukkit.getPlayer(args[2]);
//								if (target == null)
//									sender.sendMessage(DR + "Could not find " + R + args[2]);
//								else {
//									target.getInventory().addItem(conf.config.getItemStack("Flares." + args[1] + ".Activate"));
//									target.updateInventory();
//									sender.sendMessage(G + "Gave the flare " + Y + args[1] + G + " to " + Y + args[2]);
//								}
//								// End GIVE (FLARE)
//							}
//						}
//					} else if (isWITEM) {
//						if (!conf.config.contains("Witems." + args[1]))
//							sender.sendMessage(DR + "There is no witem of the name " + R + args[1]);
//						else if (args[0].equalsIgnoreCase("setregion")) {
//							Player p = (Player) sender;
//							Location f = left.get(p);
//							Location s = right.get(p);
//							if (f == null || s == null)
//								p.sendMessage(DR + "Please select a region first");
//							else {
//								conf.config.set("Witems." + args[1] + ".First", f);
//								conf.config.set("Witems." + args[1] + ".Second", s);
//								conf.save();
//								p.sendMessage(G + "Successfully set the region for " + Y + args[1]);
//								p.sendMessage(G + "Next step: add a message using " + Y + "/witem addcommand " + args[1] + " <message>");
//							}
//						} else if (args[0].equalsIgnoreCase("addcommand")) {
//							Player player = (Player) sender;
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								List<String> cmds = conf.config.getStringList("Witems." + args[1] + ".Commands");
//								if (cmds == null)
//									cmds = new ArrayList<String>();
//								String newCMD = "";
//								for (int i = 2; i < args.length; i++)
//									newCMD += (i > 2 ? " " : "") + args[i];
//								cmds.add(newCMD);
//								conf.config.set("Witems." + args[1] + ".Commands", cmds);
//								conf.save();
//								player.sendMessage(G + "Successfully added " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " to the witem " + Y + args[1]);
//								player.sendMessage(G + "You're all done setting up the witem " + Y + args[1] + G + "!");
//							}
//						} else if (args[0].equalsIgnoreCase("delcommand")) {
//							Player player = (Player) sender;
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								List<String> cmds = conf.config.getStringList("Witems." + args[1] + ".Commands");
//								if (cmds == null)
//									cmds = new ArrayList<String>();
//								String newCMD = "";
//								for (int i = 2; i < args.length; i++)
//									newCMD += (i > 2 ? " " : "") + args[i];
//								if (cmds.remove(newCMD)) {
//									conf.config.set("Witems." + args[1] + ".Commands", cmds);
//									conf.save();
//									player.sendMessage(G + "Successfully removed " + Y + "/" + ChatColor.translateAlternateColorCodes('&', newCMD) + G + " from the witem " + Y + args[1]);
//								} else
//									player.sendMessage(DR + "Could not find the message " + R + "/" + ChatColor.translateAlternateColorCodes('&', newCMD));
//
//							}
//						} else if (args[0].equalsIgnoreCase("give")) {
//							if (args.length < 3)
//								sender.sendMessage(DR + "Missing arguments.");
//							else {
//								Player target = Bukkit.getPlayer(args[2]);
//								if (target == null)
//									sender.sendMessage(DR + "Could not find " + R + args[2]);
//								else {
//									target.getInventory().addItem(conf.config.getItemStack("Witems." + args[1] + ".Activate"));
//									target.updateInventory();
//									sender.sendMessage(G + "Gave the witem " + Y + args[1] + G + " to " + Y + args[2]);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return true;
//	}

	public void openFlareInventory(Player player, String name) {
		List<ItemStack> items = (List<ItemStack>) conf.config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
		Inventory inv = Bukkit.createInventory(player, 27, "Set Flare Items For " + name);
		for (int i = 0; i < items.size(); i++)
			inv.setItem(i, items.get(i));
		player.openInventory(inv);
	}

	public void spawnParticle(int part, Location loc, double offX, double offY, double offZ, int count, Collection<Player> ents) {
		try {
			Class PPOP = Class.forName(NMSPATH + "PacketPlayOutWorldParticles");
			Class EPART = Class.forName(NMSPATH + "EnumParticle");
			Constructor PPOPCONSTRUCTOR = PPOP.getConstructor(EPART, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);
			Object enumParticle = EPART.getEnumConstants()[part];

			Class P = Class.forName(NMSPATH + "Packet");
			Object packet = P.cast(PPOPCONSTRUCTOR.newInstance(EPART.cast(enumParticle), true, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), (float) offX, (float) offY, (float) offZ, 0f, count, new int[0]));

			Class CP = Class.forName(CBPATH + "entity.CraftPlayer");

			Class PC = Class.forName(NMSPATH + "PlayerConnection");
			Class EP = Class.forName(NMSPATH + "EntityPlayer");

			Field field = EP.getField("playerConnection");
			Method getHandle = CP.getMethod("getHandle");
			Method sendPacket = PC.getMethod("sendPacket", P);

			for (Player ent : ents)
				sendPacket.invoke(field.get(getHandle.invoke(CP.cast(ent))), packet);
		} catch (Exception e) {
			getLogger().info(ExceptionUtils.getStackTrace(e));
		}
		
		/*PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(part, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), (float)offX, (float)offY, (float)offZ, 0f, 1);
		for(Entity ent : ents)
			if(ent instanceof Player)
				((CraftPlayer)ent).getHandle().playerConnection.sendPacket(packet);*/

	}

	public void startLull(final Player player) {
		int dA = conf.config.getInt("Deaths Allowed For Keep-Inv");
		int dD = conf.config.getInt("Keep-Inv Duration");

		player.sendMessage(getTrans().format("Keep-Inventory Start Message", player, dD + "", dA + ""));
		//player.sendMessage(G+"You now have "+Y+dD+G+" seconds or "+Y+dA+G+" deaths of keep-inventory.");

		deathsLeft.put(player, dA);
		new BukkitRunnable() {
			public void run() {
				if (deathsLeft.containsKey(player)) {
					deathsLeft.remove(player);

					player.sendMessage(getTrans().format("Keep-Inventory Expire Message", player));

					//player.sendMessage(G+"Your keep-inventory period has expired.");
				}
			}
		}.runTaskLater(this, dD * 20);

		final int[] t = {dD};

		new BukkitRunnable() {
			public void run() {
				if (!deathsLeft.containsKey(player) || t[0] < 0) {
					this.cancel();
					return;
				}

				String message = ChatColor.translateAlternateColorCodes('&', trans.config.getString("Keep-Inventory Actionbar Message")).replace("{left}", t[0] + "");
				if (!message.toLowerCase().equals("none"))
					ActionBar.sendActionBar(player, message);
				t[0]--;
			}
		}.runTaskTimer(this, 0, 20);
	}

	public boolean inside(Location loc, Location one, Location two) {

		if (serverHasFactions()) {
			return isWarzone(loc);
		} else {

			if (one == null || two == null)
				return false;

			boolean worlds = loc.getWorld().equals(one.getWorld()) && loc.getWorld().equals(two.getWorld());
		
		/*boolean x = Math.signum(loc.getX()-one.getX()) == -1 * Math.signum(loc.getX()-two.getX());
		boolean y = Math.signum(loc.getY()-one.getY()) == -1 * Math.signum(loc.getY()-two.getY());
		boolean z = Math.signum(loc.getZ()-one.getZ()) == -1 * Math.signum(loc.getZ()-two.getZ());*/
			boolean x = inside1D(loc.getX(), one.getBlockX(), two.getBlockX());
			boolean y = inside1D(loc.getY(), one.getBlockY(), two.getBlockY());
			boolean z = inside1D(loc.getZ(), one.getBlockZ(), two.getBlockZ());
			return worlds && x && y && z;
		}
	}

	private boolean inside1D(double a, double b, double c) {
		return (b <= c && d(s(b - a), s(c + 1 - a))) || (b > c && d(s(b + 1 - a), s(c - a)));
	}

	private boolean d(double x, double y) {
		return x == 0 || y == 0 || x == -1 * y;
	}

	private double s(double d) {
		return Math.signum(d);
	}

	public Config getConf() {
		return conf;
	}

	public Config getTrans() {
		return trans;
	}

	public Config getPlayerData() {
		return playerData;
	}

	public boolean isWarzone(Location loc) {
		if (serverHasFactions()) {
			Faction f = Board.getInstance().getFactionAt(new FLocation(loc));
			return f != null && f.isWarZone();
		}
		return true;
	}

	public boolean serverHasFactions() {
		return getServer().getPluginManager().getPlugin("Factions") != null;
	}

	public boolean playerIsActive(Player p) {
		return QIP.containsKey(p) || deathsLeft.containsKey(p) || playerFlares.containsKey(p);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent ev) {
		if (deathsLeft.containsKey(ev.getEntity())) {
			int num = deathsLeft.get(ev.getEntity());
			if (num > 0)
				ev.setKeepInventory(true);
			num--;
			if (num <= 0) {
				deathsLeft.remove(ev.getEntity());

				ev.getEntity().sendMessage(getTrans().format("Keep-Inventory Expire Message", null, ev.getEntity()));
			} else
				deathsLeft.put(ev.getEntity(), num);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent ev) {
		String t = ev.getInventory().getTitle();
		boolean a = t.contains("Set Voucher Items For ");
		boolean b = t.contains("Set Flare Items For ");
		if (a || b) {
			conf.load();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (ItemStack i : ev.getInventory().getContents())
				if (i != null && i.getType() != Material.AIR)
					items.add(i);
			String name = t.substring(t.indexOf("For ") + 4);
			conf.config.set((a ? "Quests." : "Flares.") + name + (a ? ".Rewards" : ".Contents"), items);
			conf.save();

			if (a) {
				ev.getPlayer().sendMessage(G + "Successfully set the reward items for " + Y + name);
				ev.getPlayer().sendMessage(G + "Next step: add reward commands using " + Y + "/rq addvcommand " + name + " <message>");
			} else {
				ev.getPlayer().sendMessage(G + "Successfully set the inventory of " + Y + name);
				ev.getPlayer().sendMessage(G + "You're all done setting up the flare " + Y + name + G + "!");
			}
		} else if (ev.getInventory() instanceof AnvilInventory && anvils.containsKey(ev.getInventory())) {
			AnvilInventory inv = (AnvilInventory) ev.getInventory();
			inv.setContents(new ItemStack[3]);
			anvils.remove(ev.getInventory());
		}
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent ev) {
		if (ev.getInventory().getHolder() instanceof Block) {
			BukkitTask t = partTimers.remove(((Block) ev.getInventory().getHolder()).getLocation());
			if (t != null)
				t.cancel();
		}
	}

	@EventHandler
	public void onInvClick(final InventoryClickEvent ev) {
		if (ev.getInventory().getTitle().contains("Set Flare Items For ")) {
			ItemStack cursor = ev.getCursor();
			ItemStack clicked = ev.getCurrentItem();
			boolean clBAD = clicked == null || clicked.getType() == Material.AIR;
			boolean cuBAD = cursor == null || cursor.getType() == Material.AIR;

			int slot = ev.getRawSlot();

			final ItemStack target;
			if (ev.getClick() == ClickType.SHIFT_LEFT && slot >= 27 && !clBAD)
				target = clicked;
			else if (slot < 27 && !cuBAD)
				target = cursor;
			else
				target = null;

			if (target != null) {
				final ItemStack targetClone = target.clone();
				String t = ev.getInventory().getTitle();
				final String name = t.substring(t.indexOf("For ") + 4);

				new BukkitRunnable() {
					public void run() {
						FAnvil anvil = new FAnvil(ev.getWhoClicked());
						anvil.a("Set Chance");

						ItemMeta im = targetClone.getItemMeta();
						List<String> lore = im.getLore();
						double chance = 100.0;
						if (lore != null) {
							String str = ChatColor.stripColor(lore.get(lore.size() - 1));
							try {
								chance = Double.parseDouble(str);
								lore.remove(lore.size() - 1);
								im.setLore(lore);
								targetClone.setItemMeta(im);
							} catch (Exception e) {
								chance = 100;
							}
						}

						ItemStack tag = new ItemStack(Material.NAME_TAG);
						im = tag.getItemMeta();
						im.setDisplayName(chance + "");
						tag.setItemMeta(im);

						anvil.setItem(0, CraftItemStack.asNMSCopy(tag));
						EntityPlayer ep = ((CraftPlayer) ev.getWhoClicked()).getHandle();
						int containerId = ep.nextContainerCounter();
						ep.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage("Set Chance", new Object[]{}), 0));
						ep.activeContainer = anvil;
						ep.activeContainer.windowId = containerId;
						ep.activeContainer.addSlotListener(ep);

						anvils.put(ev.getWhoClicked().getOpenInventory().getTopInventory(), new Group<ItemStack, String>(targetClone, name));

						//ev.getWhoClicked().openInventory(Bukkit.createInventory(null, InventoryType.ANVIL, "Set Chance"));
					}
				}.runTaskLater(this, 1);
			}
		} else if (ev.getInventory() instanceof AnvilInventory) {
			ItemStack clicked = ev.getCurrentItem();
			if (clicked == null || ev.getRawSlot() > 3)
				return;
			Group<ItemStack, String> g = anvils.remove(ev.getInventory());
			if (g == null)
				return;

			ev.setCancelled(true);

			ItemStack target = g.a;
			String name = g.b;

			ItemMeta im = target.getItemMeta();
			List<String> lore = im.getLore();
			if (lore == null)
				lore = new ArrayList<String>();
			lore.add(clicked.getItemMeta().getDisplayName());
			im.setLore(lore);
			target.setItemMeta(im);

			conf.load();
			List<ItemStack> items = (List<ItemStack>) conf.config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
			items.add(target);
			conf.config.set("Flares." + name + ".Contents", items);
			conf.save();

			ev.getInventory().setContents(new ItemStack[3]);

			openFlareInventory((Player) ev.getWhoClicked(), name);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(PlayerInteractEvent ev) {
		String disp = ItemStacks.getDisplayName(ev.getItem());

		if (ev.getClickedBlock() != null) {
			BukkitTask t = partTimers.remove(ev.getClickedBlock());
			if (t != null)
				t.cancel();
		}

		if (disp.equals("Rank Quest Region Selector")) {
			ev.setCancelled(true);
			if (ev.getAction() == Action.LEFT_CLICK_BLOCK) {
				Location loc = ev.getClickedBlock().getLocation();
				left.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G + "Set first position to " + Y + loc.getBlockX() + G + ", " + Y + loc.getBlockY() + G + ", " + Y + loc.getBlockZ() + G + " in " + Y + loc.getWorld().getName());
			} else if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Location loc = ev.getClickedBlock().getLocation();
				right.put(ev.getPlayer(), loc);
				ev.getPlayer().sendMessage(G + "Set second position to " + Y + loc.getBlockX() + G + ", " + Y + loc.getBlockY() + G + ", " + Y + loc.getBlockZ() + G + " in " + Y + loc.getWorld().getName());
			}
		} else {
			if (ev.getItem() != null) {
				conf.load();
				if (conf.config.contains("Flares")) {
					for (String key : conf.config.getConfigurationSection("Flares").getKeys(false)) {
						if (ItemStacks.stackIsSimilar(ev.getItem(), conf.config.getItemStack("Flares." + key + ".Activate"), false)) {
							// Matched to flare
							ev.setCancelled(true);

							if (playerIsActive(ev.getPlayer())) {
								ev.getPlayer().sendMessage(getTrans().format("Cannot Activate Flare While Doing Other Function", null, ev.getPlayer()));
								return;
							}

							int minPlayers = getConf().config.getInt("minimum-players.flare");
							if (getServer().getOnlinePlayers().size() < minPlayers) {
								ev.getPlayer().sendMessage(getTrans().format("Not Enough Players", minPlayers + ""));
								return;
							}

							if (!isWarzone(ev.getPlayer().getLocation())) {
								ev.getPlayer().sendMessage(getTrans().format("Not in Warzone Message", null, ev.getPlayer()));
							} else
								Flare.activateFlare(ev.getItem(), ev.getPlayer(), this, key);
							return;
						}
					}
				}
				if (conf.config.contains("Witems")) {
					for (String key : conf.config.getConfigurationSection("Witems").getKeys(false)) {
						if (ItemStacks.stackIsSimilar(ev.getItem(), conf.config.getItemStack("Witems." + key + ".Activate"), false)) {
							//Matched to Witem
							ev.setCancelled(true);

							int minPlayers = getConf().config.getInt("minimum-players.witem");
							if (getServer().getOnlinePlayers().size() < minPlayers) {
								ev.getPlayer().sendMessage(getTrans().format("Not Enough Players", minPlayers + ""));
								return;
							}

							if (playerIsActive(ev.getPlayer())) {
								ev.getPlayer().sendMessage(getTrans().format("Cannot Activate Witem While Doing Other Function", null, ev.getPlayer()));
								return;
							}


							if (serverHasFactions()) {
								if (!isWarzone(ev.getPlayer().getLocation())) {
									String[] message = getTrans().format("Not in Warzone Message", ev.getPlayer().getLocation(), ev.getPlayer());
									ev.getPlayer().sendMessage(message);
									return;
								}
							} else if (!inside(ev.getPlayer().getLocation(), (Location) conf.config.get("Witems." + key + ".First"), (Location) conf.config.get("Witems." + key + ".Second"))) {
								ev.getPlayer().sendMessage(getTrans().format("Not in Region Message", null, ev.getPlayer()));

								return;

								//ev.getPlayer().sendMessage(DR+"You must be inside the proper region!");
							}

							if (ev.getItem().getAmount() == 1) {
								ev.getPlayer().getInventory().setItem(ev.getPlayer().getInventory().getHeldItemSlot(), null);
							} else {
								ev.getItem().setAmount(ev.getItem().getAmount() - 1);
							}

							ev.getPlayer().updateInventory();

							SoundUtil.playWitemUseSound(this, ev.getPlayer());

							if (conf.config.contains("Witems." + key + ".Commands")) {
								for (String str : conf.config.getStringList("Witems." + key + ".Commands")) {
									this.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", ev.getPlayer().getName()));
								}
							}
						}
					}
				}
				if (conf.config.contains("Quests")) {
					for (String key : conf.config.getConfigurationSection("Quests").getKeys(false)) {
						ItemStack citem = conf.config.getItemStack("Quests." + key + ".Activate");

						if (ItemStacks.stackIsSimilar(ev.getItem(), citem, false)) {
							// Matched to Rank Quest
							ev.setCancelled(true);

							int minPlayers = getConf().config.getInt("minimum-players.rq");
							if (getServer().getOnlinePlayers().size() < minPlayers) {
								ev.getPlayer().sendMessage(getTrans().format("Not Enough Players", minPlayers + ""));
								return;
							}

							if (playerIsActive(ev.getPlayer())) {
								ev.getPlayer().sendMessage(getTrans().format("Cannot Activate Rank Quest While Doing Other Function", null, ev.getPlayer()));
								return;
							}

							if (ev.getItem().getAmount() > 1) {
								ev.getPlayer().sendMessage(getTrans().format("Cannot Activate Stacked Rank Quests Message", null, ev.getPlayer()));
							} else if (QIP.containsKey(ev.getPlayer())) {
								ev.getPlayer().sendMessage(getTrans().format("Already Doing Quest Message", null, ev.getPlayer()));
							} else if (!inside(ev.getPlayer().getLocation(), (Location) conf.config.get("Quests." + key + ".First"), (Location) conf.config.get("Quests." + key + ".Second"))) {
								if (serverHasFactions()) {
									String[] message = getTrans().format("Not in Warzone Message", null, ev.getPlayer());
									ev.getPlayer().sendMessage(message);
								} else {
									ev.getPlayer().sendMessage(getTrans().format("Not in Region Message", null, ev.getPlayer()));
								}

								//ev.getPlayer().sendMessage(DR+"You must be inside the proper region!");
							} else if (deathsLeft.containsKey(ev.getPlayer())) {
								ev.getPlayer().sendMessage(getTrans().format("Cannot Activate While in Keep Inv Message", null, ev.getPlayer()));
							} else {
								QIP.put(ev.getPlayer(), new RankQuest(ev.getPlayer().getInventory().getHeldItemSlot(), ev.getPlayer(), conf.config.getInt("Quests." + key + ".Duration"), this, key));
							}
							return;
						} else if (ItemStacks.stackIsSimilar(ev.getItem(), conf.config.getItemStack("Quests." + key + ".Voucher"), false)) {
							// Matched to Voucher
							ev.setCancelled(true);
							PlayerInventory inv = ev.getPlayer().getInventory();

							if (ev.getItem().getAmount() == 1)
								inv.setItem(inv.getHeldItemSlot(), null);
							else
								ev.getItem().setAmount(ev.getItem().getAmount() - 1);

							for (ItemStack i : (List<ItemStack>) conf.config.getList("Quests." + key + ".Rewards", new ArrayList<ItemStack>()))
								inv.addItem(i);
							ev.getPlayer().updateInventory();
							if (conf.config.contains("Quests." + key + ".Commands"))
								for (String str : conf.config.getStringList("Quests." + key + ".Commands"))
									this.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", ev.getPlayer().getName()));
						}
					}
//					if (serverHasFactions() && conf.config.contains("WQuests")) {
//						for (String key : conf.config.getConfigurationSection("WQuests").getKeys(false)) {
//							ItemStack citem = conf.config.getItemStack("WQuests." + key + ".Activate");
//
//							if (ItemStacks.stackIsSimilar(ev.getItem(), citem, false)) {
//								// Matched to Warzone Quest
//								ev.setCancelled(true);
//
//								int minPlayers = getConf().config.getInt("WRQ Minimum Players");
//								if (getServer().getOnlinePlayers().size() < minPlayers) {
//									ev.getPlayer().sendMessage(getTrans().format("Not Enough Players", minPlayers + ""));
//									return;
//								}
//
//								if (playerIsActive(ev.getPlayer())) {
//									ev.getPlayer().sendMessage(getTrans().format("Cannot Activate Warzone Quest While Doing Other Function", null, ev.getPlayer()));
//									return;
//								}
//
//								if (!isWarzone(ev.getPlayer().getLocation())) {
//									String[] message = getTrans().format("Not in Warzone Message", null, ev.getPlayer());
//									ev.getPlayer().sendMessage(message);
//								} else if (ev.getItem().getAmount() > 1) {
//									String[] message = getTrans().format("Cannot Activate Stacked Warzone Quests Message", null, ev.getPlayer());
//									ev.getPlayer().sendMessage(message);
//								} else if (WQIP.containsKey(ev.getPlayer())) {
//									String[] message = getTrans().format("Already Doing Warzone Quest Message", null, ev.getPlayer());
//									ev.getPlayer().sendMessage(message);
//
//									//ev.getPlayer().sendMessage(DR+"You are already doing a rank quest!");
//								} else if (deathsLeft.containsKey(ev.getPlayer())) {
//									ev.getPlayer().sendMessage(getTrans().format("Cannot Activate While in Keep Inv Message", null, ev.getPlayer()));
//								} else
//									WQIP.put(ev.getPlayer(), new WarzoneQuest(ev.getPlayer().getInventory().getHeldItemSlot(), ev.getPlayer(), conf.config.getInt("WQuests." + key + ".Duration"), this, key));
//								return;
//							} else if (ItemStacks.stackIsSimilar(ev.getItem(), conf.config.getItemStack("WQuests." + key + ".Voucher"), false)) {
//								// Matched to Voucher
//								ev.setCancelled(true);
//								PlayerInventory inv = ev.getPlayer().getInventory();
//
//								if (ev.getItem().getAmount() == 1)
//									inv.setItem(inv.getHeldItemSlot(), null);
//								else
//									ev.getItem().setAmount(ev.getItem().getAmount() - 1);
//
//								for (ItemStack i : (List<ItemStack>) conf.config.getList("WQuests." + key + ".Rewards", new ArrayList<ItemStack>()))
//									inv.addItem(i);
//								ev.getPlayer().updateInventory();
//								if (conf.config.contains("WQuests." + key + ".Commands"))
//									for (String str : conf.config.getStringList("WQuests." + key + ".Commands"))
//										this.getServer().dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', str).replace("{player}", ev.getPlayer().getName()));
//							}
//						}
//					}
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent ev) {
		if (playerFlares.containsKey(ev.getPlayer())) {
			playerData.config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", playerFlares.get(ev.getPlayer()));
			playerData.save();
			playerFlares.remove(ev.getPlayer());
		} else {
			playerData.config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", null);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		String flare = playerData.config.getString("players." + ev.getPlayer().getUniqueId().toString() + ".flare", "");
		if (!flare.isEmpty()) {
			// This is easier than rewriting the code to give a flare to a player
			getCommand("flare").getExecutor().onCommand(getServer().getConsoleSender(), getCommand("flare"), "flare",
					new String[]{"give", flare, ev.getPlayer().getName()});
			ev.getPlayer().sendMessage(getTrans().format("Flare Given Upon Join Message", null, ev.getPlayer()));
			playerData.config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", null);
			playerData.save();
		}

		ev.getPlayer().setMaxHealth(20d);
	}

}

class Group<K, V> {
	public K a;
	public V b;

	public Group(K a, V b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object other) {
		Group ot = ((Group) other);
		return (a.equals(ot.a) && b.equals(ot.b)) || (a.equals(ot.b) && b.equals(ot.a));
	}

	@Override
	public int hashCode() {
		return 0;
	}
}

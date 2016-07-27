package com.drizzard.faq;

import com.drizzard.faq.commands.FAQCommand;
import com.drizzard.faq.commands.FLARECommand;
import com.drizzard.faq.commands.RQCommand;
import com.drizzard.faq.commands.WITEMCommand;
import com.drizzard.faq.nms.anvil.AnvilSender;
import com.drizzard.faq.util.ActionBar;
import com.drizzard.faq.util.FireworkUtil;
import com.drizzard.faq.util.ItemStacks;
import com.drizzard.faq.util.SoundUtil;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FlareAndQuests extends JavaPlugin implements Listener {

	public static final String G = ChatColor.GRAY + "";
	public static final String Y = ChatColor.YELLOW + "";
	public static String CBPATH;
	public static String NMSPATH;
	public HashMap<Player, Location> left = new HashMap<Player, Location>();
	public HashMap<Player, Location> right = new HashMap<Player, Location>();
	Config conf;
	Config trans;
	Config playerData;
	HashMap<Player, RankQuest> QIP = new HashMap<Player, RankQuest>();
	HashMap<Player, String> playerFlares = new HashMap<Player, String>();
	HashMap<Block, BukkitTask> partTimers = new HashMap<Block, BukkitTask>();
	HashMap<Player, Integer> deathsLeft = new HashMap<Player, Integer>();
	HashMap<Inventory, Group<ItemStack, String>> anvils = new HashMap<Inventory, Group<ItemStack, String>>();

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
			try {
				// Attempt to use MassiveCraft Factions

				// Such reflection
				// Much wow
				Class boardColl = Class.forName("com.massivecraft.factions.entity.BoardColl");
				Class factionColl = Class.forName("com.massivecraft.factions.entity.FactionColl");
				Class faction = Class.forName("com.massivecraft.factions.entity.Faction");
				Class pS = Class.forName("com.massivecraft.massivecore.ps.PS");
				Object boardCollInstance = boardColl.getMethod("get", null).invoke(null, null);
				Object factionCollInstance = factionColl.getMethod("get", null).invoke(null, null);
				Object factionInstance = boardColl.getMethod("getFactionAt", pS).invoke(boardCollInstance, pS.getMethod("valueOf", Location.class).invoke(null, loc));
				Object warzoneFaction = factionColl.getMethod("getWarzone", null).invoke(factionCollInstance, null);

				Method getNameMethod = faction.getMethod("getName", null);

				return factionInstance != null && warzoneFaction != null && getNameMethod.invoke(factionInstance, null).equals(getNameMethod.invoke(warzoneFaction, null));

			} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				getLogger().info("Error trying to use MassiveCraft Factions");
				e.printStackTrace();
				getLogger().info("Attempting to use drtschock Factions");
				// Attempt to use drtshock Factions
				Faction f = Board.getInstance().getFactionAt(new FLocation(loc));
				return f != null && f.isWarZone();
			}
		}
		return true;
	}

	public boolean serverHasFactions() {
		return getServer().getPluginManager().getPlugin("Factions") != null && !conf.config.getBoolean("always-use-regions", false);
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
			inv.setContents(new ItemStack[2]);
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

						AnvilSender.send(ev.getWhoClicked(), "Set Chance", tag);

						anvils.put(ev.getWhoClicked().getOpenInventory().getTopInventory(), new Group<ItemStack, String>(targetClone, name));
					}
				}.runTaskLater(this, 1);
			}
		} else if (ev.getInventory() instanceof AnvilInventory) {
			ItemStack clicked = ev.getCurrentItem();

			if (clicked == null || ev.getRawSlot() > 3) {
				return;
			}

			if (clicked.getType().equals(Material.AIR)) {
				return;
			}

			Group<ItemStack, String> g = anvils.remove(ev.getInventory());
			if (g == null)
				return;

			ev.setCancelled(true);

			ItemStack target = g.a;
			String name = g.b;

			ItemMeta im = target.getItemMeta();
			List<String> lore = im.getLore();
			if (lore == null) {
				lore = new ArrayList<String>();
			}

			if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
				lore.add(clicked.getItemMeta().getDisplayName());
			} else {
				lore.add("100");
			}

			im.setLore(lore);
			target.setItemMeta(im);

			conf.load();
			List<ItemStack> items = (List<ItemStack>) conf.config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
			items.add(target);
			conf.config.set("Flares." + name + ".Contents", items);
			conf.save();

			ev.getInventory().setContents(new ItemStack[2]);

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

		if (disp.equals("FAQ Region Selector")) {
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
			if (ev.getAction().equals(Action.LEFT_CLICK_AIR) || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				return;
			}

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

							if (!inside(ev.getPlayer().getLocation(), (Location) conf.config.get("Flares." + key + ".First"), (Location) conf.config.get("Flares." + key + ".Second"))) {
								if (serverHasFactions()) {
									String[] message = getTrans().format("Not in Warzone Message", ev.getPlayer().getLocation(), ev.getPlayer());
									ev.getPlayer().sendMessage(message);
								} else {
									ev.getPlayer().sendMessage(getTrans().format("Not in Region Message", null, ev.getPlayer()));
								}
								return;
							} else {
								Flare.activateFlare(ev.getItem(), ev.getPlayer(), this, key);
							}
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


							if (!inside(ev.getPlayer().getLocation(), (Location) conf.config.get("Witems." + key + ".First"), (Location) conf.config.get("Witems." + key + ".Second"))) {
								if (serverHasFactions()) {
									String[] message = getTrans().format("Not in Warzone Message", ev.getPlayer().getLocation(), ev.getPlayer());
									ev.getPlayer().sendMessage(message);
								} else {
									ev.getPlayer().sendMessage(getTrans().format("Not in Region Message", null, ev.getPlayer()));
								}

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

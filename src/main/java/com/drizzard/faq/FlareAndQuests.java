package com.drizzard.faq;

import com.drizzard.faq.commands.*;
import com.drizzard.faq.listeners.*;
import com.drizzard.faq.util.ActionBar;
import com.drizzard.faq.util.FireworkUtil;
import com.drizzard.faq.util.Group;
import com.drizzard.faq.util.ItemStacks;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
	Config mysteryMobSpawners;
	HashMap<Player, RankQuest> QIP = new HashMap<Player, RankQuest>();
	HashMap<Player, String> playerFlares = new HashMap<Player, String>();
	HashMap<UUID, Flare> fallingFlares = new HashMap<>();
	HashMap<Block, BukkitTask> partTimers = new HashMap<Block, BukkitTask>();
	HashMap<Player, Integer> deathsLeft = new HashMap<Player, Integer>();
	HashMap<Inventory, Group<ItemStack, String>> flareAnvils = new HashMap<Inventory, Group<ItemStack, String>>();
	HashMap<Inventory, String> mmAnvils = new HashMap<>();

	private static int getInventorySize(int numberOfItems) {
		int rows = (int) Math.ceil(((double) numberOfItems / 9.0));
		return Math.min(rows * 9, 54);
	}

	public void onEnable() {
		registerEvents();

		conf = ConfigDefaults.setConfigDefaults(this);
		trans = ConfigDefaults.setTranslationsDefaults(this);
		playerData = new Config(this, null, "players");
		mysteryMobSpawners = ConfigDefaults.setMysteryMobDefaults(this);

		checkSpawners();

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
		getCommand("mm").setExecutor(new MMCommand(this));
		getCommand("glow").setExecutor(new GlowCommand());
	}

	public void registerEvents() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
		this.getServer().getPluginManager().registerEvents(new ActivationListener(this), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		this.getServer().getPluginManager().registerEvents(new EntityListener(this), this);
	}

	/**
	 * Runs through all the spawners configured in spawners.yml to make sure all
	 * of them are linked to a known mob.
	 */
	public void checkSpawners() {
		for (String key : mysteryMobSpawners.config.getConfigurationSection("spawners").getKeys(false)) {
			try {
				EntityType.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				getLogger().warning("Could not find mob \"" + key.toLowerCase() + "\"");
			}
		}
	}

	public void openFlareInventory(Player player, String name) {
		List<ItemStack> items = (List<ItemStack>) conf.config.getList("Flares." + name + ".Contents", new ArrayList<ItemStack>());
		Inventory inv = Bukkit.createInventory(player, 27, "Set Flare Items For " + name);
		for (int i = 0; i < items.size(); i++)
			inv.setItem(i, items.get(i));
		player.openInventory(inv);
	}

	public void openMysteryMobInventory(Player p, String name) {
		getMysteryMobSpawners().load();
		getConf().load();
		List<String> keys = new ArrayList<>(getMysteryMobSpawners().config.getConfigurationSection("spawners").getKeys(false));
		Collections.sort(keys);
		Inventory inv = Bukkit.createInventory(null, getInventorySize(keys.size()), "Select Spawners For " + name);

		for (String key : keys) {
			// Checking to ensure that only known mobs are listed.
			try {
				EntityType type = EntityType.valueOf(key.toUpperCase());

				ItemStack item;
				if (getConf().config.contains("MysteryMobs." + name + ".spawners." + type.name().toLowerCase())) {
					String chance = getConf().config.getString("MysteryMobs." + name + ".spawners." + type.name().toLowerCase());
					item = ItemStacks.generateStack(Material.STAINED_GLASS_PANE,
							getMysteryMobSpawners().config.getString("spawners." + key + ".display_name"),
							1, (short) 13, Arrays.asList(type.name(), chance));
				} else {
					item = ItemStacks.generateStack(Material.MOB_SPAWNER,
							getMysteryMobSpawners().config.getString("spawners." + key + ".display_name"),
							1, (short) 0, Arrays.asList(type.name()));
				}

				inv.addItem(item);

			} catch (IllegalArgumentException e) {

			}
		}

		p.openInventory(inv);
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

		player.sendMessage(getTrans().format("rq.keep-inv.start", player.getLocation(), player,
				new Group<>("duration", dD + ""), new Group<>("deaths", dA + "")));
		//player.sendMessage(G+"You now have "+Y+dD+G+" seconds or "+Y+dA+G+" deaths of keep-inventory.");

		deathsLeft.put(player, dA);
		new BukkitRunnable() {
			public void run() {
				if (deathsLeft.containsKey(player)) {
					deathsLeft.remove(player);

					player.sendMessage(getTrans().format("rq.keep-inv.expire", player));

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

				String message = ChatColor.translateAlternateColorCodes('&', trans.config.getString("rq.keep-inv.actionbar")).replace("{left}", t[0] + "");
				if (!message.toLowerCase().equals("none"))
					ActionBar.sendActionBar(player, message);
				t[0]--;
			}
		}.runTaskTimer(this, 0, 20);
	}

	/**
	 * Checks if a given location (loc) is in one of the always active worlds,
	 * in the warzone (when applicable), or is inside two other locations (one,
	 * two). This is used to make sure an item is activated within its proper
	 * region.
	 * @param loc
	 * @param one
	 * @param two
	 * @return
	 */
	public boolean locationChecksOut(Location loc, Location one, Location two) {
		if (loc == null) {
			return false;
		}

		List<String> usableWorlds = getConf().config.getStringList("always-usable-worlds");
		if (usableWorlds.contains(loc.getWorld().getName())) {
			return true;
		}

		if (serverHasFactions()) {
			return isWarzone(loc);
		} else {

			if (one == null || two == null) {
				return false;
			}

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

	public HashMap<Player, Integer> getDeathsLeft() {
		return deathsLeft;
	}

	public HashMap<UUID, Flare> getFallingFlares() {
		return fallingFlares;
	}

	public HashMap<Inventory, Group<ItemStack, String>> getFlareAnvils() {
		return flareAnvils;
	}

	public HashMap<Inventory, String> getMmAnvils() {
		return mmAnvils;
	}

	public HashMap<Block, BukkitTask> getPartTimers() {
		return partTimers;
	}

	public HashMap<Player, String> getPlayerFlares() {
		return playerFlares;
	}

	public HashMap<Player, RankQuest> getQIP() {
		return QIP;
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

	public Config getMysteryMobSpawners() {
		return mysteryMobSpawners;
	}

	public boolean isWarzone(Location loc) {
		if (loc == null) {
			return false;
		}

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
}
package com.drizzard.faq;

import com.drizzard.faq.util.ActionBar;
import com.drizzard.faq.util.FireworkUtil;
import com.drizzard.faq.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class RankQuest implements Listener {

	public static List<TimedAction> timedActions = new ArrayList<TimedAction>();
	Item im;
	Player owner;
	int timeLeft;
	int slot;
	FlareAndQuests plugin;
	BukkitTask timer;
	String name;
	Location one;
	Location two;

	public RankQuest(final int slot, final Player owner, int duration, final FlareAndQuests plugin, final String name) {
		ItemStack is = owner.getInventory().getItem(slot);
		this.slot = slot;
		this.owner = owner;
		this.plugin = plugin;
		this.name = name;
		timeLeft = duration;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		ItemMeta IM = is.getItemMeta();
		if (!IM.getDisplayName().contains("(") || !IM.getDisplayName().contains(")")) {
			IM.setDisplayName(IM.getDisplayName() + ChatColor.GRAY + " (" + ChatColor.YELLOW + timeLeft + ChatColor.GRAY + ")");
			is.setItemMeta(IM);
			owner.updateInventory();
		} else {
			String str = ChatColor.stripColor(IM.getDisplayName());
			timeLeft = Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
		}

		plugin.conf.load();
		one = (Location) plugin.conf.config.get("Quests." + name + ".First");
		two = (Location) plugin.conf.config.get("Quests." + name + ".Second");

		String[] message = plugin.getTrans().format("rq.broadcasts.start", owner.getLocation(), owner);
//		String message = ChatColor.translateAlternateColorCodes('&', plugin.trans.config.getString("RQ Start Broadcast")).replace("{player}", owner.getName());
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(message);
		}

		owner.setMaxHealth((double) plugin.conf.config.getInt("rq-player-health", 10) * 2);

		SoundUtil.playRQStartSound(plugin, owner);

		timer = new BukkitRunnable() {
			public void run() {
				timeLeft--;
				ItemStack is = owner.getInventory().getItem(slot);
				if (is == null)
					return;
				ItemMeta IM = is.getItemMeta();
				String snip = IM.getDisplayName().substring(0, IM.getDisplayName().indexOf("(") + 1);
				IM.setDisplayName(snip + ChatColor.YELLOW + timeLeft + ChatColor.GRAY + ")");
				is.setItemMeta(IM);

				if (owner.getInventory().getHeldItemSlot() != slot)
					ActionBar.sendActionBar(owner, ChatColor.translateAlternateColorCodes('&', plugin.trans.config.getString("Action Bar Message")).replace("{left}", timeLeft + ""));

				sendTimedActions(owner, timeLeft);

				if (timeLeft == 0) {
					plugin.conf.load();
					//if(is.getAmount() == 1)
					owner.getInventory().setItem(slot, null);
	                /*else {
						is.setAmount(is.getAmount()-1);
						IM = is.getItemMeta();
						int index = IM.getDisplayName().indexOf("(") - 3;
						if(index >= 0){
							IM.setDisplayName(IM.getDisplayName().substring(0, index));
							is.setItemMeta(IM);
						}
					}*/
					owner.getInventory().addItem(plugin.conf.config.getItemStack("Quests." + name + ".Voucher"));
					owner.updateInventory();

					FireworkUtil.fireworks(plugin, owner);

					SoundUtil.playRQFinishSound(plugin, owner);

					String[] message = plugin.getTrans().format("rq.broadcasts.start", owner.getLocation(), owner);
					for (Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(message);
					}

					plugin.startLull(owner);
					kill();
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	public static void loadTimedActions(FlareAndQuests plugin) {
		plugin.getConf().load();

		List<String> actions = plugin.getConf().config.getStringList("rq-timed-actions");

		for (String action : actions) {
			TimedAction timedAction = TimedAction.parseTimedAction(action);
			if (timedAction.getAction().name().startsWith("ERR_")) {
				plugin.getLogger().info("Error parsing '" + action + "'. Error code: " + timedAction.getAction().name());
			} else {
				timedActions.add(timedAction);
			}
		}
	}

	public static void sendTimedActions(Player p, int timeLeft) {
		for (TimedAction action : timedActions) {
			if (action.getTime() == timeLeft) {
				;
				if (action.getAction().equals(TimedAction.ActionType.MSG)) {
					p.sendMessage(action.formatMultiLineMessage(p, timeLeft + "", p.getLocation()));
				} else if (action.getAction().equals(TimedAction.ActionType.EXEC)) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), TimedAction.formatMessage(action.getMessage(), p, timeLeft + "", p.getLocation()));
				} else {
					for (String line : action.formatMultiLineMessage(p, timeLeft + "", p.getLocation())) {
						Bukkit.broadcastMessage(line);
					}
				}
			}
		}
	}

	public boolean isActive(Player player) {
		return player.equals(owner);
	}

	public void kill() {
		timer.cancel();
		owner.setMaxHealth(20d);
		plugin.QIP.remove(owner);
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent ev) {
		if (ev.getEntity().equals(owner)) {
			ItemStack is = owner.getInventory().getItem(slot);
			int am = is.getAmount();
			is.setAmount(1);
			int index = ev.getDrops().indexOf(is);
			is.setAmount(am);
			if (index > -1) {
				ItemMeta IM = is.getItemMeta();
				int ind = IM.getDisplayName().indexOf("(") - 3;
				if (index >= 0) {
					IM.setDisplayName(IM.getDisplayName().substring(0, ind));
					is.setItemMeta(IM);
					owner.updateInventory();
				}
				plugin.conf.load();
				String[] message = plugin.getTrans().format("rq.broadcasts.lost", owner.getLocation(), owner);
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage(message);
				}
				kill();
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent ev) {
		int s = ev.getInventory().getSize();
		if (s == 5)
			s = 36;
		else
			s += 27;
		if (ev.getWhoClicked().equals(owner) && (ev.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || ev.getAction() == InventoryAction.HOTBAR_SWAP || ev.getRawSlot() == slot + s))
			ev.setCancelled(true);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent ev) {
		if (ev.getPlayer().equals(owner) && !plugin.inside(ev.getTo(), one, two)) {
			ItemStack is = owner.getInventory().getItem(slot);
			plugin.conf.load();
			String[] message = plugin.getTrans().format("rq.broadcasts.reset", owner.getLocation(), owner);
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(message);
			}
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if (index >= 0) {
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			kill();
		}
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent ev) {
		if (ev.getPlayer().equals(owner) && !plugin.inside(ev.getTo(), one, two)) {
			ItemStack is = owner.getInventory().getItem(slot);
			plugin.conf.load();
			String[] message = plugin.getTrans().format("rq.broadcasts.reset", owner.getLocation(), owner);
//			String message = ChatColor.translateAlternateColorCodes('&', plugin.trans.config.getString("RQ Reset Broadcast")).replace("{player}", owner.getName());
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(message);
			}
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if (index >= 0) {
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			kill();
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent ev) {
		if (ev.getPlayer().equals(owner)) {
			ItemStack is = owner.getInventory().getItem(slot);
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if (index >= 0) {
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			plugin.conf.load();
			String[] message = plugin.getTrans().format("rq.broadcasts.reset", owner.getLocation(), owner);
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(message);
			}
			kill();
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent ev) {
		if (ev.getPlayer().equals(owner) && owner.getInventory().getHeldItemSlot() == slot) {
			//if(ev.getPlayer().equals(owner) && ev.getItemDrop().getItemStack().isSimilar(is)){
			ItemStack is = ev.getItemDrop().getItemStack();
			plugin.conf.load();
			ItemMeta IM = is.getItemMeta();
			int index = IM.getDisplayName().indexOf("(") - 3;
			if (index >= 0) {
				IM.setDisplayName(IM.getDisplayName().substring(0, index));
				is.setItemMeta(IM);
				owner.updateInventory();
			}
			String[] message = plugin.getTrans().format("rq.broadcasts.lost", owner.getLocation(), owner);
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendMessage(message);
			}
			kill();
		}
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent e) {
		if (e.getClickedInventory().getHolder() instanceof Player) {
			Player player = (Player) e.getClickedInventory().getHolder();
			if (player.equals(owner)) {
				if (e.getSlot() == slot) {
					e.setCancelled(true);
				}
			}
		}
	}

}

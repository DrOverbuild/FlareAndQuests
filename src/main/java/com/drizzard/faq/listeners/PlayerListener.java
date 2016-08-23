package com.drizzard.faq.listeners;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by jasper on 8/10/16.
 *
 * Handles all player events except for PlayerInteractEvent, which is handled
 * in ActivationListener.
 */
public class PlayerListener implements Listener {

	FlareAndQuests plugin;

	public PlayerListener(FlareAndQuests plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onDeath(PlayerDeathEvent ev) {
		if (plugin.getDeathsLeft().containsKey(ev.getEntity())) {
			int num = plugin.getDeathsLeft().get(ev.getEntity());
			if (num > 0)
				ev.setKeepInventory(true);
			num--;
			if (num <= 0) {
				plugin.getDeathsLeft().remove(ev.getEntity());

				ev.getEntity().sendMessage(plugin.getTrans().format("rq.keep-inv.expire", null, ev.getEntity()));
			} else
				plugin.getDeathsLeft().put(ev.getEntity(), num);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent ev) {
		if (plugin.getPlayerFlares().containsKey(ev.getPlayer())) {
			plugin.getPlayerData().config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", plugin.getPlayerFlares().get(ev.getPlayer()));
			plugin.getPlayerData().save();
			plugin.getPlayerFlares().remove(ev.getPlayer());
		} else {
			plugin.getPlayerData().config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", null);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		String flare = plugin.getPlayerData().config.getString("players." + ev.getPlayer().getUniqueId().toString() + ".flare", "");
		if (!flare.isEmpty()) {
			// This is easier than rewriting the code to give a flare to a player
			plugin.getCommand("flare").getExecutor().onCommand(plugin.getServer().getConsoleSender(), plugin.getCommand("flare"), "flare",
					new String[]{"give", flare, ev.getPlayer().getName()});
			ev.getPlayer().sendMessage(plugin.getTrans().format("flare.given-upon-join", null, ev.getPlayer()));
			plugin.getPlayerData().config.set("players." + ev.getPlayer().getUniqueId().toString() + ".flare", null);
			plugin.getPlayerData().save();
		}

		ev.getPlayer().setMaxHealth(20d);
	}

}

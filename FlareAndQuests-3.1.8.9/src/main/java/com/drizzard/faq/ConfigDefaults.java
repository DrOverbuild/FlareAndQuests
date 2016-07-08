package com.drizzard.faq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class ConfigDefaults {

	public static Config setConfigDefaults(FlareAndQuests plugin){
		HashMap<String, Object> defs = new HashMap<String, Object>();

		defs.put("Flare Max Tries", 100);

		defs.put("Deaths Allowed For Keep-Inv", 1);
		defs.put("Keep-Inv Duration", 60);

		defs.put("Flare Drop Radius", 10.0);
		defs.put("Flare Alert Radius", 10.0);
		defs.put("Minimum Flare Contents", 2);
		defs.put("Maximum Flare Contents", 6);
		defs.put("Flare Arrival Delay", 0);

		defs.put("RQ Minimum Players", 2);
		defs.put("Flare Minimum Players", 2);
		defs.put("Witem Minimum Players", 2);
		defs.put("WRQ Minimum Players", 2);

		List<String> timedActions = new ArrayList<>();
		timedActions.add("time:20 broadcast:&4Player {player} has {left-seconds} seconds left until he finishes his rank quest!");
		timedActions.add("time:10 broadcast:&4Player {player} has {left-seconds} seconds left until he finishes his rank quest!");
		timedActions.add("time:5 msg:{player} &eYou are almost done!");

		defs.put("rq-timed-actions", timedActions);

		return new Config(plugin, defs, "config");
	}

	public static Config setTranslationsDefaults(FlareAndQuests plugin){
		HashMap<String, Object> defsT = new HashMap<String, Object>();

		defsT.put("RQ Start Broadcast", "&e{player} &7has started a rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Complete Broadcast", "&e{player} &7has completed their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Lost Broadcast", "&e{player} &7has lost their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Reset Broadcast", "&e{player} &7has reset their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Quit Broadcast", "&e{player} &7left, so their rank quest was reset!  ({x}, {y}, {z})");
		defsT.put("WRQ Start Broadcast", "&e{player} &7has started a warzone quest!  ({x}, {y}, {z})");
		defsT.put("WRQ Complete Broadcast", "&e{player} &7has completed their warzone quest!  ({x}, {y}, {z})");
		defsT.put("WRQ Lost Broadcast", "&e{player} &7has lost their warzone quest!  ({x}, {y}, {z})");
		defsT.put("WRQ Reset Broadcast", "&e{player} &7has reset their warzone quest!  ({x}, {y}, {z})");
		defsT.put("WRQ Quit Broadcast", "&e{player} &7left, so their warzone quest was reset!  ({x}, {y}, {z})");
		defsT.put("Action Bar Message", "&b&lRank Quest: &e{left} &7seconds");

		defsT.put("Not in Warzone Message", "&4You must be in a Warzone!");
		defsT.put("Already Doing Quest Message", "&4You are already doing a rank quest!");
		defsT.put("Already Doing Warzone Quest Message", "&4You are already doing a warzone quest!");
		defsT.put("Not in Region Message", "&4You must be inside the proper region!");
		defsT.put("Keep-Inventory Start Message", "&7You now have &e{duration} &7seconds or &e{deaths} &7deaths of keep-inventory.");
		defsT.put("Keep-Inventory Expire Message", "&7Your keep-inventory period has expired.");
		defsT.put("Keep-Inventory Actionbar Message", "&b&lKeep-Inventory: &e{left} &7seconds");
		defsT.put("Flare Drop Failed Message", "&4Drop failed.");
		defsT.put("Flare Arriving In Action Bar Message", "&7Supplies arriving in: {time} seconds");
		defsT.put("Flare Arrived Message", "&7Your supplies have arrived! ({x}, {y}, {z})");
		defsT.put("Flare In Use", "&cYou can only use one flare at a time!");
		defsT.put("Flare Given Upon Join Message", "&7You have been given a flare because you disconnected whilst waiting for a flare.");
		defsT.put("Not Enough Players", "&cYou can't use that item right now! There must be at least {min-online} players online.");
		defsT.put("Cannot Activate Stacked Rank Quests Message", "&4You cannot activate more than one rank quest at the same time!");
		defsT.put("Cannot Activate Stacked Warzone Quests Message", "&4You cannot activate more than one Warzone Quest at the same time!");
		defsT.put("Cannot Activate While in Keep Inv Message", "&4You cannot activate a rank quest while in a keep inventory period!");
		defsT.put("Cannot Activate Rank Quest While Doing Other Function", "&4You cannot activate a rank quest while doing another function!");
		defsT.put("Cannot Activate Flare While Doing Other Function", "&4You cannot activate a flare while doing another function!");
		defsT.put("Cannot Activate Warzone Quest While Doing Other Function", "&4You cannot activate a warzone quest while doing another function!");
		defsT.put("Cannot Activate Witem While Doing Other Function", "&4You cannot activate a witem while doing another function!");
		defsT.put("Flare Broadcast", "&e{player} &7has used a flare!");

		return new Config(plugin, defsT, "translations");
	}

}
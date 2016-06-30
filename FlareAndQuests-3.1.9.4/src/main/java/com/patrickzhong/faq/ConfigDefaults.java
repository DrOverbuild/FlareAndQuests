package com.patrickzhong.faq;

import java.util.HashMap;

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

		return new Config(plugin, defs, "config");
	}

	public static Config setTranslationsDefaults(FlareAndQuests plugin){
		HashMap<String, Object> defsT = new HashMap<String, Object>();

		defsT.put("RQ Start Broadcast", "&e{player} &7has started a rank quest!");
		defsT.put("RQ Complete Broadcast", "&e{player} &7has completed their rank quest!");
		defsT.put("RQ Lost Broadcast", "&e{player} &7has lost their rank quest!");
		defsT.put("RQ Reset Broadcast", "&e{player} &7has reset their rank quest!");
		defsT.put("RQ Quit Broadcast", "&e{player} &7left, so their rank quest was reset!");
		defsT.put("Action Bar Message", "&b&lRank Quest: &e{left} &7seconds");

		defsT.put("Not in Warzone Message", "&4You must be in a Warzone!");
		defsT.put("Already Doing Quest Message", "&4You are already doing a rank quest!");
		defsT.put("Not in Region Message", "&4You must be inside the proper region!");
		defsT.put("Keep-Inventory Start Message", "&7You now have &e{duration} &7seconds or &e{deaths} &7deaths of keep-inventory.");
		defsT.put("Keep-Inventory Expire Message", "&7Your keep-inventory period has expired.");
		defsT.put("Keep-Inventory Actionbar Message", "&b&lKeep-Inventory: &e{left} &7seconds");
		defsT.put("Flare Drop Failed Message", "&4Drop failed.");

		defsT.put("Cannot Activate Stacked Rank Quests Message", "&4You cannot activate more than one rank quest at the same time!");
		defsT.put("Cannot Activate While in Keep Inv Message", "&4You cannot activate a rank quest while in a keep inventory period!");
		defsT.put("Flare Broadcast", "&e{player} &7has used a flare!");

		return new Config(plugin, defsT, "translations");
	}

}

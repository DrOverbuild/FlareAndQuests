package com.drizzard.faq;

import com.drizzard.faq.util.Sounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jasper on 6/28/16.
 */
public class ConfigDefaults {

	public static Config setConfigDefaults(FlareAndQuests plugin) {
		HashMap<String, Object> defs = new HashMap<String, Object>();

		defs.put("flare.max-tries", 100);
		defs.put("flare.drop-radius", 10.0);
		defs.put("flare.alert-radius", 10.0);
		defs.put("flare.min-free-above-blocks", 20);
		defs.put("flare.min-contents", 2);
		defs.put("flare.max-contents", 6);
		defs.put("flare.arrival-delay", 0);
		defs.put("flare.chest-particle-id", 24);
		defs.put("flare.pre-fall-particle-id", 3);

		defs.put("Deaths Allowed For Keep-Inv", 1);
		defs.put("Keep-Inv Duration", 60);

		defs.put("minimum-players.rq", 2);
		defs.put("minimum-players.flare", 2);
		defs.put("minimum-players.witem", 2);

		List<String> timedActions = new ArrayList<String>();
		timedActions.add("time:20 broadcast:&4Player {player} has {left-seconds} seconds left until he finishes his rank quest!");
		timedActions.add("time:10 broadcast:&4Player {player} has {left-seconds} seconds left until he finishes his rank quest!");
		timedActions.add("time:5 msg:{player} &eYou are almost done!");

		defs.put("fireworks.enabled", true);
		defs.put("fireworks.amount", 20);
		defs.put("fireworks.rate", 5);
		defs.put("fireworks.type", "RANDOM");
		defs.put("fireworks.color", "RANDOM");

		defs.put("rq-player-health", 10);
		defs.put("rq-timed-actions", timedActions);

		defs.put("sounds.rq-start", Sounds.ENDERDRAGON_GROWL.name());
		defs.put("sounds.rq-finish", Sounds.ORB_PICKUP.name());
		defs.put("sounds.flare-use", Sounds.BLAZE_BREATH.name());
		defs.put("sounds.chest-arrival", Sounds.ANVIL_LAND.name());
		defs.put("sounds.witem-use", Sounds.BLAZE_BREATH.name());
		defs.put("sounds.mm-use", Sounds.BLAZE_HIT.name());

		defs.put("always-use-regions", false);

		return new Config(plugin, defs, "config");
	}

	public static Config setTranslationsDefaults(FlareAndQuests plugin) {
		HashMap<String, Object> defsT = new HashMap<String, Object>();

		defsT.put("RQ Start Broadcast", "&e{player} &7has started a rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Complete Broadcast", "&e{player} &7has completed their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Lost Broadcast", "&e{player} &7has lost their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Reset Broadcast", "&e{player} &7has reset their rank quest!  ({x}, {y}, {z})");
		defsT.put("RQ Quit Broadcast", "&e{player} &7left, so their rank quest was reset!  ({x}, {y}, {z})");
		defsT.put("Action Bar Message", "&b&lRank Quest: &e{left} &7seconds");

		defsT.put("Not in Warzone Message", "&4You must be in a Warzone!");
		defsT.put("Already Doing Quest Message", "&4You are already doing a rank quest!");
		defsT.put("Not in Region Message", "&4You must be inside the proper region!");
		defsT.put("Keep-Inventory Start Message", "&7You now have &e{duration} &7seconds or &e{deaths} &7deaths of keep-inventory.");
		defsT.put("Keep-Inventory Expire Message", "&7Your keep-inventory period has expired.");
		defsT.put("Keep-Inventory Actionbar Message", "&b&lKeep-Inventory: &e{time} &7seconds");
		defsT.put("Flare Drop Failed Message", "&4Drop failed.");
		defsT.put("Flare Arriving In Action Bar Message", "&7Supplies arriving in: {time} seconds");
		defsT.put("Flare Arrived Message", "&7Your supplies have arrived! ({x}, {y}, {z})");
		defsT.put("Flare In Use", "&cYou can only use one flare at a time!");
		defsT.put("Flare Given Upon Join Message", "&7You have been given a flare because you disconnected whilst waiting for a flare.");
		defsT.put("Flare Not Enough Space", "&cThere is not enough space to spawn a flare! The space must be at least {free-blocks} tall.");
		defsT.put("Not Enough Players", "&cYou can't use that item right now! There must be at least {min-online} players online.");
		defsT.put("Cannot Activate Stacked Rank Quests Message", "&4You cannot activate more than one rank quest at the same time!");
		defsT.put("Cannot Activate While in Keep Inv Message", "&4You cannot activate a rank quest while in a keep inventory period!");
		defsT.put("Cannot Activate Rank Quest While Doing Other Function", "&4You cannot activate a rank quest while doing another function!");
		defsT.put("Cannot Activate Flare While Doing Other Function", "&4You cannot activate a flare while doing another function!");
		defsT.put("Cannot Activate Witem While Doing Other Function", "&4You cannot activate a witem while doing another function!");
		defsT.put("Cannot Activate Mystery Mob While Doing Other Function", "&4You cannot activate a mystery mob while doing another function!");
		defsT.put("Flare Broadcast", "&e{player} &7has used a flare!");
		defsT.put("mm-item-use", "Congratulations! You have used a mystery mob item and got a {selectedspawner}!");

		return new Config(plugin, defsT, "translations");
	}

	public static Config setMysteryMobDefaults(FlareAndQuests plugin) {
		HashMap<String, Object> defs = new HashMap<>();

		defs.put("spawners.bat.display_name", "Bat Spawner");
		defs.put("spawners.blaze.display_name", "Blaze Spawner");
		defs.put("spawners.chicken.display_name", "Chicken Spawner");
		defs.put("spawners.cow.display_name", "Cow Spawner");
		defs.put("spawners.creeper.display_name", "Creeper Spawner");
		defs.put("spawners.enderman.display_name", "Enderman Spawner");
		defs.put("spawners.endermite.display_name", "Endermite Spawner");
		defs.put("spawners.ghast.display_name", "Ghast Spawner");
		defs.put("spawners.guardian.display_name", "Guardian Spawner");
		defs.put("spawners.pig.display_name", "Pig Spawner");
		defs.put("spawners.rabbit.display_name", "Rabbit Spawner");
		defs.put("spawners.sheep.display_name", "Sheep Spawner");
		defs.put("spawners.silverfish.display_name", "Silverfish Spawner");
		defs.put("spawners.skeleton.display_name", "Skeleton Spawner");
		defs.put("spawners.spider.display_name", "Spider Spawner");
		defs.put("spawners.squid.display_name", "Squid Spawner");
		defs.put("spawners.slime.display_name", "Slime Spawner");
		defs.put("spawners.villager.display_name", "Villager Spawner");
		defs.put("spawners.witch.display_name", "Witch Spawner");
		defs.put("spawners.wolf.display_name", "Wolf Spawner");
		defs.put("spawners.cave_spider.display_name", "Cave Spider Spawner");
		defs.put("spawners.zombie.display_name", "Zombie Spawner");
		defs.put("spawners.ender_dragon.display_name", "Ender Dragon Spawner");
		defs.put("spawners.giant.display_name", "Giant Spawner");
		defs.put("spawners.snowman.display_name", "Snowman Spawner");
		defs.put("spawners.horse.display_name", "Horse Spawner");
		defs.put("spawners.mushroom_cow.display_name", "Mooshroom Spawner");
		defs.put("spawners.ocelot.display_name", "Ocelot Spawner");
		defs.put("spawners.pig_zombie.display_name", "Zombie Pigman Spawner");
		defs.put("spawners.magma_cube.display_name", "Magma Cube Spawner");
		defs.put("spawners.wither.display_name", "Wither Spawner");
		defs.put("spawners.iron_golem.display_name", "Iron Golem Spawner");

		return new Config(plugin, defs, "spawners");
	}
}

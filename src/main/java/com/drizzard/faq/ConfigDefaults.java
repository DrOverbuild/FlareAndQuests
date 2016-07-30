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

        defs.put("Flare Max Tries", 100);

        defs.put("Deaths Allowed For Keep-Inv", 1);
        defs.put("Keep-Inv Duration", 60);

        defs.put("Flare Drop Radius", 10.0);
        defs.put("Flare Alert Radius", 10.0);
        defs.put("Minimum Flare Contents", 2);
        defs.put("Maximum Flare Contents", 6);
        defs.put("Flare Arrival Delay", 0);
        defs.put("Flare Chest Particle ID", 24);

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

        //This is the old sound system
        /*
        // Uncomment these lines for 1.8.9 version
		defs.put("sounds.rq-start", Sound.ENDERDRAGON_GROWL.name());
		defs.put("sounds.rq-finish", Sound.ORB_PICKUP.name());
		defs.put("sounds.flare-use", Sound.BLAZE_BREATH.name());
		defs.put("sounds.chest-arrival", Sound.ANVIL_LAND.name());
		defs.put("sounds.witem-use", Sound.BLAZE_BREATH.name());

        defs.put("sounds.rq-start", Sounds.ENTITY_ENDERDRAGON_GROWL.name());
        defs.put("sounds.rq-finish", Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name());
        defs.put("sounds.flare-use", Sound.ENTITY_BLAZE_AMBIENT.name());
        defs.put("sounds.chest-arrival", Sound.BLOCK_ANVIL_HIT.name());
        defs.put("sounds.witem-use", Sound.ENTITY_BLAZE_AMBIENT.name());
        */

        //New sound system
        defs.put("sounds.rq-start", Sounds.ENDERDRAGON_GROWL.name());
        defs.put("sounds.rq-finish", Sounds.ORB_PICKUP.name());
        defs.put("sounds.flare-use", Sounds.BLAZE_BREATH.name());
        defs.put("sounds.chest-arrival", Sounds.ANVIL_LAND.name());
        defs.put("sounds.witem-use", Sounds.BLAZE_BREATH.name());

        defs.put("rq-timed-actions", timedActions);

        //always use regions
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
        defsT.put("Not Enough Players", "&cYou can't use that item right now! There must be at least {min-online} players online.");
        defsT.put("Cannot Activate Stacked Rank Quests Message", "&4You cannot activate more than one rank quest at the same time!");
        defsT.put("Cannot Activate While in Keep Inv Message", "&4You cannot activate a rank quest while in a keep inventory period!");
        defsT.put("Cannot Activate Rank Quest While Doing Other Function", "&4You cannot activate a rank quest while doing another function!");
        defsT.put("Cannot Activate Flare While Doing Other Function", "&4You cannot activate a flare while doing another function!");
        defsT.put("Cannot Activate Witem While Doing Other Function", "&4You cannot activate a witem while doing another function!");
        defsT.put("Flare Broadcast", "&e{player} &7has used a flare!");

        return new Config(plugin, defsT, "translations");
    }

}

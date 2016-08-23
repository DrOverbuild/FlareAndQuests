package com.drizzard.faq.util;

import com.drizzard.faq.Config;

/**
 * Created by jasper on 8/22/16.
 */
public class ConfigUtils {
	public static void updateMessagesConfig(Config config){
		renameKey(config, "RQ Start Broadcast", "rq.broadcasts.start");
		renameKey(config, "RQ Complete Broadcast", "rq.broadcasts.complete");
		renameKey(config, "RQ Lost Broadcast", "rq.broadcasts.lost");
		renameKey(config, "RQ Reset Broadcast", "rq.broadcasts.Reset");
		renameKey(config, "RQ Quit Broadcast", "rq.broadcasts.quit");
		renameKey(config, "Action Bar Message", "rq.actionbar");
		renameKey(config, "Not in Warzone Message", "not-in-warzone");
		renameKey(config, "Already Doing Quest Message", "rq.activation-error.currently-doing-quest");
		renameKey(config, "Not in Region Message", "not-in-warzone");
		renameKey(config, "Keep-Inventory Start Message", "rq.keep-inv.start");
		renameKey(config, "Keep-Inventory Expire Message", "rq.keep-inv.expire");
		renameKey(config, "Keep-Inventory Actionbar Message", "rq.keep-inv.actionbar");
		renameKey(config, "Flare Drop Failed Message", "flare.drop-failed");
		renameKey(config, "Flare Arriving In Action Bar Message", "flare.actionbar");
		renameKey(config, "Flare Arrived Message", "flare.arrived");
		renameKey(config, "Flare In Use", "flare.in-use");
		renameKey(config, "Flare Given Upon Join Message", "flare.given-upon-join");
		renameKey(config, "Flare Not Enough Space", "flare.no-space");
		renameKey(config, "Not Enough Players", "not-enough-players");
		renameKey(config, "Cannot Activate Stacked Rank Quests Message", "rq.activation-error.stacked-rq");
		renameKey(config, "Cannot Activate While in Keep Inv Message", "rq.activation-error.in-keep-inv");
		renameKey(config, "Cannot Activate Rank Quest While Doing Other Function", "other-function-active.rq");
		renameKey(config, "Cannot Activate Flare While Doing Other Function", "other-function-active.flare");
		renameKey(config, "Cannot Activate Witem While Doing Other Function", "other-function-active.witem");
		renameKey(config, "Cannot Activate Mystery Mob While Doing Other Function", "other-function-active.mm");
		renameKey(config, "Flare Broadcast", "flare.broadcast");
	}

	public static void renameKey(Config config, String oldKey, String newKey){
		if(config.config.contains(oldKey)){
			config.config.set(newKey, config.config.get(oldKey));
			config.config.set(oldKey, null);
			config.save();
		}
	}
}

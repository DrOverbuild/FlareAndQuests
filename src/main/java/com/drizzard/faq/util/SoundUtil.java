package com.drizzard.faq.util;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 7/8/16.
 */
public class SoundUtil {
    private static Sounds getSound(FlareAndQuests plugin, String configKey) {
        plugin.getConf().load();
        String name = plugin.getConf().config.getString("sounds." + configKey);
        try {
            return Sounds.valueOf(name.replace(".", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().info("Cannot find sound " + name);
            return null;
        }
    }

    public static void playRQStartSound(FlareAndQuests plugin, Player p) {
        Sounds sound = getSound(plugin, "rq-start");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }

    public static void playRQFinishSound(FlareAndQuests plugin, Player p) {
        Sounds sound = getSound(plugin, "rq-finish");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }

    public static void playFlareUseSound(FlareAndQuests plugin, Player p) {
        Sounds sound = getSound(plugin, "flare-use");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }

    public static void playChestArrivalSound(FlareAndQuests plugin, Player p) {
        Sounds sound = getSound(plugin, "chest-arrival");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }

    public static void playWitemUseSound(FlareAndQuests plugin, Player p) {
        Sounds sound = getSound(plugin, "witem-use");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }
}

package com.drizzard.faq.util;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 7/8/16.
 */
public class SoundUtil {
    private static Sounds getSound(FlareAndQuests plugin, String configKey) {
        plugin.getConf().load();
        String name = plugin.getConf().config.getString("sounds." + configKey).replace(".", "_").toUpperCase();

        Sounds sound = Sounds.soundFromPost19Value(name);

        if(sound != null){
            return sound;
        }else {
            try {
                return Sounds.valueOf(name);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().info("Cannot find sound " + name);
                return null;
            }
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

    public static void playMMUseSound(FlareAndQuests plugin, Player p){
        Sounds sound = getSound(plugin, "mm-use");
        if (sound != null) {
            p.playSound(p.getLocation(), sound.get(), 1f, 1f);
        }
    }
}

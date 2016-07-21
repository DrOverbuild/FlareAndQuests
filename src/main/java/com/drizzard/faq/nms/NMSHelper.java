package com.drizzard.faq.nms;

import org.bukkit.Bukkit;

/**
 * Created by Jaime Martinez Rincon on 21/07/2016 in project FlareAndQuests.
 */
public class NMSHelper {
    private static String nmsver;

    static {
        nmsver = Bukkit.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
    }

    public static String getNMSVersion() {
        return nmsver;
    }
}

package com.drizzard.faq.nms.anvil;

import com.drizzard.faq.nms.NMSHelper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Jaime Martinez Rincon on 21/07/2016 in project FlareAndQuests.
 */
public class AnvilSender {
    public static void send(HumanEntity entity, String name, ItemStack item) {
        if (NMSHelper.getNMSVersion().equals("v1_8_R3")) {
            com.drizzard.faq.nms.anvil.v1_8_R3.AnvilContainer container = new com.drizzard.faq.nms.anvil.v1_8_R3.AnvilContainer(entity);
            container.send(entity, name, item);
        } else if (NMSHelper.getNMSVersion().equals("v1_9_R2")) {
            com.drizzard.faq.nms.anvil.v1_9_R2.AnvilContainer container = new com.drizzard.faq.nms.anvil.v1_9_R2.AnvilContainer(entity);
            container.send(entity, name, item);
        } else throw new IllegalStateException("This version is not supported, version: " + NMSHelper.getNMSVersion());
    }
}

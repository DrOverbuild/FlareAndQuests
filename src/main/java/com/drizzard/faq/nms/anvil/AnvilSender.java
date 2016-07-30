package com.drizzard.faq.nms.anvil;

import com.drizzard.faq.nms.NMSHelper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Jaime Martinez Rincon on 21/07/2016 in project FlareAndQuests.
 */
public class AnvilSender {
    public static void send(HumanEntity entity, String name, ItemStack item) {
    	if (NMSHelper.getNMSVersion().equals("v1_7_R4"))
            new com.drizzard.faq.nms.anvil.v1_7_R4.AnvilContainer(entity).send(entity, name, item);
    	else if (NMSHelper.getNMSVersion().equals("v1_8_R3"))
            new com.drizzard.faq.nms.anvil.v1_8_R3.AnvilContainer(entity).send(entity, name, item);
        else if (NMSHelper.getNMSVersion().equals("v1_9_R2"))
            new com.drizzard.faq.nms.anvil.v1_9_R2.AnvilContainer(entity).send(entity, name, item);
        else if (NMSHelper.getNMSVersion().equals("v1_10_R1"))
            new com.drizzard.faq.nms.anvil.v1_10_R1.AnvilContainer(entity).send(entity, name, item);
        else throw new IllegalStateException("This version is not supported, version: " + NMSHelper.getNMSVersion());
    }
}

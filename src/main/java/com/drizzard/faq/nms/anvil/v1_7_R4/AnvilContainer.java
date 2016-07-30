package com.drizzard.faq.nms.anvil.v1_7_R4;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public final class AnvilContainer extends ContainerAnvil {

    public AnvilContainer(HumanEntity ent) {
        super(((CraftHumanEntity) ent).getHandle().inventory, ((CraftHumanEntity) ent).getHandle().world, 0, 0, 0, ((CraftHumanEntity) ent).getHandle());
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return true;
    }

    public void send(HumanEntity entity, String name, ItemStack item) {
        this.a(name);
        this.setItem(0, CraftItemStack.asNMSCopy(item));

        EntityPlayer ep = ((CraftPlayer) entity).getHandle();
        int containerId = ep.nextContainerCounter();
        ep.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, 8, "Set Chance", 2, false));
        ep.activeContainer = this;
        ep.activeContainer.windowId = containerId;
        ep.activeContainer.addSlotListener(ep);
    }
}
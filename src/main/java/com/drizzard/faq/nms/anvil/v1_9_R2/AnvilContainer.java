package com.drizzard.faq.nms.anvil.v1_9_R2;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public final class AnvilContainer extends ContainerAnvil {

    public AnvilContainer(HumanEntity ent) {
        super(((CraftHumanEntity) ent).getHandle().inventory, ((CraftHumanEntity) ent).getHandle().world, new BlockPosition(0, 0, 0), ((CraftHumanEntity) ent).getHandle());
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
        ep.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage("Set Chance", new Object[]{}), 0));
        ep.activeContainer = this;
        ep.activeContainer.windowId = containerId;
        ep.activeContainer.addSlotListener(ep);
    }
}
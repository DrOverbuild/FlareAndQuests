package com.drizzard.faq;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import net.minecraft.server.v1_8_R3.EntityHuman;

import org.bukkit.entity.HumanEntity;

public final class FAnvil extends ContainerAnvil {   
	
	public FAnvil(HumanEntity ent){
		super(((CraftHumanEntity)ent).getHandle().inventory, ((CraftHumanEntity)ent).getHandle().world, new BlockPosition(0, 0, 0), ((CraftHumanEntity)ent).getHandle());
	}
	
	@Override
	public boolean a(EntityHuman entityHuman){
		return true;
	}
}
package com.patrickzhong.main;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.ContainerAnvil;
import net.minecraft.server.v1_9_R1.EntityHuman;

public final class FAnvil extends ContainerAnvil {   
	
	public FAnvil(HumanEntity ent){
		super(((CraftHumanEntity)ent).getHandle().inventory, ((CraftHumanEntity)ent).getHandle().world, new BlockPosition(0, 0, 0), ((CraftHumanEntity)ent).getHandle());
	}
	
	@Override
	public boolean a(EntityHuman entityHuman){
		return true;
	}
}
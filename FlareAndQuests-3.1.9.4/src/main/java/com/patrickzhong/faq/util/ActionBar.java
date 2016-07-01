package com.patrickzhong.faq.util;

import com.patrickzhong.faq.FlareAndQuests;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 6/29/16.
 */
public class ActionBar {
	public static void sendActionBar(Player player, String message) {

		try {
			Class CP = Class.forName(FlareAndQuests.CBPATH + "entity.CraftPlayer");
			Object p = CP.cast(player);

			Class ICBC = Class.forName(FlareAndQuests.NMSPATH + "IChatBaseComponent");
			Class CS = ICBC.getDeclaredClasses()[0];
			Object cbc = CS.getMethod("a", String.class).invoke(null, "{\"text\": \"" + message + "\"}");

			Class PPOC = Class.forName(FlareAndQuests.NMSPATH + "PacketPlayOutChat");
			Object ppoc = PPOC.getConstructor(ICBC, byte.class).newInstance(cbc, (byte) 2);

			Class PC = Class.forName(FlareAndQuests.NMSPATH + "PlayerConnection");
			Class EP = Class.forName(FlareAndQuests.NMSPATH + "EntityPlayer");
			Object ep = CP.getMethod("getHandle").invoke(p);
			Object pc = EP.getField("playerConnection").get(ep);
			Class P = Class.forName(FlareAndQuests.NMSPATH + "Packet");

			PC.getMethod("sendPacket", P).invoke(pc, P.cast(ppoc));

			//CraftPlayer p = (CraftPlayer) player;
			//IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
			//PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
			//((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
		} catch (Exception e) {
			Bukkit.getPluginManager().getPlugin("FlareAndQuests").getLogger().info(ExceptionUtils.getStackTrace(e));
		}
	}
}

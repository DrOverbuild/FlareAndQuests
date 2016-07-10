package com.drizzard.faq.util;

import com.drizzard.faq.Flare;
import com.drizzard.faq.FlareAndQuests;
import com.massivecraft.factions.P;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.StreamHandler;

/**
 * Created by jasper on 7/7/16.
 */
public class FireworkUtil {
	private static List<String> types = Arrays.asList("BALL", "BALL_LARGE", "BURST", "STAR");
	private static List<Color> colors = Arrays.asList(Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GREEN, Color.LIME,
			Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.WHITE,
			Color.TEAL, Color.YELLOW);

	private static String TYPE = "RANDOM";
	private static int AMOUNT = 20;
	private static int PERIOD = 5;
	private static boolean ENABLED = false;

	public static Random random = new Random();

	public static void loadFromConfig(FlareAndQuests plugin){
		ENABLED = plugin.getConf().config.getBoolean("fireworks.enabled", false);
		TYPE = plugin.getConf().config.getString("fireworks.type", "RANDOM");
		AMOUNT = plugin.getConf().config.getInt("fireworks.amount", 20);
		PERIOD = plugin.getConf().config.getInt("fireworks.rate", 5);
	}

	public static void spawnFirework(Location loc) {
		Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();

		FireworkEffect.Type type = FireworkEffect.Type.BALL;

		try {
			if (TYPE.equalsIgnoreCase("RANDOM")) {
				type = FireworkEffect.Type.valueOf(types.get(random.nextInt(types.size())));
			} else {
				type = FireworkEffect.Type.valueOf(TYPE);
			}
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().info("Unable to find type: " + TYPE);
		}

		Color color = colors.get(random.nextInt(colors.size()));

		fireworkMeta.addEffect(
				FireworkEffect.builder()
						.flicker(random.nextBoolean())
						.withColor(color)
						.withFade(color)
						.with(type)
						.trail(random.nextBoolean())
						.build()
		);
		fireworkMeta.setPower(1);
		firework.setFireworkMeta(fireworkMeta);
	}

	public static void fireworks(FlareAndQuests plugin, final Player p) {
		if (ENABLED) {
			new BukkitRunnable() {
				int times = AMOUNT;

				@Override
				public void run() {
					times--;
					spawnFirework(p.getLocation());

					if (times <= 0) {
						this.cancel();
					}
				}
			}.runTaskTimer(plugin, 0, PERIOD);
		}
	}
}

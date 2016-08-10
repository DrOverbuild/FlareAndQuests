package com.drizzard.faq.commands;

import com.drizzard.faq.Config;
import com.drizzard.faq.FlareAndQuests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 6/28/16.
 *
 * Command classes should extend this class for easy access to the common
 * colors, error handling (incorrect permissions, wrong syntax, etc), and
 * certain convenience methods like getting the plugin and the plugin's config
 * files. This system was designed because originally all the commands and their
 * implementations were in a single class. However, they all had a similar
 * structure so the developer just intertwined the commands' implementations, so
 * it was very hard to separate them into their own classes. Therefore, this
 * system was designed, to keep the commands' structure while being better
 * organized and expandable (easy to add additional commands).
 *
 * It's easy to add another command. Simply extend this class and override the
 * constructor like this:
 *
 *      public ____Command(FlareAndQuests plugin) {
 *          super(plugin);
 *      }
 *
 * If you don't override this, the compiler will complain.
 *
 * Next, override the executeCommand(CommandSender, String[]) method. This
 * method acts just like onCommand(CommandSender, Command, String, String[])
 * from the org.bukkit.command.CommandExecutor class, however this method takes
 * care of all the boilerplate code that all commands must have. It loads up the
 * configuration and translation files, because the commands are very likely to
 * use them, it takes care of permissions, it sends help if there are no
 * arguments, and it handles when the arguments are invalid. This is all stuff
 * that every command has to have and wouldn't be fun to have to implement
 * individually in every command.
 *
 * It is also required to override the sendHelp(CommandSender) method. In this
 * method you should send usage info of the command to the player.
 *
 * Finally, you'll need to register the command into the server. Do this by
 * adding the command to plugin.yml and adding this code to
 * FlareAndQuests.registerCommands():
 *
 * 		getCommand("____").setExecutor(new ____Command(this));
 */
public abstract class BasePluginCommand implements CommandExecutor {
    public static final String DR = ChatColor.DARK_RED + "";
    public static final String R = ChatColor.RED + "";
    public static final String G = ChatColor.GRAY + "";
    public static final String Y = ChatColor.YELLOW + "";
    public static final String SEP = ChatColor.WHITE + "- " + ChatColor.GRAY;
    public static final String BEG = ChatColor.GRAY + "- " + ChatColor.AQUA;
    FlareAndQuests plugin;

    public BasePluginCommand(FlareAndQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        getConf().load();
        getTrans().load();

        if (!sender.hasPermission("faq." + command.getName().toLowerCase()) && !sender.hasPermission("faq.*")) {
            sender.sendMessage(DR + "You need the permission "+R+"faq." + command.getName().toLowerCase() + DR+ " or "+R+" faq.* "+DR+".");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        if (!executeCommand(sender, args)) {
            sender.sendMessage(DR + "Invalid syntax or missing arguments. Type "+R+"/" + command.getName() + DR + " to view help.");
        }

        return true;
    }

    public FlareAndQuests getPlugin() {
        return plugin;
    }

    public Config getConf() {
        return getPlugin().getConf();
    }

    public Config getTrans() {
        return getPlugin().getTrans();
    }

    public abstract boolean executeCommand(CommandSender sender, String[] args);

    public abstract void sendHelp(CommandSender sender);

    public void list(CommandSender sender, String listWhat) {
        String msg = "";
        getConf().load();
        if (getConf().config.contains(listWhat)) {
            for (String key : getConf().config.getConfigurationSection(listWhat).getKeys(false)) {
                msg += (msg.equals("") ? "" : G + ", ") + Y + key;
            }
        }
        sender.sendMessage(G + listWhat + ": " + (msg.equals("") ? Y + "Nothing to list for " + listWhat + "." : msg));
    }

    public boolean playerHasNamedItem(Player p) {
        if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
            p.sendMessage(DR + "You must hold an item in your hand.");
            return false;
        }

        if (!p.getItemInHand().hasItemMeta()) {
            p.sendMessage(DR + "You cannot use an unnamed/unlored item.");
            return false;
        }

        return true;
    }
}

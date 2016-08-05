package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 8/4/16.
 */
public class MMCommand extends BasePluginCommand {
	public MMCommand(FlareAndQuests plugin) {
		super(plugin);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if(args[0].equalsIgnoreCase("list")){
			list(sender, "MysteryMobs");
			return true;
		}else if(args.length < 2){
		}else if(args[0].equalsIgnoreCase("delete")){
			// TODO: Implement "/mm delete <name>" command
			return true;
		}else if(args.length < 4){
		}else if(args[0].equalsIgnoreCase("give")){
			// TODO: Implement "/mm give <player> <name> <amount>" command
			return true;
		}

		Player p;
		if (sender instanceof Player) {
			p = (Player) sender;
		} else {
			sender.sendMessage("You must be a player!");
			return true;
		}

		if(args.length < 2){
		}else if(args[0].equalsIgnoreCase("create")){
			// TODO: Implement "/mm create <name>" command
			return true;
		}else if(args[0].equalsIgnoreCase("edit")){
			// TODO: Implement "/mm edit <name>" command
			return true;
		}

		return false;
	}

	@Override
	public void sendHelp(CommandSender sender) {
		sender.sendMessage(BEG + "/mm create <name> " + SEP + "Creates a Mystery Mob.");
		sender.sendMessage(BEG + "/mm delete <name> " + SEP + "Deletes a Mystery Mob.");
		sender.sendMessage(BEG + "/mm give <player> <name> <amount>" + SEP + "Gives a player a rank quest.");
		sender.sendMessage(BEG + "/mm edit <name>" + SEP + "Edits a Mystery Mob.");
		sender.sendMessage(BEG + "/mm list " + SEP + "Lists all Mystery Mobs.");
	}
}

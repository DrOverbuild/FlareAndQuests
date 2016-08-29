package com.drizzard.faq.commands;

import com.drizzard.faq.FlareAndQuests;
import org.bukkit.command.CommandSender;

/**
 * Created by jasper on 6/28/16.
 */
public class FAQCommand extends BasePluginCommand {
    public FAQCommand(FlareAndQuests plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {
    	if(args.length > 0 && args[0].equalsIgnoreCase("reload")){
    		this.plugin.getConf().load();
    		this.plugin.getTrans().load();
    		sender.sendMessage(G+"Successfully reloaded "+Y+"config.yml "+G+"and "+Y+"translations.yml");
    	}	
    	else
    		sendHelp(sender);
        return true;
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(BEG + "/rq " + SEP + "Display rank quest help page.");
        sender.sendMessage(BEG + "/flare " + SEP + "Display flare help page.");
        sender.sendMessage(BEG + "/witem " + SEP + "Display witem help page.");
        sender.sendMessage(BEG + "/mm " + SEP + "Display MysteryMob help page.");
	    sender.sendMessage(BEG + "/glow " + SEP + "Add a glow effect to the item in your hand.");
        sender.sendMessage(BEG + "/faq reload" + SEP + "Reload the Config and Translations file.");
    }
}

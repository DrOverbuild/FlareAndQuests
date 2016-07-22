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
        sendHelp(sender);
        return true;
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(BEG + "/rq " + SEP + "Display rank quest help page.");
        sender.sendMessage(BEG + "/flare " + SEP + "Display flare help page.");
        sender.sendMessage(BEG + "/witem " + SEP + "Display witem help page.");
    }
}

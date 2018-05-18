package io.pocketvote.cmd.guru;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class GuruCommand extends Command {

    public GuruCommand() {
        super("guru", "MCPE Guru help command", "/guru", new String[]{"gu"});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.admin")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to do that.");
            return true;
        }

        sender.sendMessage(TextFormat.AQUA + "### MCPE Guru commands ###");

        sender.sendMessage("/guru " + TextFormat.GRAY + " Shows this command");
        sender.sendMessage("/gulist " + TextFormat.GRAY + "Lists links you've created, use this to find link IDs");
        sender.sendMessage("/guadd [optional title] [link] " + TextFormat.GRAY + "Adds a link");
        sender.sendMessage("/gudel [link ID obtained using /gulist] " + TextFormat.GRAY + "Deletes the specified link");

        return true;
    }
}

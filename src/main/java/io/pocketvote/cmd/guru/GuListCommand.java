package io.pocketvote.cmd.guru;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.task.guru.GetLinksTask;

public class GuListCommand extends Command {

    public GuListCommand() {
        super("gulist", "MCPE Guru list links command", "/gulist", new String[]{"gul"});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.admin")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to do that.");
            return true;
        }
        PocketVote.getPlugin().getServer().getScheduler().scheduleAsyncTask(PocketVote.getPlugin(), new GetLinksTask(sender.getName()));
        return true;
    }
}

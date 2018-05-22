package io.pocketvote.cmd.guru;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.task.guru.DeleteLinkTask;

public class GuDelCommand extends Command {

    public GuDelCommand() {
        super("gudel", "MCPE Guru Delete link command", "/gudel [id]", new String[]{"gud"});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.admin")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to do that!");
            return true;
        }

        if(args.length < 1) {
            sender.sendMessage(TextFormat.RED + "You need to specify an ID to delete, type /gulist to see your links.");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            sender.sendMessage(TextFormat.RED + "[GURU] The specified ID has to be a number.");
            return true;
        }

        if(id < 1) {
            sender.sendMessage(TextFormat.RED + "[GURU] The specified ID has to be 1 or higher.");
            return true;
        }

        PocketVote.getPlugin().getServer().getScheduler().scheduleAsyncTask(PocketVote.getPlugin(), new DeleteLinkTask(sender.getName(), id));
        return true;
    }
}

package io.pocketvote.cmd;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.task.TopVoterTask;

public class VoteCommand extends Command implements PluginIdentifiableCommand {

    private PocketVote plugin;

    public VoteCommand(PocketVote plugin) {
        super("vote", "PocketVote vote command", "/vote [top]", new String[]{"v"});
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.vote")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to use /vote.");
            return true;
        }
        if(args.length > 0 && args[0].equalsIgnoreCase("TOP")) {
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new TopVoterTask(plugin, sender.getName()));
            return true;
        }

        String link = plugin.getVoteManager().getVoteLink();
        if(link == null) {
            if(sender.hasPermission("pocketvote.admin")) {
                sender.sendMessage(TextFormat.YELLOW + "You can add a link by typing /guadd");
                sender.sendMessage(TextFormat.YELLOW + "See /guru for help!");
            } else {
                sender.sendMessage(TextFormat.YELLOW + "The server operator has not added any voting sites.");
            }
            return true;
        }
        if(sender.hasPermission("pocketvote.admin")) sender.sendMessage(TextFormat.YELLOW + "Use /guru to manage this link.");
        sender.sendMessage(TextFormat.AQUA + "You can vote at " + TextFormat.YELLOW + link);
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}

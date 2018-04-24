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
        return false; // TODO: Add vote link here when "top" is not specified.
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}

package io.pocketvote.cmd.guru;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.task.guru.AddLinkTask;
import io.pocketvote.util.ToolBox;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class GuAddCommand extends Command {

    private PocketVote plugin;

    public GuAddCommand() {
        super("guadd", "MCPE Guru Add link command", "/guadd [url]", new String[]{"gua"});
        this.plugin = PocketVote.getPlugin();
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.admin")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to do that!");
            return true;
        }

        if(args.length < 1) {
            sender.sendMessage(TextFormat.RED + "Not enough arguments, example: /guadd Title_Name http://example.com");
            sender.sendMessage(TextFormat.RED + "A title is optional but recommended.");
            return true;
        }

        if(args.length > 2) {
            sender.sendMessage(TextFormat.RED + "Too many arguments, example: /guadd Title_Name http://example.com");
            sender.sendMessage(TextFormat.RED + "A title is optional but recommended.");
            return true;
        }

        HashMap<String, Object> data = new HashMap<>();

        switch(args.length) {
            case 1:
                data.put("title", null);
                data.put("url", args[0]);
                break;

            case 2:
                if(args[0].contains("http://") || args[0].contains("https://") || args[0].contains("www.")) {
                    data.put("url", args[0]);
                    data.put("title", args[1]);
                } else {
                    data.put("title", args[0]);
                    data.put("url", args[1]);
                }
                break;
        }

        try {
            plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AddLinkTask(sender.getName(), ToolBox.createJWT(data)));
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            sender.sendMessage(TextFormat.RED + "[GURU] An error occurred when attempting to perform that command.");
        }
        return true;
    }
}

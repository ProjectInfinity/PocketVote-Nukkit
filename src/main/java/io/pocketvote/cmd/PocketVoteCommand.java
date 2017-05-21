package io.pocketvote.cmd;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.util.ToolBox;

import java.util.Iterator;

public class PocketVoteCommand extends Command implements PluginIdentifiableCommand {

    private PocketVote plugin;

    public PocketVoteCommand(PocketVote plugin) {
        super("pocketvote", "General PocketVote command", "/pocketvote [option]", new String[]{"pv"});
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!sender.hasPermission("pocketvote.admin")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to administer PocketVote.");
            return true;
        }
        if(args.length == 0) {
            sender.sendMessage(TextFormat.AQUA + "Specify an action: SECRET, IDENTITY, CMD, CMDO");
            return true;
        }
        switch(args[0].toUpperCase()) {

            case "IDENTITY":
                if(plugin.lock) {
                    sender.sendMessage(TextFormat.RED + "This command has been locked.");
                    return true;
                }
                if(args.length < 2) {
                    sender.sendMessage(TextFormat.RED + "No identity specified. Get one at pocketvote.io");
                    return true;
                }
                plugin.identity = args[1];
                sender.sendMessage(TextFormat.GREEN + "Successfully set identity.");
                plugin.getConfig().set("identity", plugin.identity);
                plugin.saveConfig();
                break;

            case "SECRET":
                if(plugin.lock) {
                    sender.sendMessage(TextFormat.RED + "This command has been locked.");
                    return true;
                }
                if(args.length < 2) {
                    sender.sendMessage(TextFormat.RED + "No secret specified. gEt one at pocketvote.io");
                    return true;
                }
                plugin.secret = args[1];
                sender.sendMessage(TextFormat.GREEN + "Successfully set secret.");
                plugin.getConfig().set("secret", plugin.secret);
                plugin.saveConfig();
                break;

            case "CMD":
                if(args.length < 2) {
                    sender.sendMessage(TextFormat.RED + "You need to specify a command. Variables: %player, %ip, %site");
                    return true;
                }
                switch(args[1].toUpperCase()) {
                    case "LIST":
                        int i = 0;
                        boolean color = true;
                        sender.sendMessage(TextFormat.YELLOW + "cmd, these run when a vote is made:");
                        for(String cmd : plugin.cmds) {
                            i++;
                            sender.sendMessage((color ? TextFormat.WHITE : TextFormat.GRAY) + Integer.toString(i) + "/" + cmd);
                            color = !color;
                        }
                        break;

                    case "ADD":
                        if(args.length < 3) {
                            sender.sendMessage(TextFormat.RED + "You need to specify a command to add.");
                            return true;
                        }
                        args[0] = null;
                        args[1] = null;
                        String cmd = ToolBox.implode(args, " ");
                        if(cmd.startsWith("/")) cmd = cmd.substring(1);
                        plugin.cmds.add(cmd);

                        sender.sendMessage(TextFormat.GREEN + "Successfully added command.");

                        plugin.getConfig().set("onvote.run-cmd", plugin.cmds);
                        plugin.saveConfig();
                        break;

                    case "REMOVE":
                        if(args.length < 3 || !ToolBox.isNumber(args[2])) {
                            sender.sendMessage(TextFormat.RED + "You need to specify the ID of the command found in LIST.");
                            return true;
                        }
                        i = 0;
                        int icmd = Integer.parseInt(args[2]);
                        Iterator<String> iterator = plugin.cmds.iterator();
                        while(iterator.hasNext()) {
                            String lcmd = iterator.next();
                            i++;
                            if(icmd > i) continue;
                            if(icmd == i) {
                                sender.sendMessage(TextFormat.GREEN + "Deleted " + lcmd + ".");
                                iterator.remove();
                                plugin.getConfig().set("onvote.run-cmd", plugin.cmds);
                                plugin.saveConfig();
                                return true;
                            }
                        }
                        break;

                    default:
                        sender.sendMessage(TextFormat.RED + "Invalid option. Use list, add or remove.");
                }
                break;

            case "CMDO":
                if(args.length < 2) {
                    sender.sendMessage(TextFormat.RED + "You need to specify a command. Variables: %player, %ip, %site");
                    return true;
                }
                switch(args[1].toUpperCase()) {
                    case "LIST":
                        int i = 0;
                        boolean color = true;
                        sender.sendMessage(TextFormat.YELLOW + "cmdo, these run when the player is online:");
                        for(String cmd : plugin.cmdos) {
                            i++;
                            sender.sendMessage((color ? TextFormat.WHITE : TextFormat.GRAY) + Integer.toString(i) + "/" + cmd);
                            color = !color;
                        }
                        break;

                    case "ADD":
                        if(args.length < 3) {
                            sender.sendMessage(TextFormat.RED + "You need to specify a command to add.");
                            return true;
                        }
                        args[0] = null;
                        args[1] = null;
                        String cmd = ToolBox.implode(args, " ");
                        if(cmd.startsWith("/")) cmd = cmd.substring(1);
                        plugin.cmdos.add(cmd);

                        sender.sendMessage(TextFormat.GREEN + "Successfully added command.");

                        plugin.getConfig().set("onvote.online-cmd", plugin.cmdos);
                        plugin.saveConfig();
                        break;

                    case "REMOVE":
                        if(args.length < 3 || !ToolBox.isNumber(args[2])) {
                            sender.sendMessage(TextFormat.RED + "You need to specify the ID of the command found in LIST.");
                            return true;
                        }
                        i = 0;
                        int icmd = Integer.parseInt(args[2]);
                        Iterator<String> iterator = plugin.cmdos.iterator();
                        while(iterator.hasNext()) {
                            String lcmd = iterator.next();
                            i++;
                            if(icmd > i) continue;
                            if(icmd == i) {
                                sender.sendMessage(TextFormat.GREEN + "Deleted " + lcmd + ".");
                                iterator.remove();
                                plugin.getConfig().set("onvote.online-cmd", plugin.cmdos);
                                plugin.saveConfig();
                                return true;
                            }
                        }
                        break;

                    default:
                        sender.sendMessage(TextFormat.RED + "Invalid option. Use list, add or remove.");
                }
                break;

            default:
                sender.sendMessage(TextFormat.RED + "Invalid option. Specify SECRET, IDENTITY, CMD or CMDO.");
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

}
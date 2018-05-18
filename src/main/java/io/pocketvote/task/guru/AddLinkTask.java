package io.pocketvote.task.guru;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.task.ApiRequest;

import java.util.HashMap;

public class AddLinkTask extends ApiRequest {

    private PocketVote plugin;
    private String name;

    public AddLinkTask(String name, String token) {
        super(PocketVote.getPlugin().isDev() ? "http://dev.mcpe.guru/api/link" : "https://mcpe.guru/api/link", "POST", "ADDLINK", new HashMap<>() {
            {
                put("token", token);
            }
        });
        this.plugin = PocketVote.getPlugin();
        this.name = name;
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = name.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(name);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Got no response when adding link to MCPE.Guru");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            player.sendMessage(TextFormat.RED + "[GURU] An error occurred while performing this command.");
            server.getLogger().error("[PocketVote] Curl error: " + result.getMessage());
            return;
        }

        player.sendMessage(TextFormat.GREEN + "[GURU] Link successfully added!");
        if(result.getRawPayload().hasNonNull("url") && PocketVote.getPlugin().getVoteManager().getVoteLink() == null) PocketVote.getPlugin().getVoteManager().setVoteLink(result.getRawPayload().get("url").asText());
    }
}
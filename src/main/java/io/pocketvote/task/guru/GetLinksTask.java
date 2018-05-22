package io.pocketvote.task.guru;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.task.ApiRequest;

import java.util.Iterator;

public class GetLinksTask extends ApiRequest {

    private String name;

    public GetLinksTask(String name) {
        super(PocketVote.getPlugin().isDev() ? "http://dev.mcpe.guru/api/links" : "https://mcpe.guru/api/links", "GET", "GETLINKS", null);
        this.name = name;
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = name.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(name);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Got no response when retrieving links from MCPE.Guru");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            player.sendMessage(TextFormat.RED + "[GURU] An error occurred while performing this command.");
            server.getLogger().error("[PocketVote] Curl error: " + result.getMessage());
            return;
        }

        if(!result.isSuccessful()) {
            player.sendMessage(TextFormat.RED + "[GURU] Failed to retrieve links.");
            return;
        }

        if(!result.hasPayload()) {
            player.sendMessage(TextFormat.YELLOW + "[GURU] There are no added links, use " + TextFormat.AQUA + "/guadd [url]" + TextFormat.YELLOW + " to add a link!");
            return;
        }

        player.sendMessage(TextFormat.GREEN + "------ Voting links ------");

        boolean color = false;
        for(Iterator<JsonNode> it = result.getRawPayload().iterator(); it.hasNext();) {
            JsonNode json = it.next();
            if(json == null) continue;
            TextFormat c = color ? TextFormat.AQUA : TextFormat.YELLOW;
            player.sendMessage(c + "ID: " + json.get("id").asText());
            player.sendMessage(c + "Title: " + json.get("title").asText());
            player.sendMessage(c + "URL: " + json.get("url").asText());

            color = !color;
        }
    }
}

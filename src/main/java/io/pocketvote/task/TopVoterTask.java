package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;

public class TopVoterTask extends ApiRequest {

    private final String player;

    public TopVoterTask(PocketVote plugin, String player) {
        super(plugin.isDev() ? "http://127.0.0.1:9000/v2/top/10" : "https://api.pocketvote.io/v2/top/10", "GET", "TOP", null);
        this.player = player;

        plugin.getLogger().debug("Getting top voters.");
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = this.player.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(this.player);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Failed to retrieve top voters. Try again later.");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            player.sendMessage(TextFormat.RED + "An error occurred while contacting the PocketVote servers, please try again later.");
            server.getLogger().error("[PocketVote] TopVoterTask: " + result.getMessage());
            return;
        }

        if(!result.hasPayload() || !result.getRawPayload().isArray()) {
            server.getLogger().debug("[PocketVote] TopVoterTask: No payload or payload was not an array.");
            return;
        }

        player.sendMessage(TextFormat.AQUA + "### Current top 10 voters ###");
        int rank = 1;
        boolean color = true;

        for(final JsonNode vote : result.getRawPayload()) {
            player.sendMessage("" + (color ? TextFormat.WHITE : TextFormat.GRAY) + rank + ". " + vote.get("player").asText() + " (" + vote.get("votes").asInt() + ")");
            rank++;
            color = !color;
        }

        if(result.getRawPayload().size() == 0) player.sendMessage(TextFormat.GRAY + "No voters found, start voting!");
    }

}
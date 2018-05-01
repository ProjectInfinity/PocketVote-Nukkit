package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;

public class DiagnoseTask extends ApiRequest {

    private String player;

    public DiagnoseTask(PocketVote plugin, String player) {
        super(plugin.isDev() ? "http://127.0.0.1:9000/v2/diagnose" : "https://api.pocketvote.io/v2/diagnose", "GET", "DIAGNOSE", null);
        this.player = player;
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = this.player.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(this.player);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Failed to diagnose PocketVote. Try again later.");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            player.sendMessage(TextFormat.RED + "An error occurred while contacting the PocketVote servers, please try again later.");
            server.getLogger().error("[PocketVote] DiagnoseTask: " + result.getMessage());
            return;
        }

        if(!result.hasPayload()) {
            player.sendMessage(TextFormat.RED + "Please wait before trying to use this command again.");
            server.getLogger().debug("[PocketVote] DiagnoseTask: No payload, possibly rate limit.");
            return;
        }

        JsonNode payload = result.getRawPayload();

        player.sendMessage((payload.get("foundServer").asBoolean() ? TextFormat.GREEN + "Yes -" : TextFormat.RED + "No -") + " Found server");
        player.sendMessage((payload.get("hasVotes").asBoolean() ? TextFormat.GREEN + "Yes -" : TextFormat.RED + "No -") + " Has votes (trivial)");

        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(PocketVote.getPlugin().secret.getBytes("UTF-8")).parseClaimsJws(payload.get("voteSample").asText());

            if(claims.getBody().size() == 0) throw new Exception("Failed to parse token");

            if(!claims.getBody().get("player").toString().equalsIgnoreCase("PocketVoteSample") ||
                    !claims.getBody().get("ip").toString().equalsIgnoreCase("127.0.0.1") ||
                    !claims.getBody().get("site").toString().equalsIgnoreCase("PocketVote.io")) {
                throw new Exception("Token did not meet expectations.");
            } else {
                player.sendMessage(TextFormat.GREEN + "Yes - Decode sample vote.");
            }
        } catch(Exception e) {
            player.sendMessage(TextFormat.RED + "No - Decode sample vote");
            player.sendMessage(TextFormat.YELLOW + "Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        player.sendMessage(TextFormat.YELLOW + "A test vote will be dispatched momentarily. If more than a couple of minutes passes the dispatch has failed.");
    }

}
package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;

import java.util.HashMap;

public class SetLinkNameTask extends ApiRequest {

    private String player;
    private String name;

    public SetLinkNameTask(PocketVote plugin, String player, String name, String token) {
        super(plugin.isDev() ? "http://dev.mcpe.guru/api/name" : "https://mcpe.guru/api/name", "POST", "SETLINK", new HashMap<>() {
            {
                put("token", token);
            }
        });
        this.player = player;
        this.name = name;
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = this.player.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(this.player);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Failed to set link name.");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            if(!result.hasPayload()) {
                player.sendMessage(TextFormat.RED + "Please wait before trying to use this command again.");
                server.getLogger().debug("[PocketVote] DiagnoseTask: No payload, possibly rate limit.");
                return;
            }

            JsonNode payload = result.getRawPayload().get("error");
            if(payload.hasNonNull("code")) {
                switch(payload.get("code").asInt()) {
                    case 100:
                        player.sendMessage(TextFormat.RED + "A token error occurred, check your server log for more info!");
                        server.getLogger().error("[PocketVote] Token error during SetLinkNameTask:");
                        server.getLogger().error("[PocketVote] " + payload.get("message").asText());
                        return;

                    case 400:
                        player.sendMessage(TextFormat.RED + "The API did not receive a name, please try again.");
                        return;

                    case 403:
                        player.sendMessage(TextFormat.RED + "Provided name is already in use, please choose a different one.");
                        return;

                    case 500:
                        player.sendMessage(TextFormat.RED + "Failed to update name. Please try again later.");
                        return;

                    default:
                        player.sendMessage(TextFormat.DARK_RED + "An unexpected error occurred.");
                        server.getLogger().error("[PocketVote] Uncaught error code occurred during SetLinkNameTask!");
                        return;
                }
            }

            player.sendMessage(TextFormat.RED + "An error occurred while contacting the MCPE.Guru servers, please try again later.");
            server.getLogger().error("[PocketVote] SetLinkNameTask: " + result.getMessage());
            return;
        }

        PocketVote.getPlugin().getVoteManager().setVoteLink("mcpe.guru/" + name);
        player.sendMessage(TextFormat.GREEN + "Your link is now " + PocketVote.getPlugin().getVoteManager().getVoteLink());
    }
}
package io.pocketvote.task.guru;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.task.ApiRequest;

public class DeleteLinkTask extends ApiRequest {

    private String name;

    public DeleteLinkTask(String name, int id) {
        super(PocketVote.getPlugin().isDev() ? "http://dev.mcpe.guru/api/link/" + id : "https://mcpe.guru/api/link/" + id, "DELETE", "DELETELINK", null);
        this.name = name;
    }

    @Override
    public void onCompletion(Server server) {
        CommandSender player = name.equalsIgnoreCase("CONSOLE") ? new ConsoleCommandSender() : server.getPlayer(name);
        if(player == null) return;

        if(!(super.getResult() instanceof TaskResult) || !hasResult() || !(getResult() instanceof TaskResult)) {
            player.sendMessage(TextFormat.RED + "Got no response when trying to delete link from MCPE.Guru");
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            player.sendMessage(TextFormat.RED + "[GURU] An error occurred while performing this command.");
            server.getLogger().error("[PocketVote] Curl error: " + result.getMessage());
            return;
        }


        if(!result.isSuccessful() && !result.hasPayload()) {
            player.sendMessage(TextFormat.RED + "[GURU] Couldn't find your server.");
            return;
        }

        if(!result.isSuccessful() && result.hasPayload()) {
            if(result.getRawPayload().get("error").get("code").asInt()== 403) player.sendMessage(TextFormat.RED + "[GURU] You do not have permission to delete that link.");
            if(result.getRawPayload().get("error").get("code").asInt() == 500) player.sendMessage(TextFormat.RED + "[GURU] Failed to delete link.");
            return;
        }

        player.sendMessage(TextFormat.GREEN + "[GURU] Successfully deleted link.");
    }
}

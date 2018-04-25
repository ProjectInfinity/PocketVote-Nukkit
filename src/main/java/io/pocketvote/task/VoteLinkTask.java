package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;

public class VoteLinkTask extends ApiRequest {

    public VoteLinkTask(PocketVote plugin) {
        super(plugin.isDev() ? "http://dev.mcpe.guru/api/link" : "https://mcpe.guru/api/link", "GET", "LINK", null);
    }

    @Override
    public void onCompletion(Server server) {
        if(!(super.getResult() instanceof TaskResult)) {
            server.getLogger().error("[PocketVote] Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        if(!hasResult() || !(getResult() instanceof TaskResult)) {
            server.getLogger().warning("[PocketVote] Failed to retrieve voting link from MCPE.guru, the API may be down.");
            return;
        }

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            server.getLogger().error(TextFormat.DARK_RED + "[PocketVote] VoteLinkTask: " + result.getMessage());
            return;
        }

        if(!result.hasPayload()) {
            server.getLogger().info(TextFormat.YELLOW + "[PocketVote] No vote link to retrieve, add a entry using /guru to create a link.");
            return;
        }

        if(result.getRawPayload().hasNonNull("url")) {
            server.getLogger().info(TextFormat.YELLOW + "[PocketVote] Voting link set to " + result.getRawPayload().get("url").asText());
            PocketVote.getPlugin().getVoteManager().setVoteLink(result.getRawPayload().get("url").asText());
        }
    }
}
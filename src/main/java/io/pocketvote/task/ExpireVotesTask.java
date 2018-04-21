package io.pocketvote.task;

import cn.nukkit.scheduler.Task;
import io.pocketvote.PocketVote;

public class ExpireVotesTask extends Task {

    private PocketVote plugin;

    public ExpireVotesTask(PocketVote plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRun(int i) {
        plugin.getLogger().debug("Cleaning up expired votes.");
        int expired = plugin.getVoteManager().expireVotes();
        if(expired > 0) plugin.getLogger().debug("Expired " + expired + " votes.");
    }

}
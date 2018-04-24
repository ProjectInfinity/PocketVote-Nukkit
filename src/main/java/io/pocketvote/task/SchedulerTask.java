package io.pocketvote.task;

import cn.nukkit.scheduler.Task;
import io.pocketvote.PocketVote;

public class SchedulerTask extends Task {

    private PocketVote plugin;

    public SchedulerTask(PocketVote plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRun(int i) {
        plugin.getLogger().debug("Checking for votes.");

        if(!plugin.multiserver || plugin.multiserverRole.equalsIgnoreCase("master")) {
            if(plugin.secret != null && !plugin.secret.isEmpty() && plugin.identity != null && !plugin.identity.isEmpty()) {
                plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new VoteCheckTask(plugin));
            } else {
                plugin.getLogger().critical("Please finish configuring PocketVote, then restart your server.");
            }
        }

        if(plugin.multiserver && plugin.multiserverRole.equalsIgnoreCase("slave")) plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new SlaveCheckTask(plugin));
    }
}

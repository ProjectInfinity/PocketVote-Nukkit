package io.pocketvote.util;

import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pocketvote.PocketVote;
import io.pocketvote.data.VRCRecord;
import io.pocketvote.data.Vote;
import io.pocketvote.task.VRCCheckTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class VoteManager {

    private PocketVote plugin;

    private List<Vote> votes;
    private ArrayList<VRCRecord> vrcs;

    private HashMap<String, TaskHandler> vrcTasks;

    private String voteLink;

    public VoteManager(PocketVote plugin) {
        this.plugin = plugin;
        this.votes = loadVotes();
        this.vrcs = new ArrayList<>();
        this.vrcTasks = new HashMap<>();
    }

    public boolean hasVotes(String player) {
        for(Vote vote : votes) {
            if(vote.getPlayer().equalsIgnoreCase(player)) return true;
        }
        return false;
    }

    public void setVoteLink(String voteLink) {
        this.voteLink = voteLink;
    }

    public String getVoteLink() {
        return voteLink;
    }

    /*public Iterator<HashMap<String, String>> getVotes(String player) {
        Iterator<HashMap<String, String>> iterator = this.votes.iterator();
        ArrayList<HashMap<String, String>> votes = new ArrayList<>();

        while(iterator.hasNext()) {
            HashMap<String, String> vote = iterator.next();
            if(vote.get("player").equalsIgnoreCase(player)) {
                votes.add(vote);
            }
        }
        return votes.iterator();
    }*/

    public Iterator<Vote> getVotes() {
        return votes.iterator();
    }

    public void addVote(String player, String site, String ip) {
        votes.add(new Vote(player, site, ip, Instant.now().getEpochSecond() + (86400 * plugin.getConfig().getInt("vote-expiration", 7))));
    }

    public void commit() {
    	List<HashMap<String, String>> commitVote = new ArrayList<>();
    	for (Vote vote : votes ) {
    		commitVote.add(vote.saveVote());
    	}
        plugin.getConfig().set("votes", commitVote);
        plugin.saveConfig();
    }

    public void loadVRCs() throws IOException {
        File vrcDir = new File(plugin.getServer().getPluginPath() + "PocketVote/vrc");

        if(!vrcDir.isDirectory()) return;

        File[] files = vrcDir.listFiles((file, name) -> name.endsWith(".vrc"));
        if(files == null) {
            plugin.getLogger().error("The VRC directory returned NULL when attempting to list files.");
            return;
        }

        for(File file : files) {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(content);

            if(!json.hasNonNull("website") || !json.hasNonNull("check") || !json.hasNonNull("claim")) {
                plugin.getLogger().error("The VRC file located at " + file.getAbsolutePath() + " is missing one or more required fields. We won't load this file.");
                return;
            }

            VRCRecord vrc = new VRCRecord(
                    json.get("website").asText(),
                    json.get("check").asText(),
                    json.get("claim").asText()
            );

            vrcs.add(vrc);
            plugin.getLogger().info(TextFormat.GREEN + "VRC enabled for " + vrc.getWebsiteUrl());
        }

        if(vrcs.size() > 0) plugin.useVRC = true;
    }

    public VRCRecord[] getVRCs() {
        return vrcs.toArray(new VRCRecord[0]);
    }

    public void scheduleVRCTask(String player) {
        if(plugin.useVRC && !plugin.multiserver || (plugin.useVRC && plugin.multiserverRole.equals("master"))) {

            if(vrcTasks.containsKey(player)) return;
            // Only run when VRC is enabled and multiserver is off or VRC is enabled and multiserver and server role is set to master.
            TaskHandler handler = plugin.getServer().getScheduler().scheduleAsyncTask(this.plugin, new VRCCheckTask(this.plugin, player));
            vrcTasks.put(player, handler);
        }
    }

    public void removeVRCTask(String player) {
        if(!vrcTasks.containsKey(player)) return;
        vrcTasks.remove(player);
    }

    public int expireVotes() {
        int expired = 0;
        Iterator<Vote> votes = getVotes();

        if(!votes.hasNext()) return expired;
        while(votes.hasNext()) {
            Vote vote = votes.next();
            
            if(Instant.now().getEpochSecond() > vote.getExpires()) {
            	System.out.println(vote.getPlayer() + " delted");
                votes.remove();
                expired++;
            }
        }
        commit();
        return expired;
    }
    
    private List<Vote> loadVotes() {
		List<Vote> loadVotes = new ArrayList<>();
		if(plugin.getConfig().getList("votes") != null) {
			for (Object v : (ArrayList<?>) plugin.getConfig().getList("votes", new ArrayList<Object>())) {
				if (v instanceof ConfigSection) {
					loadVotes.add(new Vote((ConfigSection) v));
				} else {
					plugin.getLogger().debug("unknown vote data structure");
				}
			}			
		}
		return loadVotes;
	}

}
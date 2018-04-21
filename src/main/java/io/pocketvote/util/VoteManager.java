package io.pocketvote.util;

import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pocketvote.PocketVote;
import io.pocketvote.data.VRCRecord;
import io.pocketvote.task.VRCCheckTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class VoteManager {

    private PocketVote plugin;

    private ArrayList<HashMap<String, String>> votes;
    private ArrayList<VRCRecord> vrcs;

    private HashMap<String, TaskHandler> vrcTasks;

    public VoteManager(PocketVote plugin) {
        this.plugin = plugin;
        this.votes = plugin.getConfig().get("votes", new ArrayList<>());
        this.vrcs = new ArrayList<>();
        this.vrcTasks = new HashMap<>();
    }

    public boolean hasVotes(String player) {
        for(HashMap<String, String> vote : votes) {
            if(vote.get("player").equalsIgnoreCase(player)) return true;
        }
        return false;
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

    public Iterator<HashMap<String, String>> getVotes() {
        return votes.iterator();
    }

    public void addVote(String player, String site, String ip) {
        HashMap<String, String> vote = new HashMap<>();
        vote.put("player", player);
        vote.put("site", site);
        vote.put("ip", ip);
        votes.add(vote);
    }

    public void commit() {
        plugin.getConfig().set("votes", votes);
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
        Iterator<HashMap<String, String>> votes = getVotes();

        if(!votes.hasNext()) return expired;
        while(votes.hasNext()) {
            HashMap<String, String> vote = votes.next();
            if(!vote.containsKey("expires") || Instant.now().getEpochSecond() > Long.valueOf(String.valueOf(vote.get("expires")))) {
                votes.remove();
                expired++;
            }
        }
        commit();
        return expired;
    }

}
package io.pocketvote.util;

import io.pocketvote.PocketVote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class VoteManager {

    private PocketVote plugin;

    private ArrayList<HashMap<String, String>> votes;

    public VoteManager(PocketVote plugin) {
        this.plugin = plugin;
        this.votes = plugin.getConfig().get("votes", new ArrayList<>());
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

}
package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.data.VRCRecord;
import io.pocketvote.event.VoteEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class VRCCheckTask extends AsyncTask {

    private PocketVote plugin;
    private String player;
    private String version;
    private VRCRecord[] vrcs;

    public VRCCheckTask(PocketVote plugin, String player) {
        this.plugin = plugin;
        this.player = player;
        this.vrcs = plugin.getVoteManager().getVRCs();
        this.version = plugin.getDescription().getVersion();
    }

    @Override
    public void onRun() {
        HashSet<TaskResult> results = new HashSet<>();

        for(VRCRecord vrc : vrcs) {
            plugin.getLogger().debug("Starting VRCCheckTask for " + vrc.getWebsiteUrl());
            String url = vrc.getCheckUrl().replace("{USERNAME}", player);

            try {
                URL obj = new URL(url);
                HttpURLConnection con;
                if(url.startsWith("www") || url.startsWith("http://")) {
                    con = (HttpURLConnection) obj.openConnection();
                } else if(url.startsWith("https://")) {
                    con = (HttpsURLConnection) obj.openConnection();
                } else {
                    results.add(createErrorResult("Website URL did not start with http(s):// or www."));
                    return;
                }

                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "PocketVote Nukkit v" + version);

                int responseCode = con.getResponseCode();

                // If the response code is not 200, assume something went wrong.
                if(responseCode != 200) {
                    results.add(createResult(false, true, null));
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.toString());

                if(!json.hasNonNull("voted") || !json.hasNonNull("claimed")) {
                    results.add(createErrorResult("Vote or claim field missing in response from " + vrc.getWebsiteUrl()));
                    return;
                }

                // Claim failed.
                if(json.get("voted").asBoolean() && !json.get("claimed").asBoolean()) {
                    results.add(createErrorResult("Attempted to claim a vote but it failed. Site:" + vrc.getWebsiteUrl()));
                    return;
                }

                final ObjectMapper rMapper = new ObjectMapper();

                ObjectNode payloadNode = rMapper.createObjectNode();
                payloadNode.set("player", mapper.convertValue(player, JsonNode.class));
                payloadNode.set("ip", mapper.convertValue("unknown", JsonNode.class));
                payloadNode.set("site", mapper.convertValue(vrc.getWebsiteUrl(), JsonNode.class));

                ObjectNode rootNode = rMapper.createObjectNode();
                rootNode.set("success", mapper.convertValue(true, JsonNode.class));
                rootNode.set("payload", payloadNode);

                // Vote claim succeeded!
                results.add(createResult(true, false, rootNode));

            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        setResult(results);
    }

    private TaskResult createErrorResult(String message) {
        return new TaskResult(null, true, false, message);
    }

    private TaskResult createResult(boolean success, boolean error, JsonNode json) {
        TaskResult result = new TaskResult();

        result.setIsSuccessful(success);
        result.setError(error);

        if(error) {
            if(json == null) {
                result.setMessage("A error occurred during communication with a voting site.");
            } else {
                result.setMessage(json.get("message").asText("An unspecified error occurred."));
            }
        } else {
            // Check if we had votes.
            if(json.hasNonNull("payload") && json.get("success").asBoolean(false)) {
                if(!json.get("payload").isArray()) return result;
                ArrayList<LinkedHashMap<String, String>> votes = new ArrayList<>();
                for(final JsonNode j : json.get("payload")) {
                    LinkedHashMap<String, String> vote = new LinkedHashMap<>();
                    vote.put("ip", json.get("ip").asText("127.0.0.1"));
                    vote.put("player", json.get("player").asText());
                    vote.put("site", json.get("site").asText());

                    votes.add(vote);
                }

                result.setVotes(votes);
            }
        }

        return result;
    }

    @Override
    public void onCompletion(Server server) {
        if(!hasResult()) {
            server.getLogger().emergency("[PocketVote] A VRC task finished without a result. This should never happen.");
            return;
        }

        if(!(getResult() instanceof TaskResult[])) {
            server.getLogger().warning("[PocketVote] VRCCheckTask result was not an array. This is a problem...");
            return;
        }

        TaskResult[] results = (TaskResult[]) getResult();

        for(TaskResult result : results) {
            if(result.hasError()) {
                server.getLogger().error("[PocketVote] VRCCheckTask: An issue occurred, you can ignore this unless it happens often: " + result.getMessage());
                continue;
            }

            ArrayList<LinkedHashMap<String, String>> votes = result.getVotes();

            for(LinkedHashMap<String, String> vote : votes) {
                server.getPluginManager().callEvent(new VoteEvent(vote.get("player"), vote.get("ip"), "VRC Site"));
            }
        }

        plugin.getVoteManager().removeVRCTask(player);
    }
}

package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.pocketvote.PocketVote;
import io.pocketvote.data.TaskResult;
import io.pocketvote.event.VoteEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class VoteCheckTask extends ApiRequest {

    private PocketVote plugin;
    private String identity;
    private String secret;
    private String version;
    private boolean dev;

    public VoteCheckTask(PocketVote plugin, String identity, String secret, String version) {
        super(plugin.isDev() ? "http://127.0.0.1:9000/v2/check" : "https://api.pocketvote.io/v2/check", "GET", null);
        this.plugin = plugin;
        this.identity = identity;
        this.secret = secret;
        this.version = version;
        this.dev = plugin.isDev();

        plugin.getLogger().debug("Checking for outstanding votes.");
    }

    @Override
    public void onCompletion(Server server) {
        if(!(super.getResult() instanceof TaskResult)) {
            server.getLogger().error("Result of " + getClass().getCanonicalName() + " was not an instance of TaskResult.");
            return;
        }

        if(!hasResult() || !(getResult() instanceof TaskResult)) return;

        TaskResult result = (TaskResult) getResult();

        if(result.hasError()) {
            server.getLogger().error("[PocketVote] VoteCheckTask: " + result.getMessage());
            return;
        }

        // Having no claims is the same has having no votes to process.
        if(!result.hasClaims() || !result.hasVotes()) {
            server.getLogger().debug("[PocketVote] " + result.getMessage());
            if(result.hasMeta()) plugin.startScheduler(result.getMeta().containsKey("frequency") ? (int) result.getMeta().get("frequency") : 60);
            return;
        }

        for(LinkedHashMap<String, String> vote : result.getVotes()) {
            server.getPluginManager().callEvent(new VoteEvent(vote.get("player"), vote.get("ip"), vote.get("site")));
        }

        if(result.hasMeta()) plugin.startScheduler(result.getMeta().containsKey("frequency") ? (int) result.getMeta().get("frequency") : 60);
    }

}
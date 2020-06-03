package io.pocketvote.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TaskResult {

    private ArrayList<LinkedHashMap<String, String>> votes;
    private boolean success;
    private boolean error;
    private String message;
    private HashMap<String, Object> meta;
    private Claims claims = null;
    private JsonNode rawPayload = null;

    public TaskResult() {
        this.votes = new ArrayList<>();
        this.error = false;
        this.message = null;
        this.success = false;
    }

    public TaskResult(ArrayList<LinkedHashMap<String, String>> votes) {
        this.votes = votes;
        this.message = null;
        this.error = false;
        this.success = true;
    }

    public TaskResult(ArrayList<LinkedHashMap<String, String>> votes, boolean error, boolean success, String message) {
        this.votes = votes;
        this.error = error;
        this.message = message;
        this.success = success;
    }

    public void setClaims(Claims claims, boolean process) {
        this.claims = claims;

        // If false we do not want to process claims into votes.
        if(!process) return;

        ArrayList<LinkedHashMap<String, String>> votes = new ArrayList<>();

        for(Object obj : (ArrayList) claims.values().toArray()[0]) {
            if(!(obj instanceof LinkedHashMap)) continue;
            LinkedHashMap<String, String> vote = (LinkedHashMap<String, String>) obj;
            if(!vote.containsKey("ip")) vote.put("ip", "127.0.0.1");
            votes.add(vote);
        }

        setVotes(votes);
    }

    public boolean hasError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public boolean hasVotes() {
        return votes.size() > 0;
    }

    public void setVotes(ArrayList<LinkedHashMap<String, String>> votes) {
        this.votes = votes;
    }

    public ArrayList<LinkedHashMap<String, String>> getVotes() {
        return votes;
    }

    public HashMap<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(JsonNode meta) {
        HashMap<String, Object> m = new HashMap<>();
        m.put("frequency", meta.get("frequency").asInt());
        this.meta = m;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode payload) {
        rawPayload = payload;
    }

    public boolean hasPayload() {
        return rawPayload != null;
    }

    public boolean hasClaims() {
        return claims != null;
    }

    public boolean hasMeta() {
        return meta != null && meta.size() > 0;
    }

    public boolean isSuccessful() {
        return success;
    }

    public void setIsSuccessful(boolean success) {
        this.success = success;
    }

}
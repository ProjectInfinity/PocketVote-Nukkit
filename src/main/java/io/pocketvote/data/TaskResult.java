package io.pocketvote.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskResult {

    private ArrayList<JsonNode> votes;
    private boolean error;
    private HashMap<String, ?> errorData;
    private HashMap<String, ?> meta;

    public TaskResult() {
        this.votes = new ArrayList<>();
        this.error = false;
        this.errorData = new HashMap<>();
    }

    public TaskResult(ArrayList<JsonNode> votes) {
        this.votes = votes;
        this.errorData = new HashMap<>();
        this.error = false;
    }

    public TaskResult(ArrayList<JsonNode> votes, boolean error, HashMap<String, ?> errorData) {
        this.votes = votes;
        this.error = error;
        this.errorData = errorData;
    }

    public boolean hasError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setErrorData(HashMap<String, ?> errorData) {
        this.errorData = errorData;
    }

    public HashMap<String, ?> getError() {
        return this.errorData;
    }

    public boolean hasVotes() {
        return votes.size() > 0;
    }

    public void setVotes(ArrayList<JsonNode> votes) {
        this.votes = votes;
    }

    public ArrayList<JsonNode> getVotes() {
        return votes;
    }

    public HashMap<String, ?> getMeta() {
        return meta;
    }

    public void setMeta(HashMap<String, ?> meta) {
        this.meta = meta;
    }

}
package io.pocketvote.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskResult {

    private ArrayList<JsonNode> votes;
    private boolean error;
    private String errorMessage;
    private HashMap<String, ?> meta;

    public TaskResult() {
        this.votes = new ArrayList<>();
        this.error = false;
        this.errorMessage = null;
    }

    public TaskResult(ArrayList<JsonNode> votes) {
        this.votes = votes;
        this.errorMessage = null;
        this.error = false;
    }

    public TaskResult(ArrayList<JsonNode> votes, boolean error, String errorMessage) {
        this.votes = votes;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
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
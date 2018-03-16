package io.pocketvote.data;

public class VRCRecord {

    private String websiteUrl, checkUrl, claimUrl;

    public VRCRecord() {}

    public VRCRecord(String websiteUrl, String checkUrl, String claimUrl) {
        this.websiteUrl = websiteUrl;
        this.checkUrl = checkUrl;
        this.claimUrl = claimUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getClaimUrl() {
        return claimUrl;
    }

    public void setClaimUrl(String claimUrl) {
        this.claimUrl = claimUrl;
    }
}
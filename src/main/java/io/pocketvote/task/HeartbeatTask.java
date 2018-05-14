package io.pocketvote.task;

import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import io.pocketvote.PocketVote;
import io.pocketvote.util.ToolBox;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HeartbeatTask extends AsyncTask {

    private HashMap<String, Object> meta;
    private String identity;
    private String secret;
    private boolean isDev;
    private String version;

    public HeartbeatTask() {
        PocketVote plugin = PocketVote.getPlugin();
        this.identity = plugin.identity;
        this.isDev = plugin.isDev();
        this.meta = new HashMap<>();
        this.secret = plugin.secret;
        this.version = plugin.getDescription().getVersion();

        /*
         * Used for a heartbeat on startup.
         *
         * The information is simply used to gauge what kind of servers run
         * PocketVote and how and what I need to optimise.
         */
        Config config = plugin.getConfig();
        meta.put("pluginVersion", plugin.getDescription().getVersion());
        meta.put("mcpeVersion", plugin.getServer().getVersion());
        meta.put("serverVersion", plugin.getServer().getNukkitVersion());
        meta.put("serverApiVersion", plugin.getServer().getApiVersion());
        meta.put("serverPort", plugin.getServer().getPort());
        meta.put("serverName", plugin.getServer().getName());

        meta.put("pluginConfig", new HashMap<String, Object>() {{
            put("multi-server", config.get("multi-server.enabled", false));
            put("multi-server-role", config.get("multi-server.role", "master"));
            put("lock", config.get("lock", false));
            put("vote-expiration", config.get("vote-expiration", 7));
        }});

    }

    @Override
    public void onRun() {
        if(secret == null || identity == null) return;
        String url = isDev ? "http://127.0.0.1:9000/v2/heartbeat" : "https://api.pocketvote.io/v2/heartbeat";
        HttpURLConnection con;
        try {
            URL obj = new URL(url);
            if(url.startsWith("https://")) {
                con = (HttpsURLConnection) obj.openConnection();
            } else {
                con = (HttpURLConnection) obj.openConnection();
            }

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "PocketVote Nukkit v" + version);
            con.setRequestProperty("Identity", identity);
            con.setDoOutput(true);

            byte[] postData = ("token=" + ToolBox.createJWT(meta)).getBytes();
            con.setRequestProperty("Content-Length", Integer.toString(meta.size()));
            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }
            con.getResponseCode();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
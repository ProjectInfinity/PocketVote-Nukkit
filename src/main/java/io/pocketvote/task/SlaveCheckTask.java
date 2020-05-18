package io.pocketvote.task;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import io.pocketvote.PocketVote;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SlaveCheckTask extends AsyncTask {

    private PocketVote plugin;

    public String mysqlHost;
    public int mysqlPort;
    public String mysqlUsername;
    public String mysqlPassword;
    public String mysqlDatabase;

    public String hash;

    public SlaveCheckTask(PocketVote plugin) {
        this.plugin = plugin;
        this.mysqlHost = plugin.mysqlHost;
        this.mysqlPort = plugin.mysqlPort;
        this.mysqlUsername = plugin.mysqlUsername;
        this.mysqlPassword = plugin.mysqlPassword;
        this.mysqlDatabase = plugin.mysqlDatabase;
        this.hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((plugin.getServer().getIp() + Integer.toString(plugin.getServer().getPort())).getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            this.hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRun() {
        // Should never happen but let's be safe.
        if(hash == null) return;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + "/" + mysqlDatabase + "?" +
                    "user=" + mysqlUsername + "&password=" + mysqlPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(conn == null) {
            plugin.getLogger().error("Failed to connect to the database!");
            return;
        }

        ArrayList<String> ignore = new ArrayList<>();

        int time = Math.round(System.currentTimeMillis() / 1000) - 432000;

        // Load up ignored votes.
        try {

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pocketvote_checks WHERE `server_hash` = ? AND `timestamp` > " + time);
            stmt.setString(1, hash);
            ResultSet result = stmt.executeQuery();
            stmt.close();
            // No results, exit.
            if(!result.isBeforeFirst()) return;

            while(result.next()) {
                ignore.add(Integer.toString(result.getInt("vote_id")));
            }

            // We're done grabbing information from the database.
            result.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // TODO: continue here, merge array to string and use it in "select NOT IN" statement.

        String[] array = ignore.toArray(new String[0]);

        ArrayList<HashMap<String, Object>> votes = new ArrayList<>();

        if(votes.size() > 0) setResult(votes);

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(Server server) {

    }
}
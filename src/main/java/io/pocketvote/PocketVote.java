package io.pocketvote;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.listener.VoteListener;
import io.pocketvote.cmd.PocketVoteCommand;
import io.pocketvote.task.SchedulerTask;
import io.pocketvote.util.VoteManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PocketVote extends PluginBase {

    private static PocketVote plugin;

    private VoteManager vm;
    private boolean dev;

    public List<String> cmds;
    public List<String> cmdos;

    private List<Integer> tasks;

    public String identity;
    public String secret;

    public boolean lock;
    public boolean useVRC;

    public boolean multiserver;
    public String multiserverRole;

    public String mysqlHost;
    public int mysqlPort;
    public String mysqlUsername;
    public String mysqlPassword;
    public String mysqlDatabase;

    private TaskHandler schedulerTask;
    private int schedulerTs = 0;
    private int schedulerFreq = 60;

    @Override
    public void onEnable() {
        /**
         * TODO: Add GURU support.
         * TODO: Add TopVoter
         * TODO: Add Vote link.
         * TODO: Add diagnose.
         * TODO: Add heartbeat.
         * TODO: Add expire.
         */
        plugin = this;
        saveDefaultConfig();
        reloadSettings();
        updateConfig();

        vm = new VoteManager(plugin);

        // Load VRCs, if any.
        try {
            vm.loadVRCs();
        } catch (IOException e) {
            getServer().getLogger().alert("An exception was raised when attempting to load VRC files.");
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new VoteListener(plugin), plugin);
        getServer().getCommandMap().register("pocketvote", new PocketVoteCommand(plugin));

        /* Register tasks */
        this.tasks = new ArrayList<>();

        schedulerTask = getServer().getScheduler().scheduleRepeatingTask(plugin, new SchedulerTask(plugin), 1200); // 1200 = 60 seconds.
    }

    @Override
    public void onDisable() {
        for(int task : tasks) getServer().getScheduler().cancelTask(task); // TODO: Figure out if I can remove the tasks array.
        getServer().getScheduler().cancelTask(schedulerTask.getTaskId());
        plugin = null;
    }

    private void reloadSettings() {
        identity = plugin.getConfig().getString("identity", null);
        secret = plugin.getConfig().getString("secret", null);
        lock = plugin.getConfig().getBoolean("lock", false);
        dev = plugin.getConfig().getBoolean("development", false);

        cmds = getConfig().getStringList("onvote.run-cmd");
        cmdos = getConfig().getStringList("onvote.online-cmd");

        multiserver = getConfig().getBoolean("multi-server.enabled", false);
        multiserverRole = getConfig().getString("multi-server.role", "master");

        mysqlHost = getConfig().getString("multi-server.mysql.host", "localhost");
        mysqlPort = getConfig().getInt("multi-server.mysql.port", 3306);
        mysqlUsername = getConfig().getString("multi-server.mysql.username", "pocketvote");
        mysqlPassword = getConfig().getString("multi-server.mysql.password", "pocketvote");
        mysqlDatabase = getConfig().getString("multi-server.mysql.database", "pocketvote");

        /* Ensure MySQL tables exist if multi-server is enabled. */
        if(multiserver) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (Exception ex) {
                getLogger().critical("MySQL driver missing. You may need to restart your server.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Connection conn = null;
            try {
                conn = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + "/" + mysqlDatabase + "?" +
                                "user=" + mysqlUsername + "&password=" + mysqlPassword);

                Statement stmt = conn.createStatement();
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `pocketvote_votes` (" +
                                "`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                                "`player` VARCHAR(50) NULL DEFAULT NULL," +
                                "`ip` VARCHAR(255) NULL DEFAULT NULL," +
                                "`site` VARCHAR(255) NULL DEFAULT NULL," +
                                "`timestamp` INT(10) UNSIGNED NOT NULL DEFAULT 0," +
                                "PRIMARY KEY (`id`)" +
                                ")" +
                                "ENGINE=InnoDB;"
                );
                stmt.close();

                stmt = conn.createStatement();
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS `pocketvote_checks` (" +
                                "`server_hash` VARCHAR(255) NOT NULL," +
                                "`vote_id` INT(10) UNSIGNED NOT NULL DEFAULT 0," +
                                "`timestamp` INT(10) UNSIGNED NOT NULL DEFAULT 0," +
                                "INDEX `server_hash` (`server_hash`)," +
                                "INDEX `FK_vote_id_pocketvote_votes` (`vote_id`)," +
                                "CONSTRAINT `FK_vote_id_pocketvote_votes` FOREIGN KEY (`vote_id`) REFERENCES `pocketvote_votes` (`id`) ON UPDATE CASCADE ON DELETE CASCADE" +
                                ")" +
                                "ENGINE=InnoDB;"
                );
                stmt.close();

                conn.close();
            } catch (SQLException e) {
                getLogger().critical(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static PocketVote getPlugin() {
        return plugin;
    }

    public VoteManager getVoteManager() {
        return vm;
    }

    public void updateConfig() {
        if(plugin.getConfig().getInt("version", 1) == 1) {
            getLogger().info(TextFormat.YELLOW + "Migrating config to version 2.");
            getConfig().set("multi-server.enabled", false);
            getConfig().set("multi-server.role", "master");
            getConfig().set("multi-server.mysql.host", "localhost");
            getConfig().set("multi-server.mysql.port", 3306);
            getConfig().set("multi-server.mysql.username", "pocketvote");
            getConfig().set("multi-server.mysql.password", "pocketvote");
            getConfig().set("multi-server.mysql.database", "pocketvote");
            getConfig().set("version", 2);
            saveConfig();
        }
        if(plugin.getConfig().getInt("version", 2) == 2) {
            getLogger().info(TextFormat.YELLOW + "Migrating config to version 3");
            getConfig().set("development", false);
            getConfig().set("version", 3);
            saveConfig();
        }
    }

    public boolean isDev() {
        return dev;
    }

    public void stopScheduler() {
        if(schedulerTask.isCancelled()) return;
        schedulerTask.cancel();
    }

    public void startScheduler(int seconds) {
        int time = (int) Instant.now().getEpochSecond();
        // Ensure that at least 5 minutes has passed since we last changed frequency and check that the frequency is different from before.
        if(time - schedulerTs < 300 || schedulerFreq == seconds) return;
        stopScheduler();

        schedulerTs = time;
        schedulerTask = getServer().getScheduler().scheduleRepeatingTask(plugin, new SchedulerTask(plugin), seconds > 0 ? (seconds * 20) : 1200);
        schedulerFreq = seconds;

        getLogger().debug("Scheduler interval changed to " + seconds + " seconds.");
    }

}
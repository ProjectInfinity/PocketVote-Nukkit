package io.pocketvote;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import io.pocketvote.listener.VoteListener;
import io.pocketvote.cmd.PocketVoteCommand;
import io.pocketvote.task.SlaveCheckTask;
import io.pocketvote.task.VoteCheckTask;
import io.pocketvote.util.VoteManager;

import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    public boolean multiserver;
    public String multiserverRole;

    public String mysqlHost;
    public int mysqlPort;
    public String mysqlUsername;
    public String mysqlPassword;
    public String mysqlDatabase;

    private SSLContext sslContext;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        reloadSettings();
        updateConfig();

        vm = new VoteManager(plugin);

        plugin.getLogger().warning("Let's Encrypt is currently unsupported by Java, Oracle is scheduled to release a Java update in July 2016 that adds support. Until then SSL verification is disabled.");
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                }
        };

        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        if(sslContext != null) HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        if(!Files.isDirectory(Paths.get(getServer().getPluginPath() + "libs"))) {
            getLogger().warning("Could not find library directory.");
            try {
                Files.createDirectory(Paths.get(getServer().getPluginPath() + "libs"));
                getLogger().warning("Created " + getServer().getPluginPath() + "libs.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            downloadJacksonCore();
            downloadJacksonDatabind();
            downloadJacksonAnnotations();
            downloadJWT();
            downloadMySQL();
        } else {
            if(!Files.exists(Paths.get(getServer().getPluginPath() + "libs/jackson-core-2.7.4.jar"))) {
                getLogger().warning("Jackson-Core library not found.");
                downloadJacksonCore();
            }
            if(!Files.exists(Paths.get(getServer().getPluginPath() + "libs/jackson-databind-2.7.4.jar"))) {
                getLogger().warning("Jackson-Databind library not found.");
                downloadJacksonDatabind();
            }
            if(!Files.exists(Paths.get(getServer().getPluginPath() + "libs/jackson-annotations-2.7.4.jar"))) {
                getLogger().warning("Jackson-Annotations library not found.");
                downloadJacksonAnnotations();
            }
            if(!Files.exists(Paths.get(getServer().getPluginPath() + "libs/jjwt-0.6.0.jar"))) {
                getLogger().warning("JJWT library not found.");
                downloadJWT();
            }
            if(!Files.exists(Paths.get(getServer().getPluginPath() + "libs/mysql-connector-java-5.1.39.jar"))) {
                getLogger().warning("MySQL library not found.");
                downloadMySQL();
            }
        }

        getServer().getPluginManager().registerEvents(new VoteListener(plugin), plugin);
        getServer().getCommandMap().register("pocketvote", new PocketVoteCommand(plugin));

        /** Register tasks **/
        this.tasks = new ArrayList<>();
        if(secret != null && !secret.isEmpty() && identity != null && !identity.isEmpty()) {
            if(!multiserver || multiserverRole.equalsIgnoreCase("master")) tasks.add(getServer().getScheduler().scheduleRepeatingTask(new VoteCheckTask(plugin, identity, secret, getDescription().getVersion()), 1200, true).getTaskId());
            if(multiserver && multiserverRole.equalsIgnoreCase("slave")) {
                String hash = null;
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update((getServer().getIp() + Integer.toString(getServer().getPort())).getBytes());
                    byte[] digest = md.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) {
                        sb.append(String.format("%02x", b & 0xff));
                    }
                    hash = sb.toString();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                tasks.add(getServer().getScheduler().scheduleRepeatingTask(new SlaveCheckTask(plugin, hash), 1200, true).getTaskId());
            }
        } else {
            getLogger().critical("Please finish configuring PocketVote, then restart your server.");
        }
    }

    @Override
    public void onDisable() {
        for(int task : tasks) getServer().getScheduler().cancelTask(task);
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

    private void downloadJacksonCore() {
        try {
            URL website = new URL("http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.7.4/jackson-core-2.7.4.jar");
            FileOutputStream fos;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(getServer().getPluginPath() + "libs/jackson-core-2.7.4.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            getLogger().warning("Downloaded missing Jackson-Core library.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadJacksonDatabind() {
        try {
            URL website = new URL("http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.7.4/jackson-databind-2.7.4.jar");
            FileOutputStream fos;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(getServer().getPluginPath() + "libs/jackson-databind-2.7.4.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            getLogger().warning("Downloaded missing Jackson-Databind library.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadJacksonAnnotations() {
        try {
            URL website = new URL("http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.7.4/jackson-annotations-2.7.4.jar");
            FileOutputStream fos;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(getServer().getPluginPath() + "libs/jackson-annotations-2.7.4.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            getLogger().warning("Downloaded missing Jackson-Annotations library.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadJWT() {
        try {
            URL website = new URL("http://central.maven.org/maven2/io/jsonwebtoken/jjwt/0.6.0/jjwt-0.6.0.jar");
            FileOutputStream fos;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(getServer().getPluginPath() + "libs/jjwt-0.6.0.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            getLogger().warning("Downloaded missing JJWT library.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadMySQL() {
        try {
            URL website = new URL("http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.39/mysql-connector-java-5.1.39.jar");
            FileOutputStream fos;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(getServer().getPluginPath() + "libs/mysql-connector-java-5.1.39.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            getLogger().warning("Downloaded missing MySQL library.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
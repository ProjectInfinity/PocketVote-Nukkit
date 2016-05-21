package io.pocketvote;

import cn.nukkit.plugin.PluginBase;
import io.pocketvote.listener.VoteListener;
import io.pocketvote.cmd.PocketVoteCommand;
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
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class PocketVote extends PluginBase {

    private static PocketVote plugin;

    private VoteManager vm;

    public List<String> cmds;
    public List<String> cmdos;

    private List<Integer> tasks;

    public String identity;
    public String secret;

    public boolean lock;

    private SSLContext sslContext;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        reloadSettings();
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
        }

        getServer().getPluginManager().registerEvents(new VoteListener(plugin), plugin);
        getServer().getCommandMap().register("pocketvote", new PocketVoteCommand(plugin));

        /** Register tasks **/
        this.tasks = new ArrayList<>();
        if(secret != null && !secret.isEmpty() && identity != null && !identity.isEmpty()) {
            tasks.add(getServer().getScheduler().scheduleRepeatingTask(new VoteCheckTask(plugin, identity, secret, getDescription().getVersion()), 1200, true).getTaskId());
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

        cmds = getConfig().getStringList("onvote.run-cmd");
        cmdos = getConfig().getStringList("onvote.online-cmd");
    }

    public static PocketVote getPlugin() {
        return plugin;
    }

    public VoteManager getVoteManager() {
        return vm;
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

}
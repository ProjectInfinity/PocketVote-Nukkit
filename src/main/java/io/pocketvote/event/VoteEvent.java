package io.pocketvote.event;

import cn.nukkit.event.Event;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class VoteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private String player;
    private String ip;
    private String site;

    public VoteEvent(String player, String ip, String site) {
        this.player = player;
        this.ip = ip;
        this.site = site;
    }

    /**
     * Returns the player that voted.
     *
     * @return player
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Returns the IP the player voted from.
     *
     * @return ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns the site the player voted on.
     *
     * @return site
     */
    public String getSite() {
        return site;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

}
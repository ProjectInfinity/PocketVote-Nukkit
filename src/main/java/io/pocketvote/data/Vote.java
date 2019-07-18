package io.pocketvote.data;

import java.util.HashMap;

import cn.nukkit.utils.ConfigSection;

public class Vote {
	private final String player, site, ip;
	private final long expires;

	public Vote(String player, String site, String ip, long expires) {
		this.player = player;
		this.site = site;
		this.ip = ip;
		this.expires = expires;
	}

	public Vote(ConfigSection selection) {
		this.player = selection.getString("player");
		this.site = selection.getString("site");
		this.ip = selection.getString("ip");
		this.expires = Long.parseLong(selection.getString("expires"));
	}

	public String getPlayer() {
		return player;
	}

	public String getSite() {
		return site;
	}

	public String getIp() {
		return ip;
	}

	public long getExpires() {
		return expires;
	}

	public HashMap<String, String> saveVote() {
		HashMap<String, String> voteData = new HashMap<>();
		voteData.put("site", getSite());
		voteData.put("ip", getIp());
		voteData.put("player", getPlayer());
		voteData.put("expires", Long.toString(getExpires()));
		return voteData;
	}

}

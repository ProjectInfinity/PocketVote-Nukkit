package io.pocketvote.listener;

import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import io.pocketvote.PocketVote;
import io.pocketvote.data.Vote;
import io.pocketvote.event.VoteDispatchEvent;
import io.pocketvote.event.VoteEvent;
import io.pocketvote.util.VoteManager;

import java.util.Iterator;

public class VoteListener implements Listener {

    private PocketVote plugin;
    private VoteManager vm;

    public VoteListener(PocketVote plugin) {
        this.plugin = plugin;
        this.vm = plugin.getVoteManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVoteEvent(VoteEvent event) {
        if(event.isCancelled()) return;

        ConsoleCommandSender sender = plugin.getServer().getConsoleSender();

        for(String cmd : plugin.cmds) {
            plugin.getServer().dispatchCommand(sender, cmd
                    .replaceAll("%player", event.getPlayer())
                    .replaceAll("%ip", event.getIp())
                    .replaceAll("%site", event.getSite())
            );
        }

        if(plugin.getServer().getPlayer(event.getPlayer()) == null) {
            vm.addVote(event.getPlayer(), event.getSite(), event.getIp());
            vm.commit();
            return;
        }

        for(String cmd : plugin.cmdos) {
            plugin.getServer().dispatchCommand(sender, cmd
                    .replaceAll("%player", event.getPlayer())
                    .replaceAll("%ip", event.getIp())
                    .replaceAll("%site", event.getSite())
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(plugin.useVRC) vm.scheduleVRCTask(event.getPlayer().getName());

        if(!vm.hasVotes(event.getPlayer().getName())) return;

        ConsoleCommandSender sender = plugin.getServer().getConsoleSender();

        Iterator<Vote> votes = vm.getVotes();

        if(!votes.hasNext()) return;

        while(votes.hasNext()) {
            Vote vote = votes.next();
            if(!vote.getPlayer().equalsIgnoreCase(event.getPlayer().getName())) continue;
            plugin.getServer().getPluginManager().callEvent(new VoteDispatchEvent(vote.getPlayer(), vote.getIp(), vote.getSite()));
            for(String cmd : plugin.cmdos) {
                plugin.getServer().dispatchCommand(sender, cmd
                        .replaceAll("%player", vote.getPlayer())
                        .replaceAll("%ip", vote.getIp())
                        .replaceAll("%site", vote.getSite())
                );
            }
            votes.remove();
        }
        vm.commit();
    }

}
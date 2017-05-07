package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;

public class cmdRedeemSlowchat extends Command {

	public cmdRedeemSlowchat() {
		super("SlowChat", "Put the chat in slow mode for some time");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (AwardHandler.isAwardRunning()) { 
			getTwitch().sendChat("There's already an award running, please only one at a time!");
			return false;
		} else {
			AwardHandler.start("slowmode", new Runnable() {
				@Override
				public void run() {
					getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.slow 5");
				}
			});
			getTwitch().sendChat(user + " just redeemed slow mode chat");
			getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.slowoff");
			return true;
		}
	}

}

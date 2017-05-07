package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;

public class cmdRedeemSubchat extends Command {

	public cmdRedeemSubchat() {
		super("SubOnly", "Put the chat in subscriber only mode for some time");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (AwardHandler.isAwardRunning()) { 
			getTwitch().sendChat("There's already an award running, please only one at a time!");
			return false;
		} else {
			AwardHandler.start("subonly", new Runnable() {
				@Override
				public void run() {
					getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.subscribersoff");
				}
			});
			getTwitch().sendChat(user + " just redeemed subscriber chat");
			getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.subscribers");
			return true;
		}
	}

}

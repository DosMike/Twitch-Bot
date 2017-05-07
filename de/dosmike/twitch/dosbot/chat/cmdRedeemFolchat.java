package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;

public class cmdRedeemFolchat extends Command {

	public cmdRedeemFolchat() {
		super("FollowerOnly", "Put the chat in follower only mode for some time");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (AwardHandler.isAwardRunning()) { 
			getTwitch().sendChat("There's already an award running, please only one at a time!");
			return false;
		} else {
			AwardHandler.start("followonly", new Runnable() {
				@Override
				public void run() {
					getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.followersoff");
				}
			});
			getTwitch().sendChat(user + " just redeemed follower only chat");
			getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.followers");
			return true;
		}
	}

}

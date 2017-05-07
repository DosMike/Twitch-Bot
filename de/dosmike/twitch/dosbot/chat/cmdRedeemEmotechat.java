package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;

public class cmdRedeemEmotechat extends Command {

	public cmdRedeemEmotechat() {
		super("EmoteOnly", "Put the chat in emotes only mode for some time");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (AwardHandler.isAwardRunning()) { 
			getTwitch().sendChat("There's already an award running, please only one at a time!");
			return false;
		} else {
			AwardHandler.start("emoteonly", new Runnable() {
				@Override
				public void run() {
					getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.emoteonlyoff");
				}
			});
			getTwitch().sendChat(user + " just redeemed emote only chat");
			getTwitch().enqueue("PRIVMSG #"+Executable.targetChannel+" :.emoteonly");
			return true;
		}
	}

}

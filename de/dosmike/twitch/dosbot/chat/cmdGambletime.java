package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;
import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;

public class cmdGambletime extends Command {

	public cmdGambletime() {
		super("GambleTime", "Active the !gamble command for a small ammount of time");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (AwardHandler.isAwardRunning()) { 
			getTwitch().sendChat("There's already an award running, please only one at a time!");
			return false;
		} else {
			AwardHandler.start("gambletime", new Runnable() {
				@Override
				public void run() {
					getTwitch().sendChat("Gamble time is over leaving " + cmdGamble.pot + " " + PointsHandler.getCurrencyName() + " in the pot");
				}
			});
			getTwitch().sendChat(user + " just redeemed gamble time - !gamble now");
			return true;
		}
	}

}

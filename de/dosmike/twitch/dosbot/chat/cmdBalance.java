package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.PointsHandler;

public class cmdBalance extends Command {
	public cmdBalance() {
		super(PointsHandler.getCurrencyName(), "Display the current balance for a user");
	}
	
	@Override
	public void run(String user, ChatRank rank, String[] args, boolean silent) {
		String f = args.length>0 ? args[0] : user;
		getTwitch().sendChat(f + " currently has " + PointsHandler.balance(f) + " " + getCommandName());
	}
}

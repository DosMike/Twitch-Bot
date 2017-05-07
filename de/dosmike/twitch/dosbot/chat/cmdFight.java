package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.FightsHandler;

public class cmdFight extends Command {

	public cmdFight() {
		super("Fight", "Fight the current boss in chat");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		return FightsHandler.hit(user);
	}

}

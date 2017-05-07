package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.FightsHandler;

public class cmdSummon extends Command {
	public cmdSummon() {
		super("summon", "Summon the next boss to battle!");
	}
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (FightsHandler.canSpawn()) {
			getTwitch().sendChat(user + " set up a summoning circle to call upon the boss GOWSkull");
			FightsHandler.newBoss();
			return true;
		} else {
			getTwitch().sendChat("You can not summon a boss right now - try again later");			
			return false;
		}
	}
}

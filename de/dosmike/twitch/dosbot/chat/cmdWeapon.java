package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.FightsHandler;

public class cmdWeapon extends Command {
	public cmdWeapon() {
		super("Weapon", "Check, repair or buff your weapon");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		int Type=0; //stats
		if (args.length>0) {
			if ("repair".equalsIgnoreCase(args[0]))
				Type=1;
			else if ("buff".equalsIgnoreCase(args[0]))
				Type=2;
		}
		return FightsHandler.weapon(user, Type);
	}
}

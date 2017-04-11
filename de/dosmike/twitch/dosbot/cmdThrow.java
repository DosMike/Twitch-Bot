package de.dosmike.twitch.dosbot;

import java.awt.Image;

import de.dosmike.twitch.dosbot.chat.Command;
import de.dosmike.twitch.dosbot.overlay.sprites.PhyCupEmote;

public class cmdThrow extends Command {
	
	public cmdThrow() {
		super("Throw", "Throw emotes on the stream overlay");
	}
	
	@Override
	public void run(String user, ChatRank rank, String[] args, boolean silent) {
		if (!"true".equalsIgnoreCase(Executable.cfg.get("BOT", "ThrowEmotes"))) {
			Executable.handler.sendChat("Throwing emotes was disabled.");
			return;
		}
		if (args.length == 0) {
			Executable.handler.sendChat("Throwing works like this: " + Executable.handler.CommandPrefix + "throw Kappa 10 - The number is optional");
		} else {
			for (String s : Executable.emoteMap.keySet()) {
				int i = 0;
				if (s.equals(args[0])) {
					if (args.length == 2) {
						try { i = Integer.parseInt(args[1]); }
						catch (Exception e) { break; }
					} else if (args.length == 1) i = 1;
				}
				if (i==0) continue;
				if (i>10) {
					Executable.handler.sendChat(user, "I won't throw more than 10 at a time!");
					i = 10;
				}
				
				Image t = Executable.emoteMap.get(s);
				for (; i > 0; i--)
					Executable.overlay.addSprite(new PhyCupEmote(t));
			}
		}
	}
}

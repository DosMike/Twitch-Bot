package de.dosmike.twitch.dosbot.chat;

import java.awt.Image;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.overlay.sprites.PhyCupEmote;

public class cmdThrow extends Command {
	
	public cmdThrow() {
		super("Throw", "Throw emotes on the stream overlay");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (!"true".equalsIgnoreCase(Executable.cfg.get("Overlay", "ThrowEmotes"))) {
			getTwitch().sendChat("Throwing emotes was disabled.");
			return false;
		}
		if (args.length == 0) {
			getTwitch().sendChat("Throwing works like this: " + getTwitch().CommandPrefix + "throw Kappa 10 - The number is optional");
			return false;
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
					getTwitch().sendChat(user, "I won't throw more than 10 at a time!");
					i = 10;
				}
				
				Image t = Executable.emoteMap.get(s);
				for (; i > 0; i--)
					Executable.getOverlay().addSprite(new PhyCupEmote(t));
			}
		}
		return true;
	}
}

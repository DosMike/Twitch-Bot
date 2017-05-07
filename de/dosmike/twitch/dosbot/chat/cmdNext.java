package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.SongMeta;

public class cmdNext extends Command {
	
	public cmdNext() {
		super("Next", "Skip the current song in the playlist");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
//		Now limited by chat trigger
//		if (rank.compareTo(ChatRank.MOD)<0) {
//			getTwitch().sendChat("Only mods can skip songs in the playlist");
//			return;
//		}
		
		SongMeta s = Executable.vlcPlayer.getCurrentSong();
		if (s== null) {
			getTwitch().sendChat("There's no song playing right now");
			return false;
		} else {
			getTwitch().sendChat(user + " skipped \"" + s.getTitle() + '"');
			Executable.vlcPlayer.next();
			return true;
		}
	}
	
}

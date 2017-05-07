package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.SongMeta;

public class cmdCurrentSong extends Command {

	public cmdCurrentSong() {
		super("CurrentSong", "Get information about the song currently playing");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		SongMeta c = Executable.vlcPlayer.getCurrentSong();
		if (!Executable.vlcPlayer.isPlaying() || c == null) {
			getTwitch().sendChat("There is currently no song playing!");
			return false;
		}

		getTwitch().sendChat("Current song is \"" + c.getTitle() + "\" from " + c.getAuthor() + " (" + c.getLengthString() + ") requested by " + c.getRequester() + " " + c.getMediaURL());
		return true;
	}

}
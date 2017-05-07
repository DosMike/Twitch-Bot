package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.SongMeta;

public class cmdSongRequest extends Command {

	public cmdSongRequest() {
		super("SongRequest", "Request a song to be added to the playlist");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (args.length < 1) {
			getTwitch().sendChat(user, "You need to apend a YouTube URL");
			return false;
		}
		//TODO put these limits in the config
		SongMeta m = SongMeta.fromURL(args[0], user);
		if (m == null || m.hasError()) {
			getTwitch().sendChat(user, "The song you requested could not be added!");
			return false;
		}
		
		if (Executable.vlcPlayer.getPlaylistLength() > Executable.vlcPlayer.maxPlaylistLength) {
			getTwitch().sendChat(user, "The song queue is full with "+Executable.vlcPlayer.getLength());
			return false;
		}
		
		String v;
		v = Executable.cfg.get("SongRequest", "likeRatio");
		if (m.getRating() < (v==null?0.9:Float.parseFloat(v))) {
			getTwitch().sendChat(user, "The song need to have at least 90% likes");
			return false;
		}
		v = Executable.cfg.get("SongRequest", "LengthMax");
		if (m.getLength() > (v==null?360:Integer.parseInt(v))) {
			getTwitch().sendChat(user, "The song may not exceed 5 minutes");
			return false;
		}
		v = Executable.cfg.get("SongRequest", "minViews");
		if (m.getViews() < (v==null?360:Integer.parseInt(v))) {
			getTwitch().sendChat(user, "The hast to have at least 1000 views");
			return false;
		}

		if (Executable.vlcPlayer.isSongQueued(m)) {
			getTwitch().sendChat(user, "This song is already in the playlist");
			return false;
		}
		v = Executable.cfg.get("SongRequest", "requestsPerViewer");
		if (Executable.vlcPlayer.getSongsQueuedBy(user)>=(v==null?360:Integer.parseInt(v))) {
			getTwitch().sendChat(user, "You can't queue more than 2 songs at a time");
			return false;
		}
		
		Executable.vlcPlayer.append(m);
		getTwitch().sendChat(user + " added \"" + m.getTitle() + "\" to the playlist, extending it to " + Executable.vlcPlayer.getLength());
		return true;
	}

}

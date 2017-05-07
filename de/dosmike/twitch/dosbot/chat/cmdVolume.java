package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;

public class cmdVolume extends Command {

	public cmdVolume() {
		super("Volume", "Read or set the volume for the background music");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
//		Now limited by chat trigger
//		if (rank.compareTo(ChatRank.MOD)<0) {
//			getTwitch().sendChat("Only mods can change the volume");
//			return;
//		}
		
		if (args.length < 1) {
			getTwitch().sendChat("The volume is currently set to "+Executable.vlcPlayer.getVolume()+"%");
			return false;
		} else {
			try {
				int v = Integer.parseInt(args[0]);
				if (v < 0 || v > 100) throw new RuntimeException();
				getTwitch().sendChat("Changed volume from "+Executable.vlcPlayer.getVolume()+"% to "+v+"%");
				Executable.vlcPlayer.setVolume(v);
			} catch (Exception e) {
				getTwitch().sendChat("Volume has to be a number between 0 and 100");
				return false;
			}
		}
		return true;
	}

}

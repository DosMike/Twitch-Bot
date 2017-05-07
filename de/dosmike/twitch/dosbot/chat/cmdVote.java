package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.VoteHandler;

public class cmdVote extends Command{
	public cmdVote() {
		super("Vote", "Vote on an option on the current question");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (args.length == 0) {
			VoteHandler.status(false);
			//Executable.handler.sendChat("Use number or name to vote - e.g. "+Executable.handler.CommandPrefix+"vote 1 or "+Executable.handler.CommandPrefix+"vote \"That Option\"");
		} else if (args.length == 1){
			VoteHandler.vote(user, args[0]);
		} else {
			VoteHandler.vote(user, String.join(" ", args));
		}
		return true;
	}
}

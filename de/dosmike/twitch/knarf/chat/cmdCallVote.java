package de.dosmike.twitch.knarf.chat;

import de.dosmike.twitch.knarf.ChatRank;
import de.dosmike.twitch.knarf.VoteHandler;

public class cmdCallVote extends Command{
	public cmdCallVote() {
		super("Callvote", "Create a new vote. Remember to quote the question and options.");
	}
	
	@Override
	public void run(String user, ChatRank rank, String[] args, boolean silent) {
		VoteHandler.startVote(user, args);
	}
}

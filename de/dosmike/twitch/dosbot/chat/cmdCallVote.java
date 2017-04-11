package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.VoteHandler;

public class cmdCallVote extends Command{
	public cmdCallVote() {
		super("Callvote", "Create a new vote. Remember to quote the question and options.");
	}
	
	@Override
	public void run(String user, ChatRank rank, String[] args, boolean silent) {
		VoteHandler.startVote(user, args);
	}
}

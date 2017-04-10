package de.dosmike.twitch.knarf;

import java.util.LinkedList;
import java.util.List;

public class VoteHandler {
	private static String question;
	private static List<String> options=new LinkedList<>();
	private static List<Integer> votes=new LinkedList<>();
	private static long start = 0;
	private static int votesTotal;
	private static long lastStatus = 0;
	
	public static void startVote(String byUser, String[] args) {
		if (ChatRank.forUser(byUser, false).compareTo(ChatRank.MOD)<0) {
			Executable.handler.sendChat(byUser, "You need to be at least Mod to be able to start a vote!");
			return;
		}
		if (secondsRemain()>=0) {
			Executable.handler.sendChat("A vote is already in progress!");
			return;
		}
		
		if (args.length == 0) {
			Executable.handler.sendChat(byUser, "You need to ask a question. Either add no options to make a Yea/Nay-vote or add 2 or more options");
			Executable.handler.sendChat(byUser, "If the question or options contain spaces please quote them like \"What shall i play?\"");
			return;
		} else if (args.length == 2) {
			Executable.handler.sendChat(byUser, "A single option in not vote-worthy. Either add no options to make a Yea/Nay-vote or add 2 or more options");
			Executable.handler.sendChat(byUser, "If the question or options contain spaces please quote them like \"What shall i play?\"");
			return;
		}
		
		start = System.currentTimeMillis()/1000;
		question=args[0];
		votesTotal=0;
		options.clear();
		votes.clear();
		if (args.length < 2) {
			options.add("VoteYea");
			options.add("VoteNay");
			votes.add(0);
			votes.add(0);
		} else {
			for (int i = 1; i < args.length; i++) {
				options.add(args[i]);
				votes.add(0);
			}
		}
		for (String user : ClientStorage.users.groups()) {
			ClientStorage.users.remove(user, "Vote");
		}
		status(true);
	}
	
	public static void vote(String byUser, String option) {
		if (secondsRemain()<1) {
			Executable.handler.sendChat(byUser, "Sorry, the vote is over!");
			return;
		}
		
		int i;
		if (options.contains(option)) {
			i = options.indexOf(option);
		} else {
			try {
				i = Integer.parseInt(option)-1;
			} catch (Exception e) {
				Executable.handler.sendChat(byUser, "I don't understand what you want to vote on");
				return;
			}
			if (i < 0 || i >= options.size()) {
				Executable.handler.sendChat(byUser, "There are not that many options");
				return;
			}
		}
		int prevote=Integer.parseInt(ClientStorage.getCV(byUser, "Vote").orElse("-1"));
		if (prevote>=0)
			votes.set(prevote, votes.get(prevote)-1);
		else
			votesTotal++;
		ClientStorage.setCV(byUser, "Vote", i);
		votes.set(i, votes.get(i)+1);
	}
	
	public static void tick() {
		int timeRemain = secondsRemain();
		if (timeRemain == 10) {
			Executable.handler.sendChat("Vote ends in " + timeRemain + " second(s)...");
		} else if (timeRemain == 0) {
			status(true);
		}
	}
	
	public static void status(boolean override) {
		if (System.currentTimeMillis()-lastStatus < 10000 && !override) return;
		lastStatus = System.currentTimeMillis();
		if (start == 0) {
			Executable.handler.sendChat("No vote was called yet BibleThump");
			return;
		}
		int timeRemain = secondsRemain();
		if (timeRemain>0)
			Executable.handler.sendChat("The vote runs for " + timeRemain + " more seconds...");
		else
			Executable.handler.sendChat("The last vote ended:");
		Executable.handler.sendChat(TwitchChatColor.Green, ":: " + question + " ::");
		int y = 0;
		for (int i = 0; i < options.size(); i++) {
			Executable.handler.sendChat((i+1) + ") " + options.get(i) + " (" + votes.get(i) + ", " + (votesTotal==0?0:votes.get(i)*100/votesTotal) + "%)");
			if (votes.get(i)>y)y=votes.get(i);
		}
		int winner = 0, windex = 0;
		for (int i = 0; i < options.size(); i++) {
			if (votes.get(i) == y) { winner++; windex = i; }
		}
		if (timeRemain>0) {
			if (winner==1)
				Executable.handler.sendChat(TwitchChatColor.Green, "> " + options.get(windex) + " < is leading.");
			else {
				Executable.handler.sendChat(TwitchChatColor.Green, "There's no winner yet!");
			}
		} else {
			if (winner==1)
				Executable.handler.sendChat(TwitchChatColor.Green, "> " + options.get(windex) + " < won the vote.");
			else {
				Executable.handler.sendChat(TwitchChatColor.Green, "The decision was unclear!");
			}
		}
	}
	
	private static int secondsRemain() {
		long runtime = (System.currentTimeMillis()/1000-start);
		return runtime>60?-1:(int)(60-runtime);
	}
}

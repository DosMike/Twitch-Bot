package de.dosmike.twitch.dosbot.chat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.dosmike.twitch.dosbot.ChatRank;

public class cmdCommands extends Command {

	public cmdCommands() {
		super("Commands", "List all available commands - quite obvious if you used ?commands after !commands");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		//I'm too lazy to keep a list updated here, so automatically fetch stuff :)
		List<String> commands = new LinkedList<>();
		for (String cmd : getTwitch().getCommandHandler().commands.keySet()) {
			if (!commands.contains(cmd))
				commands.add(cmd);
		}
		Collections.sort(commands);
		String res = "The following commands exist: ";
		boolean a = false;
		for (String cmd : commands) {
			if (!a) a=!a; else res+=", "; res+=cmd;
		}
		res+=" and more. Use ?command instead of !command to get help."; //user created chat triggers only expose regex, don't wanna fiddle with that
		getTwitch().sendChat(res);
		return true;
	}

}

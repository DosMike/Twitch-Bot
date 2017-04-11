package de.dosmike.twitch.dosbot.chat;

import java.util.HashMap;
import java.util.Map;

import de.dosmike.twitch.dosbot.ChatRank;

/** well, this class shrunk quite a lot... :/ */
public class CommandHandler {
	
	Map<String, Command> commands = new HashMap<>();
	
	public CommandHandler() {
		registerCommand(new cmdPlay());
		registerCommand(new cmdCallVote());
		registerCommand(new cmdVote());
		registerCommand(new cmdBalance());
		registerCommand(new cmdGamble());
	}
	
	public void registerCommand(Command command) {
		commands.put(command.getCommandName().toLowerCase(), command);
	}
	public void exec(String client, ChatRank rank, String command, String[] args, boolean silent) {
		Command cmd = commands.get(command.toLowerCase());
		if (cmd != null) cmd.run(client, rank, args, silent);
	}
}

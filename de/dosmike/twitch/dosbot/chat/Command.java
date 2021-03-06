package de.dosmike.twitch.dosbot.chat;

import java.util.Random;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.TelnetHandler;

public abstract class Command {
	protected static Random rng = new Random(System.currentTimeMillis());
	public static Random getRng() { return rng; }
	protected static TelnetHandler twitch=null;
	public static TelnetHandler getTwitch() {
		if (twitch == null) twitch = Executable.getTelnetHandler();
		return twitch;
	}
	
	protected String name, desc;
	public String getCommandName() { return name; }
	public String getDescription() { return desc; }
	public Command(String commandName, String description) {
		name=commandName; desc=description; getTwitch();
	}
	/** @returns if command was executed successfully, false if execution was canceled */
	public abstract boolean run(String user, ChatRank rank, String[] args, boolean silent);
}

package de.dosmike.twitch.knarf.chat;

import java.util.Random;

import de.dosmike.twitch.knarf.ChatRank;
import de.dosmike.twitch.knarf.Executable;
import de.dosmike.twitch.knarf.TelnetHandler;

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
	public abstract void run(String user, ChatRank rank, String[] args, boolean silent);
}

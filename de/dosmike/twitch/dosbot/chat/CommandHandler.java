package de.dosmike.twitch.dosbot.chat;

import java.util.HashMap;
import java.util.Map;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.ChatTrigger;
import de.dosmike.twitch.dosbot.Executable;

/** well, this class shrunk quite a lot... :/ */
public class CommandHandler {
	
	Map<String, Command> commands = new HashMap<>();
	
	public CommandHandler() {
		
		registerCommand(new cmdCommands());
		registerCommand(new cmdPlay());
		registerCommand(new cmdCallVote());
		registerCommand(new cmdVote());
		
		//overlay stuff
		if ("true".equalsIgnoreCase(Executable.cfg.get("Overlay", "Enable"))) {
			registerCommand(new cmdThrow());			
		}
		
		//point stuff
		if ("true".equalsIgnoreCase(Executable.cfg.get("Points", "Enable"))) {
			registerCommand(new cmdBalance());
			registerCommand(new cmdGamble());
			registerCommand(new cmdAward());
			
			registerCommand(new cmdGambletime());
			registerCommand(new cmdRedeemEmotechat());
			registerCommand(new cmdRedeemFolchat());
			registerCommand(new cmdRedeemSlowchat());
			registerCommand(new cmdRedeemSubchat());
			
			if ("true".equalsIgnoreCase(Executable.cfg.get("Points", "Fights"))) {
				registerCommand(new cmdFight());
				registerCommand(new cmdSummon());
				registerCommand(new cmdWeapon());
			}
		}
		
		//music!
		if (Executable.vlcPlayer != null) {
			registerCommand(new cmdSongRequest());
			registerCommand(new cmdCurrentSong());
			registerCommand(new cmdNext());
			registerCommand(new cmdVolume());
		}
	}
	
	public Command findCommandByName(String name) {
		return commands.get(name);
	}
	public void registerCommand(Command command) {
		commands.put(command.getCommandName().toLowerCase(), command);
	}
	/** @returns if command was successfully executed, false otherwise */
	public boolean exec(String client, ChatRank rank, String command, String[] args, boolean silent) {
		Command cmd = commands.get(command.toLowerCase());
		if (cmd != null) return cmd.run(client, rank, args, silent);
		return false;
	}
	
	public void help(String client, ChatRank rank, String command, ChatTrigger trigger) {
		Command cmd = commands.get(command.toLowerCase());
		if (cmd == null) return;
		String msg = cmd.getDescription() + " | ";
		if (trigger == null) msg += "No custom limitations.";
		else msg += trigger.readableFlags();
		Executable.getTelnetHandler().sendChat(msg);
	}
}

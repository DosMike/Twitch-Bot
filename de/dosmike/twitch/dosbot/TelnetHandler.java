package de.dosmike.twitch.dosbot;

import java.awt.Image;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itwookie.telnet.ReceiverCallback;
import com.itwookie.telnet.TelnetClient;

import de.dosmike.twitch.dosbot.chat.CommandHandler;
import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;
import de.dosmike.twitch.dosbot.modulehandler.PyramidHandler;
import de.dosmike.twitch.dosbot.overlay.sprites.EmoteDrop;

public class TelnetHandler implements ReceiverCallback {
	
	private static final String[] DosBotGreets = {
			"Do I smell fish?",
			"Don't Panic! I hope you got your towel ready.",
			"Like, Comment and Subscribe! Wait, wrong website...",
			"♪♫ I'm making a note here: Huge success ♫♪",
			"Please speak after the beep. ♪BEEP♪",
			"OMG did you see that? No? Maybe you should pay attention to the stream ;P",
			"Just one more hour Kappa"
		};
	private long lastRunningCheck=0;//to prevent spamming from typing "DosBot"
	
	public static final String TwitchTags_Moderator = "mod";
	public static final String TwitchTags_Subscriber = "subscriber";
	public static final String TwitchTags_Follower = "follower"; //pseydo tag, IRC does not deliver this information!
	
	Map<String, Long> viewers = new HashMap<>();
	public void readAllViewers() {
		enqueue(".names #" + Executable.targetChannel);
	}
	public void registerViewer(String name) {
		name = name.toLowerCase();
		if (!viewers.containsKey(name)) viewers.put(name, 0l);
	}
	public void forgetViewer(String name) {
		viewers.remove(name.toLowerCase());
	}
	public boolean knowsViewer(String name) {
		return viewers.containsKey(name.toLowerCase());
	}
	public List<String> getViewers() {
		List<String> copy = new LinkedList<>(); //no problems with modifications
		Set<String> keys = viewers.keySet();
		copy.addAll(keys);
		return copy;
	}
	public int getViewcount() {
		return viewers.size();
	}
	/** If someone chatted within the last 2 minutes they are considered active */
	public boolean isLurking(String viewer) {
		viewer = viewer.toLowerCase();
		if (!viewers.containsKey(viewer)) return true;
		return (System.currentTimeMillis()-viewers.get(viewer) <= 120000);
	}
	public void updateCTS(String u) {
		u=u.toLowerCase();
		viewers.put(u, System.currentTimeMillis());
	}
	
	List<ChatTrigger> trigger = new LinkedList<>();
	public Iterator<ChatTrigger> getTriggers() {
		return trigger.iterator();
	}
	
	List<Long> commandLimiter = new LinkedList<>(); 
	//All timestamps, do not send more than 100 Messages per 30 sec
	//Limited to 20 per 30 sec for channels we do not moderate
	static int commandsPer30sec = 90; //-10% for safety
	
	CommandHandler cmdHandler = new CommandHandler();
	public CommandHandler getCommandHandler() {
		return cmdHandler;
	}
	
	List<String> queue = new LinkedList<>();
	/** the queue is used when we want to wait for a response before continuing */
	public void enqueue(String message) {
		queue.add (message);
	}
	public String dequeue() {
		commandLimiter.add(System.currentTimeMillis());
		String m = queue.get(0);
		queue.remove(0);
		Console.println(Console.FB.BLACK, "> " + m, Console.RESET);
		//waiting=true;
		return m;
	}
	public int queueSize() {
		return queue.size();
	}
	/** the queue is used when we want to wait for a response before continuing */
	public void sendChat(String message) {
		enqueue("PRIVMSG #"+Executable.targetChannel+" :imGlitch "+message);
	}
	/** the queue is used when we want to wait for a response before continuing.
	 * Whisper this person */
	public void sendChat(String client, String message) {
		//enqueue("PRIVMSG #"+myself+" :.w "+client + " imGlitch " + message); //whisper
		enqueue("PRIVMSG #"+Executable.targetChannel+" :imGlitch (@"+client + ") " + message);
	}
	// static TwitchChatColor defaultChatColor=null;
	public void sendChat(TwitchChatColor color, String message) {
		// Twitch is lame and won't let you change chat color via IRC - or at least it won't appear on your side idk
		/*if (defaultChatColor==null) {
			try {
				defaultChatColor = TwitchChatColor.valueOf(Executable.cfg.get("IRC", "DefaultColor"));
			} catch (Exception e) {
				defaultChatColor = TwitchChatColor.BlueViolet;
			}
		}*/
		//enqueue("PRIVMSG #"+Executable.targetChannel+" :.color " + color.toString());
		enqueue("PRIVMSG #"+Executable.targetChannel+" :.me : imGlitch " + message);
		//enqueue("PRIVMSG #"+Executable.targetChannel+" :.color " + defaultChatColor.toString());
	}
	
	String server = ":tmi.twitch.tv"; //static
	String myself = "";
	public String CommandPrefix = "!";
	@SuppressWarnings("unused") private TelnetHandler() {}
	public TelnetHandler(String myself) {
		this.myself = myself.toLowerCase();
		
		if (Executable.cfg.hasKey("IRC", "CommandPrefix")) {
			CommandPrefix = Executable.cfg.get("CommandPrefix");
			if (CommandPrefix.startsWith("\"") && CommandPrefix.endsWith("\""))
				CommandPrefix = CommandPrefix.substring(1, CommandPrefix.length()-1);
			Console.println("Commands may now start with '", Console.FB.CYAN, CommandPrefix, Console.RESET, "'");
		}
	}
	
	long busy=0; boolean waiting=false;
	public boolean isBusy() {
		List<Long> cleanUp = new LinkedList<>();
		for (Long l : commandLimiter) if (System.currentTimeMillis()-l>30000) cleanUp.add(l);
		commandLimiter.removeAll(cleanUp);
		
		return commandLimiter.size()>=commandsPer30sec || waiting || System.currentTimeMillis()-busy<100; //100 ms "timeout" after last received msg
	}
	
	Pattern PRIVMSG = Pattern.compile(
			"(?:@(.*) ?)?:(?:(\\w+)!\\w+@\\w+\\.)?tmi\\.twitch\\.tv PRIVMSG #\\w+ :(.*)"
		);
	Pattern MEMBERSHIP = Pattern.compile(
			":(?:(.*)!)?.+tmi\\.twitch\\.tv (353 (?:.*)|PART|JOIN) #\\w+(?: :(.*))?"
		);
	
	@Override
	public void onReceive(String message, TelnetClient twichTelnetConnection) {
		busy=System.currentTimeMillis(); //waiting=false;
		if (message.equals("PING "+server)) {
			enqueue("PONG "+server);
			return;
		}
		
		//login failed
		if (":tmi.twitch.tv NOTICE * :Login authentication failed".equalsIgnoreCase(message))
			Console.println(Console.FB.RED, "Login Failed!", Console.RESET);
		
		//wait for join confirmation
		if (!Executable.inChannel && message.contains(".tmi.twitch.tv JOIN #")) {
			//readAllViewers();
			Executable.inChannel=true;
		}

		Console.println(Console.FB.BLACK, "< " + message, Console.RESET);
		if (!message.contains("PRIVMSG")) {
			Matcher membership = MEMBERSHIP.matcher(message);
			if (membership.matches()) {
				if (membership.group(2).startsWith("353")) {
					String[] members = membership.group(3).trim().split(" ");
					for (String s : members) registerViewer(s);
				} else if (membership.group(2).equals("JOIN")) {
					registerViewer(membership.group(1));
					Console.println(Console.FB.GREEN, membership.group(1) + " joined", Console.RESET);
				} else if (membership.group(2).equals("PART")) {
					forgetViewer(membership.group(1));
					Console.println(Console.FG.GREEN, membership.group(1) + " disconnected", Console.RESET);
				}
			}
			return;
		}
		//if (true) return; //only testing membership now
		
		Matcher preMatcher = PRIVMSG.matcher(message);
		String name = null;
		if (preMatcher.matches()) {
			name = preMatcher.group(2);
			message = preMatcher.group(3);
			
			try {
				//Console.println("Tags: " + preMatcher.group(1));
				String[] Tags = preMatcher.group(1).split(";");
				
				String[] ab = new String[2];
				for (String Tag : Tags) {
					ab[0] = Tag.substring(0, Tag.indexOf('='));
					ab[1] = Tag.substring(ab[0].length()+1);
					//Console.println("AB: ", ab[0], " -> ", ab[1]);
					ClientStorage.setCV(name, ab[0], ab[1]);
				}
			} catch (Exception e) {
				//keine gruppen
			}
		}
		if (name==null) {
			Console.println(Console.FB.RED, "Unable to read username from PRIVMSG!", Console.RESET);
			return;
		}
		
		ChatRank rank = ChatRank.forUser(name, true);
		updateCTS(name);
		String /*displayName = ClientStorage.getCV(name, "display-name").get();
		if (displayName.isEmpty())*/ displayName=name;
		//Display name is causing problems
		
		//Console.printf("%s: %s\n", name, message);
		/*Console.println(
				//( r.equals(ChatRank.USER) ? "" : "["+r+"]"),
				( rank.equals(ChatRank.HOST) ?
					Console.FG.RED :
					( rank.equals(ChatRank.MOD) ?
						Console.FG.GREEN :
						( rank.equals(ChatRank.SUB) ?
							Console.FG.PURPLE :
							( rank.equals(ChatRank.FOLLOW) ?
								Console.FB.YELLOW :
								Console.FG.YELLOW
							)
						)
					)
				),
				displayName, ": ", Console.RESET, message);*/

		// -- START CHAT TRIGGER HANDLING --
		boolean hasPerm = false, found = false;//, playPerm=true; //the playPerm is requred to check the commandless play mode
		int commandCost=0;
		ChatTrigger matchedTrigger=null;
		for (ChatTrigger t : trigger) {
			Matcher tmp = t.getMatcher(message);
			if (tmp.matches()) { 
				found=true;
				Console.println("Using chat trigger "+t);
				matchedTrigger = t;
				if (t.checkPerms(rank)) {
					if (t.checkPrice(name)) { //can we afford this command?
						if (t.hasResponses()) { // if it has responses it is a custom command
							t.respond(tmp, name);
							hasPerm=false; //just block other command execution
						} else if (message.startsWith("?")) { //is help command
							hasPerm=true; //check help first as "isOnCooldown" also sets cooldown if it would return false
						} else if (!t.isOnCooldown() //hasResponse() does cooldown check on it's own
								&& !t.isOnCooldown(name) //check both global and client cooldown
								&& message.startsWith("!")) { //no price for help
							commandCost = t.getPrice(); // cash if successful later
							hasPerm=true; //no response, see what the command handler has to offer
						} else {
							Console.println("Command on cooldown");
						}
						break;
					} else {
//						hasPerm=false;
						sendChat(name, "You do not have enough " + PointsHandler.getCurrencyName() + " for this command!");
					}
				} else {
//					hasPerm=false;
					Console.println("No permission");
				}
			}
		}
		if (!found) {
			Console.println("No trigger found");
			hasPerm=true; //if we have no rules for that command, let the command handle stuff
		}
		// -- END OF CHAT TRIGGER HANDLING --
		
		PyramidHandler.doPyramid(name, message);
		if ("DosBot".equals(message)) { //DosBot shall always execute to see if the bot's running
			if (System.currentTimeMillis()-lastRunningCheck < 120_000) return;
			lastRunningCheck = System.currentTimeMillis();
			Executable.handler.sendChat("Greetings " + displayName + ", DosBot is running CoolCat");
			Executable.handler.sendChat(DosBotGreets[Executable.rng.nextInt(DosBotGreets.length)]);
		} else if (hasPerm) { 
			if (message.startsWith(CommandPrefix)) {
				message = message.substring(CommandPrefix.length());
				if (message.contains(" ")) {
					List<String> elems = new LinkedList<>();
					Pattern argPattern = Pattern.compile(
							"\"((?:[^\"]|\"\")*)\"|([^\\s]+)"
							);
					Matcher argMatch = argPattern.matcher(message.substring(message.indexOf(' ')+1));
					while (argMatch.find()) {
						/*for (int z = 0; z < argMatch.groupCount(); z++) {
							Console.println(argMatch.group(z));
						}*/
						elems.add( argMatch.group(1)==null ? 
							argMatch.group(2) : 
							argMatch.group(1).replace("\"\"", "\"") );
					}
					String[] args = elems.toArray(new String[elems.size()]);
					message = message.substring(0, message.indexOf(' '));
					if (cmdHandler.exec(displayName, rank, message, args, false)) {
						if (commandCost>0) PointsHandler.redeem(name, commandCost);
						if (matchedTrigger!=null) matchedTrigger.setCooldown(name);
					} //else sendChat(name, "You do not have enough " + PointsHandler.getCurrencyName() + " for this command!");
				} else {
					if (cmdHandler.exec(displayName, rank, message, new String[0], false)) {
						if (commandCost>0) PointsHandler.redeem(name, commandCost);
						if (matchedTrigger!=null) matchedTrigger.setCooldown(name);
					} //else sendChat(name, "You do not have enough " + PointsHandler.getCurrencyName() + " for this command!");
				}
			} else if (message.startsWith("?")) {
				message = message.substring(1);
				if (message.contains(" ")) {
					message = message.substring(0, message.indexOf(' '));
					cmdHandler.help(displayName, rank, message, matchedTrigger);
				} else {
					cmdHandler.help(displayName, rank, message, matchedTrigger);
				}
			} else {
				if (!message.contains(" ") && !message.equalsIgnoreCase("list") && 
					"true".equalsIgnoreCase(Executable.cfg.get("Sounds", "SaySounds"))) {
					if (Executable.cfg.hasKey("Sounds", message)) {
						if (cmdHandler.exec(displayName, rank, "play", new String[]{ message }, true)) {
							if (commandCost>0) PointsHandler.redeem(name, commandCost);
						} //else sendChat(name, "You do not have enough " + PointsHandler.getCurrencyName() + " for this command!");
					}
				}
				if (Executable.overlay!=null && "true".equalsIgnoreCase(Executable.cfg.get("Overlay", "EmoteRain"))) {
					for (String s : Executable.emoteMap.keySet()) {
						Pattern p = Pattern.compile(".?\\b"+s+"\\b.?");
						Matcher m = p.matcher(message);
						int i = 0;
						while (m.find()) i++;
						if (i==0) continue;
						
						Image t = Executable.emoteMap.get(s);
	//					Console.println("Adding " + i + "x " + s + " on Screen");
						for (; i > 0; i--)
							Executable.overlay.addSprite(new EmoteDrop(t));
					}
				}
			}
		}
	}
}

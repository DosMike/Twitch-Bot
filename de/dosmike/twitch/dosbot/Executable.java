package de.dosmike.twitch.dosbot;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import com.itwookie.inireader.INIConfig;
import com.itwookie.telnet.TelnetClient;

import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;
import de.dosmike.twitch.dosbot.modulehandler.FightsHandler;
import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;
import de.dosmike.twitch.dosbot.modulehandler.VoteHandler;
import de.dosmike.twitch.dosbot.overlay.Window;

public class Executable {
	static TelnetClient client;
	static TelnetHandler handler;
	static boolean running = true;
	public static long secondstimer=0l, lastaction=0l;
	public static INIConfig cfg = new INIConfig();
	static boolean hybernate=false;
	static Window overlay=null;
	public static Map<String, Image> emoteMap = new HashMap<>();
	public static String targetChannel;
	public static boolean inChannel=false;
	public static boolean modPower=false;
	//private static APItelnetServer apiServer;
	public static VLCtelnetControl vlcPlayer=null;
	
	static List<String> cyclicMessages = new LinkedList<>();
	
	static Random rng = new Random(System.currentTimeMillis());
	
	public static TelnetClient getTelnetClient() {
		return client;
	}
	public static TelnetHandler getTelnetHandler() {
		return handler;
	}
	public static Window getOverlay() {
		return overlay;
	}
	
	public static void main(String[] args) {
		
		//*/// BEGIN Emote Download Module
		if (!new File("emotes_scanned.inf").exists()) {
			Console.println("Thanks to the guys over at ", Console.FB.WHITE, "https://twitchemotes.com", Console.RESET, " DosBot will downlaod the global emotes into an archive now...");
			
			EmoteDownloadJob edlj = new EmoteDownloadJob();
			edlj.start();
			do {
				Thread.yield();
				try {Thread.sleep(100l);} catch (Exception e) {}
			} while (edlj.isRunning());
			
			try { 
				new File("emotes_scanned.inf").createNewFile();
			} catch (Exception e) { }
			
			Console.println("Please extract the emotes somewhere and edit the config accoringly.");
			System.exit(0);
		}
		//*/// END Emote Download Module
		
		Console.println("\n", Console.FB.YELLOW, " -= DosBot 1.1 =-", Console.RESET, "\n");
		
		cfg.loadFrom(new File("DosBot.ini"));
		if (cfg.get("IRC", "Token") == null) {
			Console.println(Console.FG.RED, "No OAuth Token found.\nYou can generate one here: https://twitchapps.com/tmi/", Console.RESET);
			System.exit(0);
		}
		String myself = cfg.get("Username");
		
		//Console.println(cfg.get("Sounds", "VLC"));
		//*/// START VLC MEDIA PLAYER
		if (cfg.get("Sounds", "VLC") != null) {
			vlcPlayer = new VLCtelnetControl();
			vlcPlayer.start();
		}
		
		try {
			client = new TelnetClient(
					InetAddress.getByName("irc.chat.twitch.tv"), 6667, 
					handler = new TelnetHandler(myself));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Console.println(Console.FG.CYAN, "Loggin in...", Console.RESET);

		client.send("PASS " + cfg.get("IRC", "Token"));
		client.send("NICK " + myself);
		client.send("CAP REQ :twitch.tv/membership");
		client.send("CAP REQ :twitch.tv/tags");
		if (cfg.hasKey("IRC", "ChannelOverride") && cfg.get("ChannelOverride").length()>0)
			targetChannel = cfg.get("ChannelOverride").toLowerCase();
		else
			targetChannel = myself.toLowerCase();
		handler.enqueue("JOIN #" + targetChannel);
		//while (handler.isBusy()) Thread.yield();

		//load chat triggers
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream (new File("chat_trigger.txt") )));
			String trigger=null;
			String[] flags=null;
			String replacer;
			boolean flagLine=false; boolean defConfig=false;
			ChatTrigger ct=null;
			int lc=0;
			String line; while ((line = br.readLine())!=null) {
				lc++;
				line = line.trim();
				if (line.isEmpty() || line.charAt(0) == ';') continue;
				if (line.charAt(0) == '<') {
					if (ct != null && ct.hasResponses()) {
						handler.trigger.add(ct); ct = null; }
					
					trigger = line.substring(1);
					flagLine=true;
					defConfig=false;
					flags=null;
				} else if (line.charAt(0) == '!') {
					if (line.indexOf(' ')>0)
						throw new ParseException("Configuration of default commands do not allow arguments, only the base command at line " + lc, lc);
					if (handler.getCommandHandler().findCommandByName(line.substring(1))==null) {
						throw new ParseException("Configuration of unknown commands as default commands is not possible at line " + lc, lc);
					} else {
						trigger = "[!?]"+line.substring(1)+"(?:\\s.*)?"; //add optional any arguments to the regex, including "?" as start
						defConfig=true;
						flagLine=true;
						flags=null;
					}
				} else if (line.charAt(0) == '>') {
					if (ct==null) {
						if (trigger==null) {
							//throw new ParseException("The file can't contain replies before a trigger!", lc);
							cyclicMessages.add(line.substring(1));
							continue; //read next line
						} else {
							ct = new ChatTrigger(trigger, flags==null?new String[0]:flags);
						}
					}
					replacer = line.substring(1);
					ct.addResponse(replacer);
					flagLine=false;
				} else if (flagLine) {
					flags=line.split(", ");
					flagLine=false;
					if (defConfig) {
						ct = new ChatTrigger(trigger, flags);
						ct.setDefaultConfigurator();
						handler.trigger.add(ct);
						ct = null;
						defConfig=false;
						trigger = null;
					}
				} else {
					throw new ParseException("Line " + lc + " is not a flag line!", lc);
				}
			}
			if (ct != null && ct.hasResponses()) {
				handler.trigger.add(ct); ct = null; }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception ee) {
				
			}
		}
		
		//load emotes
		if ("true".equalsIgnoreCase(Executable.cfg.get("Overlay", "Enable"))) {
			String ebp = cfg.get("Overlay", "EmotesBasePath");
			for (String emote : cfg.keys("Emotes")) {
				File f = ebp==null?new File(cfg.get("Emotes", emote)):new File(ebp,cfg.get("Emotes", emote));
				try {
					emoteMap.put(emote, ImageIO.read(f));
				} catch (IOException e) {
					Console.println("\n", Console.FB.RED, "Error reading: " + f.getAbsolutePath(), Console.RESET);
	//				e.printStackTrace();
					continue;
				}
				Console.print(Console.LINE_RESET, Console.FG.GREEN, "Added emote ", Console.RESET, emote, " (" + emoteMap.size() + ")");
			}
			Console.println();
			
			overlay = new Window();
		}
		
//		Console.println(Console.FB.CYAN, "[Telnet] ", Console.RESET, " Starting API-Server @23232");
//		apiServer = new APItelnetServer(23232); //use a higher port as anything below 4k-something requires admin, so we can't use 23
		
		//Setup for keep-alive and waiting for return to exit
		Console.println(Console.FG.BLACK, Console.BG.CYAN, "Press return to save and exit!\n", Console.RESET);
		
		Thread stopper = new Thread() {
			@Override
			public void run() {
				try {
					System.in.read();
				} catch (Exception e) {}
				running = false;
				interrupt();
			}
		};
		stopper.start();
		
		secondstimer = System.currentTimeMillis()/1000;
		while (running) {
			if (System.currentTimeMillis()/1000 != secondstimer) {
				secondstimer=System.currentTimeMillis()/1000;
				VoteHandler.tick();
				modPower=(ChatRank.forUser(handler.myself, false).compareTo(ChatRank.MOD)>=0); //not extern since we only want to check mod
				TelnetHandler.commandsPer30sec = modPower?90:15;
				
				//TODO replace slow string compares that run EVERY MILLISECOND right now
				if ("true".equalsIgnoreCase(Executable.cfg.get("Points", "Enable")))
					PointsHandler.tick();
				if ("true".equalsIgnoreCase(Executable.cfg.get("Points", "Fights")))
					FightsHandler.tick();
			}
			if (!handler.isBusy() && (modPower || System.currentTimeMillis()-lastaction>1500)) {
				if (handler.queueSize()>0) {
					client.send(handler.dequeue());
					lastaction=System.currentTimeMillis();
				/*	lastaction=System.currentTimeMillis();
					hybernate(false);
				} else if (System.currentTimeMillis()-lastaction>10000) {
					lastaction=System.currentTimeMillis();
					hybernate(true); */
				}
			}
			//if (hybernate) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			//}
			//Thread.yield();
		}
		
//		apiServer.halt();
		AwardHandler.halt();
		client.send("QUIT");
		if (overlay!=null) overlay.close();
		if (vlcPlayer!=null) vlcPlayer.halt();
		client.halt();
		ClientStorage.save();
	}
	
	/*public static void hybernate(boolean enter) {
		if (hybernate==enter) return;
		hybernate=enter;
		Console.println(enter?"Hybernating...":"Waking up...");
	}*/
}

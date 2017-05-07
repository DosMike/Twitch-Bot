package de.dosmike.twitch.dosbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;


/** one chat trigger equals one custom chat command definition.
 * the chat trigger class is also used to linim and regulate default commands.
 * if you for any reason need to check that you can use the isDefaultConfigurator() function
 * by getting the iterator from Executable.handler or getTwitch() inside a Command sub-class. */
public class ChatTrigger {
	private Pattern p;
	private List<String> r = new LinkedList<>();
	
	private Map<String, Long>CCD=new HashMap<>(); //client cooldown map
	private long ccd=0l;
	public boolean isOnCooldown(String viewer) {
		if (ccd<=0l) return false; //client cooldowns disabled
		
		//remove all invalid cooldowns
		List<String> oldKeys = new LinkedList<>();
		for (Entry<String, Long> e : CCD.entrySet())
			if (System.currentTimeMillis()-e.getValue()>ccd)
				oldKeys.add(e.getKey());
		for (String s : oldKeys)
			CCD.remove(s);
		
		//check if we are on cooldown
		return (CCD.containsKey(viewer));
	}
	public void setCooldown(String viewer) {
		//put viewer on cooldown
		CCD.put(viewer, System.currentTimeMillis());
		//put global cooldown
		lastCall=System.currentTimeMillis();
	}
	
	private int lastAnswer = -1;
	private boolean fRandom = false;
	private long lastCall = 0;
	private int cooldown = 0;
	private int commandCost = 0;
	
	private boolean defaultConfiguratior = false;
	public void setDefaultConfigurator() {
		defaultConfiguratior=true;
	}
	public boolean isDefaultConfigurator() {
		return defaultConfiguratior;
	}
	
	//who's allowed to use this command
	private ChatRank minRank=ChatRank.NONE; //excluded
	private ChatRank maxRank=ChatRank.HOST; //included
	
	private static Pattern rng = Pattern.compile("%rng:~([0-9]+),([0-9]+)%");
	private static Pattern curl = Pattern.compile("%curl:~([^\\s]+)%");
	
	public ChatTrigger(String pattern, String... flags) {
		p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		for (String flag : flags) {
			if ("Random".equalsIgnoreCase(flag))
				fRandom = true;
			else if (flag.startsWith("Cooldown:")) {
				try {
					cooldown = Integer.parseInt(flag.substring(9));
				} catch (Exception e) {
					throw new RuntimeException("Value for Cooldown not an integer! (" + flag.substring(9) + ")");
				}
			} else if (flag.startsWith("ClientCooldown:")) {
				try {
					ccd = Integer.parseInt(flag.substring(15));
				} catch (Exception e) {
					throw new RuntimeException("Value for Cooldown not an integer! (" + flag.substring(15) + ")");
				}
			} else if (flag.startsWith("RankAbove:")) {
				ChatRank parse = ChatRank.find(flag.substring(10));
				if (parse == null)
					throw new RuntimeException("Value for RankAbove invalid!");
				else 
					minRank = parse;
			} else if (flag.startsWith("RankMax:")) {
				ChatRank parse = ChatRank.find(flag.substring(8));
				if (parse == null)
					throw new RuntimeException("Value for RankMax invalid!");
				else 
					maxRank = parse;
			} else if (flag.startsWith("Points:")) {
				int pc = Integer.parseInt(flag.substring(7));
				if (pc < 0)
					throw new RuntimeException("Cost for a Command can not be negative!");
				else 
					commandCost = pc;
			}
		}
	}
	public void addResponse(String response) {
		r.add(response);
	}
	public boolean hasResponses() {
		return r.size()>0;
	}
	
	public Matcher getMatcher(String message) {
		return p.matcher(message);
	}
	
	public boolean checkPerms(ChatRank rank) {
		return ChatRank.canUse(rank, minRank, maxRank);
	}
	public boolean checkPrice(String viewer) {
		return commandCost == 0 || PointsHandler.canAfford(viewer, commandCost);
	}
	public int getPrice() {
		return commandCost;
	}
	public boolean isOnCooldown() {
		return ((System.currentTimeMillis()-lastCall)/1000 < cooldown);
	}
	
	public String readableFlags() {
		String ret = "";
		
		if (minRank.equals(ChatRank.HOST) || maxRank.equals(ChatRank.NONE)) {
			ret += "This command was disabled. ";
		} else {
			if (minRank.equals(ChatRank.NONE) && maxRank.equals(ChatRank.HOST)) {
				ret += "This command can be used by anyone. ";
			} else if (minRank.equals(ChatRank.NONE)) {
				ret += "This command is usable up to rank "+maxRank.toString().toLowerCase()+". ";
			} else if (maxRank.equals(ChatRank.HOST)) {
				ret +="This command can not be used by "+minRank.toString().toLowerCase()+" and below. ";
			} else {
				ret +="This command requires you to be more than "+minRank.toString().toLowerCase()+" but max "+maxRank.toString().toLowerCase();
			}
			
			if (cooldown > 0) {
				ret += "The global cooldown is "+cooldown+" sec" + (ccd>0? " and "+ccd+" sec per viewer. " : ". ");
			} else if (ccd > 0) {
				ret += "There's a "+ccd+" sec cooldown per viewer. ";
			}
			
			if (commandCost > 0) {
				ret += "Using this command will cost you " + commandCost + " " + PointsHandler.getCurrencyName() + ". ";
			}
		}
		return ret.isEmpty()?"No custom limitations":ret;
	}
	
	public void respond(Matcher matcher, String sender) {
		//Spam-protection
		if (isOnCooldown()) return;
		if (isOnCooldown(sender)) return;
		
		//command is out of cooldown, pay for the response
		if (commandCost>0) PointsHandler.redeem(sender, commandCost);
		
		// start all the threads :D
		Thread asyncResponse = new Thread() {
			public void run() {
				//Console.println("Running async response thread");
				//pick response
				String resp;
				if (fRandom && r.size()>2) {
					int i;
					do { i = Executable.rng.nextInt(r.size()); }
					while (i == lastAnswer);
					lastAnswer=i;
				} else {
					if (++lastAnswer == r.size()) lastAnswer=0;
				}
				resp = new String(r.get(lastAnswer));
				
				for (int i = matcher.groupCount(); i > 0; i--) {
					resp.replace("$"+i, matcher.group(i));
				}
				resp = resp.replaceAll("%sender%", sender);
				Matcher r = rng.matcher(resp);
				while (r.find()) {
					int l = Integer.parseInt(r.group(1));
					int u = Integer.parseInt(r.group(2));
					if (u<l)l=0;
					u = u-l; //upper to range
					//r.replaceFirst(String.valueOf(Executable.rng.nextInt(u)+l));
					resp = resp.replace("%rng:~"+r.group(1)+","+r.group(2)+"%", String.valueOf(Executable.rng.nextInt(u)+l));
				}
				
				r = curl.matcher(resp);
				while (r.find()) {
					try {
						URL url = new URL(r.group(1));
						HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
						con.setRequestMethod("GET");
						con.setRequestProperty("User-Agent", "DosBot Chat Trigger curl Module");
						con.setRequestProperty("Accept-Encoding", "identity"); // do not compress result
						con.setConnectTimeout(1000);
						con.setReadTimeout(1000);
						con.setDoInput(true);
						if (con.getResponseCode() != 200) {
							resp = resp.replaceAll(r.group(0), "%-RESPONSE-"+con.getResponseCode()+"-%");
						} else {
							BufferedReader br=null;
							try {
								br = new BufferedReader(new InputStreamReader(con.getInputStream()));
								resp = resp.replaceAll(r.group(0), br.readLine());
							} catch (Exception e) {
								resp = resp.replaceAll(r.group(0), "%-READ-ERROR-%");
							} finally {
								try { br.close(); } catch (Exception ignore) {}
							}
						}
					} catch(MalformedURLException e) {
						resp = resp.replaceAll(r.group(0), "%-INVALID-URL-%");
					} catch (IOException e) {
						resp = resp.replaceAll(r.group(0), "%-READ-ERROR-%");
					}
				}
				
				Executable.handler.sendChat(resp);
			};
		};
		asyncResponse.start();
	}
}

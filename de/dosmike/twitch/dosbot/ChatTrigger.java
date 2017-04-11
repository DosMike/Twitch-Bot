package de.dosmike.twitch.dosbot;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTrigger {
	private Pattern p;
	private List<String> r = new LinkedList<>();
	
	private int lastAnswer = -1;
	private boolean fRandom = false;
	private long lastCall = 0;
	private int cooldown = 0;
	
	//who's allowed to use this command
	private ChatRank minRank=ChatRank.NONE; //excluded
	private ChatRank maxRank=ChatRank.HOST; //included
	
	private Pattern rng = Pattern.compile("%rng:~([0-9]+),([0-9]+)%");
	
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
	
	public void respond(Matcher matcher, String sender) {
		//Spam-protection
		if ((System.currentTimeMillis()-lastCall)/1000 < cooldown) return;
		lastCall=System.currentTimeMillis();
		
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
		
		Executable.handler.sendChat(resp);
	}
}

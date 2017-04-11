package de.dosmike.twitch.dosbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public enum ChatRank implements Comparable<ChatRank> {
	NONE(1),
	USER(2),
	FOLLOW(3),
	SUB(4),
	MOD(5),
	HOST(6);
	
	int val;
	ChatRank(int value) {
		val = value;
	}
	int getValue() {
		return val;
	}
	
//	public int compareTo(ChatRank that) {
//		return this.val-that.val;
//	}
	
	public static ChatRank find(String rank) {
		try {
			return ChatRank.valueOf(rank.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
	
//	public static ChatRank forUser(String user) {
//		return forUser(user, false);
//	}
	public static ChatRank forUser(String user, boolean Extern) {
		ChatRank result = 
			(user.equalsIgnoreCase(Executable.targetChannel) ?
				HOST :
				( ClientStorage.getCV(user, TelnetHandler.TwitchTags_Moderator).orElse("0").equals("1") ?
					MOD :
					( ClientStorage.getCV(user, TelnetHandler.TwitchTags_Subscriber).orElse("0").equals("1") ?
						SUB :
						( ClientStorage.getCV(user, TelnetHandler.TwitchTags_Follower).orElse("0").equals("1") ?
							FOLLOW :
							USER
						)
					)
				)
			);
		
		if (result.getValue() <= FOLLOW.getValue()) {
			// if user is no sub, check follow every 5 or so minute from
			// (raw, undocumented) api.newtimenow.com/follow-length/?channel=_CHANNEL_&user=_USER_
			// or http://api.rtainc.co/
			
			String fi = ClientStorage.getCV(user, "rtaincFollow").orElse("!0");
			final boolean follower= !fi.startsWith("!");
			
			ClientStorage.setCV(user, TelnetHandler.TwitchTags_Follower, (follower?"1":"0"), false);
			result = follower?ChatRank.FOLLOW:ChatRank.USER;
			
			long lastCheck = Long.parseLong(follower?fi:fi.substring(1));
			if (System.currentTimeMillis()-lastCheck > 1000l*60l*(follower?15l:5l)) { // check no more than once every 2 minutes / user
				//Console.println(Console.FG.PURPLE, "[Ranks] ", Console.RESET, "Poked for "+user);
				if (Extern) {
					//prevent calling twice while running:
					ClientStorage.setCV(user, "rtaincFollow", (follower?"":"!")+System.currentTimeMillis(), true);
					new Thread() {
						@Override
						public void run() {
							boolean lfollower = follower;
							BufferedReader br = null;
							try {
								Console.println(Console.FG.PURPLE, "[Ranks] ", Console.RESET, "User below SUB, checking follower-status for "+user);
								HttpsURLConnection con = (HttpsURLConnection)(new URL("https://api.rtainc.co/twitch/channels/"+Executable.targetChannel+"/followers/"+user).openConnection());
								con.setRequestMethod("GET");
								con.setConnectTimeout(1000);
								con.setReadTimeout(1000);
								con.setRequestProperty("User-Agent", "DosBot Twitch Bot / Running from " + Executable.handler.myself + " / max 1 request per bot instance per user every 5 minutes");
								con.setDoInput(true);
								if (con.getResponseCode() == 200) {
									br = new BufferedReader(new InputStreamReader(con.getInputStream()));
									lfollower = !br.readLine().contains("isn't"); // user is following if anything but "x isn't following y" is returned (too lazy for regex)
								}
							} catch(Exception e) {} finally {
								try { br.close(); } catch (Exception ignore) {}
							}
							ClientStorage.setCV(user, TelnetHandler.TwitchTags_Follower, (lfollower?"1":"0"), false);
							ClientStorage.setCV(user, "rtaincFollow", (lfollower?"":"!")+System.currentTimeMillis(), true);
							Console.println(Console.FG.PURPLE, "[Ranks] ", Console.RESET, "Updated "+user+" to "+(lfollower?"be follower!":"not following!"));
	
						}
					}.start();
				}
			}
		}
		return result;
	}

	
	public static boolean canUse(ChatRank rank, ChatRank min, ChatRank max) {
		if (rank.compareTo(min) <= 0) return false;
		if (rank.compareTo(max) > 0) return false;
		return true;
	}
}

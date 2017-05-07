package de.dosmike.twitch.dosbot;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.itwookie.telnet.ReceiverCallback;
import com.itwookie.telnet.Stoppable;
import com.itwookie.telnet.TelnetClient;

// Looks like VLC has no means to close a Telnet connection on it's own, good for us as we don't need to send keepalives
//TODO this class is a hacky mess, plis clean up and fix :)

//is stoppable to query is_playing
public class VLCtelnetControl extends Stoppable implements ReceiverCallback {
	TelnetClient vlc;
	Process process;
	boolean waiting=false;
	boolean playing=false; 
	List<SongMeta> playlist = new LinkedList<>();
	SongMeta currentsong = null;
	long timeleft=0;
	int volume = 100;
	public long maxPlaylistLength=10*60*60; // by default 10 hours
	
	//List<String> rcqueue = new LinkedList<>();
	String awnserFor=null;
	List<String> requests = new LinkedList<>();
	Map<String, String> values = new HashMap<>();
	
	@Override
	public void onStart() {
		values.put("volume", "70");
		values.put("is_playing", "0");
		values.put("get_title", "Nothing");
		try {
			maxPlaylistLength = Long.parseLong(Executable.cfg.get("SongRequest", "QueueLimit"));
			if (maxPlaylistLength<0)
				maxPlaylistLength = 10*60*60;
			else
				maxPlaylistLength *= 60; //minutes -> seconds
		} catch (Exception ignore) { }
		
		//start VLC
		try {
			String randomPW = "DosBot" + new String(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(new Long(System.currentTimeMillis()).toString().getBytes()))); 
			String app = Executable.cfg.get("Sounds", "VLC");
			if (app.indexOf(' ')>0) app = '"'+app+'"';
			process = Runtime.getRuntime().exec(app
					+ " -I telnet --telnet-password="+randomPW+" --telnet-port=8080");// --no-video
			//process = new ProcessBuilder(Executable.cfg.get("Sounds", "VLC"), "--no-video", "-I telnet", "--telnet-password="+randomPW, "--telnet-port=8080").start();
			Console.println("[VLC] started with PW " + randomPW); //TODO delete this line
			
			vlc = new TelnetClient(InetAddress.getLoopbackAddress(), 8080, this);
			rc(randomPW);
			rc("volume 70"); //TODO from config
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void loop() {
		if (awnserFor==null) rc("is_playing");
		/*if (!playing) {
			currentsong=null;
			next();
		}*/
		if (timeleft > 0) timeleft--;
		if (timeleft == 0 && "0".equals(values.get("is_playing"))) {
			timeleft=0;
			currentsong=null;
			next();
		}
		try { Thread.sleep(1000); }
		catch (Exception e) {}
	}
	
	@Override
	public void onHalted() {
		rc("shutdown"); //should quit vlc
		vlc.halt();
		process.destroy();
	}
	private void rc(String cmd) {		
		if (values.containsKey(cmd)) {
			waiting=true;
			awnserFor=cmd;
			vlc.send(cmd);
			while (waiting) Thread.yield();
		} else 
			vlc.send(cmd);
	}
	
	public void append(SongMeta m) {
		playlist.add(m);
	}
	/*
	public void append(String url, String user) {
		SongMeta m = SongMeta.fromURL(url, user);
		if (m == null || m.hasError()) {
			Executable.handler.sendChat(user, "The song you requested could not be added!");
		} else {
			playlist.add(m);
			Executable.handler.sendChat(user + " added \"" + m.getTitle() + "\" to the playlist");
		}
	}
	*/
	
	public void next() {
		if (!playlist.isEmpty()) {
			currentsong=playlist.get(0);
			playlist.remove(0);
			play(currentsong.getMediaURL());
			timeleft=currentsong.getLength();
		} else {
			vlc.send("stop");
		}
	}
	
	public void play(String url) {
		rc("clear");
		rc("add " + url);
		rc("play");
		rc("is_playing");
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public long getPlaylistLength() {
		long len = timeleft;//(currentsong != null ? currentsong.getLength() : 0);
		for (int i=0; i < playlist.size(); i++) {
			len+= playlist.get(i).getLength();
		}
		return len;
	}
	public String getLength() {
		int h=0, m=0; long s=getPlaylistLength();
		while (s > 3600) { h+=1; s-=3600; }
		while (s > 60) { m+=1; s-=60; }
		if (h>0) {
			return String.format("%d:%02d:%02d hours", h,m,s);
		} else if (m>0) {
			return String.format("%d:%02d minutes", m,s);
		} else {
			return String.format("%d seconds", s);
		}
	}
	
	public SongMeta getCurrentSong() {
		return currentsong;
	}
	
	public int getVolume() {
		rc("volume");
		volume = Integer.parseInt(values.get("volume"));
		return volume;
	}
	public void setVolume(int volume) {
		vlc.send("volume "+volume);
	}
	
	public boolean isSongQueued(SongMeta m) {
		for (int i = 0; i < playlist.size(); i++)
			if (playlist.get(i).getTitle().equals(m.getTitle()))
					return true;
		//also check currently playing
		if (currentsong != null) return currentsong.getTitle().equals(m.getTitle());
		return false;
	}
	public int getSongsQueuedBy(String user) {
		int z=0;
		for (int i = 0; i < playlist.size(); i++)
			if (playlist.get(i).getRequester().equalsIgnoreCase(user))
				z++;
		if (currentsong != null && currentsong.getRequester().equalsIgnoreCase(user)) z++;
		return z;
	}
	
	@Override
	public void onReceive(String message, TelnetClient client) {
		while (message.startsWith("> "))
		message = message.substring(2); //skip the "> " vlc is putting in front of every line to make input look better
		
		if (awnserFor == null) {
			//Console.println(Console.FB.RED, "[VLC] Unknown awnser: " + message, Console.RESET);
			return;
		}
		
		values.put(awnserFor, message);
		
		awnserFor = null; // awaiting nothing further
		waiting=false;
		//try { volume = Integer.parseInt(message); }
		//catch (Exception e) {}
	}
}

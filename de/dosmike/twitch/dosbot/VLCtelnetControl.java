package de.dosmike.twitch.dosbot;

import java.net.InetAddress;
import java.security.MessageDigest;

import com.itwookie.telnet.ReceiverCallback;
import com.itwookie.telnet.TelnetClient;

// Looks like VLC has no means to close a Telnet connection on it's own, good for us as we don't need to send keepalives
//TODO make rc commands work async by extending Stoppable
//TODO add playlist with cached track information

public class VLCtelnetControl implements ReceiverCallback {
	TelnetClient vlc;
	Process process;
	String lastCommand="";
	boolean waiting=false;
	
	public VLCtelnetControl() {
		//start VLC
		try {
			String randomPW = "DosBot"+new String(MessageDigest.getInstance("MD5").digest(new Long(System.currentTimeMillis()).toString().getBytes()));
			process = new ProcessBuilder(Executable.cfg.get("BOT", "VLC"),
					"--no-video", "-I telnet", "--telnet-password="+randomPW, "--telnet-port=8080").start();
			Console.println("[VLC] started with PW " + randomPW); //TODO delete this line
			
			vlc = new TelnetClient(InetAddress.getLoopbackAddress(), 8080, this);
			vlc.send(randomPW);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void halt() {
		vlc.send("shutdown"); //should quit vlc
	}
	
	private void rc(String cmd) {
		waiting=true;
		vlc.send(lastCommand=cmd);
		while (waiting) Thread.yield();
	}
	private void rc(String cmd, String arg) {
		waiting=true;
		vlc.send((lastCommand=cmd)+" "+arg);
		while (waiting) Thread.yield();
	}
	
	public void play(String url) {
		rc("clear");
		rc("add", url);
	}
	
	@Override
	public void onReceive(String message, TelnetClient client) {
		waiting=false;
	}
}

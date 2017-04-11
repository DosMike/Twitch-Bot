package com.itwookie.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.dosmike.twitch.dosbot.Console;

public class TelnetReceiver extends Stoppable {
	BufferedReader in;
	ReceiverCallback callback;
	TelnetClient backLink;
	
	public TelnetReceiver(TelnetClient parent, ReceiverCallback callback) {
		backLink = parent;
		this.callback = callback; 
		try {
			in= new BufferedReader(new InputStreamReader(parent.socket.getInputStream()));
		} catch (Exception e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED, "Unable to set up Receiver");
		}
	}
	
	@Override
	public void loop() {
		String ret = null;
		try {
			ret = in.readLine();
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.RESET, "Reading was interrupted");
		}
//		System.out.println("s>>c "+ret);
		if (ret != null && !(ret=ret.trim()).isEmpty()) 
			callback.onReceive(ret, null);
	}
	
	@Override
	public void onHalted() {
		Console.println(Console.FG.CYAN, "[Telnet] ", Console.RESET, "Receiver was halted!");
	}
	
	@Override
	public void halt() {
		super.halt();
		try {
			in.close();
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED, "Unable to close Receiver-stream!");
		}
	}
}

package com.itwookie.telnet;

import java.io.OutputStreamWriter;
import java.net.Socket;

import de.dosmike.twitch.dosbot.Console;

public class TelnetTransmitter {
	OutputStreamWriter out;
	
	public TelnetTransmitter(Socket socket) {
		try {
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (Exception e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED, "Unable to set up Transmitter");
		}
	}
	
	public void Transmit(String data) {
//		System.out.println("s<<c "+data);
		try {
			out.write(data+"\r\n");
			out.flush();
		} catch (Exception e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED, "Transmitter failed to send data");
		}
	}
	
	public void close() {
		try {
			out.flush();
			out.close();
		} catch (Exception e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED, "Unable to close Transmitter-stream!");
			return;
		}
		Console.println(Console.FG.CYAN, "[Telnet] ", Console.RESET, "Transmitter was closed");
	}
}

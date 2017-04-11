package com.itwookie.telnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import de.dosmike.twitch.dosbot.Console;

public class TelnetClient {
	
	TelnetTransmitter Tx;
	TelnetReceiver Rx;
	Socket socket;
	boolean running = true;
	
	public TelnetClient(InetAddress target, int port, ReceiverCallback callback) {
		
		try {
			socket = new Socket(target, port);
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED ,"Failed to open Connection!", Console.RESET);
			return;
		}
		
		Tx = new TelnetTransmitter(socket);
		Rx = new TelnetReceiver(this, callback);
		Rx.start();
		
	}
	
	protected TelnetClient(Socket s) {
		socket = s; 
	}
	public static TelnetClient fromSocket(Socket s, ReceiverCallback callback) {
		TelnetClient created = new TelnetClient(s);
		created.Tx = new TelnetTransmitter(created.socket);
		created.Rx = new TelnetReceiver(created, callback);
		created.Rx.start();
		return created;
	}
	
	public void send(String data) {
		Tx.Transmit(data);
	}
	
	public void halt() {
		running = false;
		Tx.close();
		Rx.halt();
		try {
			socket.close();
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED ,"Failed to close Connection!", Console.RESET);
			return;
		}
		Console.println(Console.FG.CYAN, "[Telnet] ", Console.RESET ,"Connection was terminated");
	}
}

package com.itwookie.telnet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import de.dosmike.twitch.knarf.Console;

public class TelnetServer extends Stoppable {
	
	ServerSocket socket;
	boolean running = true;
	ReceiverCallback globalCallback;
	List<TelnetClient> clients = new LinkedList<>();
	
	public TelnetServer(int port, ReceiverCallback callback) {
		globalCallback = callback;
		try {
			socket = new ServerSocket(port);
			socket.setSoTimeout(1000);
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED ,"Failed to open Connection!", Console.RESET);
			return;
		}
	}
	
	@Override
	public void loop() {
		try {
			Socket client = socket.accept();
			TelnetClient svClient = TelnetClient.fromSocket(client, globalCallback);  
			clients.add(svClient);
		} catch (IOException e) {
			//Timeout
		}
		
		//clear old connections
		LinkedList<TelnetClient> deadClients = new LinkedList<TelnetClient>(); 
		for (TelnetClient c : clients) {
			if (c.socket.isClosed()) {
				c.halt();
				deadClients.add(c);
			}
		}
		clients.removeAll(deadClients);
	}
	
	public void broadcast(String data) {
		//Tx.Transmit(data);
	}
	
	@Override
	public void onHalted() {
		//disconnect all clients properly
		for (TelnetClient c : clients) {
			try {
				c.socket.close();
			} catch (Exception e) {
				Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.PURPLE ,"Socket " + c.socket + " was not closed correctly", Console.RESET);
			}
			c.halt();
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			Console.println(Console.FG.CYAN, "[Telnet] ", Console.FB.RED ,"Failed to close Connection!", Console.RESET);
			return;
		}
		Console.println(Console.FG.CYAN, "[Telnet] ", Console.RESET ,"Connection was terminated");
	}

}

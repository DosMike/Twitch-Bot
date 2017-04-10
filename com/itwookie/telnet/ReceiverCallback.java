package com.itwookie.telnet;

public interface ReceiverCallback {
	public void onReceive(String message, TelnetClient client);
}

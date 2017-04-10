package com.itwookie.telnet;

public abstract class Stoppable extends Thread implements Runnable {
	
	boolean running=false;
	
	@Override
	public void run() {
		running=true;
		onStart();
		while(running) loop();
		onHalted();
	}
	
	public boolean isRunning() {
		return running;
	}
	public void halt() {
		running = false;
	}

	/** Called after start() was called on this thread **/
	public void onStart() {}
	
	public abstract void loop();
	
	/** Called when the loop() finished after calling halt() **/
	public void onHalted() {}
	
}

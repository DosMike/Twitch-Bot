package de.dosmike.twitch.dosbot.modulehandler;

import com.itwookie.telnet.Stoppable;

public class AwardHandler {
	static long awardStart=0l; //start timestamp, 0 means nothing going
	static int duration=120; //the duration in seconds
	
	static String award="";
	static Runnable stopcallback;
	static Stoppable eventTimer=null;
	
	public static boolean start(String award, Runnable whenOver) {
		if (awardStart!= 0) return false;
		
		AwardHandler.award=award;
		awardStart=System.currentTimeMillis();
		stopcallback=whenOver;
		if (whenOver != null) { 
			eventTimer=new Stoppable() {
				@Override
				public void loop() {
					if (System.currentTimeMillis()-awardStart < duration*1000)
						try { Thread.sleep(1000); } catch (Exception e) { }
					else
						halt();
				}
				public void onHalted() {
					awardStart=0;
					stopcallback.run();
					eventTimer=null;
					AwardHandler.award="";
				};
			};
			eventTimer.start();
		}
		return true;
	}
	
	public static boolean isAwardRunning() {
		return awardStart != 0l;
	}
	
	public static String getAward() {
		return award;
	}
	
	public static void halt() {
		if (eventTimer != null)	 eventTimer.halt();
		else {
			awardStart=0l;
		}
	}
}

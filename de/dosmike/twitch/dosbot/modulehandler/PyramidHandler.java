package de.dosmike.twitch.dosbot.modulehandler;

import de.dosmike.twitch.dosbot.Console;
import de.dosmike.twitch.dosbot.Executable;

public class PyramidHandler {
	static Pyramid mid = null;
	
	public static void doPyramid(String user, String message) {
		//not enough people in chat to pyramid
		if (Executable.getTelnetHandler().getViewcount()<2) return;
		
		boolean delete=false;
		if (mid == null) {
			char[] chars = message.toCharArray();
		    for (char c : chars)
		        if((c<'A'||c>'Z')&&(c<'a'||c>'z'))
		            return;
		    if (message.length() > 3) //i don't know any emotes shorter than 3 chars
		    	mid = new Pyramid(user, message);
		} else if(!mid.isOwner(user)) {
			delete=true;
			if (mid.getHeight()>=2) {
				int val = mid.breakValue();
				Executable.getTelnetHandler().sendChat(user + " was awarded " + val + " "+PointsHandler.getCurrencyName()+" for breaking the pyramid of " + mid.owner + " with " + (Executable.getTelnetHandler().getViewcount()-1) + " viewers");
				PointsHandler.award(user, val);
			}
		} else {
			String[] words = message.split(" ");
			for (int i = 1; i < words.length; i++)
				if (!words[0].equals(words[1])) { delete=true; break; }
			if (!delete) {
				Console.println("Pyramid: repeats " + words.length);
				delete = !mid.extend(words[0], words.length);
			} if (mid.isDone()) {
				delete=true;
				int val = mid.getValue();
				//Console.println("Pyramid: " + mid.emote + " " + mid.height + "x pyramid completed for " + val + " points");					
				Executable.getTelnetHandler().sendChat(user + " was awarded " + val + " "+PointsHandler.getCurrencyName()+" for building a " + mid.getHeight() + " high " + mid.getEmote() + " -mid while " + (Executable.getTelnetHandler().getViewcount()-1) + " viewers were watching, doing nothing about it.");
				PointsHandler.award(user, val);
			}
		}
		if (delete)
			mid = null;
//		else {
//			Console.println("Pyramid: " + mid.emote + " " + mid.progress + "/" + mid.length + " @" + mid.height);
//		}
	}
}

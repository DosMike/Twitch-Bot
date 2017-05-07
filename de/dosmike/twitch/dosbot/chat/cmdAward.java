package de.dosmike.twitch.dosbot.chat;

import java.util.List;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;

public class cmdAward extends Command {
	public cmdAward() {
		super("Award", "Give points directly to a viewer");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
//		Now limited by chat trigger
//		if (rank.compareTo(ChatRank.MOD)<0) {
//			getTwitch().sendChat(user, "Only Mods+ can award points!");
//			return;
//		}
		
		if (args.length < 2) {
			getTwitch().sendChat("Syntax for award is !award <VIEWER> <AMMOUNT>");
			return false;
		}
		
		int v;
		try {
			v = Integer.parseInt(args[1]);
			if (v <= 0) throw new IllegalArgumentException();
		} catch (Exception ignore) {
			getTwitch().sendChat("You have to award at least 1 "+PointsHandler.getCurrencyName());
			return false;
		}
		
		if ("@all".equalsIgnoreCase(args[0])) {
			List<String> vl = getTwitch().getViewers();
			for (String s : vl) {
				PointsHandler.award(s, v);
			}
			getTwitch().sendChat("PogChamp " + user + " just gave " + v + " " + PointsHandler.getCurrencyName() + " to everyone in chat Kreygasm");
		} else {
			if (!getTwitch().knowsViewer(args[0])) {
				getTwitch().sendChat("A viewer named " + args[0] + " is not watching the stream right now");
				return false;
			}
			
			PointsHandler.award(args[0], v);
			getTwitch().sendChat(user + " just awarded " + args[0] + " with " + v + " " + PointsHandler.getCurrencyName());
		}
		return true;
	}
}

package de.dosmike.twitch.knarf;

import java.util.List;

public class PointsHandler {
	
	private static int s = 0;
	/** call this every second */
	public static void tick() {
		if (++s < 60) return; s = 0;
		//reset executes every minute
		List<String> viewers = Executable.handler.getViewers();
		Console.println(Console.FB.YELLOW, "Giving 1 point to all " + viewers.size() + " viewers", Console.RESET);
		for (String viewer : viewers) {
			ClientStorage.setCV(viewer, "Points", Integer.parseInt(ClientStorage.getCV(viewer, "Points").orElse("0"))+1, true);
		}
	}
	
	public static void award(String viewer, int points) {
		ClientStorage.setCV(viewer, "Points", Integer.parseInt(ClientStorage.getCV(viewer, "Points").orElse("0"))+points, true);
	}
	
	public static boolean canAfford(String viewer, int points) {
		return Integer.parseInt(ClientStorage.getCV(viewer, "Points").orElse("0")) >= points;
	}
	
	public static int balance(String viewer) {
		return Integer.parseInt(ClientStorage.getCV(viewer, "Points").orElse("0"));
	}
	
	public static int redeem(String viewer, int points) {
		int n = Integer.parseInt(ClientStorage.getCV(viewer, "Points").orElse("0")) - points;
		if (n < 0) n = 0;
		ClientStorage.setCV(viewer, "Points", n, true);
		return n;
	}
	
	public static String getCurrencyName() {
		return Executable.cfg.hasKey("Points", "Currency")?Executable.cfg.get("Points", "Currency"):"Points";
	}
}

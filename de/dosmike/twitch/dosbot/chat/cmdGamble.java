package de.dosmike.twitch.dosbot.chat;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.modulehandler.AwardHandler;
import de.dosmike.twitch.dosbot.modulehandler.PointsHandler;

public class cmdGamble extends Command {
	public cmdGamble() {
		super("gamble", "Gamble "+PointsHandler.getCurrencyName()+" - either win or loose Kappa");
	}
	
	static int pot=0;
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (!AwardHandler.getAward().equals("gambletime")) {
			getTwitch().sendChat("You need to redeem !gambletime first - check ?gambletime first");
			return false;
		}
		if (args.length < 1) {
			getTwitch().sendChat(user+", you have to specify the ammount of "+PointsHandler.getCurrencyName()+" you want to gamble.");
			return false;
		} else {
			try {
				int v = Integer.parseInt(args[0]);
				if (v<=0) throw new IllegalArgumentException();
				
				if (!PointsHandler.canAfford(user, v)) {
					getTwitch().sendChat(user, "You don't have that much "+PointsHandler.getCurrencyName()+" FailFish");
					return false;
				}
				int r = rng.nextInt(100);
				if (r < 5){
					getTwitch().sendChat("(Gamble) Please gamble responsibly HeyGuys");
				} else if (r < 50) {
					pot+=v;
					PointsHandler.redeem(user, v);
					getTwitch().sendChat("(Gamble) "+user+" donated "+v+" "+PointsHandler.getCurrencyName()+" to the pot BabyRage");
				} else if (r < 95) {
					//if (pot >= v) {
						pot -= v;
						PointsHandler.award(user, v);
						getTwitch().sendChat("(Gamble) "+user+" doubled his incentive of "+v+" "+PointsHandler.getCurrencyName());
					//} else {
						//PointsHandler.award(user, pot);
						//getTwitch().sendChat("(Gamble) "+user+" won the jack pot worth "+(pot+v)+" "+PointsHandler.getCurrencyName());
						//pot = 0;
					//}
						if (pot <0) pot=0;
				} else {
					PointsHandler.award(user, pot);
					getTwitch().sendChat("(Gamble) "+user+" won the jackpot worth "+(pot+v)+" "+PointsHandler.getCurrencyName()+" (+"+pot+")");
					pot = 0;
				}
			} catch (Exception e) {
				getTwitch().sendChat(user, "Please specify a natural number, no fractions");
				return false;
			}
		}
		return false;
	}
}

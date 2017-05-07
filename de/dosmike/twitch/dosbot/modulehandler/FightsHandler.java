package de.dosmike.twitch.dosbot.modulehandler;

import java.util.List;
import java.util.Random;

import de.dosmike.twitch.dosbot.ClientStorage;
import de.dosmike.twitch.dosbot.Executable;

public class FightsHandler {
	
	static Boss[] bosses = new Boss[] {
		new Boss("Psycho Mantis"), // (METAL GEAR SOLID)
		new Boss("Mike Tyson"), // (MIKE TYSON’S PUNCH-OUT!!)
		new Boss("The Archdemon"), // (DRAGON AGE: ORIGINS)
		new Boss("The Master"), // (FALLOUT)
		new Boss("Bowser"), // (SUPER MARIO 64)
		new Boss("Ganon"), // (THE LEGEND OF ZELDA: OCARINA OF TIME)
		new Boss("Big Daddy"), // (BIOSHOCK)
		new Boss("Lavos"), // (CHRONO TRIGGER)
		new Boss("Ornstein and Smough"), // (DARK SOULS)
		new Boss("The final Colossus"), // (SHADOW OF THE COLOSSUS)
		new Boss("UGH-ZAN III"), // (SERIOUS SAM: THE FIRST ENCOUNTER)
		new Boss("Diablo"), // (DIABLO 3)
		new Boss("Ultros"), // (FINAL FANTASY VI)
		new Boss("Dracula"), // (CASTLEVANIA)
		new Boss("Nihilanth") // (HALF-LIFE)
	};
	static String[] weapons = new String[] {
		"Hellberd",
		"Zweihander",
		"Uchigatana",
		"Twin-Blades",
		"Balistic Bow",
		"AK-47",
		"Tompson Machinegun",
		"Heavy Machinegun",
		"Desert Eagle",
		"AWP",
		"Poop",
		"Tree Branch",
		"Walter PPK",
		"Gaus Cannon",
		"BFG 3000"
	};
	
	public static final String csWEAPON="Fight_Weapon";
	public static final String csDAMAGE="Fight_Damage";
	public static final String csDURAB="Fight_Durability";
	
	//current boss stuff
	static int maxLevel=1;
	static Boss current=null;
	static long lastSpawn=0;
	
	public static Random rng = new Random(System.currentTimeMillis());
	public static void newBoss() {
		AwardHandler.start("bossfight", null); //0 = not timed
		
		current = bosses[rng.nextInt(bosses.length)];
		int level = rng.nextInt(maxLevel)+1;
		int hp = maxLevel*10 + level*level + rng.nextInt(15);
		current.spawn(level, hp);
		lastSpawn = System.currentTimeMillis();
	}
	
	public static boolean hit(String viewer) {
		if (current==null) {
			Executable.getTelnetHandler().sendChat("There's currently no fight running");
			return false;
		}
		
		String result = "";
		
		String weapon = ClientStorage.getCV(viewer, csWEAPON).orElse("Hands");
		int dmg = Integer.parseInt(ClientStorage.getCV(viewer, csDAMAGE).orElse("1"));
		int dur = Integer.parseInt(ClientStorage.getCV(viewer, csDURAB).orElse("0"));
		
		if ("Hands".equals(weapon)) {
			//Don't damage
		} else {
			dur--;
			if (dur<=0) {
				result += "With that strike, the beautiful " + weapon + " of "+viewer+" crumbles in their hands!";
				ClientStorage.setCV(viewer, csWEAPON, "Hands", true);
				ClientStorage.setCV(viewer, csDAMAGE, 1, true);
				ClientStorage.setCV(viewer, csDURAB, 0, true);
			} else {
				ClientStorage.setCV(viewer, csDURAB, dur, true);
			}
		}
		if (current.hit(viewer.toLowerCase(), dmg)) {
			//killled the boss
			result += viewer + " killed " + current.name + " (Level "+current.getLevel()+") ! ";
			
			List<String> fighter = current.getRanks();
			
			//generate loot
			int wpnLevel=rng.nextInt(current.getLevel())+1;
			int wpnDmg=wpnLevel+(rng.nextInt(maxLevel)+1);
			int wpnDur=rng.nextInt(7)+5;
			String wpnName = weapons[rng.nextInt(weapons.length)] + "(L" + wpnLevel + ")";
			
			int reward = (rng.nextInt(10)+current.getLevel()*10)*10;
			
			if (fighter.size()<=1) {
				result += "The boss dropped a " + wpnName + " [" + wpnDmg + " Dmg, " + wpnDur + " Dur]"  + ("Hands".equals(weapon)?". ":" that " + viewer + " uses now instead of their old " + weapon + " [" + dmg + " Dmg, " + dur + " Dur]. ");
				ClientStorage.setCV(viewer, csWEAPON, wpnName, true);
				ClientStorage.setCV(viewer, csDAMAGE, wpnDmg, true);
				ClientStorage.setCV(viewer, csDURAB, wpnDur, true);
				
				result += "While leaving " + viewer + " gained " + reward + " " + PointsHandler.getCurrencyName() + ".";
				PointsHandler.award(viewer, reward);
			} else {
				weapon = ClientStorage.getCV(fighter.get(0), csWEAPON).orElse("Hands");
				dmg = Integer.parseInt(ClientStorage.getCV(fighter.get(0), csDAMAGE).orElse("1"));
				dur = Integer.parseInt(ClientStorage.getCV(fighter.get(0), csDURAB).orElse("0"));
				
				result += "As the one inflicting most damage, "+ fighter.get(0) +" claims the " + wpnName + " [" + wpnDmg + " Dmg, " + wpnDur + " Dur] the boss just dropped"  + ("Hands".equals(weapon)?". ":" and replaces their old " + weapon + " [" + dmg + " Dmg, " + dur + " Dur] with it. ");
				ClientStorage.setCV(fighter.get(0), csWEAPON, wpnName, true);
				ClientStorage.setCV(fighter.get(0), csDAMAGE, wpnDmg, true);
				ClientStorage.setCV(fighter.get(0), csDURAB, wpnDur, true);
				
				result += "While leaving, the warriors see some " + PointsHandler.getCurrencyName() + " on the ground. " + reward + " go to " + fighter.get(0) + " for dealing most damage, " + reward/2 + " to " + fighter.get(1) + " for being the most helpfull";
				PointsHandler.award(fighter.get(0), reward);
				PointsHandler.award(fighter.get(1), reward/2);
				if (viewer.equalsIgnoreCase(fighter.get(0)) || viewer.equalsIgnoreCase(fighter.get(1))) {
					result += ".";
				} else {
					result += " and " + reward/2 + " for " + viewer + " for killing the boss.";
					PointsHandler.award(viewer, reward/2);
				}
			}
			
			current = null;
			maxLevel++;
			AwardHandler.halt();
		} else {
			result += viewer + " hit " + current.name + "(L" + current.level + ", " + current.hp + "HP) with their " + weapon + " for " + dmg + " HP! ";
		}
		
		Executable.getTelnetHandler().sendChat(result);
		return true;
	}
	
	/** check and interact with your weapons 
	 * action: 0 status, 1 repair, 2 buff */
	public static boolean weapon(String viewer, int action) {
		String result = "";
		
		String weapon = ClientStorage.getCV(viewer, csWEAPON).orElse("Hands");
		int dmg = Integer.parseInt(ClientStorage.getCV(viewer, csDAMAGE).orElse("1"));
		int dur = Integer.parseInt(ClientStorage.getCV(viewer, csDURAB).orElse("0"));
		
		if (action==0) { //status
			result = viewer + " currently fights with their " + 
					weapon + " [" + dmg + " Dmg, " + dur + " Dur]";
		} else if (action==1) { //repair
			if ("Hands".equals(weapon)) {
				Executable.getTelnetHandler().sendChat(viewer, "You can't repair your hands NotLikeThis");
				return false;
			} else {
				dur++;
				result = viewer + " increased the durability of their weapon by 1 and now has a " + weapon + " [" + dmg + " Dmg, " + dur + " Dur]";
				ClientStorage.setCV(viewer, csDURAB, dur, true);
			}
		} else if (action==2) { //buff
			if ("Hands".equals(weapon)) {
				Executable.getTelnetHandler().sendChat(viewer, "You can't buff your hands FailFish");
				return false;
			} else {
				dmg++;
				result = viewer + " buffed their weapon over 1 dmg and now has a " + weapon + " [" + dmg + " Dmg, " + dur + " Dur]";
				ClientStorage.setCV(viewer, csDURAB, dmg, true);
			}
		} else {
			result = "Shouldn't be able to see this WutFace";
		}
		
		Executable.getTelnetHandler().sendChat(result);
		return true;
	}
	
	public static void tick() {
		if (current == null && "true".equalsIgnoreCase(Executable.cfg.get("Points", "AutoSummon")) ) {
			// .5% spawn chance / second after 15 minutes
			if (System.currentTimeMillis()-lastSpawn > 1000*60*15 && rng.nextFloat()<0.005) {
				newBoss();
			}
		} else if (current != null) {
			if (rng.nextFloat()<0.005) {
				String target="";
				String weapon;
				boolean oke=false;
				for (int i=0; i<10; i++) {
					target = Executable.getTelnetHandler().getViewers().get(rng.nextInt(Executable.getTelnetHandler().getViewcount()));
					if (Executable.getTelnetHandler().isLurking(target)) continue;
					weapon = ClientStorage.getCV(target, csWEAPON).orElse("Hands");
					if (!"Hands".equals(weapon)) oke = true;
				}
				if (!oke) return;
				int ammount = rng.nextInt(10);
				if (PointsHandler.canAfford(target, ammount)) {
					Executable.getTelnetHandler().sendChat(current.name + " strikes and hits " + target + " stealing " + ammount + " precious " + PointsHandler.getCurrencyName());
					PointsHandler.redeem(target, ammount);
				}
			}
		}
	}
	
	public static boolean canSpawn() {
		return (
			!AwardHandler.isAwardRunning() && 	
			current == null/* && System.currentTimeMillis()-lastSpawn > 1000*60*15*/); //timeout handled by chat_trigger
	}
}

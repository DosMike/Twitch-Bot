package de.dosmike.twitch.dosbot.modulehandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.dosmike.twitch.dosbot.Executable;

public class Boss {
	Map<String, Integer> damage = new HashMap<>();
	
	String name;
	Boss(String name){
		this.name=name;
	}
	int hp;
	int level;
	public void spawn(int level, int hp) {
		this.level=level;
		this.hp=hp;
		Executable.getTelnetHandler().sendChat(name + " has spawned at level " + level + " (" + hp + "HP)");
	}
	public boolean hit(String source, int a) {
		damage.put(source, (a>hp?hp:a) + (damage.containsKey(source)?damage.get(source):0));
		hp-=a;
		return hp<=0;
	}
	public int getLevel() {
		return level;
	}
	public List<String> getRanks() {
		Map<String, Integer> s = damage.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));
		List<String> desc = new LinkedList<>();
		for (String n : s.keySet()) desc.add(n);
		return desc;
	}
}

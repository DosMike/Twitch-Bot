package de.dosmike.twitch.dosbot;

import java.io.File;
import java.util.Optional;

import com.itwookie.inireader.INIConfig;

public class ClientStorage {

	static INIConfig users = null; //abusing the data-structure
	static INIConfig pUsers = null; // persistent client storage (move away from inis?)

	public static Optional<String> getCV(String client, String key) {
		check(); 
		if (pUsers.hasKey(client, key))
			return Optional.of(pUsers.get(key));
		else if (users.hasKey(client, key))
			return Optional.of(users.get(key));
		else
			return Optional.empty();
	}

	public static void setCV(String client, String key, String value) {
		check(); 
		users.set(client, key, value);
	}

	public static void setCV(String client, String key, Integer value) {
		check(); 
		users.set(client, key, String.valueOf(value));
	}

	public static void setCV(String client, String key, Long value) {
		check(); 
		users.set(client, key, String.valueOf(value));
	}
	
	public static void setCV(String client, String key, String value, boolean persist) {
		check(); 
		if (persist) pUsers.set(client, key, value); else users.set(client, key, value);
	}

	public static void setCV(String client, String key, Integer value, boolean persist) {
		check(); 
		if (persist) pUsers.set(client, key, String.valueOf(value)); else users.set(client, key, String.valueOf(value));
	}

	public static void setCV(String client, String key, Long value, boolean persist) {
		check(); 
		if (persist) pUsers.set(client, key, String.valueOf(value)); else users.set(client, key, String.valueOf(value));
	}
	
	public static void resetNonPersistantKeyForAll(String key) {
		check();
		for (String user : users.groups()) {
			users.remove(user, "Vote");
		}
	}
	
	private static void check() {
		if (users == null) {
			users = new INIConfig();
			pUsers = new INIConfig();
			File f = new File("userdata.ini");
			if (f.exists()) pUsers.loadFrom(f);
		}
	}
	
	public static void save() {
		check(); 
		pUsers.saveFile(new File("userdata.ini"));
		Console.println(Console.FG.PURPLE, "[User]", Console.RESET, " Persisten data were saved");
	}
}

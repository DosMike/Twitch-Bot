package com.itwookie.inireader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class INIConfig {
	private Map<String,Map<String,String>> config;
	private String lastGrp = null;
	
	public INIConfig() {
		config = new HashMap<String,Map<String,String>>();
	}
	
	/** Adds a new group */
	public void addGroup(String grp) {
		lastGrp = getKeyCI(config.keySet(), grp);
		if (config.containsKey(lastGrp)) return;
		config.put(lastGrp, new HashMap<String, String>());
	}
	/** Counts all Groups */
	public int countGroups() {
		return config.size();
	}
	/** Checks if Group exists */
	public boolean hasGroup(String grp) {
		lastGrp = getKeyCI(config.keySet(), grp);
		return (lastGrp != null);
	}
	/** Counts Keys in this Group */
	public int countKeys(String grp) {
		lastGrp = getKeyCI(config.keySet(), grp);
		return countKeys();
	}
	/** Counts Key in the last used Group */
	public int countKeys() {
		if (!config.containsKey(lastGrp)) return -1;
		return config.get(lastGrp).size();
	}
	/** Checks if Group has Key */
	public boolean hasKey(String grp, String key) {
		lastGrp = getKeyCI(config.keySet(), grp);
		return hasKey(key);
	}
	/** Checks if last used Group contains Key */
	public boolean hasKey(String key) {
		if (!config.containsKey(lastGrp)) return false;
		return config.get(lastGrp).containsKey(getKeyCI(config.get(lastGrp).keySet(), key));
	}
	
	/** Set a Key, Value pair in this Group */
	public void set(String grp, String key, String value) {
		lastGrp = getKeyCI(config.keySet(), grp);
		set(key, value);
	}
	/** Set a Key, Value pair in the last used Group */
	public void set(String key, String value) {
		if (lastGrp == null) return;
		Map<String, String> g;
		if (!config.containsKey(lastGrp)) {
			g = new HashMap<String, String>();
		} else {
			g = config.get(lastGrp);
		}
		g.put(getKeyCI(g.keySet(), key), value);
		config.put(lastGrp, g);
	}
	
	/** Get a Key Value from this Group */
	public String get(String grp, String key) {
		lastGrp = getKeyCI(config.keySet(), grp);
		return get(key);
	}
	/** Get a Key Value from the last used Group */
	public String get(String key) {
		if (lastGrp == null) return null;
		if (!config.containsKey(lastGrp)) return null;
		Map<String, String> g = config.get(lastGrp);
		String k = getKeyCI(g.keySet(), key);
		if (!g.containsKey(k)) return null;
		return g.get(k);
	}
	
	/** Remove a Key from this Group */ 
	public void remove(String grp, String key) {
		lastGrp = getKeyCI(config.keySet(), grp);
		remove(key);
	}
	/** Remvoe a Key from the last used Group */
	public void remove(String key) {
		if (lastGrp == null) return;
		if (!config.containsKey(lastGrp)) return;
		Map<String, String> g = config.get(lastGrp);
		String k = getKeyCI(g.keySet(), key);
		if (!g.containsKey(k)) return;
		g.remove(k);
		config.put(lastGrp, g);
	}
	
	/** Deletes a group */
	public void delete(String grp) {
		lastGrp = getKeyCI(config.keySet(), grp);
		if (!config.containsKey(lastGrp)) return;
		config.remove(lastGrp);
		lastGrp = null;
	}

	/** Get all group names */
	public Set<String> groups() {
		return config.keySet();
	}
	
	/** Get all keys in a group */
	public Set<String> keys(String grp) {
		lastGrp = getKeyCI(config.keySet(), grp);
		return keys();
	}
	/** Get all keys in the last used Group */
	public Set<String> keys() {
		if (lastGrp == null) return null;
		if (!config.containsKey(lastGrp)) return null;
		return config.get(lastGrp).keySet();
	}

	/** Resets this Config, dropping every group */
	public void drop() {
		config = new HashMap<String,Map<String,String>>();
	}
	public void loadFrom(File f) {
		drop();
		importFrom (f);
	}
	public void importFrom(File f) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line;
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(";") || line.isEmpty()) {}
				else if (line.startsWith("[") && line.endsWith("]")) 
					addGroup(line.substring(1, line.length()-1).trim());
				else if (line.contains("="))
					set(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=')+1).trim());
				else
					System.out.println("WARNING: ini contained uncommented raw line");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {}
		}
	}
	public void saveFile(File f) {
		BufferedWriter bw = null;
		String damnLN = "\r\n"; //win-doof line end
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			/*Map<String, String> g;
			for (String s : config.keySet()) {
				bw.write('[' + s + "]"+damnLN);
				g = config.get(s);
				for (Entry<String, String> kv : g.entrySet()) {
					bw.write(kv.getKey() + '=' + kv.getValue() + damnLN);
				}
				bw.write('\n');
			} */
			for (Entry<String, Map<String,String>> gk : config.entrySet()) {
				bw.write("[" + gk.getKey() + "]" + damnLN);
				for (Entry<String, String> kv : gk.getValue().entrySet()) {
					bw.write(kv.getKey() + '=' + kv.getValue() + damnLN);
				}
				bw.write(damnLN);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
			} catch (Exception e) {}
		}
	}
	
	/** so it doesn't matter what case you use, but the case wont change.
	 * e.g. loadFile has the group [Main], you can access it with "maIN", and it'll save as [Main] again 
	 */
	private String getKeyCI(Collection<String> keys, String ciKey) {
		if (ciKey==null) return null;
		for (String key : keys)
			if (key.equalsIgnoreCase(ciKey))
				return key;
		return ciKey;
	}
}

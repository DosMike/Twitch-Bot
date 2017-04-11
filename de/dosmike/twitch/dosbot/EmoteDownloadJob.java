package de.dosmike.twitch.dosbot;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmoteDownloadJob extends Thread {

	private boolean running = false;
	public boolean isRunning() { return running; }
	
	@Override
	public void run() {
		if (running) return;
		running = true;
		
		//create and open zip file
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream("emotes.zip"));
		} catch (Exception e) {
			e.printStackTrace();
			running=false;return;
		}
		
		try {
			//receive emote list
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(new URL("https://twitchemotes.com/api_cache/v2/global.json"));
			
			//url template:
			String emoteURL = root.get("template").get("small").asText();
			String img_uri;
			
			//for all emotes in the list
			Iterator<Entry<String, JsonNode>> i = root.get("emotes").fields();
			Entry<String, JsonNode> o;
			double a=root.get("emotes").size(), b=0;
			while (i.hasNext()) {
				o = i.next();
				img_uri = emoteURL.replace("{image_id}", o.getValue().get("image_id").asText());
				
				//create next zip entry
				zos.putNextEntry(new ZipEntry("global\\"+o.getKey()+".png")); //they are usually in png format
				
				//download emote
				URLConnection con = new URL(img_uri).openConnection();
				InputStream is = con.getInputStream();
				int read=0, max=con.getContentLength(), len;
				byte[] buffer = new byte[1024];
				while ((len=is.read(buffer))>0) {
					read+=len;
					zos.write(buffer, 0, len);
					Console.print(Console.LINE_RESET, String.format("[%03d%%] ", (int)(b*100/a)), "Download ", Console.FG.PURPLE, "global", Console.RESET," emote ", Console.FG.PURPLE, o.getKey(), Console.RESET, " " + String.format("%d/%d kB", read, max));
				}
				is.close();
				b++;
			}
			zos.closeEntry();
			Console.println(Console.LINE_RESET, "All "+(int)a, Console.FG.PURPLE, " global", Console.RESET, " emotes downloaded to emotes.zip");
			
			zos.close();
		} catch (Exception e) {
			e.printStackTrace();
			running=false;return;
		}
		
		running=false;return;
	}
}
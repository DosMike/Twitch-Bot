package de.dosmike.twitch.dosbot;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class SongMeta {
	String author=null, name=null, mediaURL, requestedBy;
	long length=-1;
	float rating=-10;
	long views=-1;
	String error = null;
	
	protected SongMeta() {}
	
	public String getAuthor() { return author; }
	public String getTitle() { return name; }
	public String getMediaURL() { return mediaURL; }
	public String getRequester() { return requestedBy; }
	public String getErrorMessage() { return error; }
	/** will return TRUE if there was a problem retreiving data */
	public boolean hasError() { return error != null; }
	public long getLength() { return length; }
	public String getLengthString() { 
		int h=0, m=0; long s=length;
		while (s > 3600) { h+=1; s-=3600; }
		while (s > 60) { m+=1; s-=60; }
		if (h>0) {
			return String.format("%d:%02d:%02d hours", h,m,s);
		} else if (m>0) {
			return String.format("%d:%02d minutes", m,s);
		} else {
			return String.format("%d seconds", s);
		}
	}
	public long getViews() { return views; }
	public float getRating() { return rating; }
	public boolean ratingAbove(float maxIs5) { return rating>=maxIs5; }
	
	// MAGIC BELOW :D
	
	//The captured groups are: 1 protocol, 2 subdomain, 3 domain, 4 path, 5 video code, 6 query string
	static Pattern YouTubeVideo = Pattern.compile("((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?");
	
	public static SongMeta fromURL(String url, String requestedBy) {
		SongMeta result = new SongMeta();
		result.requestedBy = requestedBy;
		
		Matcher m;
		if ((m=YouTubeVideo.matcher(url)).matches()) {
			String vid = m.group(5);
			result.mediaURL = "https://www.youtube.com/watch?v="+vid;
			//get more media info
			String _reason = "Incomplete meta information"; boolean _success=true;
			try {
				HttpsURLConnection icon = (HttpsURLConnection)(new URL("https://youtube.com/get_video_info?video_id="+vid+"&hl=EN").openConnection());
				icon.setConnectTimeout(5000);
				icon.setReadTimeout(5000);
				icon.setRequestMethod("GET");
				icon.setRequestProperty("Accept-Encoding", "identity");
				icon.setDoInput(true);
				if (icon.getResponseCode() != 200) throw new RuntimeException();
				/*/// requres content length to be returned
				int len = icon.getContentLength(), off=0, read;
				byte[] buffer = new byte[len];
				InputStream is = icon.getInputStream();
				while ((read=is.read(buffer, off, len))>=0) {
					off+=read;
					len-=read;
				}
				//*///
				StringBuilder sb = new StringBuilder(8192);
				int read; byte[] buffer = new byte[512];
				InputStream is = icon.getInputStream();
				while ((read=is.read(buffer))>=0) {
					sb.append(new String(buffer,0,read));
				}
				String[] data = sb.toString().split("&");
				for (int i = 0; i < data.length; i++)
					data[i] = URLDecoder.decode(data[i]);
				sb.setLength(0); sb=null;
				String[] kv = new String[2]; 
				for (String s : data) {
					int i = s.indexOf('=');
					if (i>0) {
						kv[0] = s.substring(0, i);
						kv[1] = i==s.length()?"":s.substring(i+1).trim();
						//Console.println(kv[0], " => ", kv[1]);
						if ("length_seconds".equals(kv[0])) {
							result.length=Long.parseLong(kv[1]);
						} else if ("author".equals(kv[0])) {
							result.author=kv[1];
						} else if ("view_count".equals(kv[0])) {
							result.views=Long.parseLong(kv[1]);
						} else if ("title".equals(kv[0])) {
							result.name=kv[1];
						} else if ("avg_rating".equals(kv[0])) {
							result.rating = Float.parseFloat(kv[1]);
						} //error stuff:
						else if ("reason".equals(kv[0])) {
							_reason = URLDecoder.decode(kv[1]);
							if (_reason.indexOf(10) >=0) {
								_reason = _reason.substring(0, _reason.indexOf(10));
							}
						} else if ("status".equals(kv[0]) && "fail".equals(kv[1])) {
							_success = false;
						}	
					} //else Console.println("E: ", s);
				}
				//check if all data were set:
				if (result.author == null ||
					result.name == null ||
					result.views == -1 ||
					result.length == -1 ||
					result.rating == -10 ||
					!_success)
					throw new RuntimeException(_reason);
			} catch (Exception e) {
				result.error = "Unable to read video info";
				e.printStackTrace();
			}
			//test thingy
			//Console.println("Added \""+result.name+"\" from Channel " + result.author + " (" + result.length + " sec, "+result.views+" views, "+String.format("%.2f",  result.rating)+" stars), requested by " + result.requestedBy + " to the playlist.");
		}
		return result;
	}
}

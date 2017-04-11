package de.dosmike.twitch.dosbot.chat;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;

import de.dosmike.twitch.dosbot.ChatRank;
import de.dosmike.twitch.dosbot.ClientStorage;
import de.dosmike.twitch.dosbot.Console;
import de.dosmike.twitch.dosbot.Executable;

public class cmdPlay extends Command {
	
	private static final String Sound_Cooldown = "Sound_Cooldown";
	static Long soundCooldown = 0l;
	static boolean soundOpen = false;
	
	public cmdPlay() {
		super("Play", "Play a sound on the stream");
	}
	
	@Override
	public void run(String user, ChatRank rank, String[] args, boolean silent) {
		if (args.length!=1) {
			if (!silent) getTwitch().sendChat(user, "Please specify what sound you want to play or type \"" + getTwitch().CommandPrefix + "play list\" to list all sounds.");
			return;
		}
		if (args[0].equalsIgnoreCase("list")) {
			if (silent) return;
			String l = "";
			for (String s : Executable.cfg.keys("Sounds"))
				l += ", " + s;
			if (l.isEmpty())
				getTwitch().sendChat("There are no sounds listed yet.");
			else
				getTwitch().sendChat("These are all sounds (* = subs only, # = mods only):" + l.substring(1));
			return;
		}
		String snd = rank.compareTo(ChatRank.MOD)<=0 && Executable.cfg.hasKey("Sounds", "#"+args[0]) ? 
					Executable.cfg.get("#"+args[0]) :
					( rank.compareTo(ChatRank.SUB)<=0 && Executable.cfg.hasKey("Sounds", "*"+args[0]) ?
						Executable.cfg.get("*"+args[0]) :
						Executable.cfg.get(args[0]) 
					);
		if (snd != null) {
		
			final long clientCooldown = ( Executable.cfg.hasKey("BOT", "SoundClientCooldown")
					? Long.parseLong(Executable.cfg.get("BOT", "SoundClientCooldown"))*1000
					: 0l );
			
			long lastPlayed = Long.parseLong(ClientStorage.getCV(user, Sound_Cooldown).orElse("-1"));
			if (lastPlayed>=0) lastPlayed = System.currentTimeMillis()-lastPlayed;
			else lastPlayed = System.currentTimeMillis();
			
			if (soundOpen || lastPlayed <= clientCooldown) {
				if (!silent) getTwitch().sendChat(user, "Please don't spam sounds.");
				return;
			}
			
			if (soundCooldown > System.currentTimeMillis()) {
				getTwitch().sendChat(user, "Please don't spam sounds. (Please wait " + (soundCooldown-System.currentTimeMillis())/1000 + " more seconds)");
				return;
			}
		
			ClientStorage.setCV(user, Sound_Cooldown, System.currentTimeMillis());
		
			String[] sounds = snd.split(";");
			playSound(sounds[rng.nextInt(sounds.length)].trim());
		} else {
			if (!silent) getTwitch().sendChat(user, "Sound not found!");
		}
	}
	
	static void playSound(String fileName) {
		soundOpen=true;
		File audioFile;
		if (fileName.charAt(1) != ':') {
			audioFile = new File(Executable.cfg.get("BOT", "SoundBasePath"), fileName);
		} else {
			audioFile = new File(fileName);
		}
		final long addCooldown = ( Executable.cfg.hasKey("BOT", "SoundGlobalCooldown")
			? Long.parseLong(Executable.cfg.get("BOT", "SoundGlobalCooldown"))*1000
			: 0l );
		try {
			final AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat format = audioStream.getFormat();
			long length = (long) (audioStream.getFrameLength()*1000.0/format.getFrameRate());
			final long soundStop = System.currentTimeMillis()+length;
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			final Clip audioClip = (Clip) AudioSystem.getLine(info);
			audioClip.open(audioStream);
			
			FloatControl vol = (FloatControl)audioClip.getControl(Type.MASTER_GAIN);
//			double gain = (vol.getMaximum()-vol.getMinimum()) //range
//					*(Integer.parseInt(Executable.cfg.get("BOT", "SoundVolume"))/100.0) //volume
//					+vol.getMinimum();
//			vol.setValue((float)gain);
			vol.setValue(20f*(float)Math.log10(Integer.parseInt(Executable.cfg.get("BOT", "SoundVolume"))/100.0));
			
			audioClip.start();
			new Thread() {
				@Override
				public void run() {
					while (soundStop>System.currentTimeMillis()) Thread.yield();
					audioClip.close();
					try {
						audioStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					soundOpen=false;
					soundCooldown = System.currentTimeMillis()+addCooldown;
					Console.println(Console.FG.YELLOW, "Closed sound-resources", Console.RESET);
				}
			}.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

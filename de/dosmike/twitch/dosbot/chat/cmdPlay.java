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
import de.dosmike.twitch.dosbot.Console;
import de.dosmike.twitch.dosbot.Executable;

public class cmdPlay extends Command {
	
	static boolean soundOpen = false;
	
	public cmdPlay() {
		super("Play", "Play a sound on the stream");
	}
	
	@Override
	public boolean run(String user, ChatRank rank, String[] args, boolean silent) {
		if (args.length < 1) {
			if (!silent) getTwitch().sendChat(user, "Please specify what sound you want to play or type \"" + getTwitch().CommandPrefix + "play list\" to list all sounds.");
			return false;
		}
		if (args[0].equalsIgnoreCase("list")) {
			if (silent) return false;
			String l = "";
			for (String s : Executable.cfg.keys("Sounds"))
				l += ", " + s;
			if (l.isEmpty())
				getTwitch().sendChat("There are no sounds listed yet.");
			else
				getTwitch().sendChat("These are all sounds (* = subs only, # = mods only):" + l.substring(1));
			return true;
		}
		String snd = rank.compareTo(ChatRank.MOD)<=0 && Executable.cfg.hasKey("Sounds", "#"+args[0]) ? 
					Executable.cfg.get("#"+args[0]) :
					( rank.compareTo(ChatRank.SUB)<=0 && Executable.cfg.hasKey("Sounds", "*"+args[0]) ?
						Executable.cfg.get("*"+args[0]) :
						Executable.cfg.get(args[0]) 
					);
		if (snd != null) {
			String[] sounds = snd.split(";");
			playSound(sounds[rng.nextInt(sounds.length)].trim());
		} else {
			if (!silent) getTwitch().sendChat(user, "Sound not found!");
			return false;
		}
		return true;
	}
	
	static void playSound(String fileName) {
		soundOpen=true;
		File audioFile;
		if (fileName.charAt(1) != ':') {
			audioFile = new File(Executable.cfg.get("Sounds", "SoundBasePath"), fileName);
		} else {
			audioFile = new File(fileName);
		}
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
//					*(Integer.parseInt(Executable.cfg.get("Sounds", "SoundVolume"))/100.0) //volume
//					+vol.getMinimum();
//			vol.setValue((float)gain);
			vol.setValue(20f*(float)Math.log10(Integer.parseInt(Executable.cfg.get("Sounds", "SoundVolume"))/100.0));
			
			audioClip.start();
			new Thread() {
				@Override
				public void run() {
					//let it play till it's done
					while (soundStop>System.currentTimeMillis()) Thread.yield();
					
					//close the sound resource
					audioClip.close();
					try {
						audioStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					soundOpen=false;
					Console.println(Console.FG.YELLOW, "Closed sound-resources", Console.RESET);
				}
			}.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

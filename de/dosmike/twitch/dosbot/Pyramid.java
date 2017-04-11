package de.dosmike.twitch.dosbot;

public class Pyramid {
	String emote;
	int height;
	int progress;
	int length;
	String owner;
	
	public Pyramid(String owner, String emote) {
		this.owner = owner;
		this.emote = emote;
		height=1;
		progress=1;
		length=0;
	}
	
	public boolean isDone() {
		return height > 2 && progress == length; 
	}
	
	/** return if this is still a valid pyramid
	 * @return false if this pyramid should be removed */
	public boolean extend(String emote, int count) {
		if (!emote.equals(this.emote)) return false; //different emote
		if (progress == height) { // Pyramid is building up
			if (count == height+1) { // Still growing
				++height; 
				++progress;
			} else if (count == height-1) { // Pyramid is now turning, top was reached 
				++progress;
				length=height*2-1; // total length of the pyramid 
			} else return false; //invalid move
		} else // Progress has to be greater Height 
			if (count == length-progress) {
				++progress;
		} else return false;
		return true;
	}
	
	public int getValue() {
		return ((int)Math.pow(2.0,height-1.0)-1) * Executable.handler.getViewcount();
	}
	public int breakValue() {
		return (int)(length==0?getValue()*0.5:getValue()*(progress+1)/length);
	}
	
	public int getHeight() {
		return height;
	}
	public String getEmote() {
		return emote;
	}
	public boolean isOwner(String user) {
		return owner.equals(user);
	}
}
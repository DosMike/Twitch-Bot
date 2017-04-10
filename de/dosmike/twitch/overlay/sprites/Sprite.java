package de.dosmike.twitch.overlay.sprites;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Random;

public abstract class Sprite {
	Image img;
	
	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static Random rng = new Random(System.currentTimeMillis());
	
	public Sprite (Image image) {
		img = image;
	}
	
	//dt = time since last render in ms
	public abstract void think(int dt);
	
	public abstract void render(Graphics g);
	
	public boolean isDead() {
		return true;
	}
}

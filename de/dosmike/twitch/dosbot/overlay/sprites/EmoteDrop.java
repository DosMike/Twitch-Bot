package de.dosmike.twitch.dosbot.overlay.sprites;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import de.dosmike.twitch.dosbot.Executable;

public class EmoteDrop extends Sprite {
	static Float anfang = null;
	static float fadeSpan;
	static float speedMin, speedRange;
	
	float fadeSpeed;
	float rotateSpeed;
	float fallSpeed;
	
	double x=0, y=0;
	float rotation=0;
	float alpha=1;
	
	Dimension imgSize, bufferedSize;
	
	public EmoteDrop (Image image) {
		super(image);
		
		if (anfang == null) {
			String[] tmp = Executable.cfg.get("Overlay", "EmotesFade").split(",");
			anfang = Float.parseFloat(tmp[0]);
			fadeSpan = Float.parseFloat(tmp[1])-anfang;
			tmp = Executable.cfg.get("Overlay", "EmotesSpeed").split(",");
			speedMin = Float.parseFloat(tmp[0])*screenSize.height;
			speedRange = Float.parseFloat(tmp[1])*screenSize.height-speedMin;
		}
		
		imgSize = new Dimension(img.getWidth(null), img.getHeight(null));
		int tmp = (int) Math.sqrt(imgSize.width*imgSize.width+imgSize.height*imgSize.height);
		bufferedSize = new Dimension(tmp,tmp);
		x = rng.nextInt(screenSize.width - imgSize.width);
		y = (int) -imgSize.getHeight();
		
		fallSpeed = rng.nextFloat()*speedRange+speedMin; //64..128 px/sec
		rotateSpeed = rng.nextFloat()*8-4; //-4..4 deg/sec
		fadeSpeed = (float) ((1.0-rng.nextFloat()*0.1)/(screenSize.height/fallSpeed));
	}
	
	public void think(int dt) {
		y += (fallSpeed*dt/1000.0);
		rotation += (rotateSpeed*dt/1000.0);
		if (rotation<0) rotation+=360;
		else if (rotation>=360) rotation-=360;
		if (y/screenSize.height >= anfang && fadeSpan != 0) {
			alpha -= fadeSpeed/fadeSpan*dt/1000.0;
			if (alpha < 0.0) alpha = 0;
		}
	}
	
	public void render(Graphics g) {
		BufferedImage b = new BufferedImage(bufferedSize.width, bufferedSize.height, BufferedImage.TYPE_4BYTE_ABGR);
		AffineTransform tx = AffineTransform.getRotateInstance(rotation, bufferedSize.width/2, bufferedSize.height/2);
		tx.concatenate( AffineTransform.getTranslateInstance(bufferedSize.width/4, bufferedSize.height/4) );
		//AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha<1?alpha:1);
		Graphics2D g2d = b.createGraphics();
		g2d.setComposite(ac);
		g2d.drawImage(img, tx, null);
		b.flush();
		g.drawImage(b, (int)(x-bufferedSize.width/4), (int)(y-bufferedSize.height/4), null);
	}
	
	@Override
	public boolean isDead() {
		return alpha <= 0 || y > screenSize.height;
	}
}

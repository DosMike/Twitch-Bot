package de.dosmike.twitch.overlay.sprites;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class PhyCupEmote extends Sprite {
	
	static List<PhyCupEmote> forCollision = new LinkedList<>();
	
	long spawnTime;
	float rotateSpeed;
	
	double x=0, y=0, dx=0, dy=0;
	float rotation=0;
	float gravity=0.1f;
	float bouncyness=0.8f;
	
	Dimension imgSize, bufferedSize;
	
	public PhyCupEmote (Image image) {
		super(image);
		
		imgSize = new Dimension(img.getWidth(null), img.getHeight(null));
		int tmp = 2*imgSize.width; //(int) Math.sqrt(imgSize.width*imgSize.width+imgSize.height*imgSize.height);
		bufferedSize = new Dimension(tmp,tmp);
		x = 0;
		//y = screenSize.getHeight()-4*imgSize.getHeight();
		y = screenSize.getHeight()/4;
		dy = -screenSize.getHeight()*(0.005+0.001*rng.nextFloat());
		dx = screenSize.getWidth()*(0.015+0.001*rng.nextFloat());
		
		forCollision.add(this);
		spawnTime = System.currentTimeMillis();
	}
	
	public void think(int dt) {
		dy += gravity*dt/10.0;
		if (dy < 0.0001f) dx -= 0.0001f; //friction
		double yn = y + dy*dt/10.0;
		double xn = x + dx*dt/50.0;
		rotateSpeed = (float)((dx-dy)*dt/10.0);
		rotation += (rotateSpeed*dt/1000.0);
		while (rotation<0) rotation+=360;
		while (rotation>=360) rotation-=360;
		
		boolean col=false; double cx=0,cy=0,mx=0,my=0;//cx=-bouncyness*dx, cy=-bouncyness*dy;
		PhyCupEmote e;
		for (int i=0; i < forCollision.size(); i++) {
			e = forCollision.get(i);
			double ww = (imgSize.width + e.imgSize.width)/2;
				ww *= ww;
			if (e.x == x && e.y == y) continue;//don't collide with self
			double xx = e.x-xn;
			double yy = e.y-yn;
			double dist = xx*xx+yy*yy;
			if (dist < ww) {
				//nur wenn die objekte aufeinander zufliegen
				//prüfen, ob wir immer noch feststecken, wenn wir uns um xx/yy von e entfernen
				double tex = (x-dx*bouncyness) - (e.x-e.dx*bouncyness);
				double tey = (y-dy*bouncyness) - (e.y-e.dy*bouncyness);
				double tedi = tex*tex+tey*tey;
				if (tedi < ww-3.0) { //im nächsten frame immer noch stuck
					// ^^ kleines padding für aufeinander liegen
					//beste maßnahme: nix tun
				} else {
					col = true;
					//TODO try to slide along object:
					//double vel = Math.sqrt((xn-x)*(xn-x)+(yn-y)*(yn-y));
					double vel = Math.sqrt(dx*dx+dy*dy);
					double ang = Math.atan2(e.x-x, e.y-y);
					/*if (ang > 180 && ang < 270) {
						ang += 90;
						cx += Math.cos(ang)*1.0;
						cy += Math.sin(ang)*vel;
					} else if (ang > 270 && ang < 360) {
						ang -= 90;
						cx += Math.cos(ang)*1.0;
						cy += Math.sin(ang)*vel;
					}*/
					//if (yn < e.y) {
						//cx -= xx*(dy+0.01)*0.01;
					//double overlap = Math.sqrt(dist);
					vel = vel*bouncyness;
					if (vel < 1) vel = 1;
						cx -= Math.sin(ang)*vel*bouncyness;
						cy -= Math.cos(ang)*vel*bouncyness;
						if (Math.abs(cx)>Math.abs(mx)) mx = cx;
						if (Math.abs(cy)>Math.abs(my)) my = cy;
					/*} else {
						cx -= bouncyness*dx;
						cy -= bouncyness*dy;
					}*/
				}
			}
		}
		if (col) {
			dx = mx; //-bouncyness*dx;
			dy = my; //-bouncyness*dy;
		} else {
			x = xn;
			y = yn;
		}
		
		double ww;
		ww = screenSize.height - imgSize.height; 
		if (y > ww) {//bounce of floor = never die
			if (dy > 0) dy *= -bouncyness;
			y = ww;
		} else if (y < -screenSize.height) {
			dy *= gravity;
			y = -screenSize.height;
		}
		//bounce of other
		ww = screenSize.width - imgSize.width;
		if (x > ww) {
			dx *= -bouncyness;
			x = ww;
		} else if (x < 0) {
			dx *= -bouncyness;
			x = 0;
		}
		
		//bis 10 sec nach Stillstand "leben"
		if (Math.abs(dx)>1 || Math.abs(dy)>1) spawnTime=System.currentTimeMillis();
	}
	
	public void render(Graphics g) {
		BufferedImage b = new BufferedImage(bufferedSize.width, bufferedSize.height, BufferedImage.TYPE_4BYTE_ABGR);
		AffineTransform tx = AffineTransform.getRotateInstance(rotation, bufferedSize.width/2, bufferedSize.height/2);
		tx.concatenate( AffineTransform.getTranslateInstance(bufferedSize.width/4, bufferedSize.height/4) );
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		//AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
		Graphics2D g2d = b.createGraphics();
//		g2d.setComposite(ac);
		g2d.drawImage(img, op.getTransform(), null);
		//g2d.drawImage(img, null, null);
		b.flush();
		g.drawImage(b, (int)(x-bufferedSize.width/4), (int)(y-bufferedSize.height/4), null);
	}
	
	@Override
	public boolean isDead() {
		boolean res = (y > screenSize.height || System.currentTimeMillis()-spawnTime > 30000);
		if (res) forCollision.remove(this);
		return res;
	}
}

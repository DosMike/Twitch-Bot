package de.dosmike.twitch.dosbot.overlay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.itwookie.telnet.Stoppable;

import de.dosmike.twitch.dosbot.Executable;
import de.dosmike.twitch.dosbot.overlay.sprites.Sprite;

public class Window extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	long lastRender=System.currentTimeMillis();
	boolean renderbusy=false;
	Stoppable thinker = new Stoppable() {
		long delay = 5;
		
		@Override
		public void onStart() {
			if (Executable.cfg.hasKey("BOT", "OverlayFrameLimit")) {
				try {
					delay = Integer.parseInt(Executable.cfg.get("OverlayFrameLimit"));
					delay = 1000/delay;
				} catch (Exception e) {}
			}
		}
		
		@Override
		public void loop() {
			while (renderbusy || System.currentTimeMillis()-lastRender < delay) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {}
				Thread.yield();
			}
			contentPane.repaint();
			Window.this.revalidate();
			lastRender=System.currentTimeMillis();
			Thread.yield();
		}
	};

	public List<Sprite> sprites = new LinkedList<>();
	public void addSprite (Sprite s) {
		sprites.add(s);
	}
	
	boolean moving=false;
	int mx=0, my=0;
	Color background = Color.decode(Executable.cfg.get("BOT", "OverlayBackground"));
	/**
	 * Create the frame.
	 */
	public Window() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);
		setTitle("Overlay Render Window");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(screenSize.width-32, 100, screenSize.width, screenSize.height);
		contentPane = new JPanel() {
			
			private static final long serialVersionUID = 8614177785912238053L;

			@Override
			public void paintComponent(Graphics g) {
				//if (renderbusy || lastRender==System.currentTimeMillis()) return;
				renderbusy=true;
				
				super.paintComponent(g);
				g.setColor(background);
				g.fillRect(0, 0, contentPane.getWidth(), contentPane.getHeight());
				
				int deltaTime = (int) (System.currentTimeMillis()-lastRender);
				setTitle("DosBot Overlay Renderer");// " + (int)(1000.0/deltaTime) + "FPS, dt=" + deltaTime + ", last=" + System.currentTimeMillis()%1000);
				
				List<Sprite> dead = new LinkedList<>();
				for (int i = 0; i < sprites.size(); i++) {
					Sprite s = sprites.get(i);
					
					s.think(deltaTime);
					s.render(g);
					if (s.isDead()) dead.add(s);
					
					Thread.yield();
				}
				sprites.removeAll(dead); //keep mem low
				renderbusy=false;
			}
		};
		contentPane.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { 
				moving=false;
			}
			@Override public void mouseReleased(MouseEvent arg0) {
				moving=false;
			}
			@Override public void mousePressed(MouseEvent arg0) {
				mx=arg0.getX();
				my=arg0.getY();
				moving=true;
			}
		});
		contentPane.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseDragged(MouseEvent arg0) { if (moving) mymove(arg0); }
			@Override public void mouseMoved(MouseEvent arg0) { if (moving) mymove(arg0); } 
			
			public void mymove(MouseEvent arg0) {
				int dx = arg0.getX()-mx;
				int dy = arg0.getY()-my;
				Window t = Window.this;
				Point l = t.getLocation();
				t.setLocation((int)l.getX()+dx, (int)l.getY()+dy);
			}
		});
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				thinker.halt();
			}
		});
		
		setVisible(true);
		thinker.start();
	}
	
	public void close() {
		thinker.halt();
		dispose();
	}
}

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class APS {
	
	static JPanel j;
	static ArrayList<OrbitalBody> oBs = new ArrayList<OrbitalBody>();
	static ArrayList<Sat> sats = new ArrayList<Sat>();
	static ArrayList<Base> bases = new ArrayList<Base>();
	final static double timeCon = 0.5;
	static double realSpeed = 0;
	static double prevMill = 0;
	static Time prevTime = new Time(2000, 1, 1, 12, 0, 0);
	static boolean paused = true;
	static double posScale = 0.000069444444d;
	static double radScale = 13.288;
	static long count = 0;
	static int xV = 950;
	static int yV = 550;

	static double orbitInc = 60;
	static double orbitHeight = 1.2E6;
	static int planes = 4;
	static int satPerPlane = 6;
	static double planeDiv = 360.0 / satPerPlane;
	
	static boolean zaxis = false;
	static int speed = 32000;
	static int rotations = 0;
	static int visible = 10, visible2 = 10, visible3 = 10, visible4 = 10, visible5 = 10;
	static int lowestVis1 = 10, lowestVis2 = 10, lowestVis3 = 10, lowestVis4 = 10, lowestVis5 = 10;
	static int minVisLat = -90;
	static Time curTime = new Time(2000, 1, 1, 12, 0, 1);
	static LinkedList<xyz>[] trails;
	static boolean leaveTrails = false;
	static Runnable r = new Runnable() {

		public void run() {
			render();
			t.start();
		}
	};
	static ActionListener render = new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent a) {
			realSpeed = curTime.diff(prevTime)*1000 / (System.currentTimeMillis() - prevMill);
			j.repaint();
			t.start();
			prevTime = new Time(curTime);
			prevMill = System.currentTimeMillis();
		}
		
		
	};
	static Timer t = new Timer(40,render);
	static KeyListener k = new KeyListener() {

		public void keyPressed(KeyEvent e) {

			if(e.getKeyCode() == KeyEvent.VK_W)
				yV+=10;
			else if(e.getKeyCode() == KeyEvent.VK_S)
				yV-=10;
			else if(e.getKeyCode() == KeyEvent.VK_A)
				xV-=10;
			else if(e.getKeyCode() == KeyEvent.VK_D)
				xV+=10;
			else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				paused = !paused;
			}else if(e.getKeyCode() == KeyEvent.VK_C) {
				clearTrails();
			}
			else if(e.getKeyCode() == KeyEvent.VK_R) {
				yV = (int) (((yV - 500) * 0.5) + 500);
				xV = (int) (((xV - 500) * 0.5) + 500);
				radScale -= 1;
				posScale *= 0.5;
			}else if(e.getKeyCode() == KeyEvent.VK_F) {
				yV = (int) (((yV - 500) * 2) + 500);
				xV = (int) (((xV - 500) * 2) + 500);
				radScale += 1;
				posScale *= 2;
			}else if(e.getKeyCode() == KeyEvent.VK_V) {
				clearTrails();
				leaveTrails = !leaveTrails;
			}else if(e.getKeyCode() == KeyEvent.VK_9) {
				speed/=2;
				if(speed == 1)
					speed = 0;
			}else if(e.getKeyCode() == KeyEvent.VK_0) {
				speed*=2;
				if(speed == 0) {
					speed = 1;
				}
			}
			j.repaint();
		}

		public void keyReleased(KeyEvent e) {
			
		}

		public void keyTyped(KeyEvent e) {
			
		}
			
	};
	
	public static void main(String[] args) {
		
		OrbitalBody Mars = new OrbitalBody("Mars", 4.282837E13,3389500,0,0,0,0,0,0,24.7,new Color(183, 65, 14));
		oBs.add(Mars);
		oBs.add(new OrbitalBody("Phobos", 716000,26,9378000,0,0,0,2138,40,5,new Color(127, 127, 127)));
		oBs.add(new OrbitalBody("Deimos", 98000,15.6,0,23459000,0,-1351.3,0,-42,5,new Color(127, 127, 127)));
		
		bases.add(new Base(90, 0, Mars));
		bases.add(new Base(45, 50, Mars));
		bases.add(new Base(0, -30, Mars));
		bases.add(new Base((-90), 180, Mars));
		bases.add(new Base((- 90), 0, Mars));
		
		for(int k = 0;k < planes;k++) {
			for(int i = 0; i < satPerPlane;i++) {
				sats.add(new Sat("Sat" + i, 1, 10, oBs.get(0),
						0.00, oBs.get(0).getRad() + orbitHeight,
						orbitInc, k*(360/planes) + 10,
						0, planeDiv*i + (planeDiv/planes) * k,
						new Color(255, 0, 255)));
			}
		}
		
		
		trails = new LinkedList[oBs.size() + sats.size()];
		
		for(int i = 0;i < oBs.size() + sats.size();i++) {
			trails[i] = new LinkedList<xyz>();
		}
		
		
		
		r.run();
		while(true) {
			if(!paused) {
				runner();
				
			}
			if(speed != 0) {
				for(int i = 0;i < speed;i++) {
					System.nanoTime();
				}
			}
			if( curTime.sec < timeCon) {
				if(curTime.hour == 0 && curTime.min == 0) {
					rotations++;
					bases.get(3).move((rotations%180 - 90),180);
					bases.get(4).move((rotations%180 - 90),0);
				}
				calcLOS();
			}
		}
		
	}
	
	
	
	//just checks if a is within r of b
	private static boolean inBound(double a, double b, double r) {
		return (Math.abs(a-b) <= r);
	}

	//calculates the gravity from every body to every other one and applies it
	private static void runner() {
		for(int i = 0; i < oBs.size(); i++) {
			for(int k = 0;k < oBs.size();k++) {
				if(i != k) {
					oBs.get(i).applyAcc(grav(oBs.get(i), oBs.get(k)), timeCon);
				}
			}
			oBs.get(i).tickVel(timeCon);
		}
		for(int i = 0; i < sats.size(); i++) {
			for(int k = 0;k < oBs.size();k++) {
				sats.get(i).applyAcc(grav(sats.get(i), oBs.get(k)), timeCon);
			}
			sats.get(i).tickVel(timeCon);
		}
		curTime.tick(timeCon);
	}
	
	//calculates the gravitational acceleration vector's x and y components from body 1 to body 2
	private static double[] grav(OrbitalBody oB, OrbitalBody oB2) {
		double[] dn = dN(oB, oB2);
		double dtot = Math.sqrt(dn[0]*dn[0] + dn[1]*dn[1] + dn[2]*dn[2]);
		double acc = (oB2.getGMass())/(dtot*dtot*dtot);
		double[] tr= {acc*dn[0],acc*dn[1], acc*dn[2]};
		return tr;
	}
	
	private static double[] dN(OrbitalBody oB, OrbitalBody oB2) {
		double[] a = oB.getPos();
		double[] b = oB2.getPos();
		double[] tr = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
		return tr;
		
	}
	
	private static void clearTrails() {
		for(int i = 0;i < trails.length;i++) {
			trails[i].clear();
		}
	}
	
	private static void calcLOS() {
		ArrayList<drawable> objects = new ArrayList<drawable>();
		visible  = bases.get(0).addLOS(objects, sats, oBs);
		visible2 = bases.get(1).addLOS(objects, sats, oBs);
		visible3 = bases.get(2).addLOS(objects, sats, oBs);
		visible4 = bases.get(3).addLOS(objects, sats, oBs);
		visible5 = bases.get(4).addLOS(objects, sats, oBs);
		
		
		
		if(visible <= lowestVis1) {
			lowestVis1 = visible;
		}
		if(visible2 <= lowestVis2) {
			lowestVis2 = visible2;
		}
		if(visible3 <= lowestVis3) {
			lowestVis3 = visible3;
		}
		if(visible4 <= lowestVis4) {
			lowestVis4 = visible4;
		}
		if(visible5 <= lowestVis5) {
			lowestVis5 = visible5;
			minVisLat = (rotations%180 - 90);
		}
	}
	
	private static void render(){
		JFrame frame = new JFrame("Orbital approximator");
		
		j = new JPanel(){
			public void paint(Graphics p) {	
				OrbitalBody oBcur;
				ArrayList<drawable> objects = new ArrayList<drawable>();
				int rad;
				int x;
				int y;
				p.setColor(Color.BLACK);
				p.fillRect(0, 0, 2000, 2000);
				p.setColor(Color.BLACK);
				p.fillRect(0, 0, 200, 100);
				p.setColor(Color.RED);
				for(int i = 0;i <= 20;i++) {
					p.drawLine(i*100, 0, i*100, 2000);
					p.drawLine(0, i*100, 2000, i*100);
				}
				p.setColor(Color.WHITE);

				for(int i = 0;i < oBs.size();i++) {
					addOb(objects, oBs.get(i), i);
				}
				for(int i = 0;i < sats.size();i++) {
					addOb(objects, sats.get(i), i + oBs.size());
					sats.get(i).addLOS(objects, sats, oBs);
				}
								
				for(int i = 0;i < bases.size();i++) {
					bases.get(i).addLOS(objects, sats, oBs);
				}
				
				Collections.sort(objects, drawable.ySort);
				for(int i = 0;i < objects.size();i++) {
					p.setColor(objects.get(i).c);
					if(objects.get(i).line) {
						p.drawLine((int)(objects.get(i).pos.x), (int)(objects.get(i).pos.z), (int)(objects.get(i).posb.x), (int)(objects.get(i).posb.z));
					}else {
						p.fillOval((int) (objects.get(i).pos.x), (int) (objects.get(i).pos.z), objects.get(i).rad, objects.get(i).rad);
					}
				}
			
				p.setColor(Color.WHITE);
				String[] strings = {
						curTime.toString(),
						"Speed: " + Math.round(realSpeed*100)/100.0,
						"Speed Factor: " + speed,/*
						"Visible Sats N: " + visible,
						"Min Vis Sats N: " + lowestVis1,
						"Visible Sats NE: " + visible2,
						"Min Vis Sats NE: " + lowestVis2,
						"Visible Sats E: " + visible3,
						"Min Vis Sats E: " + lowestVis3,
						"Visible Sats SE: " + visible4,
						"Min Vis Sats SE: " + lowestVis4,*/
						"Visible Sats M: " + visible5,
						"Min Vis Sats M: " + lowestVis5,
						"M lat: " + (rotations%180 - 90),
						"Min vis lat: " + minVisLat 
				};
				addStrings(strings, p);
				rad = 0;

			}
		};
			
		frame.addKeyListener(k);
		frame.add(j);
		frame.setLocation(800,0);
		frame.setSize(1000, 1000);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static void addOb(ArrayList<drawable> objects, OrbitalBody oBcur, int i) {
		drawable td = oBcur.getDrawable();
		objects.add(td);
		if(leaveTrails) {
			trails[i].addFirst(new xyz(td.pos.x + td.rad/2,oBcur.getPos()[1],td.pos.y + td.rad/2));
		}

		if(count > (1000/satPerPlane)) {
			trails[i].removeLast();
		}
		ListIterator<xyz> li = trails[i].listIterator(0);
		
		while(li.hasNext()) {
			xyz tra = li.next();
			if(li.hasNext()){
				xyz next = li.next();
				li.previous();
				objects.add(new drawable(tra, next, oBcur.getCol()));
			}
		}
	}



	private static void addStrings(String[] s, Graphics p) {
		for(int i = 0;i < s.length;i++) {
			p.drawString(s[i], 10, 30 + i * 20);
		}
	}


	static int scale(double d, double r, int axis) {
		if(axis == 0) {//0.0001428d
			return (int) (d*posScale + xV - r/2);	
		}
		return (int) (d*posScale + yV - r/2);
	}
	
	static int scaleRad(double r) {
		return (int) (r*2*posScale);
	}
	
	private static boolean losBlocked(xyz p1, xyz p2, OrbitalBody s) {
		double r = s.getRad();
		xyz sc = s.getXYZ();
	    double a,b,c;
	    double bb4ac;
	    xyz dp = new xyz(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);

	    a = dp.x * dp.x + dp.y * dp.y + dp.z * dp.z;
	    b = 2 * (dp.x * (p1.x - sc.x) + dp.y * (p1.y - sc.y) + dp.z * (p1.z - sc.z));
	    c = sc.x * sc.x + sc.y * sc.y + sc.z * sc.z;
	    c += p1.x * p1.x + p1.y * p1.y + p1.z * p1.z;
	    c -= 2 * (sc.x * p1.x + sc.y * p1.y + sc.z * p1.z);
	    c -= r * r;
	    bb4ac = b * b - 4 * a * c;
	    if(bb4ac > 0) {
		    double i1 = (-b + Math.sqrt(bb4ac))/(2 * a);
		    double i2 = (-b - Math.sqrt(bb4ac))/(2 * a);
		    
		    return (i1 < 1 && i1 > 0) || (i2 < 1 && i2 > 0);
	    }
	    
	    return false;
	}
	
}

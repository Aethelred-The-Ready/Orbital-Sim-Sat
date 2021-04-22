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
	final static double timeCon = 0.5;
	static double realSpeed = 0;
	static double prevMill = 0;
	static Time prevTime = new Time(2000, 1, 1, 12, 0, 0);
	static boolean paused = false;
	static double scale = 0.0000001;
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
	static boolean leaveTrails = true;
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
		
		oBs.add(new OrbitalBody("Mars", 4.282837E13,3389500,0,0,0,0,0,0,24.7,new Color(183, 65, 14), 0.0196045));
		oBs.add(new OrbitalBody("Phobos", 716000,26,9378000,0,0,0,2138,40,5,new Color(127, 127, 127), 0.3));
		oBs.add(new OrbitalBody("Deimos", 98000,15.6,0,23459000,0,-1351.3,0,-42,5,new Color(127, 127, 127), 0.5));
		
		for(int k = 0;k < planes;k++) {
			for(int i = 0; i < satPerPlane;i++) {
				oBs.add(new OrbitalBody("Sat" + i, 1, 10, oBs.get(0),
						0, oBs.get(0).getRad() + orbitHeight,
						orbitInc, k*(360/planes) + 10,
						0, planeDiv*i + (planeDiv/planes) * k,
						new Color(255, 0, 255)));
			}
		}
		
		
		trails = new LinkedList[oBs.size()];
		
		for(int i = 0;i < oBs.size();i++) {
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
					System.out.println(oBs.get(0).getPos()[1]);
					rotations++;
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
				if(i != k && !oBs.get(k).isSat()) {
					oBs.get(i).applyAcc(grav(oBs.get(i), oBs.get(k)), timeCon);
				}
			}
			oBs.get(i).tickVel(timeCon);
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
		visible  = addLOS(new Base(90,0),objects);
		visible2 = addLOS(new Base(45,50),objects);
		visible3 = addLOS(new Base(0,-30),objects);
		visible4 = addLOS(new Base(-69,0),objects);
		visible5 = addLOS(new Base((rotations%180 - 90),230),objects);
		
		if(visible < lowestVis1) {
			lowestVis1 = visible;
		}
		if(visible2 < lowestVis2) {
			lowestVis2 = visible2;
		}
		if(visible3 < lowestVis3) {
			lowestVis3 = visible3;
		}
		if(visible4 < lowestVis4) {
			lowestVis4 = visible4;
		}
		if(visible5 < lowestVis5) {
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
					oBcur = oBs.get(i);
					p.setColor(oBcur.getCol());
					if(oBcur.isSat()) {
						rad = (int) oBcur.getRad();
						x = (int) (scale(oBcur.getPos()[0], rad, 0));
						y = (int) (scale(oBcur.getPos()[2], rad, 2));
					}else {
						rad = scaleRad(oBcur.getRad());
						x = scaleOb(oBcur.getPos()[0], rad, 0);
						y = scaleOb(oBcur.getPos()[2], rad, 2);
					}
					if(leaveTrails) {
						trails[i].addFirst(new xyz(x + rad/2,oBcur.getPos()[1],y + rad/2));
					}

					if(count > (1000/satPerPlane)) {
						trails[i].removeLast();
					}
					ListIterator<xyz> li = trails[i].listIterator(0);
					
					objects.add(new drawable(
							x,
							(int) (oBcur.getPos()[1]),
							y,
							rad,
							oBcur.getCol()
							));
					
					while(li.hasNext()) {
						xyz tra = li.next();
						if(li.hasNext()){
							xyz next = li.next();
							li.previous();
							objects.add(new drawable(tra, next, oBcur.getCol()));
						}
					}
				}
				
				//visible  = addLOS(new Base(90,0),objects);
				//visible2 = addLOS(new Base(45,50),objects);
				//visible3 = addLOS(new Base(0,-30),objects);
				//visible4 = addLOS(new Base(-30,100),objects);
				visible5 = addLOS(new Base((rotations%180 - 90),230),objects);
				
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
	
	private static void addStrings(String[] s, Graphics p) {
		for(int i = 0;i < s.length;i++) {
			p.drawString(s[i], 10, 30 + i * 20);
		}
	}

	private static int addLOS(Base b, ArrayList<drawable> objects) {
		OrbitalBody oBcur = oBs.get(0);
		double h = Math.sqrt(b.pos.x * b.pos.x + b.pos.y * b.pos.y);
		double baseRot = b.the;
		xyz shiftedxyz = new xyz(h * Math.cos(oBcur.getCurRot() + baseRot), -h * Math.sin(oBcur.getCurRot() + baseRot), b.pos.z);
		int rad = scaleRad(oBcur.getRad());
		xyz base = new xyz(shiftedxyz.x * 3391010,shiftedxyz.y * 3391010,shiftedxyz.z * 3391010);
		xyz drawbase = new xyz(
				(scale(oBcur.getPos()[0], rad, 0) + rad/2 + shiftedxyz.x * rad/2),
				shiftedxyz.y + oBcur.getPos()[1],
				(scale(oBcur.getPos()[2], rad, 2) + rad/2 + shiftedxyz.z * rad/2));
		int visible = 0;
		double myPos = oBcur.getPos()[1];
		//System.out.println(base.y - oBcur.getPos()[1]);
		for(int i = 0;i < oBs.size();i++) {
			if(!oBs.get(i).isSat() || losBlocked(base, oBs.get(i))) {
				//objects.add(new drawable(drawbase, oBs.get(i).getXYZ(), Color.BLACK));
			} else{
				if(drawbase.y > myPos) {
					objects.add(new drawable(drawbase, oBs.get(i).getXYZ(), Color.WHITE));
				}
				visible++;
			}
		}
		if(drawbase.y > myPos) {
			objects.add(new drawable(
					(int)drawbase.x - 5,
					(int)drawbase.y + 10,
					(int)drawbase.z - 5,
					10,
					Color.GREEN));
		}
		
		return visible;
	}



	static int scale(double d, double r, int axis) {
		if(axis == 0) {
			return (int) (d*0.0001428d/1.44 + xV - r/2);	
		}
		return (int) (d*0.0001428d/1.44 + yV - r/2);
	}
	
	static int scaleOb(double d, double r, int axis) {
		if(axis == 0) {
			return (int) (d*0.0001428d/1.44 + xV - r/2);	
		}
		return (int) (d*0.0001428d/1.44 + yV - r/2);
	}
	
	static int scaleRad(double r) {
		return (int) (r/6779);
	}
	
	private static boolean losBlocked(xyz p1, OrbitalBody sat) {
		xyz p2 = new xyz(sat.getPos()[0], sat.getPos()[1], sat.getPos()[2]);
		for(int i = 0;i < oBs.size();i++) {
			if(!oBs.get(i).isSat()) {
				if(losBlocked(p1, p2, oBs.get(i))) {
					return true;
				}
			}
		}
		return false;
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

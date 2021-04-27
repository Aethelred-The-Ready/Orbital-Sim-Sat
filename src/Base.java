import java.awt.Color;
import java.util.ArrayList;

public class Base {
	OrbitalBody parent;
	xyz pos;
	double lat;
	double lon;
	double phi;
	double the;
	double rho; // Distance of between the transmitter and the core in units of planetary radius
	double transmitterHeight = 20;
	
	public Base(double lat, double lon, OrbitalBody on) {
		this.lat = lat;
		this.lon = lon;
		parent = on;
		move(lat, lon);
	}
	
	public xyz getCurXYZ() {
		xyz par = parent.getXYZ();
		double h = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
		xyz tr = new xyz(h * Math.cos(parent.getCurRot() + the), -h * Math.sin(parent.getCurRot() + the), pos.z);		
		return new xyz(tr.x * parent.getRad() + par.x, tr.y * parent.getRad() + par.y, tr.z * parent.getRad() + par.z);
	}
	
	public xyz getDrawXYZ() {
		double h = Math.sqrt(pos.x * pos.x + pos.y * pos.y);
		double baseRot = the;
		xyz shiftedxyz = new xyz(h * Math.cos(parent.getCurRot() + baseRot), -h * Math.sin(parent.getCurRot() + baseRot), pos.z);
		int rad = APS.scaleRad(parent.getRad());
		xyz drawbase = new xyz(
				(APS.scale(parent.getPos()[0], rad, 0) + rad/2 + shiftedxyz.x * rad/2),
				shiftedxyz.y * parent.getRad() + parent.getPos()[1] - 1,
				(APS.scale(parent.getPos()[2], rad, 2) + rad/2 + shiftedxyz.z * rad/2));
		return drawbase;
	}
	
	public void move(double lat, double lon) {
		rho = (parent.getRad() + transmitterHeight) / parent.getRad();
		phi = Math.PI/180 * lat;
		the = Math.PI/180 * lon;
		double x = rho * Math.cos(phi) * Math.cos(the);
		double y = rho * Math.cos(phi) * Math.sin(the);
		double z = -rho * Math.sin(phi);
		pos = new xyz(x,y,z);
	}
	
	public String toString() {
		return "{ " + lat + "," + lon + " }";
	}
	
	public int addLOS(ArrayList<drawable> objects, ArrayList<Sat> sats, ArrayList<OrbitalBody> oBs) {
		int visible = 0;
		xyz drawbase = getDrawXYZ();
		xyz base = getCurXYZ();
		for(int i = 0;i < sats.size();i++) {
			if(sats.get(i).hasLOS(base, oBs)) {
				objects.add(new drawable(drawbase, sats.get(i).getLOSXYZ(), Color.WHITE));
				visible++;
			}
		}
		objects.add(new drawable(
				(int)drawbase.x - 5,
				(int)drawbase.y + 10,
				(int)drawbase.z - 5,
				10,
				Color.GREEN));
		
		return visible;
	}

}

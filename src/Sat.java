import java.awt.Color;
import java.util.ArrayList;
import java.util.ListIterator;

public class Sat extends OrbitalBody {

	private int visibleSats = 0;
	
	public Sat(String n, double m, double r, double x, double y, double z, double vx, double vy, double vz, double sp,
			Color c, double J2) {
		super(n, m, r, x, y, z, vx, vy, vz, sp, c);
		isSat = true;
	}

	public Sat(String n, double m, double r, OrbitalBody parent, double ecc, double a, double inc, double lon,
			double arg, double ano, Color c) {
		super(n, m, r, parent, ecc, a, inc, lon, arg, ano, c);
		isSat = true;
	}
	
	public drawable getDrawable() {
		return new drawable(
				APS.scale(pos[0], this.radius, 0),
				pos[1],
				APS.scale(pos[2], this.radius, 2),
				(int) this.radius,
				col
				);
	}
	
	//Do I have a los to this position
	public boolean hasLOS(xyz p1, final ArrayList<OrbitalBody> oBs) {
		xyz p2 = new xyz(pos[0], pos[1], pos[2]);
		for(int i = 0;i < oBs.size();i++) {
			if(!oBs.get(i).isSat()) {
				if(losBlocked(p1, p2, oBs.get(i))) {
					return false;
				}
			}
		}
		return true;
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
	
	public void tick() {
		
	}
	
	private xyz getLOSXYZ() {
		xyz tr = getXYZ();
		tr.y -= 5;
		tr.x += 5;
		tr.z -= 5;
		return tr;
	}

	public void addLOS(ArrayList<drawable> objects, ArrayList<Sat> sats, ArrayList<OrbitalBody> oBs) {
		int visible = 0;
		xyz drawbase = new xyz(APS.scale(pos[0], this.radius, 0), pos[1] - 5, APS.scale(pos[2], this.radius, 2));
		xyz base = getXYZ();
		for(int i = 0;i < sats.size();i++) {
			if(sats.get(i).pos[0] != pos[0] && sats.get(i).hasLOS(base, oBs)) {
				objects.add(new drawable(drawbase, sats.get(i).getLOSXYZ(), Color.WHITE));
				visible++;
			}
		}
		visibleSats = visible;
	}
	
}

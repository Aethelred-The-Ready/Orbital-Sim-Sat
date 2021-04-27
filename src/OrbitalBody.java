import java.awt.Color;
import java.util.Comparator;
import java.util.UUID;


public class OrbitalBody {

	protected static double G = 6.67408E-11;
	protected double GM;//Mass in kilograms * Gravitational constant
	protected double radius;//radius of body in meters
	protected double[] pos = new double[3];//position, in meters, x,y,z
	protected double[] vel = new double[3];//velocity in m/s
	protected double spin;//spin in hours per rotation
	protected double curRot = 0;//Current rotation
	protected Color col;
	protected String name;
	protected boolean isSat = false;
	public UUID id = UUID.randomUUID();
	
	public OrbitalBody(String n, double m, double r, double x, double y, double z, double vx, double vy, double vz, double sp, Color c) {
		GM = m;
		radius = r;
		pos[0] = x;
		pos[1] = y;
		pos[2] = z;
		vel[0] = vx;
		vel[1] = vy;
		vel[2] = vz;
		spin = sp * 3600;
		col = c;
		name = n;
	}
	
	/**
	 * Creates an orbitalBody by the 6 orbital elements. Only for circular orbits, so ecc is unescessary.
	 * @param n name
	 * @param m mass
	 * @param r radius
	 * @param parent the object this orbits around
	 * @param ecc eccentricity
	 * @param a semi-major axis in m 
	 * @param inc inclination in degrees
	 * @param lon longitude of ascending node in degrees
	 * @param arg argument of periapsis in degrees
	 * @param ano true anomaly in seconds
	 * @param c
	 */
	public OrbitalBody(String n, double m, double r, OrbitalBody parent, double ecc, double a, double inc, double lon, double arg, double ano, Color c) {
		name = n;
		GM = m;
		radius = r;
		col = c;
		
		double Mt = ano;//*Math.sqrt(parent.GM/a);
		
		double Et = getEccentricFromMean((Mt * Math.PI)/180, ecc);
		
		double Vt = 2 * Math.atan2(
				Math.sqrt(1 + ecc) * Math.sin(Et/2),
				Math.sqrt(1 - ecc) * Math.cos(Et/2));
		
		double[] o = new double[3];
		
		double rt = a * (1 - ecc * Math.cos(Et));
		
		o[0] = rt * Math.cos(Vt);
		o[1] = rt * Math.sin(Vt);
		o[2] = 0;
		
		double[] oV = new double[3];
		
		oV[0] = -(Math.sqrt(parent.GM * a)/rt) * Math.sin(Math.toRadians(Mt));
		oV[1] = (Math.sqrt(parent.GM * a)/rt) * Math.cos(Math.toRadians(Mt));
		oV[2] = 0;
		
		pos[0] = parent.pos[0] + o[0]*(c(arg)*c(lon) - s(arg)*c(inc)*s(lon)) - o[1]*(s(arg)*c(lon) - c(arg)*c(inc)*s(lon));
		pos[1] = parent.pos[1] + o[0]*(c(arg)*s(lon) - s(arg)*c(inc)*c(lon)) - o[1]*(c(arg)*c(inc)*c(lon) - s(arg)*s(lon));
		pos[2] = parent.pos[2] + o[0]*(s(arg)*s(inc)) - o[1]*(c(arg)*s(inc));
		

		vel[0] = parent.vel[0] + oV[0]*(c(arg)*c(lon) - s(arg)*c(inc)*s(lon)) - oV[1]*(s(arg)*c(lon) - c(arg)*c(inc)*s(lon));
		vel[1] = parent.vel[1] + oV[0]*(c(arg)*s(lon) - s(arg)*c(inc)*c(lon)) - oV[1]*(c(arg)*c(inc)*c(lon) - s(arg)*s(lon));
		vel[2] = parent.vel[2] + oV[0]*(s(arg)*s(inc)) - oV[1]*(c(arg)*s(inc));
		
	}
	
	private double c(double v) {
		return Math.cos(Math.toRadians(v));
	}
	
	private double s(double v) {
		return Math.sin(Math.toRadians(v));
	}
	
	public void changeTo(OrbitalBody a) {
		GM = a.GM;
		radius = a.radius;
		pos[0] = a.pos[0];
		pos[1] = a.pos[1];
		pos[2] = a.pos[2];
		vel[0] = a.vel[0];
		vel[1] = a.vel[1];
		vel[2] = a.vel[2];
		col = a.col;
		name = a.name;
	}
	
	public double getGMass() {
		return GM;
	}
	
	public double[] getPos() {
		return pos;
	}
	
	//acc: acceleration in m/s/s, x,y
	//time: time in seconds
	public void applyAcc(double[] acc, double time) {
		vel[0] += acc[0] * time;
		vel[1] += acc[1] * time;
		vel[2] += acc[2] * time;
	}
	
	public void tickVel(double time) {
		pos[0] += vel[0] * time;
		pos[1] += vel[1] * time;
		pos[2] += vel[2] * time;
		curRot -= (Math.PI * 2 * time)/spin;
		if(curRot > Math.PI * 2) {
			curRot -= Math.PI * 2;
		}
	}
	
	public double getCurRot() {
		return curRot;
	}

	public double getRad() {
		return radius;
	}

	public double[] getVel() {
		return vel;
	}
	
	public Color getCol() {
		return col;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + " " + GM + " " + radius + " " + pos[0] + " " + pos[1] + " " + pos[2] + " " + vel[0] + " " + vel[1] + " " + vel[2] + " " + col.getRed() + " " + col.getGreen() + " " + col.getBlue();
	}
	
	public boolean isSat() {
		return isSat;
	}
	
	
	
	public static Comparator<OrbitalBody> ySort = new Comparator<OrbitalBody>() {
		public int compare(OrbitalBody a, OrbitalBody b) {
			return (int) (a.pos[1]-b.pos[1]);
		}
	};

	public xyz getXYZ() {
		return new xyz((APS.scale(pos[0], radius, 0)) + radius / 2, pos[1], (APS.scale(pos[2], radius, 2) + radius / 2));
	}
	
	private static double getEccentricFromMean(double mean, double e) {
		double ecc = mean;
		double eccp = 0;
		while(abs(eccp - ecc) > 0.000000001) {
			ecc = eccp;
			eccp = ecc - (ecc - e * Math.sin(ecc) - mean)/(1 - e * Math.cos(ecc));
		}
		return ecc;
	}
	
	private static double abs(double a) {
		if(a > 0) {
			return a;
		}
		return -a;
	}
	
	public drawable getDrawable() {
		int rad = APS.scaleRad(this.radius);
		return new drawable(
				APS.scale(pos[0], rad, 0),
				pos[1],
				APS.scale(pos[2], rad, 2),
				rad,
				col
				);
	}
	
	// Used to do stuff
	public void tick() {
		
	}
	
	public boolean equals(OrbitalBody b) {
		return this.id == b.id;
	}
}
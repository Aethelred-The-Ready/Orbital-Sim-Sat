
public class Base {
	xyz pos;
	double lat;
	double lon;
	double phi;
	double the;
	double rho;
	
	public Base(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		rho = 1.00001;
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
}

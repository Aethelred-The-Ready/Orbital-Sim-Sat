
public class Vector {
	
	public double x = 0;
	public double y = 0;
	public double z = 0;
	
	public Vector(double a, double b, double c) {
		x = a;
		y = b;
		z = c;
	}
	
	public Vector(double x, double y, double z, boolean normalize) {
		this.x = x;
		this.y = y;
		this.z = z;
		double temp = norm();
		this.x = x/temp;
		this.y = y/temp;
		this.z = z/temp;
	}
	
	public static Vector cross(Vector a, Vector b) {
		Vector tr = new Vector(0,0,0);
		
		tr.x = a.y * b.z - a.z * b.y;
		tr.y = a.z * b.x - a.x * b.z;
		tr.z = a.x * b.y - a.y * b.x;
		
		return tr;
	}
	
	public static double dot(Vector a, Vector b) {
		double tr = 0;
		
		tr += a.x * b.x;
		tr += a.y * b.y;
		tr += a.z * b.z;
		
		return tr;
	}
	
	public static Vector add(Vector a, Vector b) {
		Vector tr = new Vector(0,0,0);
		
		tr.x = a.x + b.x;
		tr.y = a.y + b.y;
		tr.z = a.z + b.z;
		
		return tr;
	}
	
	public static Vector sub(Vector a, Vector b) {
		Vector tr = new Vector(0,0,0);
		
		tr.x = a.x - b.x;
		tr.y = a.y - b.y;
		tr.z = a.z - b.z;
		
		return tr;
	}
	
	public double norm() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public static Vector sdot(double a, Vector b) {
		Vector tr = new Vector(0,0,0);
		
		tr.x = a * b.x;
		tr.y = a * b.y;
		tr.z = a * b.z;
		
		return tr;
	}
	
	public String toString() {
		return "x: " + x + ", y: " + y + ", z: " + z;
	}

	public Object subtract(Vector c) {
		return null;
	}
	
	
}

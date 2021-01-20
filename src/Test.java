
public class Test {

	public static void main(String[] args) {

		System.out.println(losBlocked(new xyz(0, 2, 0), new xyz(0, 3, 0)));
	}

	
	private static boolean losBlocked(xyz pos, xyz sat) {
		Vector3D u = new Vector3D(sat.x - pos.x, sat.y - pos.y, sat.y - pos.z, true);
		Vector3D o = new Vector3D(sat.x, sat.y, sat.y);
		Vector3D c = new Vector3D(0,0,0);
		double d = 4*(Math.pow(Vector3D.dot(u, Vector3D.sub(o, c)), 2) - (Math.pow(Vector3D.sub(o, c).norm(), 2) - Math.pow(2, 2)));
		double t =  ((-2*Vector3D.dot(u, Vector3D.sub(o, c))) - Math.sqrt(d))/(2 * Vector3D.dot(u,u));
		System.out.println(t);
		return t >= 0;
	}
}

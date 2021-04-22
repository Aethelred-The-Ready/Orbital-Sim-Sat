
public class Test {

	public static void main(String[] args) {

		System.out.println(losBlocked(new xyz(0, 2, 0), new xyz(0, 3, 0)));
	}

	
	private static boolean losBlocked(xyz pos, xyz sat) {
		Vector u = new Vector(sat.x - pos.x, sat.y - pos.y, sat.y - pos.z, true);
		Vector o = new Vector(sat.x, sat.y, sat.y);
		Vector c = new Vector(0,0,0);
		double d = 4*(Math.pow(Vector.dot(u, Vector.sub(o, c)), 2) - (Math.pow(Vector.sub(o, c).norm(), 2) - Math.pow(2, 2)));
		double t =  ((-2*Vector.dot(u, Vector.sub(o, c))) - Math.sqrt(d))/(2 * Vector.dot(u,u));
		System.out.println(t);
		return t >= 0;
	}
}

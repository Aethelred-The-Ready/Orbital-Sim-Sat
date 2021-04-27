import java.awt.Color;
import java.util.Comparator;

public class drawable {
	xyz pos = new xyz(0, 0, 0);
	xyz posb = new xyz(0, 0, 0);
	int rad;
	Color c;
	Boolean line;
	
	public drawable(double x, double y, double z, int rad, Color c) {
		this.pos = new xyz(x, y, z);
		this.rad = rad;
		this.c = c;
		this.line = false;
	}
	
	public drawable(xyz a, xyz b, Color c) {
		this.pos = a;
		this.posb = b;
		this.c = c;
		this.line = true;
	}
	
	private double getMinY() {
		if(line && pos.y > posb.y) {
			return posb.y;
		}
		return pos.y;
	}
	
	public static Comparator<drawable> ySort = new Comparator<drawable>() {
		public int compare(drawable a, drawable b) {
			return (int) (a.getMinY()-b.getMinY());
		}
	};
}

import java.awt.Color;
import java.util.Comparator;

public class drawable {
	xyz pos = new xyz(0, 0, 0);
	xyz posb = new xyz(0, 0, 0);
	int rad;
	Color c;
	Boolean line;
	
	public drawable(int x, int y, int z, int rad, Color c) {
		this.pos.x = x;
		this.pos.y = y;
		this.pos.z = z;
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
	
	public static Comparator<drawable> ySort = new Comparator<drawable>() {
		public int compare(drawable a, drawable b) {
			return (int) (a.pos.y-b.pos.y);
		}
	};
}

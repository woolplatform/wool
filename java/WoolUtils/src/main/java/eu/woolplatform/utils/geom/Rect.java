package eu.woolplatform.utils.geom;

public class Rect {
	private double left;
	private double top;
	private double right;
	private double bottom;

	public Rect(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public double getLeft() {
		return left;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public double getWidth() {
		return right - left;
	}

	public double getHeight() {
		return bottom - top;
	}

	public Polygon toPolygon() {
		return new Polygon(new Point(left, top), new Point(right, top),
				new Point(right, bottom), new Point(left, bottom));
	}
}

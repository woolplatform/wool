package eu.woolplatform.utils.geom;

/**
 * This class defines a point.
 *
 * @author Dennis Hofs (RRD)
 */
public class Point {
	private double x;
	private double y;

	/**
	 * Constructs a new point.
	 *
	 * @param x the X position
	 * @param y the Y position
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructs a copy of another point.
	 * 
	 * @param other the other point
	 */
	public Point(Point other) {
		this.x = other.x;
		this.y = other.y;
	}

	/**
	 * Returns the X position.
	 *
	 * @return the X position
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the Y position.
	 *
	 * @return the Y position
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Sets the X and Y position.
	 * 
	 * @param x the X position
	 * @param y the Y position
	 */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Moves this point by the specified offset.
	 *
	 * @param dx the offset along the X axis
	 * @param dy the offset along the Y axis
	 */
	public void offset(double dx, double dy) {
		x += dx;
		y += dy;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Point other = (Point)obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = (x == 0 ? 0 : (int)Double.doubleToLongBits(x));
		result = 31 * result + (y == 0 ? 0 : (int)Double.doubleToLongBits(y));
		return result;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}

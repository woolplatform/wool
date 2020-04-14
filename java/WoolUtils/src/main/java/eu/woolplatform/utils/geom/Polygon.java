package eu.woolplatform.utils.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines a polygon.
 *
 * @author Dennis Hofs (RRD)
 */
public class Polygon {
	private List<Point> points = new ArrayList<>();
	
	/**
	 * Constructs an empty polygon.
	 */
	public Polygon() {
	}

	/**
	 * Constructs a new polygon.
	 *
	 * @param points the points
	 */
	public Polygon(List<Point> points) {
		this.points = points;
	}

	/**
	 * Constructs a new polygon.
	 *
	 * @param points the points
	 */
	public Polygon(Point... points) {
		this.points = Arrays.asList(points);
	}

	/**
	 * Returns the points for this polygon
	 *
	 * @return the points for this polygon
	 */
	public List<Point> getPoints() {
		return points;
	}
	
	/**
	 * Sets the points.
	 * 
	 * @param points the points
	 */
	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	/**
	 * Sets the points.
	 * 
	 * @param points the points
	 */
	public void setPoints(Point... points) {
		this.points = Arrays.asList(points);
	}
	
	/**
	 * Adds the specified points.
	 * 
	 * @param points the points
	 */
	public void addPoints(Point... points) {
		for (Point point : points) {
			this.points.add(point);
		}
	}

	/**
	 * Returns the bounding rectangle for this polygon.
	 *
	 * @return the bounding rectangle for this polygon
	 */
	public Rect getBounds() {
		if (points.isEmpty())
			return new Rect(0, 0, 0, 0);
		Point first = points.get(0);
		double minLeft = first.getX();
		double maxRight = minLeft;
		double minTop = first.getY();
		double maxBottom = minTop;
		for (int i = 1; i < points.size(); i++) {
			Point p = points.get(i);
			if (p.getX() < minLeft)
				minLeft = p.getX();
			if (p.getX() > maxRight)
				maxRight = p.getX();
			if (p.getY() < minTop)
				minTop = p.getY();
			if (p.getY() > maxBottom)
				maxBottom = p.getY();
		}
		return new Rect(minLeft, minTop, maxRight, maxBottom);
	}

	/**
	 * Moves this polygon by the specified offset.
	 *
	 * @param dx the offset along the X axis
	 * @param dy the offset along the Y axis
	 */
	public void offset(double dx, double dy) {
		for (Point point : points) {
			point.offset(dx, dy);
		}
	}

	/**
	 * Rotates this polygon by the specified number of degrees around the first
	 * point.
	 *
	 * @param angle the angle in degrees
	 */
	public void rotate(float angle) {
		if (points.size() <= 1)
			return;
		List<LineSegment> lines = new ArrayList<>();
		for (int i = 1; i < points.size(); i++) {
			lines.add(new LineSegment(new Point(points.get(i - 1)),
					new Point(points.get(i))));
		}
		points = new ArrayList<>();
		Point lastPoint = null;
		for (LineSegment line : lines) {
			if (lastPoint == null) {
				points.add(line.getStart());
			} else {
				double dx = lastPoint.getX() - line.getStart().getX();
				double dy = lastPoint.getY() - line.getStart().getY();
				line.offset(dx, dy);
			}
			line.rotate(angle);
			lastPoint = line.getEnd();
			points.add(lastPoint);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Point point : points) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(point);
		}
		return builder.toString();
	}
}

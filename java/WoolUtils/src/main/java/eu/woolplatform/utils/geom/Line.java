/*
 * Copyright 2019 Roessingh Research and Development.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.utils.geom;

/**
 * This class defines a line. It consists of an origin and a vector. The origin
 * should be considered as an anchor, not as a starting point. The line does not
 * have a start or end.
 * 
 * @author Dennis Hofs (RRD)
 */
public class Line {
	private Point origin;
	private Vector vector;

	/**
	 * Constructs a new line.
	 * 
	 * @param origin the origin
	 * @param vector the vector
	 */
	public Line(Point origin, Vector vector) {
		this.origin = origin;
		this.vector = vector;
	}

	/**
	 * Returns the origin.
	 * 
	 * @return the origin
	 */
	public Point getOrigin() {
		return origin;
	}

	/**
	 * Sets the origin.
	 * 
	 * @param origin the origin
	 */
	public void setOrigin(Point origin) {
		this.origin = origin;
	}

	/**
	 * Returns the vector.
	 * 
	 * @return the vector
	 */
	public Vector getVector() {
		return vector;
	}

	/**
	 * Sets the vector.
	 * 
	 * @param vector the vector
	 */
	public void setVector(Vector vector) {
		this.vector = vector;
	}

	/**
	 * Returns whether another line is parallel to this line. This is also true
	 * if the other line moves in the opposite direction. The other line may or
	 * may not overlap this line.
	 * 
	 * @param other the other line
	 * @return true if the other line is parallel to this line, false otherwise
	 */
	public boolean isParallel(Line other) {
		return vector.isParallel(other.vector);
	}

	/**
	 * Returns whether this line contains the specified point.
	 *
	 * @param point the point
	 * @return true if this line contains the specified point, false otherwise
	 */
	public boolean containsPoint(Point point) {
		if (point.equals(origin))
			return true;
		if (vector.getDx() == 0) {
			return point.getX() == origin.getX();
		} else if (vector.getDy() == 0) {
			return point.getY() == origin.getY();
		} else {
			double dx = point.getX() - origin.getX();
			double dy = point.getY() - origin.getY();
			double originToPoint = dx / dy;
			double vectorDir = vector.getDx() / vector.getDy();
			return originToPoint == vectorDir;
		}
	}

	/**
	 * Tries to find an intersection between this line and another line. If the
	 * lines overlap each other, this method returns the origin of this line.
	 * If they are parallel, this method returns null. Otherwise this method
	 * returns the intersection.
	 *
	 * @param other the other line
	 * @return the intersection or null
	 */
	public Point findIntersection(Line other) {
		if (other.equals(this))
			return origin;
		if (other.isParallel(this)) {
			if (other.containsPoint(origin))
				return origin;
			else
				return null;
		}

		Point o1 = origin;
		Point o2 = other.origin;
		Vector v1 = vector;
		Vector v2 = other.vector;

		if (v1.getDx() == 0) {
			// o1.x = o2.x + b * v2.dx
			// b = (o1.x - o2.x) / v2.dx
			// v2.dx != 0, because lines are not parallel
			double b = (o1.getX() - o2.getX()) / v2.getDx();
			return new Point(o1.getX(), o2.getY() + b * v2.getDy());
		} else if (v2.getDx() == 0) {
			// o1.x + b * v1.dx = o2.x
			// b = (o2.x - o1.x) / v1.dx
			// v1.dx != 0, because lines are not parallel
			double b = (o2.getX() - o1.getX()) / v1.getDx();
			return new Point(o2.getX(), o1.getY() + b * v1.getDy());
		} else if (v1.getDy() == 0) {
			// o1.y = o2.y + b * v2.dy
			// b = (o1.y - o2.y) / v2.dy
			// v2.dy != 0, because lines are not parallel
			double b = (o1.getY() - o2.getY()) / v2.getDy();
			return new Point(o2.getX() + b * v2.getDx(), o1.getY());
		} else if (v2.getDy() == 0) {
			// o1.y + b * v1.dy = o2.y
			// b = (o2.y - o1.y) / v1.dy
			double b = (o2.getY() - o1.getY()) / v1.getDy();
			return new Point(o1.getX() + b * v1.getDx(), o2.getY());
		}
		
		
		// o1.x + a * v1.dx = o2.x + b * v2.dx
		// o1.y + a * v1.dy = o2.y + b * v2.dy
		// a = (o2.x - o1.x + b * v2.dx) / v1.dx
		// o1.y + (o2.x - o1.x + b * v2.dx) * v1.dy / v1.dx = o2.y + b * v2.dy
		// o1.y + (o2.x - o1.x) * v1.dy / v1.dx + b * v2.dx * v1.dy / v1.dx =
		//     o2.y + b * v2.dy
		// o1.y - o2.y + (o2.x - o1.x) * v1.dy / v1.dx = b * (v2.dy - v2.dx *
		//     v1.dy / v1.dx)
		// c1 = o1.y - o2.y + (o2.x - o1.x) * v1.dy / v1.dx
		// c2 = v2.dy - v2.dx * v1.dy / v1.dx
		// b = c1 / c2
		double c1 = o1.getY() - o2.getY() + (o2.getX() - o1.getX()) *
				v1.getDy() / v1.getDx();
		double c2 = v2.getDy() - v2.getDx() * v1.getDy() / v1.getDx();
		// c2 is only 0 if lines are parallel
		double b = c1 / c2;
		return new Point(o2.getX() + b * v2.getDx(),
				o2.getY() + b * v2.getDy());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Line other = (Line)obj;
		if (!origin.equals(other.origin))
			return false;
		if (!vector.equals(other.vector))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = origin.hashCode();
		result = 31 * result + vector.hashCode();
		return result;
	}
}

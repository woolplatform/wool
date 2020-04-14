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
 * This class defines a vector. It consists of an (x,y) direction.
 * 
 * @author Dennis Hofs (RRD)
 */
public class Vector {
	private double dx;
	private double dy;

	/**
	 * Constructs a new vector.
	 * 
	 * @param dx the direction along the X axis
	 * @param dy the direction along the Y axis
	 */
	public Vector(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}

	/**
	 * Creates a new vector with the specified angle and length. The vector with
	 * direction (1, 0) has angle 0.
	 *
	 * @param angle the angle in degrees
	 * @param length the length
	 * @return the vector
	 */
	public static Vector fromAngle(double angle, double length) {
		angle = angle % 360;
		if (angle < 0)
			angle += 360;
		if (angle == 0) {
			return new Vector(length, 0);
		} else if (angle == 90) {
			return new Vector(0, length);
		} else if (angle == 180) {
			return new Vector(-length, 0);
		} else if (angle == 270) {
			return new Vector(0, -length);
		} else {
			double rad = angle * Math.PI / 180;
			return new Vector(length * Math.cos(rad), length * Math.sin(rad));
		}
	}

	/**
	 * Returns the direction along the X axis.
	 * 
	 * @return the direction along the X axis
	 */
	public double getDx() {
		return dx;
	}

	/**
	 * Sets the direction along the X axis.
	 * 
	 * @param dx the duration along the X axis
	 */
	public void setDx(double dx) {
		this.dx = dx;
	}

	/**
	 * Returns the direction along the Y axis.
	 * 
	 * @return the direction along the Y axis
	 */
	public double getDy() {
		return dy;
	}

	/**
	 * Sets the direction along the Y axis.
	 * 
	 * @param dy the direction along the Y axis
	 */
	public void setDy(double dy) {
		this.dy = dy;
	}

	/**
	 * Rotates this vector by the specified number of degrees.
	 *
	 * @param angle the number of degrees
	 */
	public void rotate(double angle) {
		angle = angle % 360;
		if (angle < 0)
			angle += 360;
		if (angle == 90) {
			double oldDx = dx;
			dx = -dy;
			dy = oldDx;
		} else if (angle == 180) {
			dx = -dx;
			dy = -dx;
		} else if (angle == 270) {
			double oldDx = dx;
			dx = dy;
			dy = -oldDx;
		} else if (angle > 0) {
			double newAngle = (getAngle() + angle) % 360;
			double length = getLength();
			double rad = newAngle * Math.PI / 180;
			dx = length * Math.cos(rad);
			dy = length * Math.sin(rad);
		}
	}

	/**
	 * Returns the angle in degrees for this vector. The vector with direction
	 * (1, 0) has angle 0. This method returns a value between 0 (inclusive) and
	 * 360 (exclusive).
	 *
	 * @return the angle in degrees
	 */
	public double getAngle() {
		if (dx == 0) {
			if (dy > 0)
				return 90;
			else
				return 270;
		} else if (dy == 0) {
			if (dx > 0)
				return 0;
			else
				return 180;
		}
		double tan = Math.atan(dy / dx) * 180 / Math.PI;
		if (dx > 0 && dy > 0)
			return tan;
		else if (dx > 0 && dy < 0)
			return tan + 360;
		else
			return tan + 180;
	}

	/**
	 * Returns the length of this vector.
	 *
	 * @return the length of this vector
	 */
	public double getLength() {
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Returns whether another vector is parallel to this vector. This is also
	 * true if the other vector moves in the opposite direction.
	 * 
	 * @param other the other vector
	 * @return true if the vector is parallel, false otherwise
	 */
	public boolean isParallel(Vector other) {
		if (dx == 0) {
			return other.dx == 0;
		} else if (dy == 0) {
			return other.dy == 0;
		} else {
			double dir1 = dx / dy;
			double dir2 = other.dx / other.dy;
			return dir1 == dir2;
		}
	}
	
	@Override
	public int hashCode() {
		int result = dx == 0 ? 0 : (int)Double.doubleToLongBits(dx);
		result = 31 * result + dy == 0 ? 0 : (int)Double.doubleToLongBits(dy);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector)obj;
		if (dx != other.dx)
			return false;
		if (dy != other.dy)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + dx + ", " + dy + ")";
	}
}

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
 * This class defines a line segment. It consists of a start point and end
 * point.
 *
 * @author Dennis Hofs (RRD)
 */
public class LineSegment {
	private Point start;
	private Point end;

	/**
	 * Constructs a new line segment.
	 *
	 * @param start the start point
	 * @param end the end point
	 */
	public LineSegment(Point start, Point end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Constructs a new line segment.
	 * 
	 * @param startX the X position of the start point
	 * @param startY the Y position of the start point
	 * @param endX the X position of the end point
	 * @param endY the Y position of the end point
	 */
	public LineSegment(double startX, double startY, double endX, double endY) {
		this.start = new Point(startX, startY);
		this.end = new Point(endX, endY);
	}

	/**
	 * Constructs a new line segment.
	 *
	 * @param start the start point
	 * @param vector the vector from the start point to the end point
	 */
	public LineSegment(Point start, Vector vector) {
		this.start = start;
		this.end = new Point(start.getX() + vector.getDx(),
				start.getY() + vector.getDy());
	}

	/**
	 * Returns the start point.
	 *
	 * @return the start point
	 */
	public Point getStart() {
		return start;
	}

	/**
	 * Sets the start point.
	 * 
	 * @param start the start point
	 */
	public void setStart(Point start) {
		this.start = start;
	}

	/**
	 * Returns the end point.
	 *
	 * @return the end point
	 */
	public Point getEnd() {
		return end;
	}

	/**
	 * Sets the end point.
	 * 
	 * @param end the end point
	 */
	public void setEnd(Point end) {
		this.end = end;
	}

	/**
	 * Returns the vector from the start point to the end point.
	 *
	 * @return the vector from the start point to the end point
	 */
	public Vector toVector() {
		return new Vector(end.getX() - start.getX(), end.getY() - start.getY());
	}

	/**
	 * Returns the length of this line segment.
	 *
	 * @return the length of this line segment
	 */
	public double getLength() {
		return toVector().getLength();
	}

	/**
	 * Rotates this line segment by the specified number of degrees around the
	 * start point.
	 *
	 * @param angle the angle in degrees
	 */
	public void rotate(double angle) {
		Vector vector = toVector();
		vector.rotate(angle);
		end.set(start.getX() + vector.getDx(), start.getY() + vector.getDy());
	}
	
	/**
	 * Moves this line segment by the specified offset.
	 * 
	 * @param dx the offset along the X axis
	 * @param dy the offset along the Y axis
	 */
	public void offset(double dx, double dy) {
		start.offset(dx, dy);
		end.offset(dx, dy);
	}
	
	@Override
	public String toString() {
		return start + " - " + end;
	}
}

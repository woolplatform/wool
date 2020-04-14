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

package eu.woolplatform.utils.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A linear function is a function that is defined by straight line segments
 * between a series of points. If there are no points, the function is defined
 * by the straight horizontal line y = 0. If there is one point, the function
 * is defined by the straight horizontal line through that point. If there are
 * two or more points, the function value of a point between the first point
 * and the last point is defined by the line segment between two points. For
 * points before the first point, the value is defined by extending the first
 * line segment (between the first and second point). For points after the last
 * point, the value is defined by extending the last line segment.
 * 
 * @author Dennis Hofs (RRD)
 */
public class LinearFunction {
	private List<Point> points = new ArrayList<>();
	
	/**
	 * Constructs a new linear function without any points.
	 */
	public LinearFunction() {
	}
	
	/**
	 * Adds a point.
	 * 
	 * @param x the X value
	 * @param y the Y value
	 */
	public void addPoint(float x, float y) {
		int i = 0;
		while (i < points.size()) {
			Point cmp = points.get(i);
			if (x < cmp.x)
				points.add(i, new Point(x, y));
			else
				i++;
		}
		points.add(new Point(x, y));
	}

	/**
	 * Returns the function value for the specified point.
	 * 
	 * @param x the point
	 * @return the function value
	 */
	public float get(float x) {
		Iterator<Point> it = points.iterator();
		if (!it.hasNext())
			return 0.0f;
		Point p1 = it.next();
		if (!it.hasNext())
			return p1.y;
		Point p2 = it.next();
		while (x > p2.x && it.hasNext()) {
			p1 = p2;
			p2 = it.next();
		}
		float prop = (x - p1.x) / (p2.x - p1.x);
		return p1.y + prop * (p2.y - p1.y);
	}
	
	/**
	 * A point consists of an X and Y value.
	 */
	private class Point {
		public float x;
		public float y;
		
		/**
		 * Constructs a new point.
		 * 
		 * @param x the X value
		 * @param y the Y value
		 */
		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
}

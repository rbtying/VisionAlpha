/*******************************************************************************
 * Copyright 2013 Robert Ying, based on code by Ernesto Tapias
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package ocr;

public class DPoint implements Cloneable {
	public static final double EPS = 1.0E-4;
	public long t;
	public double x;
	public double xf;
	public double y;
	public double yf;

	public DPoint(final DPoint p) {
		super();
		this.xf = 1.0;
		this.yf = 1.0;
		this.t = Long.MIN_VALUE;
		this.x = p.x;
		this.y = p.y;
		this.t = p.t;
		this.xf = p.xf;
		this.yf = p.yf;
	}

	public DPoint(final double x_, final double y_, final long t_) {
		super();
		this.xf = 1.0;
		this.yf = 1.0;
		this.t = Long.MIN_VALUE;
		this.x = x_;
		this.y = y_;
		this.t = t_;
	}

	public DPoint(final double x_, final double y_) {
		this(x_, y_, System.currentTimeMillis());
	}

	public DPoint() {
		this(0.0, 0.0);
	}

	public double angle(final DPoint p) {
		return Math.atan2(p.y - this.y, p.x - this.x);
	}

	public double angle() {
		return Math.atan2(this.y, this.x);
	}

	public static double area2(final DPoint a, final DPoint b, final DPoint c) {
		return a.x * b.y - a.y * b.x + a.y * c.x - a.x * c.y + b.x * c.y - b.y
				* c.x;
	}

	public static boolean between(final DPoint a, final DPoint b, final DPoint c) {
		if (!collinear(a, b, c)) {
			return false;
		}
		if (a.x != b.x) {
			return (a.x <= c.x && c.x <= b.x) || (b.x <= c.x && c.x <= a.x);
		}
		return (a.y <= c.y && c.y <= b.y) || (b.y <= c.y && c.y <= a.y);
	}

	@Override
	public Object clone() {
		return new DPoint(this);
	}

	public static boolean collinear(final DPoint a, final DPoint b,
			final DPoint c) {
		return area2(a, b, c) == 0.0;
	}

	public static double distance(final DPoint a, final DPoint b,
			final DPoint c, final DPoint d) {
		if (intersect(a, b, c, d)) {
			return 0.0;
		}
		return Math.min(Math.min(distance(a, b, c), distance(a, b, d)),
				Math.min(distance(c, d, a), distance(c, d, b)));
	}

	public static double distance(final DPoint a, final DPoint b, final DPoint c) {
		final DPoint a2 = new DPoint(a.y - b.y + a.x, b.x - a.x + a.y);
		final DPoint b2 = new DPoint(a.y - b.y + b.x, b.x - a.x + b.y);
		if (right(a, a2, c) && left(b, b2, c)) {
			return height(a, b, c);
		}
		if (leftOn(a, a2, c)) {
			return a.distance(c);
		}
		return b.distance(c);
	}

	public double distance(final DPoint p) {
		return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y)
				* (this.y - p.y));
	}

	public double distance2(final DPoint p) {
		return (this.x - p.x) * (this.x - p.x) + (this.y - p.y)
				* (this.y - p.y);
	}

	public double dot(final DPoint p) {
		return this.x * p.x + this.y * p.y;
	}

	public static double height(final DPoint a, final DPoint b, final DPoint c) {
		if (a.x == b.x && a.y == b.y) {
			return a.distance(c);
		}
		return Math.abs(area2(a, b, c)) / a.distance(b);
	}

	public static boolean intersect(final DPoint a, final DPoint b,
			final DPoint c, final DPoint d) {
		return intersectProp(a, b, c, d)
				|| (between(a, b, c) || between(a, b, d) || between(c, d, a) || between(
						c, d, b));
	}

	public static boolean intersectProp(final DPoint a, final DPoint b,
			final DPoint c, final DPoint d) {
		return area2(a, b, c) * area2(a, b, d) < 0.0
				&& area2(c, d, a) * area2(c, d, b) < 0.0;
	}

	public static boolean left(final DPoint a, final DPoint b, final DPoint c) {
		return area2(a, b, c) > 0.0;
	}

	public static boolean leftOn(final DPoint a, final DPoint b, final DPoint c) {
		return area2(a, b, c) >= 0.0;
	}

	public DPoint middlePoint(final DPoint p) {
		return new DPoint(0.5 * (p.x - this.x) + this.x, 0.5 * (p.y - this.y)
				+ this.y);
	}

	public double norm() {
		return Math.sqrt(this.dot(this));
	}

	public double norm1() {
		return Math.abs(this.x) + Math.abs(this.y);
	}

	public double norm2() {
		return Math.sqrt(this.dot(this));
	}

	public double normMax() {
		return Math.max(Math.abs(this.x), Math.abs(this.y));
	}

	public static boolean right(final DPoint a, final DPoint b, final DPoint c) {
		return area2(a, b, c) < 0.0;
	}

	public static boolean rightOn(final DPoint a, final DPoint b, final DPoint c) {
		return area2(a, b, c) <= 0.0;
	}

	public void set(final DPoint p) {
		this.x = p.x;
		this.y = p.y;
		this.t = p.t;
	}

	public void set(final double x_, final double y_) {
		this.x = x_;
		this.y = y_;
	}

	public void set(final double x_, final double y_, final long t_) {
		this.x = x_;
		this.y = y_;
		this.t = t_;
	}

	public DPoint to(final DPoint p) {
		return new DPoint(p.x - this.x, p.y - this.y, this.t);
	}

	@Override
	public String toString() {
		if (this.t == Long.MIN_VALUE) {
			return new String("DPoint[x=" + this.x + ", y=" + this.y + "]");
		}
		return new String("DPoint[x=" + this.x + ", y=" + this.y + ", t="
				+ this.t + "]");
	}

	public String toTrainArrayList() {
		return "" + this.x + " " + this.y + " ";
	}
}

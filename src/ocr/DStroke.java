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

import svm.*;
import java.awt.geom.*;
import java.awt.*;

import math.*;
import math.Maths;

import java.util.*;
import java.io.*;

public class DStroke extends ArrayList<DPoint> implements Comparable<DStroke> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3327397846243972769L;
	public static double DIRECTION_PARMETER;
	public static double EPS;
	public static boolean SIZE;
	public static boolean TIME;
	public static boolean antialias;
	public Color color;
	public static boolean drawAsStroke;
	public static boolean drawCurvaturePoints;
	public static boolean drawDirection;
	public static boolean drawPoints;
	public static boolean drawVelocityPoints;
	public long first_time;
	double maxX;
	double maxY;
	double minX;
	double minY;
	public static int r;

	static {
		DStroke.EPS = 1.0E-7;
		DStroke.DIRECTION_PARMETER = 0.3;
		DStroke.SIZE = true;
		DStroke.TIME = false;
		DStroke.r = 3;
	}

	public DStroke(final int[] xs, final int[] ys, final long[] times,
			final long first_time) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < xs.length; ++i) {
			this.add(new DPoint(xs[i], ys[i], times[i]));
		}
		this.first_time = first_time;
	}

	public DStroke(final Stroke s) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < s.xs.length; ++i) {
			this.add(new DPoint(s.xs[i], s.ys[i], s.times[i]));
		}
		this.first_time = s.first_time;
	}

	public DStroke(final DStroke s) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < s.size(); ++i) {
			this.add(new DPoint(s.pointAt(i)));
		}
		this.first_time = s.first_time;
	}

	public DStroke(final double[] x, final double[] y) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; ++i) {
			this.add(new DPoint(x[i], y[i]));
		}
	}

	public DStroke(final int[] x, final int[] y) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; ++i) {
			this.add(new DPoint(x[i], y[i]));
		}
	}

	public DStroke(final int n) {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; ++i) {
			this.add(new DPoint());
		}
	}

	public DStroke() {
		super();
		this.first_time = Long.MIN_VALUE;
		this.color = Color.black;
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
	}

	public double accumulatedAngle(final double[] alpha) {
		double acc = 0.0;
		for (int i = 0; i < alpha.length - 1; ++i) {
			acc += alpha[i + 1] - alpha[i];
		}
		return 0.5 * acc / 3.141592653589793;
	}

	public double accumulatedAngle() {
		final double[] alpha = this.turningFunction();
		double acc = 0.0;
		for (int i = 0; i < alpha.length - 1; ++i) {
			acc += alpha[i + 1] - alpha[i];
		}
		return 0.5 * acc / 3.141592653589793;
	}

	public void actualizeMaxMin() {
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			this.minX = Math.min(this.minX, this.getX(i));
			this.maxX = Math.max(this.maxX, this.getX(i));
			this.minY = Math.min(this.minY, this.getY(i));
			this.maxY = Math.max(this.maxY, this.getY(i));
		}
	}

	@Override
	public void add(final int i, final DPoint dp) {
		super.add(i, dp);
		this.minX = Math.min(this.minX, dp.x);
		this.maxX = Math.max(this.maxX, dp.x);
		this.minY = Math.min(this.minY, dp.y);
		this.maxY = Math.max(this.maxY, dp.y);
	}

	@Override
	public boolean add(final DPoint dp) {
		boolean output = super.add(dp);
		this.minX = Math.min(this.minX, dp.x);
		this.maxX = Math.max(this.maxX, dp.x);
		this.minY = Math.min(this.minY, dp.y);
		this.maxY = Math.max(this.maxY, dp.y);
		return output;
	}

	public void add(final double x, final double y) {
		this.add(new DPoint(x, y, System.currentTimeMillis()));
	}

	public DStroke addPoints(final int np) {
		final DStroke ds = new DStroke(this);
		final int tsize = this.size();
		if (tsize < np) {
			final int size = ds.size();
			for (int j = 0; j < np - tsize; ++j) {
				int iMax = -1;
				double lMax = Double.NEGATIVE_INFINITY;
				for (int i = 0; i < size + j - 1; ++i) {
					if (ds.length(i, i + 1) > lMax) {
						lMax = ds.length(i, i + 1);
						iMax = i;
					}
				}
				ds.add(iMax + 1,
						new DPoint(0.5 * (ds.getX(iMax) + ds.getX(iMax + 1)),
								0.5 * (ds.getY(iMax) + ds.getY(iMax + 1))));
			}
		}
		return ds;
	}

	public double angleBetweenNeighbours(final int i) {
		return this.angleBetweenNeighbours(i, 1);
	}

	public double angleBetweenNeighbours(final int i, final int n) {
		final DPoint p = this.pointAt(i).to(this.pointAt(i - n));
		final DPoint q = this.pointAt(i).to(this.pointAt(i + n));
		return Math.acos(p.dot(q) / (p.norm() * q.norm()));
	}

	private double area(final int i, final int j, final int k) {
		final DPoint p1 = this.pointAt(i);
		final DPoint p2 = this.pointAt(j);
		final DPoint p3 = this.pointAt(k);
		final double a = p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y) + p1.x
				* (p2.y - p3.y);
		return Math.abs(a);
	}

	public double areaPoly() {
		return this.areaPoly2() / 2.0;
	}

	public double areaPoly2() {
		double sum = 0.0;
		for (int i = 1; i < this.size() - 1; ++i) {
			sum += DPoint.area2(this.pointAt(0), this.pointAt(i),
					this.pointAt(i + 1));
		}
		return sum;
	}

	public double areaPoly2RelativeToBoundingBox() {
		if (this.getHeight() * this.getWidth() != 0.0) {
			return this.areaPoly2() / (this.getHeight() * this.getWidth());
		}
		return 0.0;
	}

	public double areaPoly2RelativeToLength() {
		return this.areaPoly2() / this.length();
	}

	public double aspect() {
		if (this.getHeight() + this.getWidth() != 0.0) {
			return (this.getHeight() - this.getWidth())
					/ (this.getHeight() + this.getWidth());
		}
		return 0.0;
	}

	private static double atof(final String s) {
		return Double.valueOf(s);
	}

	private static int atoi(final String s) {
		return Integer.parseInt(s);
	}

	private static long atol(final String s) {
		return Long.parseLong(s);
	}

	public boolean boundigBoxIntersection(final DStroke ds, final double eps) {
		return intervalIntersection(this.minX, this.maxX, ds.minX, ds.maxX, eps)
				&& intervalIntersection(this.minY, this.maxY, ds.minY, ds.maxY,
						eps);
	}

	public DPoint centerOfGravity() {
		double x;
		double y = x = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			x += this.pointAt(i).x / this.size();
			y += this.pointAt(i).y / this.size();
		}
		return new DPoint(x, y);
	}

	public DStroke changeDirection() {
		final DStroke s = new DStroke();
		for (int i = 0; i < this.size(); ++i) {
			s.add(this.pointAt(this.size() - 1 - i));
		}
		return s;
	}

	public DStroke clusterPoints() {
		return this.clusterPoints(0.75);
	}

	public DStroke clusterPoints(final int np) {
		return this.clusterPoints(1.0 / np);
	}

	public DStroke clusterPoints(final double frac) {
		final DStroke ds = new DStroke();
		final double threshold = frac * this.length();
		double x;
		double y = x = 0.0;
		int i;
		int k = i = 0;
		DPoint p = this.pointAt(0);
		while (i < this.size()) {
			if (Math.abs(p.x - this.getX(i)) <= threshold
					&& Math.abs(p.y - this.getY(i)) <= threshold) {
				x += this.getX(i);
				y += this.getY(i);
				++k;
				++i;
			} else {
				ds.add(x / k, y / k);
				y = (x = 0.0);
				k = 0;
				p = this.pointAt(i);
			}
		}
		ds.add(x / k, y / k);
		return ds;
	}

	@Override
	public int compareTo(final DStroke other) {
		final double t = this.minX;
		final double o = other.minX;
		return (t < o) ? -1 : ((t == o) ? 0 : 1);
	}

	public boolean contains(final DStroke ds) {
		return this.minX <= ds.minX && ds.maxX <= this.maxX
				&& this.minY <= ds.minY && this.maxY <= ds.maxY;
	}

	public double cosAt(final int i) {
		double dy;
		double ds;
		double dx = ds = (dy = 0.0);
		try {
			dx = this.getX(i + 1) - this.getX(i);
			dy = this.getY(i + 1) - this.getY(i);
			ds = Math.sqrt(dx * dx + dy * dy);
		} catch (IndexOutOfBoundsException ex) {
		}
		double val = 2.0;
		if (ds > 0.0) {
			val = dx / ds;
		}
		return val;
	}

	public StepFunction cosMap() {
		this.filter();
		final StepFunction f = new StepFunction(this.size() - 1);
		final double len = this.length();
		int j = 0;
		int index = 0;
		double pos = 0.0;
		while (j < this.size() - 1) {
			final double dist = this.pointAt(j + 1).distance(this.pointAt(j));
			pos += dist;
			f.index[index] = pos / len;
			f.value[index++] = (this.pointAt(j + 1).x - this.pointAt(j).x)
					/ dist;
			++j;
		}
		return f;
	}

	public double curliness() {
		if (Math.max(this.getWidth(), this.getHeight()) != 0.0) {
			return this.length() / Math.max(this.getWidth(), this.getHeight())
					- 2.0;
		}
		return -1.0;
	}

	public double diameter(final DStroke s) {
		final int lens = s.size();
		double d = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			for (int j = 0; j < lens; ++j) {
				final double aux = this.pointAt(i).distance(s.pointAt(j));
				if (aux > d) {
					d = aux;
				}
			}
		}
		return d;
	}

	public static double distance(final Symbol s1, final Symbol s2) {
		double dist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < s1.size(); ++i) {
			for (int j = 0; j < s2.size(); ++j) {
				dist = Math.min(dist, distance(s1.strokeAt(i), s2.strokeAt(j)));
			}
		}
		return dist;
	}

	public static boolean distance(final DStroke s1, final DStroke s2,
			final double eps) {
		double dist = Double.POSITIVE_INFINITY;
		final double size1 = s1.size();
		final double size2 = s2.size();
		if (size1 == 1.0 && size2 == 1.0) {
			if (s1.pointAt(0).distance(s2.pointAt(0)) <= eps) {
				return true;
			}
		} else if (size1 == 1.0 && size2 > 1.0) {
			for (int j = 0; j < size2 - 1.0; ++j) {
				dist = Math.min(
						dist,
						DPoint.distance(s2.pointAt(j), s2.pointAt(j + 1),
								s1.pointAt(0)));
				if (dist <= eps) {
					return true;
				}
			}
		} else if (size1 > 1.0 && size2 == 1.0) {
			for (int j = 0; j < size1 - 1.0; ++j) {
				dist = Math.min(
						dist,
						DPoint.distance(s1.pointAt(j), s1.pointAt(j + 1),
								s2.pointAt(0)));
				if (dist <= eps) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < size1 - 1.0; ++i) {
				for (int j = 0; j < size2 - 1.0; ++j) {
					dist = Math.min(dist,
							DPoint.distance(s1.pointAt(i), s1.pointAt(i + 1),
									s2.pointAt(j), s2.pointAt(j + 1)));
					if (dist <= eps) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static double distance(final DStroke s1, final DStroke s2) {
		double dist = Double.POSITIVE_INFINITY;
		final double size1 = s1.size();
		final double size2 = s2.size();
		if (size1 == 1.0 && size2 == 1.0) {
			return s1.pointAt(0).distance(s2.pointAt(0));
		}
		if (size1 == 1.0 && size2 > 1.0) {
			for (int j = 0; j < size2 - 1.0; ++j) {
				dist = Math.min(
						dist,
						DPoint.distance(s2.pointAt(j), s2.pointAt(j + 1),
								s1.pointAt(0)));
				if (dist == 0.0) {
					return 0.0;
				}
			}
		} else if (size1 > 1.0 && size2 == 1.0) {
			for (int j = 0; j < size1 - 1.0; ++j) {
				dist = Math.min(
						dist,
						DPoint.distance(s1.pointAt(j), s1.pointAt(j + 1),
								s2.pointAt(0)));
				if (dist == 0.0) {
					return 0.0;
				}
			}
		} else {
			for (int i = 0; i < size1 - 1.0; ++i) {
				for (int j = 0; j < size2 - 1.0; ++j) {
					dist = Math.min(dist,
							DPoint.distance(s1.pointAt(i), s1.pointAt(i + 1),
									s2.pointAt(j), s2.pointAt(j + 1)));
					if (dist == 0.0) {
						return 0.0;
					}
				}
			}
		}
		return dist;
	}

	public boolean distance(final DStroke ds, final double eps) {
		return distance(this, ds, eps);
	}

	public double distance(final DStroke s) {
		return distance(this, s);
	}

	public double distance(final DPoint p) {
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			final double aux = this.pointAt(i).distance(p);
			if (aux < d) {
				d = aux;
			}
		}
		return d;
	}

	public double distance2(final DStroke s) {
		final int lens = s.size();
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			for (int j = 0; j < lens; ++j) {
				final double aux = this.pointAt(i).distance2(s.pointAt(j));
				if (aux < d) {
					d = aux;
				}
			}
		}
		return d;
	}

	public double distance2(final DPoint p) {
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			final double aux = this.pointAt(i).distance2(p);
			if (aux < d) {
				d = aux;
			}
		}
		return d;
	}

	public void draw(final Graphics2D g2, final Color c) {
		int i = 0;
		if (this.isEmpty()) {
			return;
		}
		if (DStroke.antialias) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		if (DStroke.drawAsStroke) {
			g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
		} else {
			g2.setStroke(new BasicStroke(1.0f, 2, 1));
		}
		g2.setColor(c);
		g2.draw(new Line2D.Double(this.first().x, this.first().y,
				this.first().x, this.first().y));
		for (i = 0; i < this.size() - 1; ++i) {
			g2.draw(new Line2D.Double(this.pointAt(i).x, this.pointAt(i).y,
					this.pointAt(i + 1).x, this.pointAt(i + 1).y));
		}
		if (DStroke.drawPoints) {
			for (i = 0; i < this.size(); ++i) {
				g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
				g2.draw(new Line2D.Double(this.pointAt(i).x, this.pointAt(i).y,
						this.pointAt(i).x, this.pointAt(i).y));
			}
		}
	}

	public void draw(final Graphics2D g2) {
		int i = 0;
		if (this.isEmpty()) {
			return;
		}
		if (DStroke.antialias) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		if (DStroke.drawAsStroke) {
			g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
		} else {
			g2.setStroke(new BasicStroke(1.0f, 2, 1));
		}
		g2.setColor(this.color);
		g2.draw(new Line2D.Double(this.first().x, this.first().y,
				this.first().x, this.first().y));
		final GeneralPath polyline = new GeneralPath(0, this.size());
		polyline.moveTo((float) this.pointAt(0).x, (float) this.pointAt(0).y);
		for (i = 1; i < this.size(); ++i) {
			polyline.lineTo((float) this.pointAt(i).x,
					(float) this.pointAt(i).y);
		}
		g2.draw(polyline);
		if (DStroke.drawPoints) {
			for (i = 0; i < this.size(); ++i) {
				g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
				g2.draw(new Line2D.Double(this.pointAt(i).x, this.pointAt(i).y,
						this.pointAt(i).x, this.pointAt(i).y));
			}
		}
		if (DStroke.drawDirection) {
			g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
			g2.setColor(Color.red);
			g2.draw(new Line2D.Double(this.last().x, this.last().y,
					this.last().x, this.last().y));
			g2.setColor(Color.green);
			g2.draw(new Line2D.Double(this.first().x, this.first().y, this
					.first().x, this.first().y));
		}
		if (DStroke.drawVelocityPoints && this.size() > 6) {
			final int[] index = this.getLowVelocityIndexes();
			g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
			for (i = 0; i < index.length; ++i) {
				if (index[i] == 1) {
					g2.setColor(Color.magenta);
					g2.draw(new Line2D.Double(this.pointAt(i).x, this
							.pointAt(i).y, this.pointAt(i).x, this.pointAt(i).y));
				}
			}
		}
		if (DStroke.drawCurvaturePoints && this.size() > 6) {
			final int[] index = this.getHighCurvatureIndexes();
			g2.setStroke(new BasicStroke(DStroke.r, 2, 1));
			for (i = 0; i < index.length; ++i) {
				g2.setColor(Color.black);
				g2.draw(new Line2D.Double(this.pointAt(i).x, this.pointAt(i).y,
						this.pointAt(i).x, this.pointAt(i).y));
			}
			for (i = 0; i < index.length; ++i) {
				if (index[i] == 1) {
					g2.setColor(Color.green);
					g2.draw(new Line2D.Double(this.pointAt(i).x, this
							.pointAt(i).y, this.pointAt(i).x, this.pointAt(i).y));
				} else if (index[i] == -1) {
					g2.setColor(Color.red);
					g2.draw(new Line2D.Double(this.pointAt(i).x, this
							.pointAt(i).y, this.pointAt(i).x, this.pointAt(i).y));
				}
			}
		}
	}

	public void drawBoundingBox(final Graphics2D g2, final Color c) {
		g2.setColor(c);
		g2.drawRect((int) this.minX, (int) this.minY,
				(int) (this.maxX - this.minX), (int) (this.maxY - this.minY));
	}

	public DStroke dynamicProgrammingLengthError(final int numPoints) {
		final double[][] E = new double[numPoints][this.size()];
		final int[][] I = new int[numPoints][this.size()];
		final double[] error = new double[this.size()];
		E[0][0] = 0.0;
		for (int n = 1; n < this.size(); ++n) {
			E[0][n] = Double.POSITIVE_INFINITY;
		}
		final ArrayList<Integer> minIndexes = new ArrayList<Integer>();
		for (int m = 1; m < numPoints; ++m) {
			for (int n = m; n < this.size(); ++n) {
				double min = Double.POSITIVE_INFINITY;
				// int j_min = -1;
				minIndexes.clear();
				for (int j = m - 1; j < n; ++j) {
					error[j] = E[m - 1][j] + this.strokeAt(j, n).lenghtError();
					if (error[j] < min) {
						min = error[j];
						// j_min = j;
					}
				}
				E[m][n] = min;
				for (int j = m - 1; j < n; ++j) {
					if (min == error[j]) {
						minIndexes.add(new Integer(j));
					}
				}
				I[m][n] = minIndexes.get(minIndexes.size() / 2);
			}
		}
		final DStroke ds = new DStroke();
		int n = this.size() - 1;
		for (int m = numPoints - 1; m >= 0; --m) {
			ds.add(0, new DPoint(this.pointAt(n)));
			n = I[m][n];
		}
		return ds;
	}

	public DStroke dynamicProgrammingMaxDistanceError(final int numPoints) {
		final double[][] E = new double[numPoints][this.size()];
		final int[][] I = new int[numPoints][this.size()];
		final double[] error = new double[this.size()];
		E[0][0] = 0.0;
		for (int n = 1; n < this.size(); ++n) {
			E[0][n] = Double.POSITIVE_INFINITY;
		}
		final ArrayList<Integer> minIndexes = new ArrayList<Integer>();
		for (int m = 1; m < numPoints; ++m) {
			for (int n = m; n < this.size(); ++n) {
				double min = Double.POSITIVE_INFINITY;
				// int j_min = -1;
				minIndexes.clear();
				for (int j = m - 1; j < n; ++j) {
					error[j] = Math.max(E[m - 1][j], this.strokeAt(j, n)
							.maxDistanceError());
					if (error[j] < min) {
						min = error[j];
						// j_min = j;
					}
				}
				E[m][n] = min;
				for (int j = m - 1; j < n; ++j) {
					if (min == error[j]) {
						minIndexes.add(new Integer(j));
					}
				}
				I[m][n] = minIndexes.get(minIndexes.size() / 2);
			}
		}
		final DStroke ds = new DStroke();
		int n = this.size() - 1;
		for (int m = numPoints - 1; m >= 0; --m) {
			ds.add(0, new DPoint(this.pointAt(n)));
			n = I[m][n];
		}
		return ds;
	}

	public DStroke dynamicProgrammingSumDistanceError(final int numPoints) {
		final double[][] E = new double[numPoints][this.size()];
		final int[][] I = new int[numPoints][this.size()];
		final double[] error = new double[this.size()];
		E[0][0] = 0.0;
		for (int n = 1; n < this.size(); ++n) {
			E[0][n] = Double.POSITIVE_INFINITY;
		}
		final ArrayList<Integer> minIndexes = new ArrayList<Integer>();
		for (int m = 1; m < numPoints; ++m) {
			for (int n = m; n < this.size(); ++n) {
				double min = Double.POSITIVE_INFINITY;
				// int j_min = -1;
				minIndexes.clear();
				for (int j = m - 1; j < n; ++j) {
					error[j] = E[m - 1][j]
							+ this.strokeAt(j, n).sumDistanceError();
					if (error[j] < min) {
						min = error[j];
						// j_min = j;
					}
				}
				E[m][n] = min;
				for (int j = m - 1; j < n; ++j) {
					if (min == error[j]) {
						minIndexes.add(new Integer(j));
					}
				}
				I[m][n] = minIndexes.get(minIndexes.size() / 2);
			}
		}
		final DStroke ds = new DStroke();
		int n = this.size() - 1;
		for (int m = numPoints - 1; m >= 0; --m) {
			ds.add(0, new DPoint(this.pointAt(n)));
			n = I[m][n];
		}
		return ds;
	}

	public DStroke equidistant(final double r) {
		final DStroke equidistantStroke = new DStroke();
		final int len = this.size();
		final DPoint center = new DPoint(this.get(0));
		equidistantStroke.add(new DPoint(center));
		if (len > 1) {
			final DPoint init = new DPoint(center);
			final DPoint end = new DPoint(this.get(1));
			int i = 1;
			while (true) {
				final double dist = center.distance(end);
				if (dist > r) {
					final DPoint intersection = this.intersection(center, r,
							init, end);
					center.set(intersection);
					equidistantStroke.add(new DPoint(center));
					init.set(center);
				} else if (dist < r) {
					init.set(end);
					if (++i >= len) {
						break;
					}
					end.set(this.get(i));
				} else {
					center.set(end);
					equidistantStroke.add(new DPoint(center));
					init.set(center);
					if (++i >= len) {
						break;
					}
					end.set(this.get(i));
				}
			}
		}
		return equidistantStroke;
	}

	public DStroke equidistantApproximation(final int points) {
		final DStroke equidistantStroke = new DStroke();
		int len = this.size();
		final double r = this.length() / points;
		DPoint center = new DPoint(this.get(0));
		equidistantStroke.add(new DPoint(center));
		if (len > 1) {
			final DPoint init = new DPoint(center);
			final DPoint end = new DPoint(this.get(1));
			int i = 1;
			while (true) {
				final double dist = center.distance(end);
				if (dist > r) {
					final DPoint intersection = this.intersection(center, r,
							init, end);
					center.set(intersection);
					equidistantStroke.add(new DPoint(center));
					init.set(center);
				} else if (dist < r) {
					init.set(end);
					if (++i >= len) {
						break;
					}
					end.set(this.get(i));
				} else {
					center.set(end);
					equidistantStroke.add(new DPoint(center));
					init.set(center);
					if (++i >= len) {
						break;
					}
					end.set(this.get(i));
				}
			}
		}
		len = equidistantStroke.size();
		if (len > 1 && len < points) {
			final double diffx = equidistantStroke.getX(len - 1)
					- equidistantStroke.getX(len - 2);
			final double diffy = equidistantStroke.getY(len - 1)
					- equidistantStroke.getY(len - 2);
			final double norm = Math.sqrt(diffx * diffx + diffy * diffy);
			final DPoint end = new DPoint(r * diffx / norm, r * diffy / norm);
			for (int i = len; i < points; ++i) {
				center = equidistantStroke.last();
				equidistantStroke.add(new DPoint(center.x + end.x, center.y
						+ end.y));
			}
		}
		return equidistantStroke;
	}

	public DStroke equidistantLength(final int np) {
		final DStroke eq = new DStroke();
		final double len = this.length();
		final double[] t = new double[this.size()];
		t[0] = 0.0;
		int i;
		for (i = 1; i < this.size() - 1; ++i) {
			t[i] = t[i - 1] + this.length(i - 1, i);
		}
		t[i] = len;
		final double[] d = new double[np];
		final double inc = len / (np - 1);
		d[0] = 0.0;
		int k;
		for (k = 1; k < np - 1; ++k) {
			d[k] = d[k - 1] + inc;
		}
		d[k] = len;
		i = 0;
		k = 1;
		eq.add(new DPoint(this.first()));
		while (i < t.length && k < d.length) {
			if (d[k] <= t[i]) {
				final double a = (d[k] - t[i - 1]) / (t[i] - t[i - 1]);
				eq.add(this.getX(i - 1) + a * (this.getX(i) - this.getX(i - 1)),
						this.getY(i - 1) + a
								* (this.getY(i) - this.getY(i - 1)));
				++k;
			} else {
				++i;
			}
		}
		return eq;
	}

	public DStroke equidistantLength(final double inc) {
		final DStroke eq = new DStroke();
		final double len = this.length();
		final double[] t = new double[this.size()];
		t[0] = 0.0;
		int i;
		for (i = 1; i < this.size() - 1; ++i) {
			t[i] = t[i - 1] + this.length(i - 1, i);
		}
		t[i] = len;
		final int np = (int) (len / inc + 1.0);
		final double[] d = new double[np];
		d[0] = 0.0;
		int k;
		for (k = 1; k < np - 1; ++k) {
			d[k] = d[k - 1] + inc;
		}
		d[k] = len;
		i = 0;
		k = 1;
		eq.add(new DPoint(this.first()));
		while (i < t.length && k < d.length) {
			if (d[k] <= t[i]) {
				final double a = (d[k] - t[i - 1]) / (t[i] - t[i - 1]);
				eq.add(this.getX(i - 1) + a * (this.getX(i) - this.getX(i - 1)),
						this.getY(i - 1) + a
								* (this.getY(i) - this.getY(i - 1)));
				++k;
			} else {
				++i;
			}
		}
		return eq;
	}

	public DStroke equidistantOptimized(final int np) {
		final DStroke ds = this.proyectionPolygonal(np);
		final DStroke eq = new DStroke();
		final double len = ds.length();
		final double[] alpha = ds.turningFunction();
		final StepFunction x = new StepFunction(np - 1);
		final StepFunction y = new StepFunction(np - 1);
		final double inc = len / (np - 1);
		x.value[0] = alpha[0];
		x.index[0] = ds.length(0, 1) / len;
		y.value[0] = 0.0;
		y.index[0] = inc / len;
		for (int h = 1; h < x.length; ++h) {
			x.value[h] = alpha[h];
			x.index[h] = x.index[h - 1] + ds.length(h, h + 1) / len;
			y.value[h] = 0.0;
			y.index[h] = y.index[h - 1] + inc / len;
		}
		int h;
		int k = h = 0;
		double a = 0.0;
		while (h < x.length && k < y.length) {
			if (x.index[h] == y.index[k]) {
				final double[] value = y.value;
				final int n = k;
				value[n] += len * x.value[h] * (x.index[h] - a) / inc;
				a = x.index[h];
				++h;
				++k;
			} else if (x.index[h] < y.index[k]) {
				final double[] value2 = y.value;
				final int n2 = k;
				value2[n2] += len * x.value[h] * (x.index[h] - a) / inc;
				a = x.index[h];
				++h;
			} else {
				final double[] value3 = y.value;
				final int n3 = k;
				value3[n3] += len * x.value[h] * (y.index[k] - a) / inc;
				a = y.index[k];
				++k;
			}
		}
		eq.add(ds.pointAt(0));
		for (h = 1; h < ds.size(); ++h) {
			eq.add(eq.getX(h - 1) + inc * Math.cos(y.value[h - 1]),
					eq.getY(h - 1) + inc * Math.sin(y.value[h - 1]));
		}
		return eq;
	}

	public DStroke equidistantRespecToMaxWidthHeight(final int numPoints) {
		final double r = Math.max(this.getWidth(), this.getHeight())
				/ numPoints;
		return this.equidistant(r);
	}

	public DStroke filter(final double sigma, final int size) {
		final double[][] ker = new double[size + 1][];
		for (int i = 0; i <= size; ++i) {
			double sum = 0.0;
			ker[i] = new double[2 * i + 1];
			for (int j = -i; j <= i; ++j) {
				final double n = sum;
				final double[] array = ker[i];
				final int n2 = j + i;
				final double exp = Math.exp(-sigma * j * j);
				array[n2] = exp;
				sum = n + exp;
			}
			for (int j = -i; j <= i; ++j) {
				final double[] array2 = ker[i];
				final int n3 = j + i;
				array2[n3] /= sum;
			}
		}
		final int len = this.size();
		if (len <= size) {
			final DStroke fx = new DStroke();
			final DPoint p = new DPoint();
			for (int i = 0; i < len; ++i) {
				double sumx;
				double sumy = sumx = 0.0;
				for (int j = -i; j <= i; ++j) {
					try {
						p.set(this.pointAt(j + i));
					} catch (IndexOutOfBoundsException e) {
						continue;
					}
					sumx += p.x * ker[i][j + i];
					sumy += p.y * ker[i][j + i];
				}
				fx.add(new DPoint(sumx, sumy));
			}
			return fx;
		}
		final DStroke fx = new DStroke();
		final DPoint p = new DPoint();
		for (int i = 0; i <= size; ++i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				try {
					p.set(this.pointAt(j + i));
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
				sumx += p.x * ker[i][j + i];
				sumy += p.y * ker[i][j + i];
			}
			fx.add(new DPoint(sumx, sumy));
		}
		for (int i = size + 1; i <= len - 1 - size; ++i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -size; j <= size; ++j) {
				try {
					p.set(this.pointAt(j + i));
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
				sumx += p.x * ker[size][j + size];
				sumy += p.y * ker[size][j + size];
			}
			fx.add(new DPoint(sumx, sumy));
		}
		for (int i = size - 1; i >= 0; --i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				try {
					p.set(this.pointAt(len - 1 - i + j));
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
				sumx += p.x * ker[i][j + i];
				sumy += p.y * ker[i][j + i];
			}
			fx.add(new DPoint(sumx, sumy));
		}
		return fx;
	}

	public void filter() {
		for (int i = 0; i < this.size() - 1; ++i) {
			if (this.pointAt(i).x == this.pointAt(i + 1).x
					&& this.pointAt(i).y == this.pointAt(i + 1).y) {
				this.remove(i + 1);
			}
		}
	}

	public DPoint first() {
		return new DPoint(this.pointAt(0));
	}

	public double firstLastPointsDistanceRelativeToLength() {
		final double len = this.length();
		if (len != 0.0) {
			return this.last().distance(this.first()) / len;
		}
		return -1.0;
	}

	public double getCurvature() {
		final double[] alpha = new double[this.size() - 1];
		for (int k = 0; k < this.size() - 1; ++k) {
			alpha[k] = this.pointAt(k).angle(this.pointAt(k + 1));
		}
		// final DStroke alphad = new DStroke();
		double c = 0.0;
		for (int k = 0; k < this.size() - 2; ++k) {
			c += Maths.modPI(alpha[k + 1] - alpha[k]);
		}
		return 0.5 * c / 3.141592653589793;
	}

	public double getCurvature2() {
		final double[] alpha = new double[this.size() - 1];
		for (int k = 0; k < this.size() - 1; ++k) {
			alpha[k] = this.pointAt(k).angle(this.pointAt(k + 1));
		}
		// final DStroke alphad = new DStroke();
		double c = 0.0;
		for (int k = 0; k < this.size() - 2; ++k) {
			final double a = Maths.modPI(alpha[k + 1] - alpha[k]);
			c += a * a;
		}
		return 0.5 * c / 3.141592653589793;
	}

	public DStroke getDehooked(final double tangle, double tlength) {
		tlength *= this.length();
		double len = 0.0;
		int init = 0;
		for (int i = 0; i < this.size() - 1; ++i) {
			len += this.length(i, i + 1);
			if (len > tlength) {
				init = i + 1;
				break;
			}
		}
		int fi = 0;
		for (int i = init; i >= 1; --i) {
			final int eps = (i == 1) ? 1 : 1;
			double adiff = this.pointAt(i).angle(this.pointAt(i + eps))
					- this.pointAt(i - eps).angle(this.pointAt(i));
			adiff = 180.0 * (adiff + modPIPhase(adiff)) / 3.141592653589793;
			if (Math.abs(adiff) > tangle) {
				fi = i;
			}
		}
		len = 0.0;
		init = this.size() - 2;
		for (int i = this.size() - 1; i >= 1; --i) {
			len += this.length(i - 1, i);
			if (len > tlength) {
				init = i - 1;
				break;
			}
		}
		int li = this.size() - 1;
		for (int i = init; i < this.size() - 1; ++i) {
			final int eps = (i == this.size() - 2) ? 1 : 1;
			double adiff = this.pointAt(i).angle(this.pointAt(i + eps))
					- this.pointAt(i - eps).angle(this.pointAt(i));
			adiff = 180.0 * (adiff + modPIPhase(adiff)) / 3.141592653589793;
			if (Math.abs(adiff) > tangle) {
				li = i;
			}
		}
		final DStroke ds = new DStroke();
		for (int i = fi; i <= li; ++i) {
			ds.add(this.pointAt(i));
		}
		return ds;
	}

	public DStroke getDehooked() {
		return this.getDehooked(90.0, 0.12);
	}

	public int[] getExtrema(final double[] a, final int w) {
		final int[] indexes = new int[a.length];
		for (int i = 0; i < indexes.length; ++i) {
			boolean extremal;
			boolean extremar;
			try {
				extremal = true;
				for (int j = 1; j <= w; ++j) {
					extremal &= (a[i] >= a[i - j]);
				}
				for (int j = 1; j <= w; ++j) {
					extremal &= (a[i] > a[i + j]);
				}
				extremar = true;
				for (int j = 1; j <= w; ++j) {
					extremar &= (a[i] > a[i - j]);
				}
				for (int j = 1; j <= w; ++j) {
					extremar &= (a[i] >= a[i + j]);
				}
			} catch (IndexOutOfBoundsException e) {
				extremar = (extremal = false);
			}
			if (extremal || extremar) {
				indexes[i] = 1;
			}
			try {
				extremal = true;
				for (int j = 1; j <= w; ++j) {
					extremal &= (a[i] <= a[i - j]);
				}
				for (int j = 1; j <= w; ++j) {
					extremal &= (a[i] < a[i + j]);
				}
				extremar = true;
				for (int j = 1; j <= w; ++j) {
					extremar &= (a[i] < a[i - j]);
				}
				for (int j = 1; j <= w; ++j) {
					extremar &= (a[i] <= a[i + j]);
				}
			} catch (IndexOutOfBoundsException e) {
				extremar = (extremal = false);
			}
			if (extremal || extremar) {
				indexes[i] = -1;
			}
		}
		return indexes;
	}

	public double getHeight() {
		return this.getMaxY() - this.getMinY();
	}

	public int[] getHighCurvatureIndexes(final double ks, final double kl,
			final int w) {
		int[] indexes = null;
		double[] a = this.smooth().smooth().turningFunction();
		a = this.smooth(this.smooth(a));
		double[] da = new double[a.length - 1];
		da[0] = 180.0 * (a[1] - a[0]) / 3.141592653589793;
		for (int i = 1; i < da.length; ++i) {
			da[i] = 180.0 * (a[i + 1] - a[i]) / 3.141592653589793;
		}
		da = this.smooth(this.smooth(da));
		double I = 0.0;
		for (int i = 0; i < da.length; ++i) {
			I += da[i] * da[i];
		}
		I = ks * Math.sqrt(I / da.length) + kl;
		final int[] idx = this.getExtrema(da, w);
		for (int i = 0; i < idx.length; ++i) {
			if (idx[i] > 0 && da[i + 1] < I) {
				idx[i] = 0;
			} else if (idx[i] < 0 && da[i + 1] > -I) {
				idx[i] = 0;
			}
		}
		indexes = new int[this.size()];
		System.arraycopy(idx, 0, indexes, 1, idx.length);
		return indexes;
	}

	public int[] getHighCurvatureIndexes() {
		return this.getHighCurvatureIndexes(0.125, 10.0, 3);
	}

	public double getHorizontalFactor() {
		double xm = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			xm += this.getX(i);
		}
		xm /= this.size();
		int na;
		int nb = na = 0;
		double xa;
		double xb = xa = 0.0;
		double ya;
		double yb = ya = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			if (this.getX(i) < xm) {
				xa += this.getX(i);
				ya += this.getY(i);
				++na;
			} else {
				xb += this.getX(i);
				yb += this.getY(i);
				++nb;
			}
		}
		xa /= na;
		ya /= na;
		xb /= nb;
		yb /= nb;
		return Math.atan2(ya - yb, xa - xb)
				* Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb))
				/ Math.max(this.getWidth(), this.getHeight())
				/ 3.141592653589793;
	}

	public int[] getLowVelocityIndexes() {
		final DStroke ds = this.smooth().smooth().smooth();
		final double[] v = new double[this.size()];
		final double[] vn = new double[this.size()];
		for (int i = 1; i < v.length; ++i) {
			final double dx = ds.getX(i) - ds.getX(i - 1);
			final double dy = ds.getY(i) - ds.getY(i - 1);
			vn[i] = Math.sqrt(dx * dx + dy * dy);
		}
		vn[0] = v[1];
		for (int i = 2; i < v.length - 2; ++i) {
			v[i] = vn[i - 2] + 4.0 * vn[i - 1] + 6.0 * vn[i] + 4.0 * v[i + 1]
					+ v[i + 2];
			v[i] /= 16.0;
		}
		final int[] indexes = new int[v.length];
		for (int i = 3; i < indexes.length - 3; ++i) {
			if ((v[i] <= v[i - 3] && v[i] <= v[i - 2] && v[i] <= v[i - 1]
					&& v[i] < v[i + 1] && v[i] < v[i + 2] && v[i] < v[i + 3])
					|| (v[i] < v[i - 3] && v[i] < v[i - 2] && v[i] < v[i - 1]
							&& v[i] <= v[i + 1] && v[i] <= v[i + 2] && v[i] <= v[i + 3])) {
				indexes[i - 1] = 1;
			}
		}
		return indexes;
	}

	public DPoint getLowerLeft() {
		return new DPoint(this.getMinX(), this.getMaxY());
	}

	public DPoint getLowerRight() {
		return new DPoint(this.getMaxX(), this.getMaxY());
	}

	public double getMaxX() {
		return this.maxX;
	}

	public double getMaxY() {
		return this.maxY;
	}

	public double getMinX() {
		return this.minX;
	}

	public double getMinY() {
		return this.minY;
	}

	public ArrayList<Integer> getOnlyHighCurvatureIndexes(final double ks,
			final double kl, final int w) {
		final ArrayList<Integer> indexes = new ArrayList<Integer>();
		double[] a = this.smooth().smooth().turningFunction();
		a = this.smooth(this.smooth(a));
		double[] da = new double[a.length - 1];
		da[0] = 180.0 * (a[1] - a[0]) / 3.141592653589793;
		for (int i = 1; i < da.length; ++i) {
			da[i] = 180.0 * (a[i + 1] - a[i]) / 3.141592653589793;
		}
		da = this.smooth(this.smooth(da));
		double I = 0.0;
		for (int i = 0; i < da.length; ++i) {
			I += da[i] * da[i];
		}
		I = ks * Math.sqrt(I / da.length) + kl;
		final int[] idx = this.getExtrema(da, w);
		indexes.add(new Integer(0));
		for (int i = 0; i < idx.length; ++i) {
			if (idx[i] > 0 && da[i + 1] < I) {
				idx[i] = 0;
			} else if (idx[i] < 0 && da[i + 1] > -I) {
				idx[i] = 0;
			} else {
				indexes.add(new Integer(i));
			}
		}
		indexes.add(new Integer(this.size() - 1));
		return indexes;
	}

	public ArrayList<Integer> getOnlyHighCurvatureIndexes() {
		return this.getOnlyHighCurvatureIndexes(0.125, 10.0, 4);
	}

	public ArrayList<Point> getPerceptuallyImportantIndexes(double theta_max) {
		theta_max = theta_max * 3.141592653589793 / 180.0;
		final DStroke domain = new DStroke();
		domain.add(0.0, 0.0);
		for (int i = 1; i < this.size() - 1; ++i) {
			int j;
			for (j = 1; j < this.size() / 2; ++j) {
				try {
					final DPoint m = this.pointAt(i).to(
							this.pointAt(i - j)
									.middlePoint(this.pointAt(i + j)));
					final double theta_b = this.pointAt(i - (j - 1))
							.to(this.pointAt(i - j)).angle(m);
					final double theta_f = m.angle(this.pointAt(i + (j - 1))
							.to(this.pointAt(i + j)));
					if (Math.abs(theta_b) <= theta_max
							&& Math.abs(theta_f) <= theta_max) {
						break;
					}
				} catch (IndexOutOfBoundsException e) {
					break;
				}
			}
			int N0;
			if (j != 1) {
				N0 = 3 * j;
			} else {
				N0 = 1;
			}
			double FI = 0.0;
			for (j = N0; j < this.size() / 2; ++j) {
				try {
					final DPoint m = this.pointAt(i).to(
							this.pointAt(i - j)
									.middlePoint(this.pointAt(i + j)));
					final double theta_b = this.pointAt(i - (j - 1))
							.to(this.pointAt(i - j)).angle(m);
					final double theta_f = m.angle(this.pointAt(i + (j - 1))
							.to(this.pointAt(i + j)));
					if (Math.abs(theta_b) <= theta_max
							&& Math.abs(theta_f) <= theta_max) {
						FI += Math.cos(theta_b) * Math.cos(theta_f);
					}
				} catch (IndexOutOfBoundsException e) {
				}
			}
			domain.add(new DPoint(FI, i));
		}
		domain.add(0.0, 0.0);
		domain.smooth(5.0, 10);
		final double[] f = new double[domain.size()];
		for (int i = 0; i < domain.size(); ++i) {
			f[i] = domain.pointAt(i).x;
		}
		final ArrayList<Point> v = Maths.locateLocalExtrema(f);
		final ArrayList<Point> perceptual = new ArrayList<Point>();
		for (int i = 0; i < v.size(); ++i) {
			final Point p = v.get(i);
			if (p.y > 0) {
				perceptual.add(new Point(p.x, 1));
			}
		}
		return perceptual;
	}

	public String getProcessed(final int npradius, final int np) {
		final String str = "";
		return str;
	}

	public String getSparseVectorStringCode(final double ks, final double kl,
			final int w, int k, final int n) {
		String str = "";
		int[] indexes = null;
		double[] a = this.smooth().smooth().turningFunction();
		a = this.smooth(a);
		double[] da = new double[a.length - 1];
		for (int i = 0; i < da.length; ++i) {
			da[i] = a[i + 1] - a[i];
		}
		da = this.smooth(this.smooth(da));
		double I = 0.0;
		for (int i = 0; i < da.length; ++i) {
			I += da[i] * da[i];
		}
		I = ks * Math.sqrt(I / da.length) + kl;
		final int[] idx = this.getExtrema(da, w);
		for (int i = 0; i < idx.length; ++i) {
			if (idx[i] > 0 && da[i + 1] < I) {
				idx[i] = 0;
			} else if (idx[i] < 0 && da[i + 1] > -I) {
				idx[i] = 0;
			}
		}
		indexes = new int[this.size()];
		System.arraycopy(idx, 0, indexes, 1, idx.length);
		for (int i = 1; i < indexes.length; ++i) {
			str = str + (k + resPI(a[i - 1], n)) + ":1.0 ";
			k += n;
			str += ((indexes[i] == 1) ? (k + ":1.0 ") : "");
			++k;
			str += ((indexes[i] == -1) ? (k + ":1.0 ") : "");
			++k;
		}
		return str;
	}

	public DPoint getTopLeft() {
		return new DPoint(this.getMinX(), this.getMinY());
	}

	public DPoint getTopRight() {
		return new DPoint(this.getMaxX(), this.getMinY());
	}

	public double getVerticalFactor() {
		double ym = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			ym += this.getY(i);
		}
		ym /= this.size();
		int na;
		int nb = na = 0;
		double xa;
		double xb = xa = 0.0;
		double ya;
		double yb = ya = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			if (this.getY(i) < ym) {
				xa += this.getX(i);
				ya += this.getY(i);
				++na;
			} else {
				xb += this.getX(i);
				yb += this.getY(i);
				++nb;
			}
		}
		xa /= na;
		ya /= na;
		xb /= nb;
		yb /= nb;
		return Math.atan2(ya - yb, xa - xb)
				* Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb))
				/ Math.max(this.getWidth(), this.getHeight())
				/ 3.141592653589793;
	}

	public double getWidth() {
		return this.getMaxX() - this.getMinX();
	}

	public double getX(final int i) {
		return this.pointAt(i).x;
	}

	public double getY(final int i) {
		return this.pointAt(i).y;
	}

	public double globalCos() {
		double val = 0.0;
		val = this.last().distance(this.first());
		if (val != 0.0) {
			val = (this.last().x - this.first().x) / val;
		} else {
			val = 2.0;
		}
		return val;
	}

	public double globalSin() {
		double val = 0.0;
		val = this.last().distance(this.first());
		if (val != 0.0) {
			val = (this.last().y - this.first().y) / val;
		} else {
			val = -2.0;
		}
		return val;
	}

	@SuppressWarnings("unused")
	private double height(final int i, final int j, final int k) {
		final double a = this.area(i, j, k);
		final DPoint p1 = this.pointAt(i);
		final DPoint p2 = this.pointAt(k);
		final double dist = p1.distance(p2);
		if (dist == 0.0) {
			return this.pointAt(j).distance(p1);
		}
		return Math.abs(a) / dist;
	}

	private static boolean inInterval(final double x, final double a,
			final double b) {
		return a - DStroke.EPS < x && x < b + DStroke.EPS;
	}

	public DPoint intersection(final DPoint center, final double r,
			final DPoint init, final DPoint end) {
		DPoint inter = null;
		if (init.x == end.x) {
			final double A = 1.0;
			final double B = -2.0 * center.y;
			final double C = (init.x - center.x) * (init.x - center.x)
					+ center.y * center.y - r * r;
			double u = -0.5 * (B + sign(B) * Math.sqrt(B * B - 4.0 * A * C));
			double sol = u / A;
			if (inInterval(sol, init.y, end.y)
					|| inInterval(sol, end.y, init.y)) {
				inter = new DPoint(init.x, sol);
			}
			u = -0.5 * (B - sign(B) * Math.sqrt(B * B - 4.0 * A * C));
			sol = u / A;
			if (inInterval(sol, init.y, end.y)
					|| inInterval(sol, end.y, init.y)) {
				inter = new DPoint(init.x, sol);
			}
		} else {
			final double m = (init.y - end.y) / (init.x - end.x);
			final double b = init.y - m * init.x;
			final double A = 1.0 + m * m;
			final double B = 2.0 * (m * (b - center.y) - center.x);
			final double C = center.x * center.x + (b - center.y)
					* (b - center.y) - r * r;
			double u = -0.5 * (B + sign(B) * Math.sqrt(B * B - 4.0 * A * C));
			double sol = u / A;
			if (inInterval(sol, init.x, end.x)
					|| inInterval(sol, end.x, init.x)) {
				inter = new DPoint(sol, m * (sol - init.x) + init.y);
			}
			u = -0.5 * (B - sign(B) * Math.sqrt(B * B - 4.0 * A * C));
			sol = u / A;
			if (inInterval(sol, init.x, end.x)
					|| inInterval(sol, end.x, init.x)) {
				inter = new DPoint(sol, m * (sol - init.x) + init.y);
			}
		}
		if (inter == null || (inter.x == center.x && inter.y == center.y)) {
			final double x_ = end.x - center.x;
			final double y_ = end.y - center.y;
			final double dist = center.distance(end);
			inter = new DPoint(r * x_ / dist + center.x, r * y_ / dist
					+ center.y);
		}
		return inter;
	}

	public double intersectionX(final DStroke s) {
		double A = this.getMinX();
		double B = this.getMaxX();
		double a = s.getMinX();
		double b = s.getMaxX();
		if (A > a) {
			double aux = A;
			A = a;
			a = aux;
			aux = B;
			B = b;
			b = aux;
		}
		if (b <= B) {
			return (b - a) / (B - A);
		}
		return (B - a) / (b - A);
	}

	public double intersectionY(final DStroke s) {
		double A = this.getMinY();
		double B = this.getMaxY();
		double a = s.getMinY();
		double b = s.getMaxY();
		if (A > a) {
			double aux = A;
			A = a;
			a = aux;
			aux = B;
			B = b;
			b = aux;
		}
		if (b <= B) {
			return (b - a) / (B - A);
		}
		return (B - a) / (b - A);
	}

	public boolean intersects(final DStroke ds) {
		return this.boundigBoxIntersection(ds, Math.max(DStroke.r, DStroke.r))
				&& this.distance(ds, Math.max(DStroke.r, DStroke.r));
	}

	public boolean intersects(final DStroke ds, final double eps) {
		return this.boundigBoxIntersection(ds,
				eps * Math.max(DStroke.r, DStroke.r))
				&& this.distance(ds, eps * Math.max(DStroke.r, DStroke.r));
	}

	static boolean intervalIntersection(final double a, final double b,
			final double c, final double d, final double eps) {
		if (a < c) {
			return b + eps - c > 0.0;
		}
		return d + eps - a > 0.0;
	}

	public void join(final DStroke s) {
		for (int len = s.size(), i = 0; i < len; ++i) {
			this.add(s.pointAt(i));
		}
	}

	public DPoint last() {
		return new DPoint(this.pointAt(this.size() - 1));
	}

	public double lenghtError() {
		return Math.abs(this.length() - this.first().distance(this.last()));
	}

	public double length(final int i0, final int i1) {
		double len = 0.0;
		for (int j = i0; j < i1; ++j) {
			len += this.pointAt(j).distance(this.pointAt(j + 1));
		}
		return len;
	}

	public double length() {
		double len = 0.0;
		for (int i = 0; i < this.size() - 1; ++i) {
			len += this.pointAt(i).distance(this.pointAt(i + 1));
		}
		return len;
	}

	public double lengthPosition(final int i) {
		final double len = this.length();
		if (len > 0.0) {
			return this.length(0, i) / len;
		}
		return -1.0;
	}

	public double lengthRatio(final DStroke s) {
		final double ls = s.length();
		if (ls == 0.0) {
			return -1.0;
		}
		return this.length() / ls;
	}

	public double linearity() {
		return this.sumDistanceError();
	}

	public double localCos(final int i) {
		double dy;
		double ds;
		double dx = ds = (dy = 0.0);
		try {
			dx = this.getX(i - 1) - this.getX(i + 1);
			dy = this.getY(i - 1) - this.getY(i + 1);
			ds = Math.sqrt(dx * dx + dy * dy);
		} catch (IndexOutOfBoundsException ex) {
		}
		double val = 2.0;
		if (ds > 0.0) {
			val = dx / ds;
		}
		return val;
	}

	public double localCosCur(final int i) {
		double val = 0.0;
		if (i <= 1) {
			val = this.localCos(i) * this.localCos(i + 1) + this.localSin(i)
					* this.localSin(i + 1);
		} else if (i >= this.size() - 2) {
			val = this.localCos(i) * this.localCos(i - 1) + this.localSin(i)
					* this.localSin(i - 1);
		} else {
			val = this.localCos(i - 1) * this.localCos(i + 1)
					+ this.localSin(i - 1) * this.localSin(i + 1);
		}
		return val;
	}

	public double localSin(final int i) {
		final double dx = this.getX(i - 1) - this.getX(i + 1);
		final double dy = this.getY(i - 1) - this.getY(i + 1);
		final double ds = Math.sqrt(dx * dx + dy * dy);
		double val = 2.0;
		if (ds > 0.0) {
			val = dy / ds;
		}
		return val;
	}

	public double localSinCur(final int i) {
		double val = 0.0;
		if (i <= 1) {
			val = this.localCos(i) * this.localSin(i + 1) - this.localSin(i)
					* this.localCos(i + 1);
		} else if (i >= this.size() - 2) {
			val = this.localCos(i - 1) * this.localSin(i)
					- this.localSin(i - 1) * this.localCos(i);
		} else {
			val = this.localCos(i - 1) * this.localSin(i + 1)
					- this.localSin(i - 1) * this.localCos(i + 1);
		}
		return val;
	}

	public static void main(final String[] argv) {
		final int n = Integer.parseInt(argv[0]);
		for (double inc = 0.39269908169872414, t = 0.0 - inc; t <= 6.283185307179586 + inc; t += inc) {
			resPI(t, n);
		}
	}

	public double maxDistanceError() {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < this.size(); ++i) {
			max = Math.max(max,
					Geom.height(this.first(), this.last(), this.pointAt(i)));
		}
		return max;
	}

	public static double modPIDegrees(final double alpha) {
		double a = alpha * 180.0 / 3.141592653589793;
		a = (int) a;
		int factor = 0;
		double phase;
		for (phase = 0.0; a <= -180.0 || a > 180.0; a += phase) {
			if (a > 180.0) {
				--factor;
			} else if (a <= -180.0) {
				++factor;
			}
			phase += factor * 360;
		}
		return phase;
	}

	public static double modPIPhase(final double alpha) {
		double a = alpha;
		double phase = 0.0;
		int factor = 0;
		while (a <= -3.141592653589793 || a > 3.141592653589793) {
			if (a > 3.141592653589793) {
				--factor;
			} else if (a <= -3.141592653589793) {
				++factor;
			}
			phase = 2.0 * factor * 3.141592653589793;
			a += phase;
			factor = 0;
		}
		return a - alpha;
	}

	public static double modPIPhaseDegrees(final double alpha) {
		double a = alpha * 180.0 / 3.141592653589793;
		a = (int) a;
		int factor = 0;
		double phase;
		for (phase = 0.0; a <= -180.0 || a > 180.0; a += phase) {
			if (a > 180.0) {
				--factor;
			} else if (a <= -180.0) {
				++factor;
			}
			phase += factor * 360;
		}
		return phase * 3.141592653589793 / 180.0;
	}

	public void normalize() {
		if (this.length() == 0.0) {
			for (int i = 0; i < this.size(); ++i) {
				this.pointAt(i).set(0.0, 0.0);
			}
			return;
		}
		final double minx = this.getMinX();
		final double diffx = minx - this.getMaxX();
		final double miny = this.getMinY();
		final double diffy = miny - this.getMaxY();
		if (diffx < diffy) {
			for (int j = 0; j < this.size(); ++j) {
				final double x = (minx - this.pointAt(j).x) / diffx;
				final double y = (miny - this.pointAt(j).y) / diffx;
				this.pointAt(j).set(x, y);
			}
		} else {
			for (int j = 0; j < this.size(); ++j) {
				final double x = (minx - this.pointAt(j).x) / diffy;
				final double y = (miny - this.pointAt(j).y) / diffy;
				this.pointAt(j).set(x, y);
			}
		}
	}

	public void normalizeDirection() {
		if (this.size() == 0) {
			return;
		}
		final double D = this.getTopLeft().distance(this.getLowerRight());
		if (D == 0.0) {
			return;
		}
		final DPoint pf = this.first();
		final DPoint pl = this.last();
		final double Rx = Math.abs(pf.x - pl.x) / D;
		final double Ry = Math.abs(pf.y - pl.y) / D;
		if (Rx >= DStroke.DIRECTION_PARMETER && Ry < DStroke.DIRECTION_PARMETER) {
			if (pf.x > pl.x) {
				this.reverse();
			}
		} else if (Rx < DStroke.DIRECTION_PARMETER
				&& Ry >= DStroke.DIRECTION_PARMETER) {
			if (pf.y > pl.y) {
				this.reverse();
			}
		} else if (Rx >= DStroke.DIRECTION_PARMETER
				&& Ry >= DStroke.DIRECTION_PARMETER) {
			if (pf.y > pl.y) {
				this.reverse();
			}
		}
	}

	public void normalizeRespectToX(final DStroke s) {
		final double minx = s.getMinX();
		final double diffx = minx - s.getMaxX();
		final double miny = s.getMinY();
		final double diffy = miny - s.getMaxY();
		if (diffx == 0.0) {
			if (diffy == 0.0) {
				for (int i = 0; i < this.size(); ++i) {
					this.pointAt(i).set(0.0, 0.0);
				}
			} else {
				for (int i = 0; i < this.size(); ++i) {
					final double y = (miny - this.pointAt(i).y) / diffy;
					this.pointAt(i).set(0.0, y);
				}
			}
		} else {
			final int len = this.size();
			final double ratio = diffy / diffx;
			for (int j = 0; j < len; ++j) {
				final double x = (minx - this.pointAt(j).x) / diffx;
				final double y2 = ratio * (miny - this.pointAt(j).y) / diffy;
				this.pointAt(j).set(x, y2);
			}
		}
	}

	public void normalizeRespectToX() {
		int len = this.size();
		final double minx = this.getMinX();
		final double diffx = minx - this.getMaxX();
		final double miny = this.getMinY();
		final double diffy = miny - this.getMaxY();
		if (diffx == 0.0) {
			if (diffy == 0.0) {
				for (int i = 0; i < len; ++i) {
					this.pointAt(i).set(0.0, 0.0);
				}
			} else {
				for (int i = 0; i < len; ++i) {
					final double y = (miny - this.pointAt(i).y) / diffy;
					this.pointAt(i).set(0.0, y);
				}
			}
		} else {
			len = this.size();
			final double ratio = diffy / diffx;
			for (int j = 0; j < len; ++j) {
				final double x = (minx - this.pointAt(j).x) / diffx;
				final double y2 = ratio * (miny - this.pointAt(j).y) / diffy;
				this.pointAt(j).set(x, y2);
			}
		}
	}

	public void normalizeRespectToY(final DStroke s) {
		final double minx = s.getMinX();
		final double diffx = minx - s.getMaxX();
		final double miny = s.getMinY();
		final double diffy = miny - s.getMaxY();
		if (diffy == 0.0) {
			if (diffx == 0.0) {
				for (int i = 0; i < this.size(); ++i) {
					this.pointAt(i).set(0.0, 0.0);
				}
			} else {
				for (int i = 0; i < this.size(); ++i) {
					final double x = (minx - this.pointAt(i).x) / diffx;
					this.pointAt(i).set(x, 0.0);
				}
			}
		} else {
			final int len = this.size();
			final double ratio = diffx / diffy;
			for (int j = 0; j < len; ++j) {
				final double x2 = ratio * (minx - this.pointAt(j).x) / diffx;
				final double y = (miny - this.pointAt(j).y) / diffy;
				this.pointAt(j).set(x2, y);
			}
		}
	}

	public void normalizeRespectToY() {
		final double minx = this.getMinX();
		final double diffx = minx - this.getMaxX();
		final double miny = this.getMinY();
		final double diffy = miny - this.getMaxY();
		if (diffy == 0.0) {
			if (diffx == 0.0) {
				for (int i = 0; i < this.size(); ++i) {
					this.pointAt(i).set(0.0, 0.0);
				}
			} else {
				for (int i = 0; i < this.size(); ++i) {
					final double x = (minx - this.pointAt(i).x) / diffx;
					this.pointAt(i).set(x, 0.0);
				}
			}
		} else {
			final int len = this.size();
			final double ratio = diffx / diffy;
			for (int j = 0; j < len; ++j) {
				final double x2 = ratio * (minx - this.pointAt(j).x) / diffx;
				final double y = (miny - this.pointAt(j).y) / diffy;
				this.pointAt(j).set(x2, y);
			}
		}
	}

	public void normalizeXRespectTo(final double a, final double b,
			final double c, final double d) {
		for (int i = 0; i < this.size(); ++i) {
			this.pointAt(i).x = (d - c) * (this.pointAt(i).x - a) / (b - a) + c;
		}
	}

	public void normalizeYRespectTo(final double a, final double b,
			final double c, final double d) {
		for (int i = 0; i < this.size(); ++i) {
			this.pointAt(i).y = (d - c) * (this.pointAt(i).y - a) / (b - a) + c;
		}
	}

	public double normalizedAccumulatedAngle() {
		double a = 0.0;
		for (int i = 0; i < this.size() - 2; ++i) {
			final DPoint p = this.pointAt(i).to(this.pointAt(i + 1));
			final DPoint q = this.pointAt(i + 1).to(this.pointAt(i + 2));
			a += p.dot(q) / (p.norm1() * q.norm1());
		}
		if (this.size() > 2) {
			a /= this.size() - 2.0;
		}
		return a;
	}

	public int numberOfStrokeDescriptors() {
		double r = Math.max(this.getHeight(), this.getWidth()) / 20.0;
		final DStroke ds = this.equidistant(r);
		final double[] angle = new double[ds.size() - 2];
		r *= r;
		for (int i = 1; i < ds.size() - 1; ++i) {
			angle[i - 1] = Math.acos((ds.getX(i) - ds.getX(i - 1))
					* (ds.getX(i + 1) - ds.getX(i)) / r
					+ (ds.getY(i) - ds.getY(i - 1))
					* (ds.getY(i + 1) - ds.getY(i)) / r);
		}
		// double acc_angle = 0.0;
		int n = 0;
		for (int i = 0; i < ds.size() - 2; ++i) {
			// acc_angle += angle[i];
			if (angle[i] > 2.356194490192345) {
				++n;
			}
		}
		return n;
	}

	public DPoint pointAt(final int i) {
		return this.get(i);
	}

	public DStroke proyectionPolygonal(final int points,
			final ArrayList<Integer> initial) {
		final DStroke ds = new DStroke(this);
		// final int len = this.size();
		final ArrayList<Integer> taked = new ArrayList<Integer>(initial);
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = DPoint.height(ds.pointAt(init),
							ds.pointAt(end), ds.pointAt(j));
					if (dist < aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final DStroke poly = new DStroke();
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			poly.add(new DPoint(ds.pointAt(init)));
		}
		return poly;
	}

	public DStroke proyectionPolygonal(final int points) {
		final DStroke ds = new DStroke(this);
		final int len = this.size();
		if (len == points || len == 1) {
			return ds;
		}
		if (len < points) {
			return this.addPoints(points);
		}
		final ArrayList<Integer> taked = new ArrayList<Integer>();
		// final ArrayList<Integer> optimal = new ArrayList<Integer>();
		taked.add(new Integer(0));
		taked.add(new Integer(len - 1));
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = DPoint.height(ds.pointAt(init),
							ds.pointAt(end), ds.pointAt(j));
					if (dist <= aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final DStroke poly = new DStroke();
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			poly.add(new DPoint(ds.pointAt(init)));
		}
		return poly;
	}

	public DStroke proyectionPolygonalDistance(final int points) {
		final int len = this.size();
		final ArrayList<Integer> taked = new ArrayList<Integer>();
		taked.add(new Integer(0));
		taked.add(new Integer(len - 1));
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.POSITIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = this.strokeAt(init, j)
							.sumDistanceError()
							+ this.strokeAt(j, end).sumDistanceError();
					if (dist > aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final DStroke poly = new DStroke();
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			poly.add(new DPoint(this.pointAt(init)));
		}
		return poly;
	}

	public int[] proyectionPolygonalIndexes(final int points) {
		final int len = this.size();
		final ArrayList<Integer> taked = new ArrayList<Integer>();
		taked.add(new Integer(0));
		taked.add(new Integer(len - 1));
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = Geom.height(this.pointAt(init),
							this.pointAt(end), this.pointAt(j));
					if (dist < aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final int[] indexes = new int[taked.size()];
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			indexes[i] = init;
		}
		return indexes;
	}

	public int[] proyectionPolygonalIndexes(final int points,
			final ArrayList<Integer> initial) {
		// final int len = this.size();
		final ArrayList<Integer> taked = new ArrayList<Integer>(initial);
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = Geom.height(this.pointAt(init),
							this.pointAt(end), this.pointAt(j));
					if (dist < aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final int[] indexes = new int[taked.size()];
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			indexes[i] = init;
		}
		return indexes;
	}

	public DStroke proyectionPolygonalLength(final int points) {
		final int len = this.size();
		final ArrayList<Integer> taked = new ArrayList<Integer>();
		taked.add(new Integer(0));
		taked.add(new Integer(len - 1));
		while (taked.size() < points) {
			int i_taked = -1;
			double dist = Double.POSITIVE_INFINITY;
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				for (int end = taked.get(i + 1), j = init + 1; j < end; ++j) {
					final double aux = Math.abs(this.pointAt(init).distance(
							this.pointAt(j))
							- this.strokeAt(init, j).length())
							+ Math.abs(this.pointAt(j).distance(
									this.pointAt(end))
									- this.strokeAt(j, end).length());
					if (dist > aux) {
						dist = aux;
						i_taked = j;
					}
				}
			}
			for (int i = 0; i < taked.size() - 1; ++i) {
				final int init = taked.get(i);
				final int end = taked.get(i + 1);
				if (init <= i_taked && i_taked <= end) {
					taked.add(i + 1, new Integer(i_taked));
					break;
				}
			}
		}
		final DStroke poly = new DStroke();
		for (int i = 0; i < taked.size(); ++i) {
			final int init = taked.get(i);
			poly.add(new DPoint(this.pointAt(init)));
		}
		return poly;
	}

	public double proyectionX(final DStroke s) {
		final double A = this.getMinX();
		final double B = this.getMaxX();
		final double a = s.getMinX();
		final double b = s.getMaxX();
		if (A < B) {
			if (a < A) {
				if (b <= B) {
					return (b - A) / (B - A);
				}
				return 1.0;
			} else {
				if (a >= B) {
					return (B - a) / (B - A);
				}
				if (b <= B) {
					return (b - a) / (B - A);
				}
				return (B - a) / (B - A);
			}
		} else if (a < b) {
			if (A < a) {
				return (A - a) / (b - a);
			}
			if (A <= b) {
				return 1.0;
			}
			return (b - A) / (b - a);
		} else {
			if (A == a) {
				return 1.0;
			}
			return -1.0;
		}
	}

	public double proyectionY(final DStroke s) {
		final double A = this.getMinY();
		final double B = this.getMaxY();
		final double a = s.getMinY();
		final double b = s.getMaxY();
		if (A < B) {
			if (a < A) {
				if (b <= B) {
					return (b - A) / (B - A);
				}
				return 1.0;
			} else {
				if (a >= B) {
					return (B - a) / (B - A);
				}
				if (b <= B) {
					return (b - a) / (B - A);
				}
				return (B - a) / (B - A);
			}
		} else if (a < b) {
			if (A < a) {
				return (A - a) / (b - a);
			}
			if (A <= b) {
				return 1.0;
			}
			return (b - A) / (b - a);
		} else {
			if (A == a) {
				return 1.0;
			}
			return -1.0;
		}
	}

	public void read(final BufferedReader filein) throws IOException {
		this.clear();
		final String line;
		if ((line = filein.readLine()) == null) {
			throw new EOFException();
		}
		final StringTokenizer st = new StringTokenizer(line, ",: \n\t\r\f");
		final int size = atoi(st.nextToken());
		if (size == 0) {
			throw new IOException("Epmty line...");
		}
		this.first_time = atol(st.nextToken());
		for (int i = 0; i < size; ++i) {
			final DPoint p = new DPoint();
			p.t = atol(st.nextToken());
			p.x = atof(st.nextToken());
			p.y = atof(st.nextToken());
			this.add(p);
		}
	}

	public double relativeFirstLastPointDistance() {
		final double d = this.getTopLeft().distance(this.getLowerRight());
		if (d != 0.0) {
			return this.first().distance(this.last()) / d;
		}
		return -1.0;
	}

	public double relativeLengthFirstLastPointDistance() {
		final double len = this.length();
		if (len != 0.0) {
			return this.first().distance(this.last()) / len;
		}
		return -1.0;
	}

	@Override
	public void clear() {
		super.clear();
		this.minX = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
	}

	public static int resPI(final double t, final int n) {
		int m;
		for (m = (int) Math.floor(n * t / 6.283185307179586 + 0.5); m < 0; m += n) {
		}
		return m % n;
	}

	public void reverse() {
		for (int size = this.size(), i = 1; i < size; ++i) {
			final DPoint p = new DPoint(this.pointAt(size - (i + 1)));
			this.remove(size - (i + 1));
			this.add(p);
		}
	}

	public void set(final int i, final double x, final double y) {
		this.pointAt(i).set(x, y);
	}

	@SuppressWarnings("unused")
	private int sgn(final double x) {
		if (x > 0.0) {
			return 1;
		}
		if (x < 0.0) {
			return -1;
		}
		return 0;
	}

	private static int sign(final double x) {
		if (x >= 0.0) {
			return 1;
		}
		return -1;
	}

	public double sinAt(final int i) {
		double dy;
		double ds;
		double dx = ds = (dy = 0.0);
		try {
			dx = this.getX(i + 1) - this.getX(i);
			dy = this.getY(i + 1) - this.getY(i);
			ds = Math.sqrt(dx * dx + dy * dy);
		} catch (IndexOutOfBoundsException ex) {
		}
		double val = -2.0;
		if (ds > 0.0) {
			val = dy / ds;
		}
		return val;
	}

	public StepFunction sinMap() {
		this.filter();
		final StepFunction f = new StepFunction(this.size() - 1);
		final double len = this.length();
		int j = 0;
		int index = 0;
		double pos = 0.0;
		while (j < this.size() - 1) {
			final double dist = this.pointAt(j + 1).distance(this.pointAt(j));
			pos += dist;
			f.index[index] = pos / len;
			f.value[index++] = (this.pointAt(j + 1).y - this.pointAt(j).y)
					/ dist;
			++j;
		}
		return f;
	}

	double[] smooth(final double[] a) {
		final double[] sa = new double[a.length];
		sa[0] = a[0];
		int i;
		for (i = 1; i < sa.length - 1; ++i) {
			sa[i] = 0.25 * a[i - 1] + 0.5 * a[i] + 0.25 * a[i + 1];
		}
		sa[i] = a[i];
		return sa;
	}

	public DStroke smooth() {
		final DStroke ds = new DStroke(this);
		for (int i = 2; i < ds.size() - 2; ++i) {
			double dx = this.getX(i - 2) + 4.0 * this.getX(i - 1) + 6.0
					* this.getX(i) + 4.0 * this.getX(i + 1) + this.getX(i + 2);
			dx /= 16.0;
			double dy = this.getY(i - 2) + 4.0 * this.getY(i - 1) + 6.0
					* this.getY(i) + 4.0 * this.getY(i + 1) + this.getY(i + 2);
			dy /= 16.0;
			ds.set(i, dx, dy);
		}
		return ds;
	}

	public double[] smooth(final double[] alpha, final double sigma,
			final int size) {
		final double[][] ker = new double[size + 1][];
		for (int i = 0; i <= size; ++i) {
			double sum = 0.0;
			ker[i] = new double[2 * i + 1];
			for (int j = -i; j <= i; ++j) {
				final double n = sum;
				final double[] array = ker[i];
				final int n2 = j + i;
				final double exp = Math.exp(-sigma * sigma * j * j);
				array[n2] = exp;
				sum = n + exp;
			}
			for (int j = -i; j <= i; ++j) {
				final double[] array2 = ker[i];
				final int n3 = j + i;
				array2[n3] /= sum;
			}
		}
		final int len = Math.min(alpha.length / 2, size);
		final double[] f = new double[alpha.length];
		for (int i = 0; i < len; ++i) {
			double sumx;
			// final double sumy =
			sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				sumx += ker[i][j + i] * alpha[j + i];
			}
			f[i] = sumx;
		}
		for (int i = size; i < this.size() - size; ++i) {
			double sumx;
			// final double sumy =
			sumx = 0.0;
			for (int j = -size; j <= size; ++j) {
				sumx += ker[i][j + i] * alpha[j + i];
			}
			f[i] = sumx;
		}
		for (int i = len - 1; i >= 0; --i) {
			double sumx;
			// final double sumy = 0.0;
			sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				sumx += ker[i][j + i] * alpha[alpha.length - 1 - i + j];
			}
			f[i] = sumx;
		}
		return f;
	}

	public DStroke smooth(final double sigma, final int size) {
		final double[][] ker = new double[size + 1][];
		for (int i = 0; i <= size; ++i) {
			double sum = 0.0;
			ker[i] = new double[2 * i + 1];
			for (int j = -i; j <= i; ++j) {
				final double n = sum;
				final double[] array = ker[i];
				final int n2 = j + i;
				final double exp = Math.exp(-sigma * sigma * j * j);
				array[n2] = exp;
				sum = n + exp;
			}
			for (int j = -i; j <= i; ++j) {
				final double[] array2 = ker[i];
				final int n3 = j + i;
				array2[n3] /= sum;
			}
		}
		final int len = Math.min(this.size() / 2, size);
		final DStroke f = new DStroke();
		for (int i = 0; i < len; ++i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				sumx += ker[i][j + i] * this.pointAt(j + i).x;
				sumy += ker[i][j + i] * this.pointAt(j + i).y;
			}
			f.add(new DPoint(sumx, sumy));
		}
		for (int i = size; i < this.size() - size; ++i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -size; j <= size; ++j) {
				sumx += ker[size][size + j] * this.pointAt(j + i).x;
				sumy += ker[size][size + j] * this.pointAt(j + i).y;
			}
			f.add(new DPoint(sumx, sumy));
		}
		for (int i = len - 1; i >= 0; --i) {
			double sumx;
			double sumy = sumx = 0.0;
			for (int j = -i; j <= i; ++j) {
				sumx += ker[i][j + i] * this.pointAt(this.size() - 1 - i + j).x;
				sumy += ker[i][j + i] * this.pointAt(this.size() - 1 - i + j).y;
			}
			f.add(new DPoint(sumx, sumy));
		}
		return f;
	}

	public DStroke strokeAt(final int i, final int j) {
		final DStroke ds = new DStroke();
		if (i < j) {
			for (int k = i; k <= j; ++k) {
				try {
					ds.add(new DPoint(this.pointAt(k)));
				} catch (IndexOutOfBoundsException e) {
				}
			}
		} else {
			for (int k = j; k <= i; ++k) {
				try {
					ds.add(new DPoint(this.pointAt(k)));
				} catch (IndexOutOfBoundsException e) {
				}
			}
		}
		return ds;
	}

	public double sumDistanceError() {
		double sum = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			sum += Geom.height(this.first(), this.last(), this.pointAt(i));
		}
		return sum / this.size();
	}

	public double sumDistanceErrorRelative() {
		double sum = 0.0;
		for (int i = 0; i < this.size(); ++i) {
			sum += Geom.height(this.first(), this.last(), this.pointAt(i));
		}
		final double m = Math.max(this.getWidth(), this.getHeight());
		if (m != 0.0) {
			return sum / (this.size() * m);
		}
		return -1.0;
	}

	public void tangentTansformation(final double alpha) {
		final DStroke tangent = new DStroke(this.size());
		double factor = alpha * Math.random();
		for (int i = 0; i < this.size(); ++i) {
			tangent.set(i, tangent.getX(i) - factor * tangent.getX(i),
					tangent.getY(i) + factor * tangent.getY(i));
		}
		factor = alpha * Math.random();
		for (int i = 0; i < this.size(); ++i) {
			tangent.set(i, tangent.getX(i) - factor * tangent.getY(i),
					tangent.getY(i) + factor * tangent.getX(i));
		}
		factor = alpha * Math.random();
		for (int i = 0; i < this.size(); ++i) {
			tangent.set(i, tangent.getX(i) + factor * tangent.getY(i),
					tangent.getY(i) + factor * tangent.getX(i));
		}
		factor = alpha * Math.random();
		for (int i = 1; i < this.size() - 1; ++i) {
			tangent.set(i, tangent.getX(i) + factor
					* (tangent.getX(i + 1) - tangent.getX(i)), tangent.getY(i)
					+ factor * (tangent.getY(i + 1) - tangent.getY(i)));
		}
		for (int i = 0; i < this.size() - 1; ++i) {
			this.set(i, this.getX(i) + tangent.getX(i),
					this.getY(i) + tangent.getY(i));
		}
	}

	public StringBuffer toStringBuffer() {
		final StringBuffer strb = new StringBuffer();
		final int len = this.size();
		if (DStroke.SIZE) {
			strb.append(len + " ");
		}
		if (DStroke.TIME) {
			strb.append(this.first_time + " ");
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				strb.append(p.t + " " + p.x + " " + p.y + " ");
			}
		} else {
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				strb.append(p.x + " " + p.y + " ");
			}
		}
		strb.append("\n");
		return strb;
	}

	public String toTrainArrayList() {
		String s = "";
		for (int i = 0; i < this.size(); ++i) {
			s = s + "" + this.pointAt(i).x + " " + this.pointAt(i).y + " ";
		}
		return s;
	}

	public void translate(final double deltaX, final double deltaY) {
		for (int i = 0; i < this.size(); ++i) {
			this.set(i, this.pointAt(i).x + deltaX, this.pointAt(i).y + deltaY);
		}
		this.minX += deltaX;
		this.maxX += deltaX;
		this.minY += deltaY;
		this.maxY += deltaY;
	}

	public double turningAngle(final int i) {
		return modPIPhase(this.pointAt(i).angle(this.pointAt(i + 1))
				- this.pointAt(i - 1).angle(this.pointAt(i)));
	}

	public double turningAngleGrad(final int i) {
		return 180.0 * modPIPhase(i) / 3.141592653589793;
	}

	public double[] turningFunction() {
		final double[] theta = new double[this.size() - 1];
		for (int i = 0; i < theta.length; ++i) {
			theta[i] = this.pointAt(i).angle(this.pointAt(i + 1));
		}
		for (int j = 1; j < theta.length; ++j) {
			final double[] array = theta;
			final int n = j;
			array[n] += modPIPhase(theta[j] - theta[j - 1]);
		}
		return theta;
	}

	public void write(final DataOutputStream fileout, final String label)
			throws IOException {
		final int len = this.size();
		if (DStroke.SIZE) {
			fileout.writeBytes(len + " ");
		}
		if (DStroke.TIME) {
			fileout.writeBytes(this.first_time + " ");
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				fileout.writeBytes(p.t + " " + p.x + " " + p.y + " ");
			}
		} else {
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				fileout.writeBytes(p.x + " " + p.y + " ");
			}
		}
		fileout.writeBytes(label + "\n");
	}

	public void write(final DataOutputStream fileout) throws IOException {
		final int len = this.size();
		if (DStroke.SIZE) {
			fileout.writeBytes(len + " ");
		}
		if (DStroke.TIME) {
			fileout.writeBytes(this.first_time + " ");
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				fileout.writeBytes(p.t + " " + p.x + " " + p.y + " ");
			}
		} else {
			for (int i = 0; i < len; ++i) {
				final DPoint p = this.pointAt(i);
				fileout.writeBytes(p.x + " " + p.y + " ");
			}
		}
		fileout.writeBytes("\n");
	}

	public static void writeSize(final boolean b) {
		DStroke.SIZE = b;
	}

	public static void writeTime(final boolean b) {
		DStroke.TIME = b;
	}
}

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
/**
 * \uFFFDberschrift: <p>
 * Beschreibung: <p>
 * Copyright: Copyright (c) Ernesto Tapia<p>
 * Organisation: FU Berlin<p>
 * @author Ernesto Tapia
 * @version 1.0
 */
package math;

import java.awt.*;
import java.util.*;
import ocr.*;

public class Maths {
	public static double area2(DPoint a, DPoint b, DPoint c) {
		return (a.x * b.y - a.y * b.x + a.y * c.x - a.x * c.y + b.x * c.y - b.y
				* c.x);
	}

	public static boolean left(DPoint a, DPoint b, DPoint c) {
		return area2(a, b, c) > 0.0;
	}

	public static boolean leftOn(DPoint a, DPoint b, DPoint c) {
		return area2(a, b, c) >= 0.0;
	}

	public static boolean right(DPoint a, DPoint b, DPoint c) {
		return area2(a, b, c) < 0.0;
	}

	public static boolean rightOn(DPoint a, DPoint b, DPoint c) {
		return area2(a, b, c) <= 0.0;
	}

	public static boolean collinear(DPoint a, DPoint b, DPoint c) {
		return area2(a, b, c) == 0.0;
	}

	public static boolean intersectProp(DPoint a, DPoint b, DPoint c, DPoint d) {
		return area2(a, b, c) * area2(a, b, d) < 0
				&& area2(c, d, a) * area2(c, d, b) < 0;
	}

	public static boolean between(DPoint a, DPoint b, DPoint c) {
		if (!collinear(a, b, c))
			return false;

		if (a.x != b.x)
			return (a.x <= c.x && c.x <= b.x) || (b.x <= c.x && c.x <= a.x);
		else
			return (a.y <= c.y && c.y <= b.y) || (b.y <= c.y && c.y <= a.y);
	}

	public static boolean intersect(DPoint a, DPoint b, DPoint c, DPoint d) {
		if (intersectProp(a, b, c, d))
			return true;
		else if (between(a, b, c) || between(a, b, d) || between(c, d, a)
				|| between(c, d, b))
			return true;
		else
			return false;
	}

	public static double height(DPoint a, DPoint b, DPoint c) {
		if ((a.x == b.x) && (a.y == b.y))
			return a.distance(c);

		double area = Math.abs(area2(a, b, c));
		return (area / a.distance(b));
	}

	public static double distance(DPoint a, DPoint b, DPoint c) {
		// double dist;
		DPoint a1;
		DPoint b1;

		a1 = new DPoint(a.y - b.y + a.x, b.x - a.x + a.y);
		b1 = new DPoint(a.y - b.y + b.x, b.x - a.x + b.y);

		if (right(a, a1, c) && left(b, b1, c))
			return height(a, b, c);
		else if (leftOn(a, a1, c))
			return a.distance(c);
		else
			return b.distance(c);
	}

	public static double distance(DPoint a, DPoint b, DPoint c, DPoint d) {
		if (intersect(a, b, c, d))
			return 0.0;

		return Math.min(Math.min(distance(a, b, c), distance(a, b, d)),
				Math.min(distance(c, d, a), distance(c, d, b)));
	}

	public static double distance(DStroke s1, DStroke s2) {
		double dist = Double.POSITIVE_INFINITY;
		double size1, size2;
		int i, j;

		size1 = s1.size();
		size2 = s2.size();

		if (size1 == 1 && size2 == 1) {
			return s1.pointAt(0).distance(s2.pointAt(0));
		} else if (size1 == 1 && size2 > 1) {
			for (j = 0; j < size2 - 1; j++) {
				dist = Math.min(
						dist,
						distance(s2.pointAt(j), s2.pointAt(j + 1),
								s1.pointAt(0)));
				if (dist == 0.0)
					return 0.0;
			}
		} else if (size1 > 1 && size2 == 1) {
			for (j = 0; j < size1 - 1; j++) {
				dist = Math.min(
						dist,
						distance(s1.pointAt(j), s1.pointAt(j + 1),
								s2.pointAt(0)));
				if (dist == 0.0)
					return 0.0;
			}
		} else {
			for (i = 0; i < size1 - 1; i++) {
				for (j = 0; j < size2 - 1; j++) {
					dist = Math.min(
							dist,
							distance(s1.pointAt(i), s1.pointAt(i + 1),
									s2.pointAt(j), s2.pointAt(j + 1)));
					if (dist == 0.0)
						return 0.0;
				}
			}
		}

		return dist;
	}

	public static double[] rootsCuadratic(double a, double b, double c) {
		double[] x = new double[2];
		double q;

		q = -0.5 * (b + sign(b) * Math.sqrt(b * b - 4.0 * a * c));

		x[0] = q / a;
		x[1] = c / q;

		return x;
	}

	public static double sign(double x) {
		if (x > 0.0) {
			return 1.0;
		} else if (x < 0.0) {
			return -1.0;
		}

		return 0.0;
	}

	public static double modPI(double alpha) {
		double a;
		int factor;

		a = alpha;
		factor = 0;
		while (a <= -Math.PI || a > Math.PI) {
			if (a > Math.PI) {
				factor--;
			} else if (a <= -Math.PI) {
				factor++;
			}

			a += 2.0 * factor * Math.PI;
		}

		return a;
	}

	public static double modPIPhase(double alpha) {
		double a, phase;
		int factor;

		a = alpha;
		factor = 0;
		phase = 0;
		while (a <= -Math.PI || a > Math.PI) {
			if (a > Math.PI) {
				factor--;
			} else if (a <= -Math.PI) {
				factor++;
			}

			phase += 2.0 * factor * Math.PI;
			a += phase;
		}

		return phase;
	}

	public static double tanh(double x) {
		double exp = Math.exp(x);

		return (exp * exp - 1.0) / (exp * exp + 1.0);
	}

	public static boolean distance(DStroke s1, DStroke s2, double eps) {
		double dist = Double.POSITIVE_INFINITY;
		double size1, size2;
		int i, j;

		size1 = s1.size();
		size2 = s2.size();

		if (size1 == 1 && size2 == 1) {
			if (s1.pointAt(0).distance(s2.pointAt(0)) <= eps)
				return true;
		} else if (size1 == 1 && size2 > 1) {
			for (j = 0; j < size2 - 1; j++) {
				dist = Math.min(
						dist,
						distance(s2.pointAt(j), s2.pointAt(j + 1),
								s1.pointAt(0)));
				if (dist <= eps)
					return true;
			}
		} else if (size1 > 1 && size2 == 1) {
			for (j = 0; j < size1 - 1; j++) {
				dist = Math.min(
						dist,
						distance(s1.pointAt(j), s1.pointAt(j + 1),
								s2.pointAt(0)));
				if (dist <= eps)
					return true;
			}
		} else {
			for (i = 0; i < size1 - 1; i++) {
				for (j = 0; j < size2 - 1; j++) {
					dist = Math.min(
							dist,
							distance(s1.pointAt(i), s1.pointAt(i + 1),
									s2.pointAt(j), s2.pointAt(j + 1)));
					if (dist <= eps)
						return true;
				}
			}
		}

		return false;
	}

	public static ArrayList<Point> locateLocalExtrema(double[] f, int indexf,
			int indexl) {
		ArrayList<Point> extrema;
		double old;
		double current;
		double val;
		// int pnnp;
		int nopo;
		int i;

		extrema = new ArrayList<Point>();
		// extrema.add(new Point(0,0));

		old = f[indexf + 1] - f[indexf];
		nopo = -1;
		for (i = indexf + 1; i < indexl - 1; i++) {
			current = f[i + 1] - f[i];

			if (old > 0.0) {
				if (current < 0.0) {
					extrema.add(new Point(i, 1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current > 0.0 do nothing
			}

			else if (old < 0.0) {
				if (current > 0.0) {
					extrema.add(new Point(i, -1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current < 0.0 do nothing
			}

			else { // old == 0.0
				try {
					val = f[nopo + 1] - f[nopo];
				} catch (ArrayIndexOutOfBoundsException e1) {
					old = current;
					continue; // nopo = -1;
				}

				if (current > 0.0) {
					if (val > 0.0) {
						nopo = -1;
					} else if (val < 0.0) {
						extrema.add(new Point((nopo + i) / 2, -2));
					}
				} else if (current < 0.0) {
					if (val > 0.0) {
						extrema.add(new Point((nopo + i) / 2, 2));
					} else if (val < 0.0) {
						nopo = -1;
					}
				}
				// current == 0.0 do nothing
			}

			old = current;
		}

		// extrema.add(new Point(f.length-1,0));

		return extrema;
	}

	public static ArrayList<Point> locateLocalExtrema(double[] f) {
		// return locateLocalExtrema(f,0,f.length);

		ArrayList<Point> extrema;
		double old;
		double current;
		double val;
		// int pnnp;
		int nopo;
		int i;

		extrema = new ArrayList<Point>();
		// extrema.add(new Point(0,0));

		old = f[1] - f[0];
		nopo = -1;
		for (i = 1; i < f.length - 1; i++) {
			current = f[i + 1] - f[i];

			if (old > 0.0) {
				if (current < 0.0) {
					extrema.add(new Point(i, 1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current > 0.0 do nothing
			}

			else if (old < 0.0) {
				if (current > 0.0) {
					extrema.add(new Point(i, -1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current < 0.0 do nothing
			}

			else { // old == 0.0
				try {
					val = f[nopo + 1] - f[nopo];
				} catch (ArrayIndexOutOfBoundsException e1) {
					old = current;
					continue; // nopo = -1;
				}

				if (current > 0.0) {
					if (val > 0.0) {
						nopo = -1;
					} else if (val < 0.0) {
						extrema.add(new Point((nopo + i) / 2, -2));
					}
				} else if (current < 0.0) {
					if (val > 0.0) {
						extrema.add(new Point((nopo + i) / 2, 2));
					} else if (val < 0.0) {
						nopo = -1;
					}
				}
				// current == 0.0 do nothing
			}

			old = current;
		}

		// extrema.add(new Point(f.length-1,0));

		return extrema;

	}

	public static ArrayList<Point> locateLocalExtremaInflexion(double[] f) {
		ArrayList<Point> extrema;
		double old;
		double current;
		double val;
		// int pnnp;
		int nopo;
		int i;

		extrema = new ArrayList<Point>();
		// extrema.add(new Point(0,0));

		old = f[1] - f[0];
		nopo = -1;
		for (i = 1; i < f.length - 1; i++) {
			current = f[i + 1] - f[i];

			if (old > 0.0) {
				if (current < 0.0) {
					extrema.add(new Point(i, 1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current > 0.0 do nothing
			}

			else if (old < 0.0) {
				if (current > 0.0) {
					extrema.add(new Point(i, -1));
				} else if (current == 0.0) {
					nopo = i - 1;
				}
				// current < 0.0 do nothing
			}

			else { // old == 0.0
				try {
					val = f[nopo + 1] - f[nopo];
				} catch (ArrayIndexOutOfBoundsException e1) {
					old = current;
					continue; // nopo = -1;
				}

				if (current > 0.0) {
					if (val > 0.0) {
						nopo = -1;
					} else if (val < 0.0) {
						extrema.add(new Point((nopo + i) / 2, -2));
					}
				} else if (current < 0.0) {
					if (val > 0.0) {
						extrema.add(new Point((nopo + i) / 2, 2));
					} else if (val < 0.0) {
						nopo = -1;
					}
				}
				// current == 0.0 do nothing
			}

			old = current;
		}

		// extrema.add(new Point(f.length-1,0));

		return extrema;
	}

	public static ArrayList<Point> locateLocalExtrema(double[] f, double factor) {
		ArrayList<Point> extrema;
		double[] diff;
		double old;
		double current;
		double val;
		double eps;
		// int pnnp;
		int nopo;
		int i;

		diff = new double[f.length - 1];
		val = 0.0;
		for (i = 0; i < diff.length; i++) {
			diff[i] = f[i + 1] - f[i];
			val += diff[i] * diff[i];
		}

		val = Math.sqrt(val / diff.length);

		eps = factor * val;

		System.out.println("val: " + val + ", eps: " + eps);

		extrema = new ArrayList<Point>();
		// extrema.add(new Point(0,0));

		old = diff[0];
		nopo = -1;
		for (i = 1; i < diff.length; i++) {
			current = diff[i];

			if (old > eps) {
				if (current < -eps) {
					extrema.add(new Point(i, 1));
				} else if (current >= -eps && current <= eps) {
					nopo = i;
				}
				// current > 0.0 do nothing
			}

			else if (old < -eps) {
				if (current > eps) {
					extrema.add(new Point(i, -1));
				} else if (current >= -eps && current <= eps) {
					nopo = i;
				}
				// current < 0.0 do nothing
			}

			else { // old == 0.0
				try {
					val = diff[nopo - 1]; // f[nopo] - f[nopo - 1]
				} catch (ArrayIndexOutOfBoundsException e1) {
					old = current;
					continue; // nopo = -1;
				}

				if (current > eps) {
					if (val > eps) {
						nopo = -1;
					}
					if (val < eps) {
						extrema.add(new Point((nopo + i + 1) / 2, -2));
					}
				} else if (current < -eps) {
					if (val > eps) {
						extrema.add(new Point((nopo + i + 1) / 2, 2));
					}
					if (val < -eps) {
						nopo = -1;
					}
				}
				// current == 0.0 do nothing
			}

			old = current;
		}

		// extrema.add(new Point(f.length-1,0));

		return extrema;
	}

	public static void main(String[] argv) {
		// 2 1 0 100.0 106.0 1 199.0 106.0
		// 2 2 0 146.0 214.0 1 153.0 111.0
		/*
		 * DPoint a = new DPoint(100.0,106.0); DPoint b = new
		 * DPoint(199.0,106.0); DPoint c = new DPoint(146.0,214.0); DPoint d =
		 * new DPoint(153.0,111.0);
		 */

		DPoint a = new DPoint(0.0, 0.0);
		DPoint b = new DPoint(100.0, 0.0);
		DPoint c = new DPoint(50.0, 14.0);
		DPoint d = new DPoint(c.x, c.y + 10);

		DStroke s1 = new DStroke();
		DStroke s2 = new DStroke();
		s1.add(a);
		s1.add(b);
		s2.add(c);
		// s2.add(d);

		System.out.println(Geom.distance(b, a, c, d));
		System.out.println(Geom.distance(s1, s2));
		System.out.println(Geom.distance(s1, s2, 1));

		s1.equidistant(1);
		System.out.println("s1.equidistant(1);");

		double[] f = new double[16];

		/*
		 * for(int i = 0; i < 10; i++) { f[i] = (i-5)*(i-5); }
		 */

		f[2] = 1;
		f[3] = 1;
		f[4] = 1;
		f[5] = 1;

		f[10] = 1;
		f[11] = 1;
		f[12] = 1;
		f[13] = 2;
		f[14] = 1;
		f[15] = 2;

		ArrayList<Point> v = Geom.locateLocalExtrema(f, 0.0);

		System.out.println(v.toString());

		double[] x = Maths.rootsCuadratic(1, -3, 2);

		System.out.println(x[0] + " " + x[1]);

		System.out.println((new DPoint(3, 2)).middlePoint(new DPoint(1, 3))
				.toString());
		System.out.println((new DPoint(1, 3)).middlePoint(new DPoint(3, 2))
				.toString());

		System.out.println((new DPoint(1, 3)).to(new DPoint(3, 2)).toString());

		DStroke s = new DStroke();
		s.add(new DPoint(0, 0));
		s.add(new DPoint(0.5, 0));
		s.add(new DPoint(1, 0));
		// System.out.println(s.toString()+": " +s.norm());

		s = new DStroke();
		s.add(new DPoint(0, 0));
		s.add(new DPoint(0, 0.5));
		s.add(new DPoint(0, 1));
		// System.out.println(s.toString()+": " +s.norm());
		s.clear();

		s = new DStroke();
		s.add(new DPoint(1, 0));
		s.add(new DPoint(0, 3));
		s.add(new DPoint(2, 4));
		s.add(new DPoint(4, 2));
		s.add(new DPoint(3, 0));
		// s.add(new DPoint(1.2,1));
		System.out.println(s.toString());
		System.out.println("" + s.pointAt(0).to(s.pointAt(1)));
		System.out.println("" + s.pointAt(1).to(s.pointAt(2)));
		System.out.println("s.accumulatedAngle(): "
				+ Math.toDegrees(s.accumulatedAngle()));

		// System.out.println("<s,s>: " +s.dot(s));

	}

}

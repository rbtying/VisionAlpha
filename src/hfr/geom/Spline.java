package hfr.geom;
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
 * <p>�berschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

import ocr.*;

public class Spline {
	double x[];
	double y[];
	double x2[];
	double y2[];
	double t[];
	int n;

	public Spline(DStroke ds) {
		double yp1, ypn;
		int i;

		n = ds.size();
		x = new double[n + 1];
		y = new double[n + 1];
		x2 = new double[n + 1];
		y2 = new double[n + 1];
		t = new double[n + 1];

		for (i = 1; i <= n; i++) {
			x[i] = ds.getX(i - 1);
			y[i] = ds.getY(i - 1);
			if (i > 1) {
				t[i] = t[i - 1] + ds.length(i - 2, i - 1);
			}
		}

		yp1 = 2.0 * (y[2] - y[1]) / (t[2] - t[1]) - derivativeBessel(t, y, 2);
		ypn = 2.0 * (y[n] - y[n - 1]) / (t[n] - t[n - 1])
				- derivativeBessel(t, y, n - 1);

		y2 = spline(t, y, yp1, ypn);

		yp1 = 2.0 * (x[2] - x[1]) / (t[2] - t[1]) - derivativeBessel(t, x, 2);
		ypn = 2.0 * (x[n] - x[n - 1]) / (t[n] - t[n - 1])
				- derivativeBessel(t, x, n - 1);

		x2 = spline(t, x, yp1, ypn);

	}

	public static DStroke splineStroke(DStroke ds, int np) throws Exception {
		DStroke nds = new DStroke();
		Spline sp = new Spline(ds.proyectionPolygonal(np));
		double len = ds.length() / (np - 1);
		int k;

		nds.add(ds.first());
		for (k = 1; k < np - 1; k++) {
			nds.add(sp.splint(k * len));
		}
		nds.add(ds.last());

		return nds;
	}

	public static DStroke splineStroke(DStroke ds, int np, int n)
			throws Exception {
		DStroke nds = new DStroke();
		Spline sp = new Spline(ds.proyectionPolygonal(np));
		double len = ds.length() / (n - 1);
		int k;

		nds.add(ds.first());
		for (k = 1; k < n - 1; k++) {
			nds.add(sp.splint(k * len));
		}
		nds.add(ds.last());

		return nds;
	}

	public static DStroke splineStroke(DStroke ds, int np, double len)
			throws Exception {
		DStroke nds = new DStroke();
		Spline sp = new Spline(ds.proyectionPolygonal(np));
		// double len = ds.length()/(n-1);
		int k;
		double length = ds.length();

		// nds.add(ds.first());
		for (k = 0; k * len < length; k++) {
			nds.add(sp.splint(k * len));
		}
		nds.add(ds.last());

		return nds;
	}

	public static double derivativeBessel(double x[], double y[], int i) {
		double ai;

		ai = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);

		return (1.0 - ai) * (y[i + 1] - y[i]) / (x[i + 1] - x[i]) + ai
				* (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
	}

	public static double[] spline(double x[], double y[], double yp1, double ypn) {
		double y2[];
		double p, qn, sig, un, u[];
		int n, i, k;

		n = x.length - 1;

		y2 = new double[n + 1];
		u = new double[n + 1];

		if (yp1 > 0.99e30) {
			y2[1] = u[1] = 0.0;
		} else {
			y2[1] = -0.5;
			u[1] = (3.0 / (x[2] - x[1]))
					* ((y[2] - y[1]) / (x[2] - x[1]) - yp1);
		}

		for (i = 2; i <= n - 1; i++) {
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1])
					/ (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}

		if (ypn > .99e30) {
			qn = un = 0.0;
		} else {
			qn = 0.5;
			un = (3.0 / (x[n] - x[n - 1]))
					* (ypn - (y[n] - y[n - 1]) / (x[n] - x[n - 1]));
		}

		y2[n] = (un - qn * u[n - 1]) / (qn * y2[n - 1] + 1.0);

		for (k = n - 1; k >= 1; k--) {
			y2[k] = y2[k] * y2[k + 1] + u[k];
		}

		/*
		 * for(k=n-1; k>=1; k--) { y2[k]=y2[k]*0.7; }
		 */

		return y2;
	}

	public static double splint(double xa[], double ya[], double y2a[], double x)
			throws Exception {
		double h, b, a;
		int n, klo, khi, k;

		n = xa.length - 1;
		klo = 1;
		khi = n;

		while (khi - klo > 1) {
			k = (khi + klo) >> 1;
			if (xa[k] > x) {
				khi = k;
			} else {
				klo = k;
			}
		}

		h = xa[khi] - xa[klo];
		if (h == 0.0) {
			throw new Exception("Bad xa input to routine Spline.splint");
		}

		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;

		return a * ya[klo] + b * ya[khi]
				+ ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi])
				* (h * h) / 6.0;
	}

	public DPoint splint(double ta) throws Exception {
		return new DPoint(splint(t, x, x2, ta), splint(t, y, y2, ta));
	}

}

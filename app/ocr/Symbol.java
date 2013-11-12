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

import hfr.SymbolNode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import math.Geom;
import mlp.NeuralNetworkModel;
import svm.SparseVector;
import svm.SvmModel;

public class Symbol extends ArrayList<DStroke> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8515743749590923693L;
	public static double NORMALIZING_FACTOR;
	public static boolean SIZE;
	public static boolean TIME;
	public static boolean automaticClassification;
	public int classifierType;
	public static String dot;
	public static boolean drawBoundingBox;
	public static boolean drawCenterOfMass;
	public static boolean drawIndex;
	public static boolean drawLabel;
	public static boolean drawNumberOfPoints;
	public double[] features;
	public double maxX;
	public double maxY;
	public double minX;
	public double minY;
	public static int n;
	public String name;
	public static NeuralNetworkModel nn1;
	public static NeuralNetworkModel nn2;
	public static boolean nnUsed;
	public static boolean normalizeDirection;
	public static int np;
	public int numStrokes;
	public DStroke processed;
	public static SvmModel svm1;
	public static SvmModel svm2;
	public static boolean symbolTypeRaded;

	static {
		Symbol.SIZE = true;
		Symbol.TIME = true;
		Symbol.NORMALIZING_FACTOR = 200.0;
		Symbol.drawCenterOfMass = false;
		Symbol.normalizeDirection = true;
		Symbol.automaticClassification = true;
		Symbol.svm1 = null;
		Symbol.svm2 = null;
		Symbol.nn1 = null;
		Symbol.nn2 = null;
		Symbol.nnUsed = false;
		Symbol.symbolTypeRaded = false;
		Symbol.dot = "dot";
		Symbol.np = 16;
		Symbol.n = 8;
	}

	public Symbol(final Symbol s) {
		super();
		name = "no_name";
		processed = null;
		numStrokes = 0;
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		classifierType = -2;
		features = null;
		for (int i = 0; i < s.size(); ++i) {
			add(new DStroke(s.strokeAt(i)));
		}
		name = new String(s.name);
		classifierType = s.classifierType;
	}

	public Symbol(final String name_) {
		super();
		name = "no_name";
		processed = null;
		numStrokes = 0;
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		classifierType = -2;
		features = null;
		name = new String(name_);
	}

	public Symbol() {
		super();
		name = "no_name";
		processed = null;
		numStrokes = 0;
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		classifierType = -2;
		features = null;
	}

	public void actualizeMaxMin() {
		minX = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			ds.actualizeMaxMin();
			minX = Math.min(minX, ds.getMinX());
			maxX = Math.max(maxX, ds.getMaxX());
			minY = Math.min(minY, ds.getMinY());
			maxY = Math.max(maxY, ds.getMaxY());
		}
	}

	@Override
	public void add(final int i, final DStroke ds) {
		super.add(i, ds);
		minX = Math.min(minX, ds.getMinX());
		maxX = Math.max(maxX, ds.getMaxX());
		minY = Math.min(minY, ds.getMinY());
		maxY = Math.max(maxY, ds.getMaxY());
	}

	@Override
	public boolean add(final DStroke ds) {
		boolean out = super.add(ds);
		minX = Math.min(minX, ds.getMinX());
		maxX = Math.max(maxX, ds.getMaxX());
		minY = Math.min(minY, ds.getMinY());
		maxY = Math.max(maxY, ds.getMaxY());
		return out;
	}

	public boolean boundigBoxIntersection(final DStroke ds, final double eps) {
		return intervalIntersection(minX, maxX, ds.minX, ds.maxX, eps)
				&& intervalIntersection(minY, maxY, ds.minY, ds.maxY, eps);
	}

	public Symbol changeOrder() {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(strokeAt(size() - 1 - i));
		}
		return s;
	}

	public void classify() {
		if (!Symbol.automaticClassification) {
			return;
		}
		if (isEmpty()) {
			name = "no_name";
		} else if (Symbol.nnUsed && Symbol.nn1 != null && Symbol.nn2 != null) {
			if (getWidth() <= 4.0 && getHeight() <= 4.0) {
				name = "dot";
			} else if (size() == 1) {
				name = Symbol.nn1.classify(getAsSparseVector(32, 10, 3));
			} else if (size() > 1) {
				name = Symbol.nn2.classify(getAsSparseVector(32, 10, 3));
			}
		} else if (Symbol.svm1 != null && Symbol.svm2 != null) {
			if (getWidth() <= 4.0 && getHeight() <= 4.0) {
				name = "dot";
			} else if (size() == 1) {
				name = Symbol.svm1.classify(this.getAsSparseVectorSingle(12));
			} else if (size() > 1) {
				name = Symbol.svm2.classify(this.getAsSparseVectorSingle(12));
			}
		}
	}

	public Symbol clusterPoints(final int np) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).clusterPoints(np)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol dehook(final double ta, final double tl) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).getDehooked(ta, tl)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol dehook() {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).getDehooked()));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public double distance(final Symbol s, final int np) {
		double d = Double.POSITIVE_INFINITY;
		for (int i = 0; i < size(); ++i) {
			final DStroke di = (strokeAt(i).size() > np) ? this.strokeAt(i)
					.proyectionPolygonal(np) : strokeAt(i);
			for (int j = 0; j < s.size(); ++j) {
				final DStroke dj = (strokeAt(j).size() > np) ? this.strokeAt(j)
						.proyectionPolygonal(np) : strokeAt(j);
				d = Math.min(d, di.distance(dj));
			}
		}
		return d;
	}

	public boolean distance(final DStroke s, final double r) {
		final boolean dist = false;
		for (int i = 0; i < size(); ++i) {
			if (Geom.distance(strokeAt(i), s, r)) {
				return true;
			}
		}
		return dist;
	}

	public double distance(final DStroke s) {
		double dist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < size(); ++i) {
			dist = Math.min(dist, Geom.distance(strokeAt(i), s));
		}
		return dist;
	}

	public void draw(final Graphics2D g2) {
		final FontMetrics metrics = g2.getFontMetrics();
		if (isEmpty()) {
			return;
		}
		if (Symbol.drawBoundingBox) {
			g2.setColor(new Color(0, 0, 255, 30));
			g2.fill(new Rectangle2D.Double(getMinX(), getMinY(), this
					.getWidth(), getHeight()));
			if (!name.equals("no_name")) {
				SymbolNode.draw(g2,
						new SymbolNode((int) getMinX(), (int) this.getMinY(),
								(int) getMaxX(), (int) this.getMaxY(), name));
			}
		}
		final boolean dp = DStroke.drawPoints;
		DStroke.drawPoints = name.equals("\\dot");
		for (int i = 0; i < size(); ++i) {
			strokeAt(i).draw(g2);
		}
		DStroke.drawPoints = dp;
		g2.setColor(Color.black);
		if (Symbol.drawIndex) {
			for (int i = 0; i < size(); ++i) {
				final DStroke ds = strokeAt(i);
				final int width = metrics.stringWidth("" + i);
				final int height = metrics.getHeight();
				g2.drawString("" + i, (int) (ds.first().x - 2 * width),
						(int) (ds.first().y - height / 2));
			}
		}
		if (Symbol.drawNumberOfPoints) {
			final int width = metrics.stringWidth("" + numOfPoints());
			final int height = metrics.getHeight();
			g2.drawString("" + numOfPoints(), (int) (maxX - width),
					(int) (maxY + 3.0 + height / 2));
		}
		if (Symbol.drawLabel) {
			// final int width = metrics.stringWidth("" + name);
			// final int height = metrics.getHeight();
			g2.drawString("" + name, (int) minX, (int) (minY - 3.0));
		}
		if (Symbol.drawCenterOfMass) {
			g2.setColor(new Color(255, 0, 0, 200));
			g2.setStroke(new BasicStroke(3.0f, 2, 1));
			final DPoint p = getCenterOfMass();
			g2.draw(new Line2D.Double(p.x, p.y, p.x, p.y));
		}
	}

	public void drawBoundingBox(final Graphics2D g2, final Color c) {
		g2.setColor(c);
		g2.drawRect((int) getMinX(), (int) getMinY(), (int) getWidth(),
				(int) getHeight());
	}

	public Symbol equidistant(final double r) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).equidistant(r)));
		}
		return s;
	}

	public Symbol equidistantApproximation(final int np) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).equidistantApproximation(np)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol equidistantLength(final int np) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).equidistantLength(np)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol equidistantLength(final double inc) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).equidistantLength(inc)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol equidistantOptimized(final int np) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).equidistantOptimized(np)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol equidistantRespecToMaxWidthHeight(final int numPoints) {
		final double r = Math.max(getWidth(), getHeight()) / numPoints;
		return equidistant(r);
	}

	public void filter() {
		for (int i = 0; i < size(); ++i) {
			if (strokeAt(i).length() == 0.0) {
				remove(i);
			}
		}
		for (int j = 0; j < size(); ++j) {
			strokeAt(j).filter();
		}
	}

	public DStroke first() {
		return first();
	}

	public SparseVector getAsSparseVector(final int npradius, final int np,
			final int n) {
		final StringTokenizer st = new StringTokenizer(getProcessed(npradius,
				np, n), " :");
		final SparseVector v = new SparseVector(st.countTokens() / 2, name);
		for (int i = 0; i < v.length; ++i) {
			v.index[i] = Integer.parseInt(st.nextToken());
			v.value[i] = Double.parseDouble(st.nextToken());
		}
		return v;
	}

	public SparseVector getAsSparseVectorSingle(final int np) {
		final StringTokenizer st = new StringTokenizer(getProcessedSingle(np),
				" :");
		final SparseVector v = new SparseVector(st.countTokens() / 2, name);
		for (int i = 0; i < v.length; ++i) {
			v.index[i] = Integer.parseInt(st.nextToken());
			v.value[i] = Double.parseDouble(st.nextToken());
		}
		return v;
	}

	public DPoint getCenterOfMass() {
		if (isEmpty()) {
			return new DPoint();
		}
		double xm;
		double ym = xm = 0.0;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				xm += ds.getX(j);
				ym += ds.getY(j);
			}
		}
		return new DPoint(xm / numOfPoints(), ym / numOfPoints());
	}

	public double getCenterX() {
		return 0.5 * (getMaxX() + getMinX());
	}

	public double getCenterY() {
		return 0.5 * (getMaxY() + getMinY());
	}

	public double getHeight() {
		return getMaxY() - getMinY();
	}

	public double getHorizontalFactor() {
		double xm = 0.0;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				xm += ds.getX(j);
			}
		}
		xm /= numOfPoints();
		int na;
		int nb = na = 0;
		double xa;
		double xb = xa = 0.0;
		double ya;
		double yb = ya = 0.0;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				if (ds.getX(j) < xm) {
					xa += ds.getX(j);
					ya += ds.getY(j);
					++na;
				} else {
					xb += ds.getX(j);
					yb += ds.getY(j);
					++nb;
				}
			}
		}
		xa /= na;
		ya /= na;
		xb /= nb;
		yb /= nb;
		return Math.atan2(ya - yb, xa - xb)
				* Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb));
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public String getProcessed(final int npradius, final int np, final int n) {
		filter();
		normalizeDirection();
		orderStrokes();
		Symbol newSymbol;
		if (size() > 1) {
			newSymbol = new Symbol();
			final DStroke ds = new DStroke();
			for (int i = 0; i < size() / 2; ++i) {
				ds.join(strokeAt(i));
			}
			newSymbol.add(new DStroke(ds));
			ds.clear();
			for (int i = size() / 2; i < size(); ++i) {
				ds.join(strokeAt(i));
			}
			newSymbol.add(new DStroke(ds));
		} else {
			newSymbol = new Symbol(this);
		}
		newSymbol = newSymbol.equidistantRespecToMaxWidthHeight(npradius);
		newSymbol = newSymbol.smooth();
		newSymbol.scale();
		String str = "";
		int count = 0;
		for (int i = 0; i < newSymbol.size(); ++i) {
			final DStroke ds = newSymbol.strokeAt(i);
			int[] indexes;
			if (ds.size() < np / newSymbol.size()) {
				indexes = ds.equidistant(ds.length() / (2 * np))
						.proyectionPolygonalIndexes(np / newSymbol.size());
			} else {
				indexes = ds.proyectionPolygonalIndexes(np / newSymbol.size());
			}
			str = str + "" + count++ + ":" + (float) ds.first().x + " ";
			str = str + "" + count++ + ":" + (float) ds.first().y + " ";
			for (int k = 1; k < indexes.length - 1; ++k) {
				final int j = indexes[k];
				final DStroke substroke = ds.strokeAt(j - n, j + n);
				str = str + "" + count++ + ":" + (float) ds.getX(j) + " ";
				str = str + "" + count++ + ":" + (float) ds.getY(j) + " ";
				str = str + "" + count++ + ":" + (float) ds.localCos(j) + " ";
				str = str + "" + count++ + ":" + (float) ds.localSin(j) + " ";
				str = str + "" + count++ + ":" + (float) ds.localCosCur(j)
						+ " ";
				str = str + "" + count++ + ":" + (float) ds.localSinCur(j)
						+ " ";
				str = str + "" + count++ + ":" + (float) substroke.aspect()
						+ " ";
				str = str + "" + count++ + ":" + (float) substroke.curliness()
						+ " ";
				str = str + "" + count++ + ":" + (float) substroke.linearity()
						+ " ";
				str = str
						+ ""
						+ count++
						+ ":"
						+ (float) substroke
								.firstLastPointsDistanceRelativeToLength()
						+ " ";
				str = str + "" + count++ + ":" + (float) substroke.globalCos()
						+ " ";
				str = str + "" + count++ + ":" + (float) substroke.globalSin()
						+ " ";
			}
			str = str + "" + count++ + ":" + (float) ds.aspect() + " ";
			str = str + "" + count++ + ":" + (float) ds.curliness() + " ";
			str = str + "" + count++ + ":" + (float) ds.linearity() + " ";
			str = str + "" + count++ + ":"
					+ (float) ds.firstLastPointsDistanceRelativeToLength()
					+ " ";
			str = str + "" + count++ + ":" + (float) ds.globalCos() + " ";
			str = str + "" + count++ + ":" + (float) ds.globalSin() + " ";
			str = str + "" + count++ + ":" + (float) ds.last().x + " ";
			str = str + "" + count++ + ":" + (float) ds.last().y + " ";
		}
		return str;
	}

	public String getProcessedSingle(final int np) {
		return getProcessedSingle(0.0, np);
	}

	public String getProcessedSingle(final double alpha, final int np) {
		Symbol newSymbol = new Symbol();
		final Symbol sym = new Symbol();
		for (int i = 0; i < size(); ++i) {
			DStroke ds = new DStroke(strokeAt(i));
			ds = ds.smooth();
			ds = ds.proyectionPolygonal(np);
			ds = ds.equidistantLength(np);
			sym.add(ds);
		}
		if (alpha != 0.0) {
			sym.tangentTansformation(alpha);
		}
		newSymbol = new Symbol(sym);
		newSymbol.filter();
		if (Symbol.normalizeDirection) {
			newSymbol.normalizeDirection();
		}
		newSymbol.orderStrokes();
		newSymbol.scale();
		String str = "";
		int count = 0;
		DStroke oldds = null;
		for (int j = 0; j < newSymbol.size(); ++j) {
			final DStroke ds = newSymbol.strokeAt(j);
			// double sumcos2 = 0.0;
			// double sumsin2 = 0.0;
			// double sumcos1 = 0.0;
			// double sumsin1 = 0.0;
			double linearity;
			double aa;
			double d;
			double lp = d = (aa = (linearity = 0.0));
			final double[] a = ds.turningFunction();
			for (int i = 0; i < ds.size(); ++i) {
				str = str + "" + count++ + ":" + (float) ds.getX(i) + " ";
				str = str + "" + count++ + ":" + (float) ds.getY(i) + " ";
				lp += d;
				str = str + "" + count++ + ":" + (float) lp + " ";
				final double cos1 = ds.cosAt(i);
				final double sin1 = ds.sinAt(i);
				linearity += Geom.height(ds.first(), ds.last(), ds.pointAt(i));
				if (i < ds.size() - 1) {
					d = ds.length(i, i + 1);
					// sumcos1 += d * cos1;
					// sumsin1 += d * sin1;
					str = str + "" + count++ + ":" + (float) cos1 + " ";
					str = str + "" + count++ + ":" + (float) sin1 + " ";
				}
				if (i < ds.size() - 2) {
					aa += a[i + 1] - a[i];
					final double cos2 = Math.cos(a[i + 1] - a[i]);
					final double sin2 = Math.sin(a[i + 1] - a[i]);
					final double diffcos = cos2;
					final double diffsin = sin2;
					// sumcos2 += d * diffcos;
					// sumsin2 += d * diffsin;
					str = str + "" + count++ + ":" + (float) diffcos + " ";
					str = str + "" + count++ + ":" + (float) diffsin + " ";
				}
			}
			str = str + "" + count++ + ":" + (float) ds.centerOfGravity().x
					+ " ";
			str = str + "" + count++ + ":" + (float) ds.centerOfGravity().y
					+ " ";
			str = str + "" + count++ + ":" + 0.5 * aa / 3.141592653589793 + " ";
			str = str + "" + count++ + ":"
					+ (float) ds.areaPoly2RelativeToBoundingBox() + " ";
			str = str + "" + count++ + ":" + (float) ds.aspect() + " ";
			str = str + "" + count++ + ":" + (float) ds.curliness() + " ";
			str = str + "" + count++ + ":" + (float) ds.getCurvature2() + " ";
			str = str + "" + count++ + ":" + (float) ds.getWidth()
					/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			str = str + "" + count++ + ":" + (float) ds.getHeight()
					/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			str = str + "" + count++ + ":"
					+ (float) ds.relativeFirstLastPointDistance() + " ";
			str = str + "" + count++ + ":"
					+ (float) ds.relativeLengthFirstLastPointDistance() + " ";
			str = str + "" + count++ + ":" + linearity / ds.size() + " ";
			if (oldds != null) {
				str = str + "" + count++ + ":"
						+ (float) ds.first().distance(oldds.first()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.first().distance(oldds.last()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().distance(oldds.first()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().distance(oldds.last()) + " ";
			}
			oldds = ds;
		}
		return str;
	}

	public Symbol getProcessedSingleSymbol(final double alpha, final int np) {
		Symbol newSymbol = new Symbol();
		final Symbol sym = new Symbol();
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = new DStroke(strokeAt(i));
			ds.smooth();
			sym.add(ds.proyectionPolygonal(np).equidistantLength(np));
		}
		if (alpha != 0.0) {
			sym.tangentTansformation(alpha);
		}
		newSymbol = new Symbol(sym);
		newSymbol.filter();
		if (Symbol.normalizeDirection) {
			newSymbol.normalizeDirection();
		}
		newSymbol.orderStrokes();
		return newSymbol;
	}

	public String getSparseVectorStringCode(final double ks, final double kl,
			final int w, final int n, final double alpha, final int np) {
		final Symbol newSymbol = new Symbol();
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = new DStroke(strokeAt(i));
			ds.filter();
			ds.smooth();
			newSymbol.add(ds.equidistantLength(np));
		}
		if (alpha != 0.0) {
			newSymbol.tangentTansformation(alpha);
		}
		if (Symbol.normalizeDirection) {
			newSymbol.normalizeDirection();
		}
		newSymbol.orderStrokes();
		newSymbol.scale();
		String str = "";
		int count = 0;
		DStroke oldds = null;
		for (int j = 0; j < newSymbol.size(); ++j) {
			final DStroke ds = newSymbol.strokeAt(j);
			if (oldds != null) {
				str = str + "" + count++ + ":"
						+ (float) ds.first().distance(oldds.first()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.first().distance(oldds.last()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().distance(oldds.first()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().distance(oldds.last()) + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.first().angle(oldds.first())
						/ 3.141592653589793 + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.first().angle(oldds.last())
						/ 3.141592653589793 + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().angle(oldds.first())
						/ 3.141592653589793 + " ";
				str = str + "" + count++ + ":"
						+ (float) ds.last().angle(oldds.last())
						/ 3.141592653589793 + " ";
			}
			str += ds.getSparseVectorStringCode(ks, kl, w, count, n);
			count += (n + 2) * np;
			oldds = ds;
		}
		return str;
	}

	public String getSparseVectorStringCode(final int n, final double alpha,
			final int np) {
		return getSparseVectorStringCode(0.125, 0.017453292519943295, 3, n,
				alpha, np);
	}

	public double getVerticalFactor() {
		double ym = 0.0;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				ym += ds.getY(j);
			}
		}
		ym /= numOfPoints();
		int na;
		int nb = na = 0;
		double xa;
		double xb = xa = 0.0;
		double ya;
		double yb = ya = 0.0;
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				if (ds.getY(j) < ym) {
					xa += ds.getX(j);
					ya += ds.getY(j);
					++na;
				} else {
					xb += ds.getX(j);
					yb += ds.getY(j);
					++nb;
				}
			}
		}
		xa /= na;
		ya /= na;
		xb /= nb;
		yb /= nb;
		return Math.atan2(ya - yb, xa - xb)
				* Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb));
	}

	public double getWidth() {
		return getMaxX() - getMinX();
	}

	public SparseVector globalFeaturesToSparseVector() {
		setGlobalFeatures();
		return new SparseVector(features, name, true);
	}

	public String globalFeaturesToString() {
		String str = "";
		setGlobalFeatures();
		for (int i = 0; i < features.length; ++i) {
			str = str + features[i] + " ";
		}
		return str;
	}

	public boolean intersects(final DStroke ds) {
		for (int i = 0; i < size(); ++i) {
			if (ds.intersects(strokeAt(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(final DStroke ds, final double eps) {
		for (int i = 0; i < size(); ++i) {
			if (ds.intersects(strokeAt(i), eps)) {
				return true;
			}
		}
		return false;
	}

	boolean intervalIntersection(final double a, final double b,
			final double c, final double d, final double eps) {
		if (a < c) {
			return b + eps - c > 0.0;
		}
		return d + eps - a > 0.0;
	}

	public DStroke last() {
		return get(size() - 1);
	}

	public double length() {
		double l = 0.0;
		for (int i = 0; i < size(); ++i) {
			l += strokeAt(i).length();
		}
		return l;
	}

	public void normalizeDirection() {
		for (int i = 0; i < size(); ++i) {
			strokeAt(i).normalizeDirection();
		}
	}

	public int numOfPoints() {
		int n = 0;
		for (int i = 0; i < size(); ++i) {
			n += strokeAt(i).size();
		}
		return n;
	}

	public void orderStrokes() {
		final int size = size();
		final double minx = getMinX();
		final double miny = getMinY();
		for (int i = 0; i < size; ++i) {
			double min = Double.POSITIVE_INFINITY;
			int index = -1;
			for (int j = i; j < size; ++j) {
				final DPoint p = strokeAt(j).get(strokeAt(j).size() - 1);
				final double angle = Math.atan2(p.y - miny, p.x - minx);
				if (min > angle) {
					min = angle;
					index = j;
				}
			}
			final DStroke aux = new DStroke(strokeAt(i));
			set(i, new DStroke(strokeAt(index)));
			set(index, aux);
		}
	}

	public Symbol permutation() {
		if (size() <= 1) {
			return this;
		}
		final Symbol s = new Symbol();
		final int[] perm = new int[s.size()];
		for (int i = 0; i < perm.length; ++i) {
			perm[i] = i;
		}
		boolean identity = true;
		while (identity) {
			for (int j = 0; j < perm.length; ++j) {
				final int rand = (int) (Math.random() * perm.length);
				perm[rand] = perm[j];
				perm[j] = rand;
			}
			for (int k = 0; k < perm.length; ++k) {
				identity = (identity && perm[k] == k);
			}
		}
		for (int j = 0; j < perm.length; ++j) {
			add(new DStroke(s.strokeAt(perm[j])));
		}
		return s;
	}

	public Symbol proyeccionPolygonal(final int np) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).proyectionPolygonal(np)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public void read(final BufferedReader filein) throws IOException {
		DStroke.writeSize(Symbol.SIZE);
		DStroke.writeTime(Symbol.TIME);
		clear();
		final String line;
		if ((line = filein.readLine()) == null) {
			throw new IOException();
		}
		final StringTokenizer st = new StringTokenizer(line, " \n\t\r\f");
		final int count = st.countTokens();
		name = new String(st.nextToken());
		final int len = Integer.parseInt(st.nextToken());
		if (count >= 3) {
			classifierType = Integer.parseInt(st.nextToken());
		}
		for (int i = 0; i < len; ++i) {
			final DStroke s = new DStroke();
			s.read(filein);
			add(new DStroke(s));
		}
	}

	public static Symbol readSymbol(final BufferedReader filein)
			throws IOException {
		final Symbol sym = new Symbol();
		DStroke.writeSize(Symbol.SIZE);
		DStroke.writeTime(Symbol.TIME);
		final String line;
		if ((line = filein.readLine()) == null) {
			throw new IOException();
		}
		final StringTokenizer st = new StringTokenizer(line, " \n\t\r\f");
		// final int count = st.countTokens();
		sym.name = new String(st.nextToken());
		final int len = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens()) {
			sym.classifierType = Integer.parseInt(st.nextToken());
		}
		if (st.hasMoreTokens()) {
			final int dim = st.countTokens();
			sym.features = new double[dim];
			for (int i = 0; i < dim; ++i) {
				sym.features[i] = Double.parseDouble(st.nextToken());
			}
		}
		for (int i = 0; i < len; ++i) {
			final DStroke s = new DStroke();
			s.read(filein);
			sym.add(new DStroke(s));
		}
		return sym;
	}

	public void reduceStrokes(final int ns) {
		if (ns > 0 && size() > ns) {
			for (int i = size() - 1; i >= ns; --i) {
				strokeAt(ns - 1).join(new DStroke(strokeAt(i)));
				remove(i);
			}
			// goto Label_0059;
		}
	}

	@Override
	public void clear() {
		super.clear();
		final double n = Double.POSITIVE_INFINITY;
		minY = n;
		minX = n;
		final double n2 = Double.NEGATIVE_INFINITY;
		maxY = n2;
		maxX = n2;
	}

	@Override
	public DStroke remove(final int index) {
		DStroke d = super.remove(index);
		final double n = Double.POSITIVE_INFINITY;
		minY = n;
		minX = n;
		final double n2 = Double.NEGATIVE_INFINITY;
		maxY = n2;
		maxX = n2;
		for (int i = 0; i < size(); ++i) {
			minX = Math.min(minX, strokeAt(i).getMinX());
			maxX = Math.max(maxX, strokeAt(i).getMaxX());
			minY = Math.min(minY, strokeAt(i).getMinY());
			maxY = Math.max(maxY, strokeAt(i).getMaxY());
		}
		return d;
	}

	public void removeSmallStrokes(final double factor) {
		final double t = Math.max(getWidth(), getHeight());
		for (int i = size() - 1; i >= 0; --i) {
			final DStroke ds = strokeAt(i);
			if (Math.max(ds.getWidth(), ds.getHeight()) < factor * t) {
				remove(i);
			}
		}
	}

	public void scale(final double xc, final double yc, final double scale,
			final boolean binary) {
		final double minx = getMinX();
		final double miny = getMinY();
		final double diffx = getMaxX() - minx;
		final double diffy = getMaxY() - miny;
		if (binary) {
			if (diffx > diffy) {
				final double r = diffy / diffx;
				for (int i = 0; i < size(); ++i) {
					final DStroke s = strokeAt(i);
					for (int j = 0; j < s.size(); ++j) {
						final DPoint p = s.pointAt(j);
						final double x = scale * (p.x - minx) / diffx + xc;
						final double y = scale
								* ((p.y - miny) / diffx + 0.5 * (1.0 - r)) + yc;
						p.set(x, y);
					}
				}
			} else if (diffy > 0.0) {
				final double r = diffx / diffy;
				for (int i = 0; i < size(); ++i) {
					final DStroke s = strokeAt(i);
					for (int j = 0; j < s.size(); ++j) {
						final DPoint p = s.pointAt(j);
						final double x = scale
								* ((p.x - minx) / diffy + 0.5 * (1.0 - r)) + xc;
						final double y = scale * (p.y - miny) / diffy + yc;
						p.set(x, y);
					}
				}
			}
		} else if (diffx > diffy) {
			final double r = diffy / diffx;
			for (int i = 0; i < size(); ++i) {
				final DStroke s = strokeAt(i);
				for (int j = 0; j < s.size(); ++j) {
					final DPoint p = s.pointAt(j);
					final double x = scale * (2.0 * (p.x - minx) / diffx - 1.0)
							+ xc;
					final double y = scale * (2.0 * (p.y - miny) / diffx - r)
							+ yc;
					p.set(x, y);
				}
			}
		} else if (diffy > 0.0) {
			final double r = diffx / diffy;
			for (int i = 0; i < size(); ++i) {
				final DStroke s = strokeAt(i);
				for (int j = 0; j < s.size(); ++j) {
					final DPoint p = s.pointAt(j);
					final double x = scale * (2.0 * (p.x - minx) / diffy - r)
							+ xc;
					final double y = scale * ((2.0 * p.y - miny) / diffy - 1.0)
							+ yc;
					p.set(x, y);
				}
			}
		}
	}

	public void scale(final double factor) {
		double wg = Math.max(getHeight(), getWidth());
		if (wg == 0.0) {
			return;
		}
		final double wr = getWidth() / wg;
		final double hr = getHeight() / wg;
		wg = 2.0 * factor / wg;
		for (int i = 0; i < size(); ++i) {
			final DStroke dsi = strokeAt(i);
			for (int j = 0; j < dsi.size(); ++j) {
				final double x = wg * (dsi.getX(j) - dsi.getMinX()) - wr;
				final double y = wg * (dsi.getY(j) - dsi.getMinY()) - hr;
				dsi.set(j, x, y);
			}
		}
	}

	public void scale() {
		double wg = Math.max(getHeight(), getWidth());
		if (wg == 0.0) {
			return;
		}
		final double wr = getWidth() / wg;
		final double hr = getHeight() / wg;
		wg = 2.0 / wg;
		for (int i = 0; i < size(); ++i) {
			final DStroke dsi = strokeAt(i);
			for (int j = 0; j < dsi.size(); ++j) {
				final double x = wg * (dsi.getX(j) - dsi.getMinX()) - wr;
				final double y = wg * (dsi.getY(j) - dsi.getMinY()) - hr;
				dsi.set(j, x, y);
			}
		}
	}

	public static void setClassificationModel() {
	}

	public void setGlobalFeatures() {
		final Symbol s = new Symbol(this);
		if (size() < 2) {
			return;
		}
		features = new double[13 * size()];
		s.filter();
		s.normalizeDirection();
		s.orderStrokes();
		s.smooth();
		int i = 0;
		int j = 0;
		while (i < size()) {
			final DStroke ds = s.strokeAt(i);
			features[j++] = ds.accumulatedAngle();
			features[j++] = ds.areaPoly2RelativeToBoundingBox();
			features[j++] = ds.aspect();
			features[j++] = ds.curliness();
			features[j++] = ds.getCurvature();
			features[j++] = ds.getCurvature2();
			features[j++] = ds.globalCos();
			features[j++] = ds.globalSin();
			features[j++] = ds.relativeFirstLastPointDistance();
			features[j++] = ds.relativeLengthFirstLastPointDistance();
			features[j++] = ds.sumDistanceErrorRelative();
			features[j++] = ds.getVerticalFactor();
			features[j++] = ds.getHorizontalFactor();
			++i;
		}
	}

	public void setName(final String name_) {
		name = new String(name_);
	}

	public void setTopLeft(final double x, final double y) {
		for (int i = 0; i < size(); ++i) {
			final DStroke ds = strokeAt(i);
			for (int j = 0; j < ds.size(); ++j) {
				final double xp = x + ds.getX(j) - getMinX();
				final double yp = y + ds.getY(j) - getMinY();
				ds.pointAt(j).set(xp, yp);
			}
		}
		minX = x;
		maxX = x + maxX - minX;
		minY = y;
		maxY = y + maxY - minY;
	}

	public Symbol slopeTansformation(final double factor, final int sdx,
			final int sdy) {
		final Symbol tangent = new Symbol();
		for (int j = 0; j < size(); ++j) {
			tangent.add(new DStroke(strokeAt(j)));
		}
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			// final double len = dstroke.length();
			for (int i = 1; i < dstroke.size() - 1; ++i) {
				final double dx = dstroke.getX(i + 1) - dstroke.getX(i - 1);
				final double dy = dstroke.getY(i + 1) - dstroke.getY(i - 1);
				final double d = 1.0;
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) + d * factor * dx * sdx,
						tangent.strokeAt(j).getY(i) + d * factor * dy * sdy);
			}
			int i = 0;
			double dx = dstroke.getX(i + 1) - dstroke.getX(i);
			double dy = dstroke.getY(i + 1) - dstroke.getY(i);
			double d = 1.0;
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + d * factor * dx * sdx,
					tangent.strokeAt(j).getY(i) + d * factor * dy * sdy);
			i = dstroke.size() - 1;
			dx = dstroke.getX(i) - dstroke.getX(i - 1);
			dy = dstroke.getY(i) - dstroke.getY(i - 1);
			d = 1.0;
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + d * factor * dx * sdx,
					tangent.strokeAt(j).getY(i) + d * factor * dy * sdy);
		}
		return tangent;
	}

	public Symbol smooth() {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).smooth()));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public Symbol smooth(final double sigma, final int size) {
		final Symbol s = new Symbol();
		for (int i = 0; i < size(); ++i) {
			s.add(new DStroke(strokeAt(i).smooth(sigma, size)));
		}
		s.classifierType = classifierType;
		if (s.classifierType < 0) {
			s.name = name;
		}
		return s;
	}

	public int sng(final double x) {
		return (x < 0.0) ? -1 : ((x == 0.0) ? 0 : 1);
	}

	public DStroke strokeAt(final int i) {
		return get(i);
	}

	public void tangentTansformation(final double alpha, final int sdx,
			final int sdy) {
		final Symbol tangent = new Symbol();
		for (int j = 0; j < size(); ++j) {
			tangent.add(new DStroke(strokeAt(j).size()));
		}
		// final double factor = 5.0 * alpha * (2.0 * Math.random() - 1.0);
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			// final double len = dstroke.length();
			for (int i = 1; i < dstroke.size() - 1; ++i) {
				final double dx = dstroke.getX(i + 1) - dstroke.getX(i - 1);
				final double dy = dstroke.getY(i + 1) - dstroke.getY(i - 1);
				// final double d = dstroke.length(i - 1, i + 1);
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) + alpha * dx * sdx,
						tangent.strokeAt(j).getY(i) + alpha * dy * sdy);
			}
			int i = 0;
			double dx = dstroke.getX(i + 1) - dstroke.getX(i);
			double dy = dstroke.getY(i + 1) - dstroke.getY(i);
			// double d = dstroke.length(i, i + 1);
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + alpha * dx * sdx,
					tangent.strokeAt(j).getY(i) + alpha * dy * sdy);
			i = dstroke.size() - 1;
			dx = dstroke.getX(i) - dstroke.getX(i - 1);
			dy = dstroke.getY(i) - dstroke.getY(i - 1);
			// d = dstroke.length(i - 1, i);
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + alpha * dx * sdx,
					tangent.strokeAt(j).getY(i) + alpha * dy * sdy);
		}
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			for (int i = 0; i < dstroke.size(); ++i) {
				dstroke.set(i, dstroke.getX(i) + tangent.strokeAt(j).getX(i),
						dstroke.getY(i) + tangent.strokeAt(j).getY(i));
			}
		}
	}

	public void tangentTansformation(final double alpha) {
		final Symbol tangent = new Symbol();
		for (int j = 0; j < size(); ++j) {
			tangent.add(new DStroke(strokeAt(j).size()));
		}
		double factor = alpha * (2.0 * Math.random() - 1.0);
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			for (int i = 0; i < dstroke.size(); ++i) {
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) - factor * dstroke.getX(i),
						tangent.strokeAt(j).getY(i) + factor * dstroke.getY(i));
			}
		}
		factor = alpha * (2.0 * Math.random() - 1.0);
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			for (int i = 0; i < dstroke.size(); ++i) {
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) - factor * dstroke.getY(i),
						tangent.strokeAt(j).getY(i) + factor * dstroke.getX(i));
			}
		}
		factor = alpha * (2.0 * Math.random() - 1.0);
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			for (int i = 0; i < dstroke.size(); ++i) {
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) + factor * dstroke.getY(i),
						tangent.strokeAt(j).getY(i) + factor * dstroke.getX(i));
			}
		}
		factor = 2.0 * alpha * (2.0 * Math.random() - 1.0);
		final double factor2 = 2.0 * alpha * (2.0 * Math.random() - 1.0);
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			// final double len = dstroke.length();
			for (int i = 1; i < dstroke.size() - 1; ++i) {
				final double dx = dstroke.getX(i + 1) - dstroke.getX(i - 1);
				final double dy = dstroke.getY(i + 1) - dstroke.getY(i - 1);
				final double d = 1.0;
				tangent.strokeAt(j).set(i,
						tangent.strokeAt(j).getX(i) + d * factor * dx,
						tangent.strokeAt(j).getY(i) + d * factor2 * dy);
			}
			int i = 0;
			double dx = dstroke.getX(i + 1) - dstroke.getX(i);
			double dy = dstroke.getY(i + 1) - dstroke.getY(i);
			double d = 1.0;
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + d * factor * dx,
					tangent.strokeAt(j).getY(i) + d * factor2 * dy);
			i = dstroke.size() - 1;
			dx = dstroke.getX(i) - dstroke.getX(i - 1);
			dy = dstroke.getY(i) - dstroke.getY(i - 1);
			d = 1.0;
			tangent.strokeAt(j).set(i,
					tangent.strokeAt(j).getX(i) + d * factor * dx,
					tangent.strokeAt(j).getY(i) + d * factor2 * dy);
		}
		for (int j = 0; j < size(); ++j) {
			final DStroke dstroke = strokeAt(j);
			for (int i = 0; i < dstroke.size(); ++i) {
				dstroke.set(i, dstroke.getX(i) + tangent.strokeAt(j).getX(i),
						dstroke.getY(i) + tangent.strokeAt(j).getY(i));
			}
		}
	}

	public Symbol tangentTansformationSymbol(final double alpha) {
		final Symbol tangent = new Symbol(this);
		tangent.tangentTansformation(alpha);
		return tangent;
	}

	@Override
	public String toString() {
		String str = "";
		str = str + "Symbol[name=" + name + ", minX=" + minX + ", minY=" + minY
				+ ", maxX=" + maxX + ", maxY=" + maxY + "]";
		return str;
	}

	public StringBuffer toStringBuffer() {
		final StringBuffer strb = new StringBuffer();
		DStroke.writeSize(Symbol.SIZE);
		DStroke.writeTime(Symbol.TIME);
		strb.append(name + " " + size() + " " + classifierType + "\n");
		for (int i = 0; i < size(); ++i) {
			strb.append(strokeAt(i).toStringBuffer().toString());
		}
		return strb;
	}

	public void translate(final double deltaX, final double deltaY) {
		for (int i = 0; i < size(); ++i) {
			strokeAt(i).translate(deltaX, deltaY);
		}
		minX += deltaX;
		maxX += deltaX;
		minY += deltaY;
		maxY += deltaY;
	}

	public void write(final DataOutputStream fileout) throws IOException {
		DStroke.writeSize(Symbol.SIZE);
		DStroke.writeTime(Symbol.TIME);
		fileout.writeBytes(name + " " + size() + " " + classifierType);
		if (features != null) {
			for (int i = 0; i < features.length; ++i) {
				fileout.writeBytes(" " + (float) features[i]);
			}
		}
		fileout.writeBytes("\n");
		for (int i = 0; i < size(); ++i) {
			strokeAt(i).write(fileout);
		}
	}

	public static void writeSize(final boolean b) {
		Symbol.SIZE = b;
	}

	public static void writeTime(final boolean b) {
		Symbol.TIME = b;
	}
}

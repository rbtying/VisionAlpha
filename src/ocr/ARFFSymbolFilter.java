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

import math.*;
import java.util.*;

import weka.core.*;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;
import java.io.*;

public class ARFFSymbolFilter {
	static boolean accumulatedAngle;
	static double alpha;
	static boolean areaPoly2RelativeToBoundingBox;
	static boolean aspect;
	static boolean centerOfGravity;
	static boolean cluster;
	static boolean costurning;
	static boolean costurningchange;
	static boolean curliness;
	static boolean dehook;
	static boolean equidistant;
	static boolean filter;
	static boolean firstLastPointsDistanceRelativeToLength;
	static boolean getCurvature;
	static boolean getCurvature2;
	static boolean getX;
	static boolean getY;
	static boolean globalCos;
	static boolean globalSin;
	static boolean labelOut;
	static boolean length;
	static boolean lengthPosition;
	static boolean linearity;
	static int na;
	static int nc;
	static boolean normalizedDirection;
	static int np;
	static int npeq;
	static int ns;
	static boolean orderStrokes;
	static boolean proyectionPolygonal;
	static boolean random;
	static boolean relativeFirstLastPointDistance;
	static boolean relativeHeight;
	static boolean relativeLengthFirstLastPointDistance;
	static boolean relativeWidth;
	static Random rnd;
	static boolean scale;
	static double sigma;
	static boolean sinturning;
	static boolean sinturningchange;
	static boolean smooth;
	static boolean strokenumber;
	static boolean sumDistanceError;
	static boolean tangent;
	static int times;

	static {
		ARFFSymbolFilter.alpha = 0.15;
		ARFFSymbolFilter.na = -1;
		ARFFSymbolFilter.np = 24;
		ARFFSymbolFilter.npeq = 32;
		ARFFSymbolFilter.nc = 80;
		ARFFSymbolFilter.ns = -1;
		ARFFSymbolFilter.times = 1;
		ARFFSymbolFilter.labelOut = false;
		ARFFSymbolFilter.random = false;
		ARFFSymbolFilter.sigma = 0.15;
		ARFFSymbolFilter.rnd = new Random();
		ARFFSymbolFilter.centerOfGravity = false;
		ARFFSymbolFilter.accumulatedAngle = true;
		ARFFSymbolFilter.areaPoly2RelativeToBoundingBox = true;
		ARFFSymbolFilter.aspect = true;
		ARFFSymbolFilter.curliness = true;
		ARFFSymbolFilter.firstLastPointsDistanceRelativeToLength = true;
		ARFFSymbolFilter.getCurvature = false;
		ARFFSymbolFilter.getCurvature2 = true;
		ARFFSymbolFilter.relativeWidth = false;
		ARFFSymbolFilter.relativeHeight = false;
		ARFFSymbolFilter.globalCos = false;
		ARFFSymbolFilter.globalSin = false;
		ARFFSymbolFilter.length = true;
		ARFFSymbolFilter.linearity = true;
		ARFFSymbolFilter.relativeFirstLastPointDistance = false;
		ARFFSymbolFilter.relativeLengthFirstLastPointDistance = false;
		ARFFSymbolFilter.sumDistanceError = false;
		ARFFSymbolFilter.smooth = true;
		ARFFSymbolFilter.proyectionPolygonal = false;
		ARFFSymbolFilter.dehook = true;
		ARFFSymbolFilter.equidistant = true;
		ARFFSymbolFilter.cluster = true;
		ARFFSymbolFilter.tangent = true;
		ARFFSymbolFilter.filter = false;
		ARFFSymbolFilter.normalizedDirection = true;
		ARFFSymbolFilter.orderStrokes = true;
		ARFFSymbolFilter.scale = true;
		ARFFSymbolFilter.getX = true;
		ARFFSymbolFilter.getY = true;
		ARFFSymbolFilter.lengthPosition = false;
		ARFFSymbolFilter.costurning = true;
		ARFFSymbolFilter.sinturning = true;
		ARFFSymbolFilter.costurningchange = true;
		ARFFSymbolFilter.sinturningchange = true;
		ARFFSymbolFilter.strokenumber = true;
	}

	public static FastVector constructAttributes(final int n) {
		final FastVector fv = new FastVector();
		boolean oldds = false;
		for (int j = 0; j < n; ++j) {
			if (ARFFSymbolFilter.centerOfGravity) {
				fv.addElement(new Attribute("centerOfGravityX"));
			}
			if (ARFFSymbolFilter.centerOfGravity) {
				fv.addElement(new Attribute("centerOfGravityY"));
			}
			if (ARFFSymbolFilter.accumulatedAngle) {
				fv.addElement(new Attribute("accumulatedAngle"));
			}
			if (ARFFSymbolFilter.areaPoly2RelativeToBoundingBox) {
				fv.addElement(new Attribute("areaPoly2RelativeToBoundingBox"));
			}
			if (ARFFSymbolFilter.aspect) {
				fv.addElement(new Attribute("aspect"));
			}
			if (ARFFSymbolFilter.curliness) {
				fv.addElement(new Attribute("curliness"));
			}
			if (ARFFSymbolFilter.firstLastPointsDistanceRelativeToLength) {
				fv.addElement(new Attribute(
						"firstLastPointsDistanceRelativeToLength"));
			}
			if (ARFFSymbolFilter.getCurvature) {
				fv.addElement(new Attribute("getCurvature"));
			}
			if (ARFFSymbolFilter.getCurvature2) {
				fv.addElement(new Attribute("getCurvature2"));
			}
			if (ARFFSymbolFilter.relativeWidth) {
				fv.addElement(new Attribute("relativeWidth"));
			}
			if (ARFFSymbolFilter.relativeHeight) {
				fv.addElement(new Attribute("relativeHeight"));
			}
			if (ARFFSymbolFilter.globalCos) {
				fv.addElement(new Attribute("globalCos"));
			}
			if (ARFFSymbolFilter.globalSin) {
				fv.addElement(new Attribute("globalSin"));
			}
			if (ARFFSymbolFilter.length) {
				fv.addElement(new Attribute("length"));
			}
			if (ARFFSymbolFilter.linearity) {
				fv.addElement(new Attribute("linearity"));
			}
			if (ARFFSymbolFilter.relativeFirstLastPointDistance) {
				fv.addElement(new Attribute("relativeFirstLastPointDistance"));
			}
			if (ARFFSymbolFilter.relativeLengthFirstLastPointDistance) {
				fv.addElement(new Attribute(
						"relativeLengthFirstLastPointDistance"));
			}
			if (ARFFSymbolFilter.sumDistanceError) {
				fv.addElement(new Attribute("sumDistanceError"));
			}
			if (oldds) {
				fv.addElement(new Attribute(
						"ds.first().distance(oldds.first())"));
				fv.addElement(new Attribute("ds.first().distance(oldds.last())"));
				fv.addElement(new Attribute("ds.last().distance(oldds.first())"));
				fv.addElement(new Attribute("ds.last().distance(oldds.last())"));
			}
			for (int i = 0; i < ARFFSymbolFilter.np; ++i) {
				if (ARFFSymbolFilter.getX) {
					fv.addElement(new Attribute("strokeAt(" + j + ").getX(" + i
							+ ")"));
				}
				if (ARFFSymbolFilter.getY) {
					fv.addElement(new Attribute("strokeAt(" + j + ").getY(" + i
							+ ")"));
				}
				if (ARFFSymbolFilter.lengthPosition && i != 0
						&& i != ARFFSymbolFilter.np - 1) {
					fv.addElement(new Attribute("strokeAt(" + j
							+ ").lengthPosition(" + i + ")"));
				}
				if (i < ARFFSymbolFilter.np - 1) {
					if (ARFFSymbolFilter.costurning) {
						fv.addElement(new Attribute("strokeAt(" + j
								+ ").cos(a[" + i + "])"));
					}
					if (ARFFSymbolFilter.sinturning) {
						fv.addElement(new Attribute("strokeAt(" + j
								+ ").sin(a[" + i + "])"));
					}
				}
				if (i < ARFFSymbolFilter.np - 2) {
					if (ARFFSymbolFilter.costurningchange) {
						fv.addElement(new Attribute("strokeAt(" + j
								+ ").cos(a[" + (i + 1) + "]-a[" + i + "])"));
					}
					if (ARFFSymbolFilter.sinturningchange) {
						fv.addElement(new Attribute("strokeAt(" + j
								+ ").sin(a[" + (i + 1) + "]-a[" + i + "])"));
					}
				}
			}
			oldds = true;
		}
		fv.addElement(new Attribute("class", (FastVector) null));
		return fv;
	}

	public static Instance constructInstance(final Symbol s, final double alph) {
		final StringTokenizer st = new StringTokenizer(constructStringInstance(
				s, alph), " ");
		ARFFSymbolFilter.na = st.countTokens() / 2;
		if (ARFFSymbolFilter.na == 0) {
			return null;
		}
		final Instance instance = new Instance(ARFFSymbolFilter.na + 1);
		for (int i = 0; i < ARFFSymbolFilter.na; ++i) {
			st.nextToken();
			instance.setValue(i, Double.parseDouble(st.nextToken()));
		}
		return instance;
	}

	public static String constructStringInstance(final Symbol s,
			final double alph) {
		String str = null;
		Symbol newSymbol = new Symbol();
		newSymbol = new Symbol();
		for (int i = 0; i < s.size(); ++i) {
			DStroke ds = new DStroke(s.strokeAt(i));
			if (ds.getWidth() < 5.0 && ds.getHeight() < 5.0 && ds.size() < 3) {
				return null;
			}
			try {
				if (ARFFSymbolFilter.smooth) {
					ds = ds.smooth();
				}
				if (ARFFSymbolFilter.proyectionPolygonal) {
					ds = ds.proyectionPolygonal(ARFFSymbolFilter.np);
				}
				if (ARFFSymbolFilter.cluster) {
					ds = ds.clusterPoints(1.0 / ARFFSymbolFilter.nc);
				}
				if (ARFFSymbolFilter.equidistant) {
					ds = ds.equidistantLength(ARFFSymbolFilter.np);
				}
				if (ARFFSymbolFilter.dehook) {
					ds = ds.getDehooked();
					if (ds.size() < ARFFSymbolFilter.np) {
						if (ARFFSymbolFilter.equidistant) {
							ds = ds.equidistantLength(ARFFSymbolFilter.np);
						} else {
							ds = ds.proyectionPolygonal(ARFFSymbolFilter.np);
						}
					}
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				return null;
			}
			newSymbol.add(ds);
		}
		if (alph != 0.0 && ARFFSymbolFilter.tangent) {
			newSymbol.tangentTansformation(alph);
		}
		if (ARFFSymbolFilter.filter) {
			newSymbol.filter();
		}
		if (ARFFSymbolFilter.normalizedDirection) {
			newSymbol.normalizeDirection();
		}
		if (ARFFSymbolFilter.orderStrokes) {
			newSymbol.orderStrokes();
		}
		if (ARFFSymbolFilter.scale) {
			newSymbol.scale();
		}
		str = "";
		DStroke oldds = null;
		for (int j = 0; j < newSymbol.size(); ++j) {
			final DStroke ds = newSymbol.strokeAt(j);
			if (ARFFSymbolFilter.centerOfGravity) {
				str = str + "centerOfGravityX "
						+ (float) ds.centerOfGravity().x + " ";
			}
			if (ARFFSymbolFilter.centerOfGravity) {
				str = str + "centerOfGravityY "
						+ (float) ds.centerOfGravity().y + " ";
			}
			if (ARFFSymbolFilter.accumulatedAngle) {
				str = str + "accumulatedAngle " + (float) ds.accumulatedAngle()
						+ " ";
			}
			if (ARFFSymbolFilter.areaPoly2RelativeToBoundingBox) {
				str = str + "areaPoly2RelativeToBoundingBox "
						+ (float) ds.areaPoly2RelativeToBoundingBox() + " ";
			}
			if (ARFFSymbolFilter.aspect) {
				str = str + "aspect " + (float) ds.aspect() + " ";
			}
			if (ARFFSymbolFilter.curliness) {
				str = str + "curliness " + (float) ds.curliness() + " ";
			}
			if (ARFFSymbolFilter.firstLastPointsDistanceRelativeToLength) {
				str = str + "firstLastPointsDistanceRelativeToLength "
						+ (float) ds.firstLastPointsDistanceRelativeToLength()
						+ " ";
			}
			if (ARFFSymbolFilter.getCurvature) {
				str = str + "getCurvature " + (float) ds.getCurvature() + " ";
			}
			if (ARFFSymbolFilter.getCurvature2) {
				str = str + "getCurvature2 " + (float) ds.getCurvature2() + " ";
			}
			if (ARFFSymbolFilter.relativeWidth) {
				str = str + "relativeWidth " + (float) ds.getWidth()
						/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			}
			if (ARFFSymbolFilter.relativeHeight) {
				str = str + "relativeHeight " + (float) ds.getHeight()
						/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			}
			if (ARFFSymbolFilter.globalCos) {
				str = str + "globalCos " + (float) ds.globalCos() + " ";
			}
			if (ARFFSymbolFilter.globalSin) {
				str = str + "globalSin " + (float) ds.globalSin() + " ";
			}
			if (ARFFSymbolFilter.length) {
				str = str + "length " + (float) ds.length() + " ";
			}
			if (ARFFSymbolFilter.linearity) {
				str = str + "linearity " + (float) ds.linearity() + " ";
			}
			if (ARFFSymbolFilter.relativeFirstLastPointDistance) {
				str = str + "relativeFirstLastPointDistance "
						+ (float) ds.relativeFirstLastPointDistance() + " ";
			}
			if (ARFFSymbolFilter.relativeLengthFirstLastPointDistance) {
				str = str + "relativeLengthFirstLastPointDistance "
						+ (float) ds.relativeLengthFirstLastPointDistance()
						+ " ";
			}
			if (ARFFSymbolFilter.sumDistanceError) {
				str = str + "sumDistanceError " + (float) ds.sumDistanceError()
						+ " ";
			}
			if (oldds != null) {
				str = str + "ds.first().distance(oldds.first()) "
						+ (float) ds.first().distance(oldds.first()) + " ";
				str = str + "ds.first().distance(oldds.last()) "
						+ (float) ds.first().distance(oldds.last()) + " ";
				str = str + "ds.last().distance(oldds.first()) "
						+ (float) ds.last().distance(oldds.first()) + " ";
				str = str + "ds.last().distance(oldds.last()) "
						+ (float) ds.last().distance(oldds.last()) + " ";
			}
			double d;
			double lp = d = ((0.0));
			final double[] a = ds.turningFunction();
			for (int i = 0; i < ds.size(); ++i) {
				if (ARFFSymbolFilter.getX) {
					str = str + "strokeAt(" + j + ").getX(" + i + ") "
							+ (float) ds.getX(i) + " ";
				}
				if (ARFFSymbolFilter.getY) {
					str = str + "strokeAt(" + j + ").getY(" + i + ") "
							+ (float) ds.getY(i) + " ";
				}
				lp += d;
				if (ARFFSymbolFilter.lengthPosition && i != 0
						&& i != ds.size() - 1) {
					str = str + "strokeAt(" + j + ").lengthPosition(" + i
							+ ") " + (float) lp + " ";
				}
				if (ARFFSymbolFilter.linearity) {
					Geom.height(ds.first(), ds.last(), ds.pointAt(i));
				}
				if (i < ds.size() - 1) {
					d = ds.length(i, i + 1);
					final double cos1 = Math.cos(a[i]);
					final double sin1 = Math.sin(a[i]);
					if (ARFFSymbolFilter.costurning) {
						str = str + "strokeAt(" + j + ").cos(a[" + i + "]) "
								+ (float) cos1 + " ";
					}
					if (ARFFSymbolFilter.sinturning) {
						str = str + "strokeAt(" + j + ").sin(a[" + i + "]) "
								+ (float) sin1 + " ";
					}
				}
				if (i < ds.size() - 2) {
					final double cos2 = Math.cos(a[i + 1] - a[i]);
					final double sin2 = Math.sin(a[i + 1] - a[i]);
					final double diffcos = cos2;
					final double diffsin = sin2;
					if (ARFFSymbolFilter.costurningchange) {
						str = str + "strokeAt(" + j + ").cos(a[" + (i + 1)
								+ "]-a[" + i + "]) " + (float) diffcos + " ";
					}
					if (ARFFSymbolFilter.sinturningchange) {
						str = str + "strokeAt(" + j + ").sin(a[" + (i + 1)
								+ "]-a[" + i + "]) " + (float) diffsin + " ";
					}
				}
			}
			oldds = ds;
		}
		return str;
	}

	public static String constructStringSparseVector(final Symbol s,
			final double alph) {
		String str = "";
		final StringTokenizer st = new StringTokenizer(constructStringInstance(
				s, alph), " ");
		final int na = st.countTokens() / 2;
		if (na == 0) {
			return null;
		}
		for (int i = 0; i < na; ++i) {
			st.nextToken();
			str = str + " " + i + ":" + st.nextToken();
		}
		str = s.name + str;
		return str;
	}

	public static String getProcessedSingle(final Symbol s) {
		return getProcessedSingle(s, 0.0);
	}

	public static String getProcessedSingle(final Symbol s, final double alph) {
		Symbol newSymbol = new Symbol();
		newSymbol = new Symbol();
		for (int i = 0; i < s.size(); ++i) {
			DStroke ds = new DStroke(s.strokeAt(i));
			if (ds.getWidth() < 5.0 && ds.getHeight() < 5.0 && ds.size() < 3) {
				return "";
			}
			try {
				if (ARFFSymbolFilter.smooth) {
					ds = ds.smooth();
				}
				if (ARFFSymbolFilter.proyectionPolygonal) {
					ds = ds.proyectionPolygonal(ARFFSymbolFilter.np);
				}
				if (ARFFSymbolFilter.cluster) {
					ds = ds.clusterPoints(1.0 / ARFFSymbolFilter.nc);
				}
				if (ARFFSymbolFilter.dehook) {
					ds = ds.getDehooked();
				}
				if (ARFFSymbolFilter.equidistant) {
					ds = ds.equidistantLength(ARFFSymbolFilter.np);
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				return "";
			}
			newSymbol.add(ds);
		}
		if (alph != 0.0 && ARFFSymbolFilter.tangent) {
			newSymbol.tangentTansformation(alph);
		}
		if (ARFFSymbolFilter.filter) {
			newSymbol.filter();
		}
		if (ARFFSymbolFilter.normalizedDirection) {
			newSymbol.normalizeDirection();
		}
		if (ARFFSymbolFilter.orderStrokes) {
			newSymbol.orderStrokes();
		}
		if (ARFFSymbolFilter.scale) {
			newSymbol.scale();
		}
		String str = "";
		int count = 0;
		DStroke oldds = null;
		for (int j = 0; j < newSymbol.size(); ++j) {
			final DStroke ds = newSymbol.strokeAt(j);
			if (ARFFSymbolFilter.centerOfGravity) {
				str = str + "" + count++ + ":" + (float) ds.centerOfGravity().x
						+ " ";
			}
			if (ARFFSymbolFilter.centerOfGravity) {
				str = str + "" + count++ + ":" + (float) ds.centerOfGravity().y
						+ " ";
			}
			if (ARFFSymbolFilter.accumulatedAngle) {
				str = str + "" + count++ + ":" + (float) ds.accumulatedAngle()
						+ " ";
			}
			if (ARFFSymbolFilter.areaPoly2RelativeToBoundingBox) {
				str = str + "" + count++ + ":"
						+ (float) ds.areaPoly2RelativeToBoundingBox() + " ";
			}
			if (ARFFSymbolFilter.aspect) {
				str = str + "" + count++ + ":" + (float) ds.aspect() + " ";
			}
			if (ARFFSymbolFilter.curliness) {
				str = str + "" + count++ + ":" + (float) ds.curliness() + " ";
			}
			if (ARFFSymbolFilter.firstLastPointsDistanceRelativeToLength) {
				str = str + "" + count++ + ":"
						+ (float) ds.firstLastPointsDistanceRelativeToLength()
						+ " ";
			}
			if (ARFFSymbolFilter.getCurvature) {
				str = str + "" + count++ + ":" + (float) ds.getCurvature()
						+ " ";
			}
			if (ARFFSymbolFilter.getCurvature2) {
				str = str + "" + count++ + ":" + (float) ds.getCurvature2()
						+ " ";
			}
			if (ARFFSymbolFilter.relativeWidth) {
				str = str + "" + count++ + ":" + (float) ds.getWidth()
						/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			}
			if (ARFFSymbolFilter.relativeHeight) {
				str = str + "" + count++ + ":" + (float) ds.getHeight()
						/ Math.max(ds.getWidth(), ds.getHeight()) + " ";
			}
			if (ARFFSymbolFilter.globalCos) {
				str = str + "" + count++ + ":" + (float) ds.globalCos() + " ";
			}
			if (ARFFSymbolFilter.globalSin) {
				str = str + "" + count++ + ":" + (float) ds.globalSin() + " ";
			}
			if (ARFFSymbolFilter.length) {
				str = str + "" + count++ + ":" + (float) ds.length() + " ";
			}
			if (ARFFSymbolFilter.linearity) {
				str = str + "" + count++ + ":" + (float) ds.linearity() + " ";
			}
			if (ARFFSymbolFilter.relativeFirstLastPointDistance) {
				str = str + "" + count++ + ":"
						+ (float) ds.relativeFirstLastPointDistance() + " ";
			}
			if (ARFFSymbolFilter.relativeLengthFirstLastPointDistance) {
				str = str + "" + count++ + ":"
						+ (float) ds.relativeLengthFirstLastPointDistance()
						+ " ";
			}
			if (ARFFSymbolFilter.sumDistanceError) {
				str = str + "" + count++ + ":" + (float) ds.sumDistanceError()
						+ " ";
			}
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
			double d;
			double lp = d = ((0.0));
			final double[] a = ds.turningFunction();
			for (int i = 0; i < ds.size(); ++i) {
				if (ARFFSymbolFilter.getX) {
					str = str + "" + count++ + ":" + (float) ds.getX(i) + " ";
				}
				if (ARFFSymbolFilter.getY) {
					str = str + "" + count++ + ":" + (float) ds.getY(i) + " ";
				}
				lp += d;
				if (ARFFSymbolFilter.lengthPosition && i != 0
						&& i != ds.size() - 1) {
					str = str + "" + count++ + ":" + (float) lp + " ";
				}
				if (ARFFSymbolFilter.linearity) {
					Geom.height(ds.first(), ds.last(), ds.pointAt(i));
				}
				if (i < ds.size() - 1) {
					d = ds.length(i, i + 1);
					final double cos1 = Math.cos(a[i]);
					final double sin1 = Math.sin(a[i]);
					if (ARFFSymbolFilter.costurning) {
						str = str + "" + count++ + ":" + (float) cos1 + " ";
					}
					if (ARFFSymbolFilter.sinturning) {
						str = str + "" + count++ + ":" + (float) sin1 + " ";
					}
				}
				if (i < ds.size() - 2) {
					final double cos2 = Math.cos(a[i + 1] - a[i]);
					final double sin2 = Math.sin(a[i + 1] - a[i]);
					final double diffcos = cos2;
					final double diffsin = sin2;
					if (ARFFSymbolFilter.costurningchange) {
						str = str + "" + count++ + ":" + (float) diffcos + " ";
					}
					if (ARFFSymbolFilter.sinturningchange) {
						str = str + "" + count++ + ":" + (float) diffsin + " ";
					}
				}
			}
			oldds = ds;
		}
		return str;
	}

	public static String oldReplace(final String aInput,
			final String aOldPattern, final String aNewPattern) {
		final StringBuffer result = new StringBuffer();
		int startIdx = 0;
		for (int idxOld = 0; (idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0; startIdx = idxOld
				+ aOldPattern.length()) {
			result.append(aInput.substring(startIdx, idxOld));
			result.append(aNewPattern);
		}
		result.append(aInput.substring(startIdx));
		return result.toString();
	}

	public static ArrayList<ArrayList<Symbol>> read(BufferedReader filein) {
		StringTokenizer st;
		String line;
		ArrayList<ArrayList<Symbol>> symbolData = new ArrayList<ArrayList<Symbol>>();
		Symbol sym;
		ArrayList<Symbol> group;
		int nsim, i, count;

		try {
			while ((line = filein.readLine()) != null) {
				st = new StringTokenizer(line, " \n\t\r\f");
				count = st.countTokens();

				line = "";
				for (i = 0; i < count - 1; i++) {
					line += st.nextToken() + " ";
				}

				nsim = Integer.parseInt(st.nextToken());
				group = new ArrayList<Symbol>();
				for (i = 0; i < nsim; i++) {
					sym = Symbol.readSymbol(filein);
					System.out.print(".");
					if (sym.size() == 0 || sym.name.equals("no_name")) {
						continue;
					}
					ns = Math.max(ns, sym.size());
					group.add(new Symbol(sym));
				}
				symbolData.add(group);
			}
			System.out.println(".");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return symbolData;
	}

	public static ArrayList<ArrayList<Symbol>> read(final String filenamein) {
		BufferedReader filein = null;
		ArrayList<ArrayList<Symbol>> v = new ArrayList<ArrayList<Symbol>>();
		try {
			filein = new BufferedReader(new FileReader(filenamein));
			System.out.println("Reading file " + filenamein);
			v = read(filein);
		} catch (IOException ioe) {
			System.out.println("Error reading " + filenamein + ".");
			ioe.printStackTrace();
		}
		return v;
	}

	public static ArrayList<?> readOld(final String filenamein) {
		BufferedReader filein = null;
		ArrayList<?> v = new ArrayList<Object>();
		try {
			filein = new BufferedReader(new FileReader(filenamein));
			System.out.println("Reading file " + filenamein);
			v = readOld(filein);
		} catch (IOException ioe) {
			System.out.println("Error reading " + filenamein + ".");
			ioe.printStackTrace();
		}
		return v;
	}

	public static ArrayList<ArrayList<Symbol>> readOld(
			final BufferedReader filein) throws IOException {
		final ArrayList<String> symbolNames = new ArrayList<String>();
		final ArrayList<ArrayList<Symbol>> symbolData = new ArrayList<ArrayList<Symbol>>();
		final String line;
		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			return symbolData;
		}
		StringTokenizer st = new StringTokenizer(line, " \n\t\r\f");
		int size = st.countTokens();
		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			return symbolData;
		}
		st.nextToken();
		final int numGroups = Integer.parseInt(st.nextToken());
		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		for (int i = 0; i < size; ++i) {
			symbolNames.add(st.nextToken());
		}
		for (int j = 0; j < numGroups; ++j) {
			final ArrayList<Symbol> actualGroup = new ArrayList<Symbol>();
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			st.nextToken();
			for (int k = 0; k < size; ++k) {
				final Symbol actualSymbol = new Symbol();
				actualSymbol.read(filein);
				actualSymbol.name = new String(symbolNames.get(k));
				System.out.print(".");
				if (actualSymbol.size() != 0) {
					if (!actualSymbol.name.equals("no_name")) {
						actualGroup.add(actualSymbol);
						ARFFSymbolFilter.ns = Math.max(ARFFSymbolFilter.ns,
								actualSymbol.size());
					}
				}
			}
			symbolData.add(actualGroup);
		}
		System.out.println(".");
		return symbolData;
	}

	public static void write(final String filenameout,
			final ArrayList<?> symbolData) {
		final int nsold = ARFFSymbolFilter.ns;
		ARFFSymbolFilter.tangent = (ARFFSymbolFilter.times > 1);
		try {
			if (!ARFFSymbolFilter.strokenumber) {
				ARFFSymbolFilter.ns = 1;
			}
			final DataOutputStream[] fileout = new DataOutputStream[ARFFSymbolFilter.ns];
			System.out.println("Writing file");
			for (int i = 0; i < ARFFSymbolFilter.ns; ++i) {
				final int k = ARFFSymbolFilter.strokenumber ? i : (nsold - 1);
				fileout[ARFFSymbolFilter.strokenumber ? i : 0] = new DataOutputStream(
						new FileOutputStream(
								filenameout
										+ (ARFFSymbolFilter.strokenumber ? ("" + (k + 1))
												: "") + ".sv"));
				constructAttributes(k + 1);
			}
			final int tot = symbolData.size();
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<?> group = (ArrayList<?>) symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = (Symbol) group.get(i);
					final int k = ARFFSymbolFilter.strokenumber ? (sym.size() - 1)
							: 0;
					if (sym.name.equals("no_name")
							|| sym.name.equals("empty_symbol")) {
						System.out.print("#" + sym.name + "#");
					} else {
						for (int t = 0; t < ARFFSymbolFilter.times; ++t) {
							final String line = constructStringSparseVector(
									sym, ARFFSymbolFilter.alpha);
							if (line == null) {
								System.out.print("inst=null");
							} else {
								try {
									oldReplace(line, "\\", "");
								} catch (ArrayIndexOutOfBoundsException aioobe) {
									System.out.print("!");
								}
								fileout[k].writeBytes(line + "\n");
							}
						}
					}
				}
				if ((int) (100.0 * j) / tot % 10 == 0) {
					System.out.print((int) (100.0 * j) / tot + "%-");
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				fileout[k].close();
			}
			System.out.println("100.0%");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public static void writeData(final String filenameout,
			final ArrayList<ArrayList<Symbol>> symbolData) {
		ARFFSymbolFilter.tangent = (ARFFSymbolFilter.times > 1);
		try {
			final DataOutputStream[] fileout = new DataOutputStream[ARFFSymbolFilter.ns];
			System.out.println("Writing file");

			@SuppressWarnings("unchecked")
			final ArrayList<String>[] names = new ArrayList[ARFFSymbolFilter.ns];
			final int[] ndata = new int[ARFFSymbolFilter.ns];
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				names[k] = new ArrayList<String>();
			}
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<Symbol> group = symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = group.get(i);
					final int k = sym.size() - 1;
					try {
						int l;
						for (l = 0; l < names[k].size()
								&& !names[k].get(l).equals(sym.name); ++l) {
						}
						if (l == names[k].size()) {
							names[k].add(sym.name);
						}
						final int[] array = ndata;
						final int n = k;
						++array[n];
					} catch (ArrayIndexOutOfBoundsException ex) {
						ex.printStackTrace();
					}
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				fileout[k] = new DataOutputStream(new FileOutputStream(
						filenameout + k + ".dat"));
			}
			int nex = 0;
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<?> group = symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = (Symbol) group.get(i);
					final int k = sym.size() - 1;
					if (sym.name.equals("no_name")
							|| sym.name.equals("empty_symbol")) {
						System.out.print("#" + sym.name + "#");
					} else {
						for (int t = 0; t < ARFFSymbolFilter.times; ++t) {
							System.out.print(".");
							String line;
							try {
								line = getProcessedSingle(sym,
										ARFFSymbolFilter.alpha);
							} catch (Exception ex2) {
								System.out.print("*");
								++nex;
								continue;
							}
							if (ARFFSymbolFilter.labelOut) {
								int l;
								for (l = 0; l < names[k].size()
										&& !names[k].get(l).equals(sym.name); ++l) {
								}
								fileout[k].writeBytes(l + " ");
							} else {
								fileout[k].writeBytes(sym.name + " ");
							}
							fileout[k].writeBytes(line + "\n");
						}
					}
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				fileout[k].close();
			}
			if (nex != 0) {
				System.out.println("getProcessedSingle(Symbol,double) throwed "
						+ nex + " Exceptions while processing your data.");
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				fileout[k] = new DataOutputStream(new FileOutputStream(
						filenameout + k + ".names"));
				for (int l = 0; l < names[k].size(); ++l) {
					fileout[k].writeBytes(names[k].get(l) + " ");
				}
				fileout[k].writeBytes("\n");
				fileout[k].close();
			}
			System.out.println();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void writeGini(final String filenameout,
			final ArrayList<ArrayList<Symbol>> symbolData) {
		double noise = 0.0;
		ARFFSymbolFilter.tangent = (ARFFSymbolFilter.times > 1);
		try {
			final DataOutputStream[] fileout = new DataOutputStream[ARFFSymbolFilter.ns];

			@SuppressWarnings("unchecked")
			final ArrayList<String>[] names = new ArrayList[ARFFSymbolFilter.ns];
			final int[] count = new int[ARFFSymbolFilter.ns];
			final int[] ndata = new int[ARFFSymbolFilter.ns];
			System.out.println("Writing file");
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				names[k] = new ArrayList<String>();
			}
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<Symbol> group = symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = group.get(i);
					final int k = sym.size() - 1;
					try {
						int l;
						for (l = 0; l < names[k].size()
								&& !names[k].get(l).equals(sym.name); ++l) {
						}
						if (l == names[k].size()) {
							names[k].add(sym.name);
						}
						final int[] array = ndata;
						final int n = k;
						++array[n];
					} catch (ArrayIndexOutOfBoundsException ex) {
						ex.printStackTrace();
					}
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				boolean oldds = false;
				fileout[k] = new DataOutputStream(new FileOutputStream(
						filenameout + (k + 1) + ".gini"));
				for (int j = 0; j < k + 1; ++j) {
					if (ARFFSymbolFilter.centerOfGravity) {
						final int[] array2 = count;
						final int n2 = k;
						++array2[n2];
					}
					if (ARFFSymbolFilter.centerOfGravity) {
						final int[] array3 = count;
						final int n3 = k;
						++array3[n3];
					}
					if (ARFFSymbolFilter.accumulatedAngle) {
						final int[] array4 = count;
						final int n4 = k;
						++array4[n4];
					}
					if (ARFFSymbolFilter.areaPoly2RelativeToBoundingBox) {
						final int[] array5 = count;
						final int n5 = k;
						++array5[n5];
					}
					if (ARFFSymbolFilter.aspect) {
						final int[] array6 = count;
						final int n6 = k;
						++array6[n6];
					}
					if (ARFFSymbolFilter.curliness) {
						final int[] array7 = count;
						final int n7 = k;
						++array7[n7];
					}
					if (ARFFSymbolFilter.firstLastPointsDistanceRelativeToLength) {
						final int[] array8 = count;
						final int n8 = k;
						++array8[n8];
					}
					if (ARFFSymbolFilter.getCurvature) {
						final int[] array9 = count;
						final int n9 = k;
						++array9[n9];
					}
					if (ARFFSymbolFilter.getCurvature2) {
						final int[] array10 = count;
						final int n10 = k;
						++array10[n10];
					}
					if (ARFFSymbolFilter.relativeWidth) {
						final int[] array11 = count;
						final int n11 = k;
						++array11[n11];
					}
					if (ARFFSymbolFilter.relativeHeight) {
						final int[] array12 = count;
						final int n12 = k;
						++array12[n12];
					}
					if (ARFFSymbolFilter.globalCos) {
						final int[] array13 = count;
						final int n13 = k;
						++array13[n13];
					}
					if (ARFFSymbolFilter.globalSin) {
						final int[] array14 = count;
						final int n14 = k;
						++array14[n14];
					}
					if (ARFFSymbolFilter.length) {
						final int[] array15 = count;
						final int n15 = k;
						++array15[n15];
					}
					if (ARFFSymbolFilter.linearity) {
						final int[] array16 = count;
						final int n16 = k;
						++array16[n16];
					}
					if (ARFFSymbolFilter.relativeFirstLastPointDistance) {
						final int[] array17 = count;
						final int n17 = k;
						++array17[n17];
					}
					if (ARFFSymbolFilter.relativeLengthFirstLastPointDistance) {
						final int[] array18 = count;
						final int n18 = k;
						++array18[n18];
					}
					if (ARFFSymbolFilter.sumDistanceError) {
						final int[] array19 = count;
						final int n19 = k;
						++array19[n19];
					}
					if (oldds) {
						final int[] array20 = count;
						final int n20 = k;
						++array20[n20];
						final int[] array21 = count;
						final int n21 = k;
						++array21[n21];
						final int[] array22 = count;
						final int n22 = k;
						++array22[n22];
						final int[] array23 = count;
						final int n23 = k;
						++array23[n23];
					}
					for (int i = 0; i < ARFFSymbolFilter.np; ++i) {
						if (ARFFSymbolFilter.getX) {
							final int[] array24 = count;
							final int n24 = k;
							++array24[n24];
						}
						if (ARFFSymbolFilter.getY) {
							final int[] array25 = count;
							final int n25 = k;
							++array25[n25];
						}
						if (ARFFSymbolFilter.lengthPosition && i != 0
								&& i != ARFFSymbolFilter.np - 1) {
							final int[] array26 = count;
							final int n26 = k;
							++array26[n26];
						}
						if (i < ARFFSymbolFilter.np - 1) {
							if (ARFFSymbolFilter.costurning) {
								final int[] array27 = count;
								final int n27 = k;
								++array27[n27];
							}
							if (ARFFSymbolFilter.sinturning) {
								final int[] array28 = count;
								final int n28 = k;
								++array28[n28];
							}
						}
						if (i < ARFFSymbolFilter.np - 2) {
							if (ARFFSymbolFilter.costurningchange) {
								final int[] array29 = count;
								final int n29 = k;
								++array29[n29];
							}
							if (ARFFSymbolFilter.sinturningchange) {
								final int[] array30 = count;
								final int n30 = k;
								++array30[n30];
							}
						}
					}
					oldds = true;
				}
				fileout[k].writeBytes(count[k] + "\n");
				fileout[k].writeBytes(names[k].size() + "\n");
				fileout[k].writeBytes(ndata[k] * ARFFSymbolFilter.times + "\n");
			}
			final int tot = symbolData.size();
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<?> group = symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = (Symbol) group.get(i);
					final int k = sym.size() - 1;
					if (sym.name.equals("no_name")
							|| sym.name.equals("empty_symbol")) {
						System.out.print("#" + sym.name + "#");
					} else {
						for (int t = 0; t < ARFFSymbolFilter.times; ++t) {
							final String line = getProcessedSingle(sym,
									ARFFSymbolFilter.alpha);
							final StringTokenizer st = new StringTokenizer(
									line, " :\n\t");
							final int ntk = st.countTokens() / 2;
							if (ntk == 0) {
								System.out.print("ntk == 0");
							} else if (line.equals("")) {
								System.out.print("line.equals(\"\")");
							} else if (ntk != count[k]) {
								System.out.print("ntk != count[k]");
							} else {
								if (ARFFSymbolFilter.labelOut) {
									int l;
									for (l = 0; l < names[k].size()
											&& !names[k].get(l)
													.equals(sym.name); ++l) {
									}
									fileout[k].writeBytes(l + "\n");
								} else {
									int lrand = -1;
									noise = 0.0;
									if (ARFFSymbolFilter.random) {
										lrand = (int) (names[k].size() * Math
												.random());
										noise = ARFFSymbolFilter.rnd
												.nextDouble()
												* ARFFSymbolFilter.sigma;
									}
									for (int l = 0; l < names[k].size(); ++l) {
										if (names[k].get(l).equals(sym.name)) {
											fileout[k].writeBytes(1.0 - noise
													+ "\n");
										} else if (lrand == l) {
											fileout[k].writeBytes(noise + "\n");
										} else {
											fileout[k].writeBytes("0.0\n");
										}
									}
								}
								fileout[k].writeBytes("1\n");
								for (int l = 0; l < ntk; ++l) {
									st.nextToken();
									fileout[k]
											.writeBytes(st.nextToken() + "\n");
								}
								System.out.print(".");
							}
						}
					}
				}
				if ((int) (100.0 * j) / tot % 10 == 0) {
					System.out.print((int) (100.0 * j) / tot + "%");
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				fileout[k].close();
			}
			System.out.println();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void writeWeka(final String filenameout,
			final ArrayList<?> symbolData) {
		final int nsold = ARFFSymbolFilter.ns;
		ARFFSymbolFilter.tangent = (ARFFSymbolFilter.times > 1);
		try {
			if (!ARFFSymbolFilter.strokenumber) {
				ARFFSymbolFilter.ns = 1;
			}
			final DataOutputStream[] fileout = new DataOutputStream[ARFFSymbolFilter.ns];
			final Instances[] instances = new Instances[ARFFSymbolFilter.ns];
			System.out.println("Writing file");
			for (int i = 0; i < ARFFSymbolFilter.ns; ++i) {
				final int k = ARFFSymbolFilter.strokenumber ? i : (nsold - 1);
				fileout[ARFFSymbolFilter.strokenumber ? i : 0] = new DataOutputStream(
						new FileOutputStream(
								filenameout
										+ (ARFFSymbolFilter.strokenumber ? ("" + (k + 1))
												: "") + ".arff#"));
			}
			final int tot = symbolData.size();
			for (int j = 0; j < symbolData.size(); ++j) {
				final ArrayList<?> group = (ArrayList<?>) symbolData.get(j);
				for (int i = 0; i < group.size(); ++i) {
					final Symbol sym = (Symbol) group.get(i);
					final int k = ARFFSymbolFilter.strokenumber ? (sym.size() - 1)
							: 0;
					if (sym.name.equals("no_name")
							|| sym.name.equals("empty_symbol")) {
						System.out.print("#" + sym.name + "#");
					} else {
						for (int t = 0; t < ARFFSymbolFilter.times; ++t) {
							final String line = constructStringInstance(sym,
									ARFFSymbolFilter.alpha);
							if (line == null) {
								System.out.print("line=null!");
							} else {
								if (instances[k] == null) {
									final StringTokenizer st = new StringTokenizer(
											line, " ");
									final int nt = st.countTokens() / 2;
									final FastVector att = new FastVector();
									for (int kk = 0; kk < nt; ++kk) {
										final String token = st.nextToken();
										att.addElement(new Attribute(
												new String(token)));
										st.nextToken();
									}
									att.addElement(new Attribute("class",
											(FastVector) null));
									(instances[k] = new Instances(
											"Symbols of Size " + (k + 1), att,
											1)).setClassIndex(att.size() - 1);
								}
								final StringTokenizer st = new StringTokenizer(
										line, " ");
								final int nt = st.countTokens() / 2;
								final Instance inst = new Instance(nt + 1);
								for (int kk = 0; kk < nt; ++kk) {
									st.nextToken();
									final String token = new String(
											st.nextToken());
									inst.setValue(kk, Double.parseDouble(token));
								}
								inst.setDataset(instances[k]);
								inst.setClassValue(oldReplace(sym.name, "\\",
										""));
								instances[k].add(inst);
							}
						}
					}
				}
				if ((int) (100.0 * j) / tot % 10 == 0) {
					System.out.print((int) (100.0 * j) / tot + "%-");
				}
			}
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				if (fileout[ARFFSymbolFilter.strokenumber ? k : 0] == null) {
					System.out.println("fo"
							+ fileout[ARFFSymbolFilter.strokenumber ? k : 0]);
				}
				if (instances[ARFFSymbolFilter.strokenumber ? k : 0] == null) {
					System.out.println("in:"
							+ instances[ARFFSymbolFilter.strokenumber ? k : 0]);
				}
				fileout[ARFFSymbolFilter.strokenumber ? k : 0]
						.writeBytes(instances[ARFFSymbolFilter.strokenumber ? k
								: 0].toString());
				fileout[ARFFSymbolFilter.strokenumber ? k : 0].close();
			}
			final StringToNominal filter = new StringToNominal();
			final String[] args = new String[4];
			for (int k = 0; k < ARFFSymbolFilter.ns; ++k) {
				args[0] = "-i";
				args[1] = filenameout
						+ (ARFFSymbolFilter.strokenumber ? ("" + (k + 1)) : "")
						+ ".arff#";
				args[2] = "-o";
				args[3] = filenameout
						+ (ARFFSymbolFilter.strokenumber ? ("" + (k + 1)) : "")
						+ ".arff";
				Filter.filterFile(filter, args);
				new File(args[1]).delete();
			}
			System.out.println("100.0%");
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}
}

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

/**
 * <p>�berschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import DataStructures.BinaryHeap;
import DataStructures.Overflow;
import Jama.Matrix;

public class Tangent {
	public static double DELTA = 0.1;
	public static boolean X_TRANSLATION = true;
	public static boolean Y_TRANSLATION = true;
	public static boolean SCALE = true;
	public static boolean AXIS_DEFORMATION = true;
	public static boolean ROTATION = true;
	public static boolean DIAGONAL_DEFORMATION = true;
	public static boolean SLOPE = true;
	public static int ntrans = 0;
	public static int np = 8;
	BinaryHeap heap;

	@SuppressWarnings("unchecked")
	ArrayList<Symbol> symbols[] = new ArrayList[3];
	static boolean debug = false;

	public Tangent() {
		super();
	}

	public Tangent(String filename) {
		String names[] = new String[1];
		names[0] = filename;
		this.readSymbols(names);
	}

	public static double distance(Symbol sp, Symbol sq) {
		return distance(sp, sq, DELTA);
	}

	public static double distance(Symbol sp, Symbol sq, double delta) {
		DStroke ds;
		Matrix MTp, Malpha, Mp, Mq;
		double Tp[][], p[][], q[][];
		double d = 0, dx, dy, len, c;
		int i, j, k, nt;

		p = getArrayList(sp);
		q = getArrayList(sq);

		ntrans = 0;
		if (X_TRANSLATION) {
			ntrans++;
		}
		if (Y_TRANSLATION) {
			ntrans++;
		}
		if (SCALE) {
			ntrans++;
		}
		if (AXIS_DEFORMATION) {
			ntrans++;
		}
		if (ROTATION) {
			ntrans++;
		}
		if (DIAGONAL_DEFORMATION) {
			ntrans++;
		}
		if (SLOPE) {
			ntrans++;
		}
		if (debug)
			System.out.println("ntrans = " + ntrans);

		Tp = new double[2 * sp.numOfPoints()][ntrans];

		for (k = 0; k < sp.numOfPoints(); k++) {
			nt = 0;
			if (X_TRANSLATION) {
				Tp[2 * k][nt] = 1;
				Tp[2 * k + 1][nt] = 0;
				nt++;
			}
			if (Y_TRANSLATION) {
				Tp[2 * k][nt] = 0;
				Tp[2 * k + 1][nt] = 1;
				nt++;
			}
			if (SCALE) {
				Tp[2 * k][nt] = p[2 * k][0];
				Tp[2 * k + 1][nt] = p[2 * k + 1][0];
				nt++;
			}
			if (AXIS_DEFORMATION) {
				Tp[2 * k][nt] = -p[2 * k][0];
				Tp[2 * k + 1][nt] = p[2 * k + 1][0];
				nt++;
			}
			if (ROTATION) {
				Tp[2 * k][nt] = -p[2 * k + 1][0];
				Tp[2 * k + 1][nt] = p[2 * k][0];
				nt++;
			}
			if (DIAGONAL_DEFORMATION) {
				Tp[2 * k][nt] = p[2 * k + 1][0];
				Tp[2 * k + 1][nt] = p[2 * k][0];
				nt++;
			}

		}

		// tangent transformation
		if (SLOPE) {
			nt = ntrans - 1;
			if (debug)
				System.out.println("ntrans - 1 = " + nt);
			for (j = 0, k = 0; j < sp.size(); j++) {
				ds = sp.strokeAt(j);
				len = ds.length();
				for (i = 0; i < ds.size(); i++) {
					if (i < ds.size() - 1) {
						dx = ds.getX(i + 1) - ds.getX(i);
						dy = ds.getY(i + 1) - ds.getY(i);
						d = Math.sqrt(dx * dx + dy * dy);
						c = len / d;
						if (i > 0) {
							dx = 0.5 * dx;
							dy = 0.5 * dy;
						}
						Tp[2 * k][nt] = dx * c;
						Tp[2 * k + 1][nt] = dy * c;
					}
					if (i > 0) {
						dx = ds.getX(i) - ds.getX(i - 1);
						dy = ds.getY(i) - ds.getY(i - 1);
						d = Math.sqrt(dx * dx + dy * dy);
						c = len / d;
						// c = 32;
						if (i < ds.size() - 1) {
							dx = 0.5 * dx;
							dy = 0.5 * dy;
						}
						Tp[2 * k][nt] = dx * c;
						Tp[2 * k + 1][nt] = dy * c;
					}
					k++;
				}
			}
		}

		Mp = new Matrix(p);
		print(Mp, "Mp");
		Mq = new Matrix(q);
		print(Mp, "Mp");

		MTp = new Matrix(Tp);
		print(MTp, "Mtp");

		Malpha = (MTp.transpose().times(MTp)).inverse();
		print(Malpha, "MTp.transpose().times(MTp)).inverse()");
		print(Mq.minus(Mp), "Mq.minus(Mp)");
		Malpha = Malpha.times(MTp.transpose().times(Mq.minus(Mp)));
		print(Malpha, "MTpp");

		for (i = 0; i < Malpha.getRowDimension(); i++) {
			for (j = 0; j < Malpha.getColumnDimension(); j++) {
				if (Malpha.get(i, j) >= delta && i >= 3) {
					Malpha.set(i, j, delta);
				} else if (Malpha.get(i, j) <= -delta && i >= 3) {
					Malpha.set(i, j, -delta);
				}
				if (debug)
					System.out.print(" " + Malpha.get(i, j));
			}
		}

		print(Malpha, "\nMTpp");

		Malpha = Mp.plus(MTp.times(Malpha).minus(Mq));

		Malpha = Malpha.transpose().times(Malpha.times(0.5));
		print(Malpha, "MTpp");
		if (debug)
			System.out.println("Tangent " + sp.name + " " + sq.name + " "
					+ Malpha.get(0, 0));

		/*
		 * Malpha = Mq.minus(Mp); Malpha =
		 * Malpha.transpose().times(Malpha.times(0.5)); if(debug)
		 * System.out.println
		 * ("Tangent "+sp.name+" "+sq.name+" "+Malpha.get(0,0));
		 */

		return Malpha.get(0, 0);
	}

	public static double[] getAlpha(Symbol sp, Symbol sq) {
		return getAlpha(sp, sq, DELTA);
	}

	public static double[] getAlpha(Symbol sp, Symbol sq, double delta) {
		DStroke ds;
		Matrix MTp, Malpha, Mp, Mq;
		double Tp[][], p[][], q[][], alpha[];
		double d = 0, dx, dy, len, c;
		int i, j, k, nt;

		p = getArrayList(sp);
		q = getArrayList(sq);

		ntrans = 0;
		if (X_TRANSLATION) {
			ntrans++;
		}
		if (Y_TRANSLATION) {
			ntrans++;
		}
		if (SCALE) {
			ntrans++;
		}
		if (AXIS_DEFORMATION) {
			ntrans++;
		}
		if (ROTATION) {
			ntrans++;
		}
		if (DIAGONAL_DEFORMATION) {
			ntrans++;
		}
		if (SLOPE) {
			ntrans++;
		}
		if (debug)
			System.out.println("ntrans = " + ntrans);

		Tp = new double[2 * sp.numOfPoints()][ntrans];

		for (k = 0; k < sp.numOfPoints(); k++) {
			nt = 0;
			if (X_TRANSLATION) {
				Tp[2 * k][nt] = 1;
				Tp[2 * k + 1][nt] = 0;
				nt++;
			}
			if (Y_TRANSLATION) {
				Tp[2 * k][nt] = 0;
				Tp[2 * k + 1][nt] = 1;
				nt++;
			}
			if (SCALE) {
				Tp[2 * k][nt] = p[2 * k][0];
				Tp[2 * k + 1][nt] = p[2 * k + 1][0];
				nt++;
			}
			if (AXIS_DEFORMATION) {
				Tp[2 * k][nt] = -p[2 * k][0];
				Tp[2 * k + 1][nt] = p[2 * k + 1][0];
				nt++;
			}
			if (ROTATION) {
				Tp[2 * k][nt] = -p[2 * k + 1][0];
				Tp[2 * k + 1][nt] = p[2 * k][0];
				nt++;
			}
			if (DIAGONAL_DEFORMATION) {
				Tp[2 * k][nt] = p[2 * k + 1][0];
				Tp[2 * k + 1][nt] = p[2 * k][0];
				nt++;
			}

		}

		// tangent transformation
		if (SLOPE) {
			nt = ntrans - 1;
			if (debug)
				System.out.println("ntrans - 1 = " + nt);
			for (j = 0, k = 0; j < sp.size(); j++) {
				ds = sp.strokeAt(k);
				len = ds.length();
				for (i = 0; i < ds.size(); i++) {
					if (i < ds.size() - 1) {
						dx = ds.getX(i + 1) - ds.getX(i);
						dy = ds.getY(i + 1) - ds.getY(i);
						d = Math.sqrt(dx * dx + dy * dy);
						c = len / d;
						if (i > 0) {
							dx = 0.5 * dx;
							dy = 0.5 * dy;
						}
						Tp[2 * k][nt] = dx * c;
						Tp[2 * k + 1][nt] = dy * c;
					}
					if (i > 0) {
						dx = ds.getX(i) - ds.getX(i - 1);
						dy = ds.getY(i) - ds.getY(i - 1);
						d = Math.sqrt(dx * dx + dy * dy);
						c = len / d;
						// c = 32;
						if (i < ds.size() - 1) {
							dx = 0.5 * dx;
							dy = 0.5 * dy;
						}
						Tp[2 * k][nt] = dx * c;
						Tp[2 * k + 1][nt] = dy * c;
					}
					k++;
				}
			}
		}

		Mp = new Matrix(p);
		print(Mp, "Mp");
		Mq = new Matrix(q);
		print(Mp, "Mp");

		MTp = new Matrix(Tp);
		print(MTp, "Mtp");

		Malpha = (MTp.transpose().times(MTp)).inverse();
		print(Malpha, "MTp.transpose().times(MTp)).inverse()");
		print(Mq.minus(Mp), "Mq.minus(Mp)");
		Malpha = Malpha.times(MTp.transpose().times(Mq.minus(Mp)));
		print(Malpha, "MTpp");

		/*
		 * for(i = 0; i < Malpha.getRowDimension(); i++) { for(j = 0; j <
		 * Malpha.getColumnDimension(); j++) { if(Malpha.get(i,j) >= delta) {
		 * Malpha.set(i,j,delta); } else if(Malpha.get(i,j) <= -delta) {
		 * Malpha.set(i,j,-delta); } if(debug)
		 * System.out.print(" "+Malpha.get(i,j)); } }
		 */

		alpha = new double[Malpha.getRowDimension()];

		for (i = 0; i < Malpha.getRowDimension(); i++) {
			alpha[i] = Malpha.get(i, 0);
		}

		print(Malpha, "\nMTpp");

		Malpha = Mp.plus(MTp.times(Malpha).minus(Mq));

		Malpha = Malpha.transpose().times(Malpha.times(0.5));
		print(Malpha, "MTpp");
		if (debug)
			System.out.println("Tangent " + sp.name + " " + sq.name + " "
					+ Malpha.get(0, 0));

		return alpha;
	}

	public static Symbol tangentTansformation(Symbol s, double alpha[]) {
		Symbol tangent;
		DStroke dstroke;
		int i, j, nt = 0;

		tangent = new Symbol();
		for (j = 0; j < s.size(); j++) {
			tangent.add(new DStroke(s.strokeAt(j).size()));
		}

		// x-translation and initialization of the tangent ArrayList
		if (Tangent.X_TRANSLATION) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(i, alpha[nt], 0);
				}
			}
			nt++;
		}

		// y-translation
		if (Tangent.Y_TRANSLATION) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(i, tangent.strokeAt(j).getX(i),
							alpha[nt]);
				}
			}
			nt++;
		}

		// scale
		if (Tangent.SCALE) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(
							i,
							tangent.strokeAt(j).getX(i) + alpha[nt]
									* dstroke.getX(i),
							tangent.strokeAt(j).getY(i) + alpha[nt]
									* dstroke.getY(i));
				}
			}
			nt++;
		}

		// axis deformation
		if (Tangent.AXIS_DEFORMATION) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				// factor = alpha*(2*Math.random() - 1);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(
							i,
							tangent.strokeAt(j).getX(i) - alpha[nt]
									* dstroke.getX(i),
							tangent.strokeAt(j).getY(i) + alpha[nt]
									* dstroke.getY(i));
				}
			}
			nt++;
		}

		// rotation
		if (Tangent.ROTATION) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(
							i,
							tangent.strokeAt(j).getX(i) - alpha[nt]
									* dstroke.getY(i),
							tangent.strokeAt(j).getY(i) + alpha[nt]
									* dstroke.getX(i));
				}
			}
			nt++;
		}

		// diagonal deformation
		if (Tangent.DIAGONAL_DEFORMATION) {
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				// factor = 0.5*alpha*(2*Math.random() - 1);
				for (i = 0; i < dstroke.size(); i++) {
					tangent.strokeAt(j).set(
							i,
							tangent.strokeAt(j).getX(i) + alpha[nt]
									* dstroke.getY(i),
							tangent.strokeAt(j).getY(i) + alpha[nt]
									* dstroke.getX(i));
				}
			}
			nt++;
		}

		// slope
		if (Tangent.SLOPE) {
			double len, dx, dy, d, c;
			for (j = 0; j < s.size(); j++) {
				dstroke = s.strokeAt(j);
				len = dstroke.length();
				for (i = 0; i < dstroke.size() - 1; i++) {
					dx = dstroke.getX(i + 1) - dstroke.getX(i);
					dy = dstroke.getY(i + 1) - dstroke.getY(i);
					d = Math.sqrt(dx * dx + dy * dy);
					c = len / d;
					// c = 32;
					if (i > 0) {
						dx = 0.5 * dx;
						dy = 0.5 * dy;
					}
					tangent.strokeAt(j).set(i,
							tangent.strokeAt(j).getX(i) + alpha[nt] * dx * c,
							tangent.strokeAt(j).getY(i) + alpha[nt] * dy * c);
				}
				for (i = dstroke.size() - 1; i > 0; i--) {
					dx = dstroke.getX(i) - dstroke.getX(i - 1);
					dy = dstroke.getY(i) - dstroke.getY(i - 1);
					d = Math.sqrt(dx * dx + dy * dy);
					c = len / d;
					// c = 32;
					if (i < dstroke.size() - 1) {
						dx = 0.5 * dx;
						dy = 0.5 * dy;
					}
					tangent.strokeAt(j).set(i,
							tangent.strokeAt(j).getX(i) + alpha[nt] * dx * c,
							tangent.strokeAt(j).getY(i) + alpha[nt] * dy * c);
				}
			}
			nt++;
		}

		for (j = 0; j < s.size(); j++) {
			dstroke = s.strokeAt(j);
			for (i = 0; i < dstroke.size(); i++) {
				dstroke.set(i, dstroke.getX(i) + tangent.strokeAt(j).getX(i),
						dstroke.getY(i) + tangent.strokeAt(j).getY(i));
			}
		}

		return tangent;
	}

	static double[][] getArrayList(Symbol sp) {
		DStroke ds;
		double p[][];
		int i, k;

		p = new double[2 * sp.numOfPoints()][1];
		for (k = 0; k < sp.size(); k++) {
			ds = sp.strokeAt(k);
			for (i = 0; i < ds.size(); i++) {
				p[2 * i][0] = ds.getX(i);
				p[2 * i + 1][0] = ds.getY(i);
			}
		}

		return p;
	}

	static void print(Matrix M, String label) {
		if (debug)
			System.out.println(label + ": " + M.getRowDimension() + " "
					+ M.getColumnDimension());
	}

	public void readSymbols(String filename[]) {
		int i;

		for (i = 0; i < symbols.length; i++) {
			symbols[i] = new ArrayList<Symbol>();
		}

		for (i = 0; i < filename.length; i++) {
			System.out.print("Reading file " + filename[i]);
			try {
				convert(filename[i]);
			} catch (FileNotFoundException fnfe) {
				System.err.println("File " + filename + " not found...");
				System.exit(1);
			} catch (IOException ioe) {
				System.err.println("Error reading file " + filename + "...");
				System.exit(1);
			}
			System.out.println();
		}
	}

	public void convert(String filenamein) throws IOException {
		BufferedReader filein;

		filein = new BufferedReader(new FileReader(filenamein));

		ArrayList<String> symbolNames;
		String line;
		StringTokenizer st;

		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			filein.close();
			return;
		}

		st = new StringTokenizer(line, " \n\t\r\f");

		int size = st.countTokens();

		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			filein.close();
			return;
		}

		st.nextToken();
		int numGroups = Integer.parseInt(st.nextToken());

		line = filein.readLine();
		st = new StringTokenizer(line, " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		for (int i = 0; i < numGroups; i++) {
			// a group of symbol
			line = filein.readLine();
			st = new StringTokenizer(line, " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (int j = 0; j < size; j++) {
				TangentSymbol actualSymbol = new TangentSymbol();
				actualSymbol.read(filein);
				if (debug)
					System.out.println("\t" + symbolNames.get(j) + "\n");
				try {
					actualSymbol = actualSymbol.getProcessed(0, np);
					actualSymbol.name = symbolNames.get(j);
					symbols[actualSymbol.size() - 1].add(actualSymbol);
					System.out.print("." + actualSymbol.size() + ".");
				} catch (ArrayIndexOutOfBoundsException aioobe) {
				}
			}
		}

		filein.close();
	}

	public String classify(Symbol s, int n) {
		TangentSymbol symbol[], ts;
		int i;

		s = s.getProcessedSingleSymbol(0, np);

		heap = new BinaryHeap(this.symbols[s.size() - 1].size());

		for (i = 0; i < this.symbols[s.size() - 1].size(); i++) {
			try {
				ts = (TangentSymbol) this.symbols[s.size() - 1].get(i);
				ts.setDist(s);
				heap.insert(ts);
			} catch (Overflow ov) {
			}
		}

		symbol = new TangentSymbol[n];
		for (i = 0; i < n; i++) {
			symbol[i] = (TangentSymbol) heap.deleteMin();
			System.out.print(symbol[i].name + " ");
		}
		System.out.println();

		return symbol[0].name;
	}

	class TangentSymbol extends Symbol implements DataStructures.Comparable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6188141291943142337L;
		double dist = 0;

		public TangentSymbol(TangentSymbol s) {
			super(s);
		}

		public TangentSymbol() {
			super();
		}

		@Override
		public int compareTo(DataStructures.Comparable o) {
			return compareTo((TangentSymbol) o);
		}

		public int compareTo(TangentSymbol s) {
			if (this.dist < s.dist)
				return -1;
			else if (this.dist == s.dist)
				return 0;
			else
				return 1;
		}

		public void setDist(Symbol s) {
			this.dist = Tangent.distance(this, s);
		}

		public TangentSymbol getProcessed(double alpha, int np) {
			TangentSymbol newSymbol, sym;
			DStroke ds;
			int i;

			newSymbol = new TangentSymbol();
			// if(this.numOfPoints() < np) {
			// for(i = 0; i < this.size(); i++) {
			// ds = this.strokeAt(i).equidistantApproximation(2*np);
			// newSymbol.add(ds);
			// }
			// //System.out.println("this.numOfPoints() < np");
			// }
			// else {
			// for(i = 0; i < this.size(); i++) {
			// newSymbol.add(new DStroke(this.strokeAt(i)));
			// }
			// System.out.println("this.numOfPoints() >= np");
			// }

			sym = new TangentSymbol();
			for (i = 0; i < this.size(); i++) {
				// sym.add(newSymbol.strokeAt(i).proyectionPolygonal(np/this.size()));
				// sym.add(newSymbol.strokeAt(i).proyectionPolygonal(np));
				ds = new DStroke(this.strokeAt(i));
				ds.smooth();
				sym.add(ds.proyectionPolygonal(np).equidistantOptimized(np));
			}

			// sym.tangentTansformation(alpha);

			/*
			 * ds = new DStroke(); for(i = 0; i < this.size(); i++) {
			 * ds.join(new DStroke(sym.strokeAt(i))); }
			 * 
			 * newSymbol.clear(); newSymbol.add(ds);
			 */

			newSymbol = new TangentSymbol(sym);

			newSymbol.filter();// System.out.println("this.filter();");
			newSymbol.normalizeDirection();// System.out.println("this.normalizeDirection();");
			newSymbol.orderStrokes();// System.out.println("this.orderStrokes();");
			newSymbol.scale();
			// newSymbol.smooth();

			if (alpha != 0) {
				newSymbol.tangentTansformation(alpha);
			}

			/*
			 * ds = new DStroke(); for(i = 0; i < this.size(); i++) {
			 * ds.join(new DStroke(sym.strokeAt(i))); }
			 * 
			 * newSymbol.clear(); newSymbol.add(ds);
			 */

			newSymbol = new TangentSymbol(sym);

			// System.out.println("END");

			return newSymbol;
		}
	}
}

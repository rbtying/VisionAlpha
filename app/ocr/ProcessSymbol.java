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
 * �berschrift: <p>
 * Beschreibung: <p>
 * Copyright: Copyright (c) <p>
 * Organisation: <p>
 * @author
 * @version 1.0
 */
package ocr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import svm.IO;
import svm.SparseVector;

public class ProcessSymbol {
	static int np = 8;
	static int extraCoord = 6;
	static boolean debug = true;

	/*
	 * public static String convertToData(Symbol sym) throws IOException {
	 * Symbol s; String data; ocr.DStroke s1, s2; int index, i;
	 * 
	 * s = new Symbol(sym);
	 * 
	 * s.filter();
	 * 
	 * if(s.isEmpty() || s.length() == 0.0) { throw new
	 * IOException("convertToData(Symbol, int): s.isEmpty() || s.length() == 0.0\n"
	 * ); }
	 * 
	 * data = ""; index = 0; if(s.size() == 1) { if(debug)
	 * System.out.println("s.size() == 1");
	 * 
	 * s.normalizeDirection(); s.orderStrokes();
	 * 
	 * if(s.strokeAt(0).size() <= np) { s1 = new
	 * DStroke(s.strokeAt(0).equidistant(s.strokeAt(0).length()/(1.5*np))); }
	 * else { s1 = new DStroke(s.strokeAt(0)); }
	 * 
	 * s1 = new DStroke(s1.proyectionPolygonal(np));
	 * 
	 * s.clear(); s.add(s1); s.scale();
	 * 
	 * for(i = 0; i < np/2; i++) { data +=
	 * (index++)+":"+(float)s1.pointAt(i).x+" "; data +=
	 * (index++)+":"+(float)s1.pointAt(i).y+" "; }
	 * 
	 * data += (index++)+":"+(float)s1.equidistant(0.1).getCurvature()+" "; data
	 * += (index++)+":"+(float)s1.cosMap().integral()+" "; data +=
	 * (index++)+":"+(float)s1.sinMap().integral()+" ";
	 * 
	 * for(i = np/2; i < np; i++) { data +=
	 * (index++)+":"+(float)s1.pointAt(i).x+" "; data +=
	 * (index++)+":"+(float)s1.pointAt(i).y+" "; }
	 * 
	 * 
	 * data += (index++)+":0 "; data += (index++)+":0 "; data +=
	 * (index++)+":0 "; } else { //if(s.size() == 2) { if(s.size() > 2) { s1 =
	 * new DStroke(); for(i = 0; i < s.size()/2; i++) { s1.join(new
	 * DStroke(s.strokeAt(i))); }
	 * 
	 * s2 = new DStroke(); for(i = s.size()/2; i < s.size(); i++) { s2.join(new
	 * DStroke(s.strokeAt(i))); }
	 * 
	 * s.clear(); s.add(s1); s.add(s2); }
	 * 
	 * s.normalizeDirection(); s.orderStrokes();
	 * 
	 * if(s.strokeAt(0).size() <= np) { s1 = new
	 * DStroke(s.strokeAt(0).equidistant(s.strokeAt(0).length()/(1.5*np))); }
	 * else { s1 = new DStroke(s.strokeAt(0)); }
	 * 
	 * if(s.strokeAt(1).size() <= np) { s2 = new
	 * DStroke(s.strokeAt(1).equidistant(s.strokeAt(1).length()/(1.5*np))); }
	 * else { s2 = new DStroke(s.strokeAt(1)); }
	 * 
	 * s1 = new DStroke(s1.proyectionPolygonal(np/2)); s2 = new
	 * DStroke(s2.proyectionPolygonal(np/2));
	 * 
	 * s.clear(); s.add(s1); s.add(s2); s.scale();
	 * 
	 * for(i = 0; i < np/2; i++) { data +=
	 * (index++)+":"+(float)s1.pointAt(i).x+" "; data +=
	 * (index++)+":"+(float)s1.pointAt(i).y+" "; }
	 * 
	 * data += (index++)+":"+(float)s1.equidistant(0.1).getCurvature()+" "; data
	 * += (index++)+":"+(float)s1.cosMap().integral()+" "; data +=
	 * (index++)+":"+(float)s1.sinMap().integral()+" ";
	 * 
	 * // s2 // for(i = 0; i < np/2; i++) { data +=
	 * (index++)+":"+(float)s2.pointAt(i).x+" "; data +=
	 * (index++)+":"+(float)s2.pointAt(i).y+" "; }
	 * 
	 * data += (index++)+":"+(float)s2.equidistant(0.1).getCurvature()+" "; data
	 * += (index++)+":"+(float)s2.cosMap().integral()+" "; data +=
	 * (index++)+":"+(float)s2.sinMap().integral()+" "; }
	 * 
	 * return data; }
	 */

	public static String convertToData(Symbol symbol) {
		int pointsPolygonal = np, k, window = 10, numPoints = 64;
		double len, unit, sigma = 5.0;
		Symbol newSymbol = new Symbol();
		DStroke s, ss;
		for (k = 0; k < symbol.size(); k++) {
			s = symbol.strokeAt(k);
			len = s.length();
			unit = Math.max(symbol.getMaxX() - symbol.getMinX(),
					symbol.getMaxY() - symbol.getMinY());
			if (len > 0.0 && unit > 0.0) {
				newSymbol.add(new DStroke(s.equidistant(unit / numPoints)
						.smooth(sigma, window)));
			} else {
				newSymbol.add(new DStroke(s));
			}
		}

		for (k = 0; k < symbol.size(); k++) {
			newSymbol.strokeAt(k).normalizeDirection();
		}
		newSymbol.orderStrokes();

		ss = new DStroke();
		for (k = 0; k < symbol.size(); k++) {
			ss.join(new DStroke(newSymbol.strokeAt(k)));
		}
		newSymbol.clear();
		newSymbol.add(ss.proyectionPolygonal(pointsPolygonal));
		symbol.processed = new DStroke(newSymbol.strokeAt(0));
		newSymbol.scale(0, 0, 1, true);

		// double[] v = new double[2 * pointsPolygonal + 5];

		ss = new DStroke(newSymbol.strokeAt(0));

		// int count = 0;
		String vec = "";
		for (k = 0; k < pointsPolygonal; k++) {
			vec += "" + (2 * k) + ":" + ss.pointAt(k).x + " ";// v[2*k] =
																// ss.pointAt(k).x;
			vec += "" + (2 * k + 1) + ":" + ss.pointAt(k).y + " ";// v[2*k+1] =
																	// ss.pointAt(k).y;
		}
		for (k = 1; k <= 5; k++) {
			if (k <= symbol.size()) {
				vec += "" + (2 * pointsPolygonal + k - 1) + ":" + 1 + " ";// v[2*pointsPolygonal
																			// +
																			// k
																			// -
																			// 1]
																			// =
																			// 1.0;
			} else {
				vec += "" + (2 * pointsPolygonal + k - 1) + ":" + 0 + " ";// v[2*pointsPolygonal
																			// +
																			// k
																			// -
																			// 1]
																			// =
																			// 0.0;
			}
		}

		return vec;
	}

	public static void convert(String filenamein, String filenameout)
			throws IOException {
		BufferedReader filein;
		DataOutputStream fileout;

		filein = new BufferedReader(new FileReader(filenamein));
		fileout = new DataOutputStream(new FileOutputStream(filenameout));

		ArrayList<String> symbolNames;
		String line;
		StringTokenizer st;

		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			filein.close();
			fileout.close();
			return;
		}

		st = new StringTokenizer(line, " \n\t\r\f");

		int size = st.countTokens();

		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			filein.close();
			fileout.close();
			return;
		}

		st.nextToken();
		int numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		// fileout.writeBytes("SparseVector "+(numGroups*symbolNames.size())+"\n");
		for (int i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (int j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();
				actualSymbol.read(filein);
				if (debug)
					System.out.println("\t" + symbolNames.get(j) + "\n");
				if (actualSymbol.size() > 0) {
					// if(debug) System.out.println("actualSymbol.size() == 1");

					String data = null;

					try {
						data = convertToData(actualSymbol);
					} catch (Exception ex) {
						System.err.println(ex.toString());
						continue;
					}
					fileout.writeBytes(symbolNames.get(j) + " " + data + " "
							+ "\n");

				} else if (debug)
					System.out.println("actualSymbol.size() == 0");
				// actualSymbol.write(fileout);
			}
		}

		filein.close();
		fileout.close();
	}

	public static void convert(String filenamein, String filenameout1,
			String filenameout2) throws IOException {
		BufferedReader filein;
		DataOutputStream fileout1;
		DataOutputStream fileout2;

		filein = new BufferedReader(new FileReader(filenamein));
		fileout1 = new DataOutputStream(new FileOutputStream(filenameout1));
		fileout2 = new DataOutputStream(new FileOutputStream(filenameout2));

		ArrayList<String> symbolNames;
		String line;
		StringTokenizer st;

		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			filein.close();
			fileout1.close();
			fileout2.close();
			return;
		}

		st = new StringTokenizer(line, " \n\t\r\f");

		int size = st.countTokens();

		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			filein.close();
			fileout1.close();
			fileout2.close();
			return;
		}

		st.nextToken();
		int numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		// fileout.writeBytes("SparseVector "+(numGroups*symbolNames.size())+"\n");
		for (int i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (int j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();
				actualSymbol.read(filein);
				if (debug)
					System.out.println("\t" + symbolNames.get(j) + "\n");
				// if(actualSymbol.size() > 1) {
				// if(debug) System.out.println("actualSymbol.size() == 1");

				try {
					if (actualSymbol.size() <= 1)
						fileout1.writeBytes(symbolNames.get(j) + " "
								+ convertToData(actualSymbol) + "\n");
					else
						fileout2.writeBytes(symbolNames.get(j) + " "
								+ convertToData(actualSymbol) + "\n");
				} catch (IOException ioe) {
				}
				// }
				// actualSymbol.write(fileout);
			}
		}

		filein.close();
		fileout1.close();
		fileout2.close();
	}

	public static SparseVector getProcessedSymbol(Symbol symbol) {
		int i;

		SparseVector v = new SparseVector(2 * np + extraCoord);
		StringTokenizer st = null;
		try {
			st = new StringTokenizer(convertToData(symbol), ": ");
		} catch (Exception ex) {
			return null;
		}

		// if(debug) System.out.println("st.countTokens(): "+st.countTokens());
		for (i = 0; i < 2 * np + extraCoord; i++) {
			v.index[i] = IO.atoi(st.nextToken());
			v.value[i] = IO.atof(st.nextToken());
		}
		// if(debug) System.out.println("v.toString(): "+v.value[i-1]);

		return (v);
	}

	public String getProcessedSymbol() {
		return "";
	}

	public static void main(String[] argv) throws Exception {
		np = Integer.parseInt(argv[0]);
		// int j;

		if (argv.length == 3)
			convert(argv[1], argv[2]);
		else if (argv.length == 4)
			convert(argv[1], argv[2], argv[3]);
		else
			System.err.println("Error in number of arguments");

	}
}

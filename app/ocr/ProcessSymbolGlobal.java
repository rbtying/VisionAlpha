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
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import svm.IO;

public class ProcessSymbolGlobal {
	static int np = 16;
	static double alpha = 0.15;
	static int times = 1;
	static boolean debug = true;
	static String ext = ".dat";
	static boolean matlab = false;

	public static void convert(String filenamein, String filenameout)
			throws IOException {
		BufferedReader filein;
		DataOutputStream fileout[];
		// String data1, data2;
		// int count1, count2;
		int maxsize = Integer.MIN_VALUE;

		System.out.println(filenamein);

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
		int numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		// fileout.writeBytes("SparseVector "+(numGroups*symbolNames.size())+"\n");
		int i, j, k;
		// data1 = data2 = "";
		// count1 = count2 = 0;
		for (i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();

				actualSymbol.read(filein);
				// if(debug)
				// System.out.println("\t"+(String)symbolNames.get(j)+" "+j+"\n");
				// if(actualSymbol.size() > 1) {
				// if(debug) System.out.println("actualSymbol.size() == 1");

				try {
					maxsize = Math.max(maxsize, actualSymbol.size());
				} catch (Exception ioe) {
				}
				// }
				// actualSymbol.write(fileout);
			}
		}

		filein.close();

		// fileout1.writeBytes("SparseVector "+count1+"\n");
		// fileout2.writeBytes("SparseVector "+count2+"\n");

		filein = new BufferedReader(new FileReader(filenamein));

		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			return;
		}

		st = new StringTokenizer(line, " \n\t\r\f");

		size = st.countTokens();

		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			return;
		}

		st.nextToken();
		numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		fileout = new DataOutputStream[maxsize];
		for (i = 0; i < maxsize; i++) {
			fileout[i] = new DataOutputStream(new FileOutputStream(filenameout
					+ "_" + i + ext));
		}
		for (i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();

				actualSymbol.read(filein);
				if (debug)
					System.out.println("\t" + symbolNames.get(j) + " " + j
							+ "\n");
				// if(actualSymbol.size() > 1) {
				// if(debug) System.out.println("actualSymbol.size() == 1");

				if (matlab) {
					for (k = 0; k < times; k++) {
						fileout[actualSymbol.size() - 1]
								.writeBytes(actualSymbol
										.tangentTansformationSymbol(alpha)
										.globalFeaturesToString()
										+ "\n");
					}
				} else {
					try {
						for (k = 0; k < times; k++) {
							fileout[actualSymbol.size() - 1]
									.writeBytes(actualSymbol
											.tangentTansformationSymbol(alpha)
											.globalFeaturesToSparseVector()
											.toWrite(symbolNames.get(j)));
						}
					} catch (Exception ioe) {
					}
					// }
					// actualSymbol.write(fileout);
				}
			}
		}

		filein.close();

		for (i = 0; i < maxsize; i++) {
			fileout[i].close();
		}
	}

	public static void matlabHistogram(String filenamein, String filenameout)
			throws IOException {
		BufferedReader filein;
		DataOutputStream fileout[];
		// String data1, data2;
		// int count1, count2;
		int maxsize = Integer.MIN_VALUE;

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
		int numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		// fileout.writeBytes("SparseVector "+(numGroups*symbolNames.size())+"\n");
		int i, j, k;
		// data1 = data2 = "";
		// count1 = count2 = 0;
		for (i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();

				actualSymbol.read(filein);
				// if(debug)
				// System.out.println("\t"+(String)symbolNames.get(j)+" "+j+"\n");
				// if(actualSymbol.size() > 1) {
				// if(debug) System.out.println("actualSymbol.size() == 1");

				try {
					maxsize = Math.max(maxsize, actualSymbol.size());
				} catch (Exception ioe) {
				}
				// }
				// actualSymbol.write(fileout);
			}
		}

		filein.close();

		// fileout1.writeBytes("SparseVector "+count1+"\n");
		// fileout2.writeBytes("SparseVector "+count2+"\n");

		filein = new BufferedReader(new FileReader(filenamein));

		if ((line = filein.readLine()) == null) {
			System.err.println("ERROR: Empty file...");
			return;
		}

		st = new StringTokenizer(line, " \n\t\r\f");

		size = st.countTokens();

		if (size == 0) {
			System.err.println("ERROR: Empty line...");
			return;
		}

		st.nextToken();
		numGroups = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
		size = st.countTokens();
		symbolNames = new ArrayList<String>();
		for (i = 0; i < size; i++) {
			symbolNames.add(new String(st.nextToken()));
		}

		fileout = new DataOutputStream[maxsize];
		for (i = 0; i < maxsize; i++) {
			fileout[i] = new DataOutputStream(new FileOutputStream(filenameout
					+ "_" + i + ext));
		}
		for (i = 0; i < numGroups; i++) {
			// a group of symbol
			st = new StringTokenizer(filein.readLine(), " \n\t\r\f");
			// group name
			String groupName = st.nextToken();
			// we start to read the group of symbols
			if (debug)
				System.out.println(groupName + "\n");
			for (j = 0; j < size; j++) {
				// this.symbolDrawJPanel.erase();
				// st = new StringTokenizer(filein.readLine()," \n\t\r\f");
				Symbol actualSymbol = new Symbol();

				actualSymbol.read(filein);
				if (debug)
					System.out.println("\t" + symbolNames.get(j) + " " + j
							+ "\n");
				// if(actualSymbol.size() > 1) {
				// if(debug) System.out.println("actualSymbol.size() == 1");

				try {
					for (k = 0; k < times; k++) {
						fileout[actualSymbol.size() - 1]
								.writeBytes(actualSymbol
										.globalFeaturesToSparseVector()
										.toWrite(symbolNames.get(j)));
					}
				} catch (Exception ioe) {
				}
				// }
				// actualSymbol.write(fileout);
			}
		}

		filein.close();

		for (i = 0; i < maxsize; i++) {
			fileout[i].close();
		}
	}

	public static void main(String[] argv) throws Exception {
		String filenamein = null;
		String filenameout = null;

		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'a':
					alpha = Double.parseDouble(argv[i]);
					break;
				case 'e':
					ext = argv[i];
					break;
				case 'm':
					// matlab = argv[i].equals("true") || argv[i].equals("t") ||
					// argv[i].equals("1");
					matlab = true;
					i--;
					break;
				case 'n':
					np = Integer.parseInt(argv[i]);
					break;
				case 'i':
					filenamein = argv[i];
					break;
				case 'o':
					filenameout = argv[i];
					break;
				case 't':
					times = Integer.parseInt(argv[i]);
					break;
				default:
					System.err.print("unknown option: " + argv[i - 1].charAt(1)
							+ "");
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.err.print(e.toString());
			System.exit(1);
		}

		// convert(filenamein,filenameout+"_1.dat",filenameout+"_2.dat");
		convert(filenamein, filenameout);

	}
}

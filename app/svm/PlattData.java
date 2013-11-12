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
package svm;

import java.util.*;
import java.io.*;

public class PlattData {
	static BufferedReader filein;

	public static void main(String argv[]) throws Exception {
		BufferedReader filein;
		DataOutputStream fileout;
		String line;
		StringTokenizer st;
		filein = new BufferedReader(new FileReader(argv[0]));
		fileout = new DataOutputStream(new FileOutputStream(argv[1]));

		int dim = IO.atoi(argv[2]);
		for (int i = 0; i <= dim; i++) {
			filein.readLine();
		}

		double[] v = null;
		String name = null;
		String header = null;
		String index = null;
		String value = null;
		fileout.writeBytes("SparseVector " + IO.atoi(argv[3]) + "\n");
		while ((line = filein.readLine()) != null) {
			st = new StringTokenizer(line, ",");
			header = st.nextToken();
			index = st.nextToken();
			value = st.nextToken();

			if (header.equals("C")) {
				if (v != null) {
					(new SparseVector(v, name)).write(fileout);
				}
				v = new double[dim];
			} else {
				if (index.equals("1000")) {
					name = value;
				} else {
					v[IO.atoi(index) - 1001] = IO.atof(value);
				}
			}
		}
		filein.close();
	}

	/*
	 * public static void main(String argv[]) throws Exception{ for(int i = 0; i
	 * < argv.length - 4; i++) {
	 * write(argv[0],argv[1]+"-"+(i+1)+argv[2]+".dst",argv[1]+"-"+(i+1)+"a.trn",
	 * IO.atoi(argv[3]), IO.atoi(argv[i+3])); } }
	 */

	public static void write(String path, String filenamein,
			String filenameout, int dim, int numData) throws Exception {
		BufferedReader filein;
		DataOutputStream fileout;
		String line;
		StringTokenizer st;
		filein = new BufferedReader(new FileReader(path + filenamein));
		fileout = new DataOutputStream(new FileOutputStream(path + filenameout));

		// int dim = IO.atoi(argv[2]);
		for (int i = 0; i <= dim; i++) {
			filein.readLine();
		}

		double[] v = null;
		String name = null;
		String header = null;
		String index = null;
		String value = null;
		fileout.writeBytes("SparseVector " + numData + "\n");
		while ((line = filein.readLine()) != null) {
			st = new StringTokenizer(line, ",");
			header = st.nextToken();
			index = st.nextToken();
			value = st.nextToken();

			if (header.equals("C")) {
				if (v != null) {
					(new SparseVector(v, name)).write(fileout);
				}
				v = new double[dim];
			} else {
				if (index.equals("1000")) {
					name = value;
				} else {
					v[IO.atoi(index) - 1001] = IO.atof(value);
				}
			}
		}
		filein.close();
	}
}

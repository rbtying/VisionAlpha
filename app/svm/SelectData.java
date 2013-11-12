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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SelectData {
	public static void main(String[] argv) {
		String line;
		BufferedReader filein = null;
		DataOutputStream fileout;
		ArrayList<String> v = new ArrayList<String>();
		int i = 1;
		try {
			filein = new BufferedReader(new FileReader(argv[0]));
			// System.err.println("File "+filein+" not found...");
			/*
			 * line = filein.readLine(); st = new StringTokenizer(line," ");
			 * 
			 * type = st.nextToken();
			 */
			ArrayList<String> u = new ArrayList<String>();
			while ((line = filein.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				u.add(new String(line));
			}
			// int len = v.size();
			while (!u.isEmpty()) {
				int rndint = (int) (u.size() * Math.random());
				/*
				 * p = new SparseVector((String)u.get(rndint)); found = false;
				 * for(i = 0; i < v.size(); i++) { q = new
				 * SparseVector((String)v.get(i)); if(found =p.equals(q)) break;
				 * } if(found) { System.out.print("*"); continue; }
				 * System.out.print(".");
				 */
				v.add(new String(u.remove(rndint)));
			}

			filein.close();
		} catch (FileNotFoundException fnfe1) {
			System.err.println("File " + argv[0] + " not found...");
			System.exit(1);
		} catch (IOException ioe1) {
			System.err.println("Error reading file " + argv[0] + "...");
			System.exit(1);
		}

		try {
			int init = 0, inc;
			System.out.println("Num lines " + v.size());
			for (i = 1; i < argv.length; i += 2) {
				double p = atof(argv[i + 1]);
				fileout = new DataOutputStream(new FileOutputStream(argv[i]));
				inc = (int) (p * v.size());
				System.out.println("Saving file " + argv[i]);
				System.out.println("p = " + p + ", " + init + "-->"
						+ (init + inc) + ", " + inc);

				// fileout.writeBytes(type+" "+inc+"\n");
				for (int j = init; j < init + inc; j++) {
					fileout.writeBytes(v.get(j) + "\n");
				}
				init += inc;
				fileout.close();
			}
		} catch (FileNotFoundException fnfe) {
			System.err.println("File " + argv[i] + " not found...");
			System.exit(1);
		} catch (IOException ioe) {
			System.err.println("Error reading file " + argv[i] + "...");
			System.exit(1);
		}
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

}

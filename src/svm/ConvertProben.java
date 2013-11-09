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

public class ConvertProben {

	static BufferedReader filein;
	static DataOutputStream filetrn, filetst, fileval;
	static double[] v;
	static String line;
	static StringTokenizer st;
	static int bool_in, real_in, bool_out, real_out;
	static int training_examples, validation_examples, test_examples;
	static int i, j, index;

	static void write(BufferedReader fin, DataOutputStream fileout,
			int num_examples) throws Exception {
		fileout.writeBytes("SparseVector " + num_examples + "\n");
		for (i = 0; i < num_examples; i++) {
			st = new StringTokenizer(fin.readLine(), " ");
			for (j = 0; j < bool_in + real_in; j++) {
				v[j] = IO.atof(st.nextToken());
			}

			if (bool_out == 0 && real_out == 1) {
				(new SparseVector(v, st.nextToken())).write(fileout);
			} else {
				index = -1;
				for (j = 0; j < bool_out + real_out; j++) {
					if (st.nextToken().equals("1")) {
						index = j;
						break;
					}
				}

				(new SparseVector(v, "" + index)).write(fileout);
			}
		}

		fileout.close();
	}

	/*
	 * public static void main(String argv[]) throws Exception{ filein = new
	 * BufferedReader(new FileReader(argv[0])); filetrn = new
	 * DataOutputStream(new FileOutputStream(argv[1]));
	 * 
	 * 
	 * 
	 * ArrayList data = new ArrayList(); String name; int dim; while((line =
	 * filein.readLine())!= null) { st = new StringTokenizer(line, ", ");
	 * 
	 * dim = st.countTokens() - 1; if(dim <= 0) continue;
	 * 
	 * v = new double[dim]; for(i = 0; i < v.length; i++) { v[i] =
	 * IO.atof(st.nextToken())/100.0; }
	 * 
	 * name = new String(st.nextToken());
	 * 
	 * data.add(new SparseVector(v,name)); }
	 * 
	 * filetrn.writeBytes("SparseVector "+data.size()+"\n"); for(i = 0; i <
	 * data.size(); i++) { ((SparseVector) data.get(i)).write(filetrn); }
	 * 
	 * filetrn.close();
	 * 
	 * }
	 */

	public static void main(String argv[]) throws Exception {
		filein = new BufferedReader(new FileReader(argv[0]));
		filetrn = new DataOutputStream(new FileOutputStream(argv[0] + ".trn"));
		filetst = new DataOutputStream(new FileOutputStream(argv[0] + ".tst"));
		fileval = new DataOutputStream(new FileOutputStream(argv[0] + ".val"));

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		bool_in = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		real_in = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		bool_out = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		real_out = IO.atoi(st.nextToken());

		//

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		training_examples = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		validation_examples = IO.atoi(st.nextToken());

		st = new StringTokenizer(filein.readLine(), "=");
		st.nextToken();
		test_examples = IO.atoi(st.nextToken());

		v = new double[bool_in + real_in];

		write(filein, filetrn, training_examples);
		write(filein, fileval, validation_examples);
		write(filein, filetst, test_examples);

	}

}

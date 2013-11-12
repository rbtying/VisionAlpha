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
package svm;

/**
 * <p>�berschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author unascribed
 * @version 1.0
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ToCascadeCorrelationFormat {

	public static String doubleToString(double x) {
		StringTokenizer st;
		String str = "";

		st = new StringTokenizer(Double.toString(x), ".Ee");

		if (st.countTokens() == 2) {
			str = Double.toString(x);
		} else if (st.countTokens() == 3) {
			long intPart = Long.parseLong(st.nextToken());
			long decPart = Long.parseLong(st.nextToken());
			int pow = Integer.parseInt(st.nextToken()), i;

			if (pow < 0) {
				// System.err.print(intPart+" "+decPart+" "+pow);

				if (intPart < 0) {
					str += "-0.";
					intPart = -intPart;
				} else {
					str += "0.";
				}
				for (i = 1; i <= -pow - 1; i++) {
					str += "0";
				}
				str += "" + intPart + "" + decPart;

				// System.err.println(" "+str);
			}
		}

		return str;
	}

	public static void main(String[] args) {
		Data data;
		ArrayList<String> label;
		SparseVector p;
		int i, j, dim;

		IO.verbosity = -1;

		data = SparseVector.readData(args[0]);
		label = new ArrayList<String>();

		// label.add("");
		for (i = 0; i < data.l; i++) {
			p = (SparseVector) data.point[i];
			for (j = 0; j < label.size(); j++) {
				if (p.name.equals(label.get(j))) {
					break;
				}
			}

			if (j == label.size()) {
				label.add(data.label[i]);
			}
		}

		dim = ((SparseVector) data.point[0]).length;

		System.out.println("$SETUP\n");

		System.out.println("PROTOCOL:\tIO;");
		System.out.println("OFFSET:\t0;");
		System.out.println("INPUTS:\t" + dim + ";");
		System.out.println("OUTPUTS:\t" + label.size() + ";\n");

		for (i = 0; i < dim; i++) {
			System.out.println("IN [" + (i + 1) + "]: CONT;");
		}

		System.out.println();

		for (i = 0; i < label.size(); i++) {
			System.out.println("OUT [" + (i + 1) + "]: BINARY;");
		}

		System.out.println("\n$TRAIN\n");

		for (i = 0; i < data.l; i++) {

			try {
				if ((int) (data.l * Float.parseFloat(args[1])) == i) {
					System.out.println("\n$VALIDATION\n");
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
			}

			try {
				if ((int) (data.l * Float.parseFloat(args[2])) == i) {
					System.out.println("\n$TEST\n");
				}
			} catch (ArrayIndexOutOfBoundsException ex1) {
			}

			p = (SparseVector) data.point[i];

			for (j = 0; j < dim; j++) {
				// if(Math.abs(p.value[j]) < 1E-3)
				// p.value[j] = 0.001;
				System.out.print(doubleToString(p.value[j]));
				if (j < dim - 1) {
					System.out.print(", ");
				}
			}

			System.out.print(" => ");

			for (j = 0; j < label.size(); j++) {
				if (p.name.equals(label.get(j))) {
					System.out.print("+");
				} else {
					System.out.print("-");
				}
				if (j < label.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println(";");
		}

		// System.out.println(label);
	}
}

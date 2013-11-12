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

import java.util.*;
import java.io.*;

public class ToMatlabFormat {

	public static void main(String[] args) throws Exception {
		DataOutputStream fileout;
		Data data;
		ArrayList<String> label;
		SparseVector p;
		int i, j, dim;

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

		fileout = new DataOutputStream(new FileOutputStream(args[1] + ".dat"));

		System.out.println("Writing data: ");
		for (i = 0; i < dim; i++) {
			for (j = 0; j < data.l; j++) {
				p = (SparseVector) data.point[j];
				fileout.writeBytes(p.value[i] + " ");
			}
			fileout.writeBytes("\n");
			System.out.print(".");
		}
		System.out.println();
		fileout.close();

		fileout = new DataOutputStream(new FileOutputStream(args[1] + ".lab"));

		System.out.println("Writing Labels: ");
		for (i = 0; i < label.size(); i++) {
			for (j = 0; j < data.l; j++) {
				p = (SparseVector) data.point[j];

				if (p.name.equals(label.get(i))) {
					fileout.writeBytes("1.0" + " ");
				} else {
					fileout.writeBytes("0.0" + " ");
				}

			}
			fileout.writeBytes("\n");
			System.out.print(".");
		}
		System.out.println();
		fileout.close();

		fileout = new DataOutputStream(new FileOutputStream(args[1] + ".names"));

		System.out.println("Labels: ");
		for (i = 0; i < label.size(); i++) {
			fileout.writeBytes((label.get(i)) + "\n");
			System.out.print(".");
		}
		System.out.println();
		fileout.close();
	}

}

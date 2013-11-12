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

import java.io.*;

public class OrderData {
	static InnerProductSpace[] point;

	static int numLabels;
	static String[] label;
	static int[] numElements;
	static int[] classStart;
	static int[] indexClass;
	static int l;

	public static void read(Data data) {
		int i, j;
		int maxLabels = 10;

		l = data.l;

		label = new String[maxLabels];
		numElements = new int[maxLabels];

		indexClass = new int[l];

		numLabels = 0;
		System.out.println("numLabels = 0;");
		for (i = 0; i < l; i++) {
			String labelTaked = data.label[i];

			for (j = 0; j < numLabels; j++)
				if (labelTaked.equals(label[j]))
					break;

			if (j == numLabels) {
				if (j == maxLabels) {
					maxLabels += maxLabels;
					String[] ArrayList = new String[maxLabels];
					System.arraycopy(label, 0, ArrayList, 0, label.length);
					label = ArrayList;
					int[] iArrayList = new int[maxLabels];
					System.arraycopy(numElements, 0, iArrayList, 0,
							numElements.length);
					numElements = iArrayList;
				}
				label[j] = labelTaked;
				numLabels++;
			}

			indexClass[i] = j;
			numElements[j]++;
		}

		classStart = new int[numLabels];
		point = new SparseVector[l];

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		for (i = 0; i < l; i++) {
			point[classStart[indexClass[i]]] = data.point[i];
			++classStart[indexClass[i]];
		}

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}
	}

	public static void write(String filename) throws IOException {
		int i, j;
		DataOutputStream[] fileout;

		fileout = new DataOutputStream[numLabels];

		for (i = 0; i < numLabels; i++) {
			fileout[i] = new DataOutputStream(new FileOutputStream(filename + i
					+ ".ord"));

			for (j = classStart[i]; j < classStart[i] + numElements[i]; j++) {
				point[j].write(fileout[i]);
			}

			fileout[i].close();
		}
	}

	public static void main(String[] argv) throws Exception {
		Data data = null;

		try {
			data = SparseVector.readData(argv[0]);
		} catch (RuntimeException e) {
			data = StepFunction.readData(argv[0]);
		}

		read(data);
		write(argv[1]);
	}
}

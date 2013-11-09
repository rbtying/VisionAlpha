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

public class Data {
	public InnerProductSpace[] point = null;
	public String[] label = null;
	public String type = null;
	public int l = -1;

	public boolean grouped = false;
	public int numLabels;
	public int[] numElements;
	public int[] classStart;
	public int[] indexClass;

	public Data(int l_) {
		point = new InnerProductSpace[l_];
		label = new String[l_];
		l = l_;
	}

	public Data(InnerProductSpace[] point_, String[] label_, String type_,
			int l_) {
		point = point_;
		label = label_;
		type = type_;
		l = l_;
	}

	public static Data group(Data data, String classLabel[]) {
		Data groupedData = new Data(data.l);
		String labelTaked;
		// String[] ArrayList;
		int i, j;
		// int[] iArrayList;

		groupedData.numLabels = classLabel.length;
		groupedData.numElements = new int[groupedData.numLabels];
		groupedData.indexClass = new int[groupedData.l];
		// System.out.println("numLabels = 0;");
		for (i = 0; i < groupedData.l; i++) {
			labelTaked = data.label[i];

			for (j = 0; j < classLabel.length; j++)
				if (labelTaked.equals(classLabel[j]))
					break;

			if (j == groupedData.numLabels) {
				throw new ArrayIndexOutOfBoundsException("Label " + labelTaked
						+ " not found.");
			}

			groupedData.indexClass[i] = j;
			groupedData.numElements[j]++;
		}

		groupedData.classStart = new int[groupedData.numLabels];
		// groupedData.point = new InnerProductSpace[groupedData.l];

		groupedData.classStart[0] = 0;
		for (i = 1; i < groupedData.numLabels; i++) {
			groupedData.classStart[i] = groupedData.classStart[i - 1]
					+ groupedData.numElements[i - 1];
		}

		for (i = 0; i < groupedData.l; i++) {
			groupedData.point[groupedData.classStart[groupedData.indexClass[i]]] = data.point[i];
			groupedData.label[groupedData.classStart[groupedData.indexClass[i]]] = data.label[i];
			++groupedData.classStart[groupedData.indexClass[i]];
		}

		groupedData.classStart[0] = 0;
		for (i = 1; i < groupedData.numLabels; i++) {
			groupedData.classStart[i] = groupedData.classStart[i - 1]
					+ groupedData.numElements[i - 1];
		}

		/*
		 * for(i = 0; i < groupedData.numLabels; i++) {
		 * System.out.println(classLabel[i]+":");
		 * for(j=groupedData.classStart[i]; j < groupedData.classStart[i] +
		 * groupedData.numElements[i]; j++) {
		 * System.out.println("\t"+groupedData.point[j]); } }
		 */

		groupedData.grouped = true;

		return groupedData;
	}

	public double[] doubleLabel() {
		double[] lab;

		lab = new double[l];

		for (int i = 0; i < l; i++) {
			lab[i] = IO.atof(label[i]);
		}

		return lab;
	}
}

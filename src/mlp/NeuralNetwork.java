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
 * \uFFFDberschrift: <p>
 * Beschreibung: <p>
 * Copyright: Copyright (c) <p>
 * Organisation: <p>
 * @author
 * @version 1.0
 */
package mlp;

import java.io.*;
import svm.*;

public abstract class NeuralNetwork {
	SparseVector[] point;
	public String dataType;
	int l;

	public double[][][] weigth;
	public double[][] output;
	public double[] e;
	public double[][] delta;

	public int[] numUnits;
	public int numLayers;
	public int inputDimension;

	int numLabels;
	String[] label;
	int[] numElements;
	int[] classStart;
	int[] indexClass;

	public String activationFunction = "sigmoid";

	public NeuralNetwork(Data data, int[] numUnits_) {
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

			if (labelTaked.equals("no_name")) {
				indexClass[i] = -1;
				continue;
			}

			for (j = 0; j < numLabels; j++) {
				if (labelTaked.equals(label[j]))
					break;
			}

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
			if (indexClass[i] == -1)
				continue;
			point[classStart[indexClass[i]]] = (SparseVector) data.point[i];
			++classStart[indexClass[i]];
		}

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		for (i = 0; i < l; i++) {
			point[i] = (SparseVector) data.point[i];
			// System.out.println(point[i].toString());
		}

		dataType = data.type;

		numLayers = numUnits_.length;

		numUnits = new int[numLayers + 1];
		System.arraycopy(numUnits_, 0, numUnits, 0, numLayers);
		numUnits[numLayers] = numLabels;
		inputDimension = numUnits_[0];
	}

	public NeuralNetwork(Data data, double factor) {
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

			if (labelTaked.equals("no_name")) {
				indexClass[i] = -1;
				continue;
			}

			for (j = 0; j < numLabels; j++) {
				if (labelTaked.equals(label[j]))
					break;
			}

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
			if (indexClass[i] == -1)
				continue;
			point[classStart[indexClass[i]]] = (SparseVector) data.point[i];
			++classStart[indexClass[i]];
		}

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		int maxdim = -1;
		for (i = 0; i < l; i++) {
			point[i] = (SparseVector) data.point[i];
			maxdim = Math.max(maxdim, point[i].index[point[i].length - 1]);
			// System.out.println(point[i].toString());
		}

		dataType = data.type;

		numLayers = 2;

		numUnits = new int[numLayers + 1];
		// System.arraycopy(numUnits_,0,numUnits,0,numLayers);
		numUnits[0] = maxdim;
		numUnits[1] = (int) (factor * numUnits[0]);
		numUnits[numLayers] = numLabels;
		inputDimension = numUnits[0];
	}

	public NeuralNetwork(Data data) {
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

			if (labelTaked.equals("no_name")) {
				indexClass[i] = -1;
				continue;
			}

			for (j = 0; j < numLabels; j++) {
				if (labelTaked.equals(label[j]))
					break;
			}

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
			if (indexClass[i] == -1)
				continue;
			point[classStart[indexClass[i]]] = (SparseVector) data.point[i];
			++classStart[indexClass[i]];
		}

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		int maxdim = -1;
		for (i = 0; i < l; i++) {
			point[i] = (SparseVector) data.point[i];
			maxdim = Math.max(maxdim, point[i].index[point[i].length - 1]);
			// System.out.println(point[i].toString());
		}

		dataType = data.type;

		numLayers = 1;

		numUnits = new int[numLayers + 1];
		// System.arraycopy(numUnits_,0,numUnits,0,numLayers);
		numUnits[0] = maxdim;
		// numUnits[numLayers] = (int)(factor*numUnits[0]);
		numUnits[numLayers] = numLabels;
		inputDimension = numUnits[0];
	}

	public abstract void train();

	public void initialize() {
		initialize(0.5);
	}

	public void initialize(double zeta) {
		int layer, unit, k;

		weigth = new double[numLayers + 1][][];
		output = new double[numLayers + 1][];
		delta = new double[numLayers + 1][];

		output[0] = new double[inputDimension];
		for (layer = 1; layer <= numLayers; layer++) {

			weigth[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];
			output[layer] = new double[numUnits[layer]];
			delta[layer] = new double[numUnits[layer]];

			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					weigth[layer][k][unit] = zeta * (2.0 * Math.random() - 1);
				}
			}
		}

		e = new double[numLabels];
	}

	public void feedForward() {
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {

				output[layer][unit] = 0.0;
				for (k = 0; k < numUnits[layer - 1]; k++) {
					output[layer][unit] += weigth[layer][k][unit]
							* output[layer - 1][k];
				}

				output[layer][unit] += weigth[layer][k][unit];

				output[layer][unit] = activationFunction(output[layer][unit]);
			}
		}
	}

	public abstract void backPropagation();

	public abstract void updateWeights();

	public abstract void updateDeltaWeight();

	public abstract void writeModel(String filename) throws IOException;

	public abstract double activationFunction(double x);

	public abstract double activationFunctionDerivative(double x);

	@Override
	public abstract String toString();
}

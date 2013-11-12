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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import svm.Data;
import svm.IO;
import svm.SparseVector;

public class NeuralNetworkModel {
	public double[][][] weigth;
	public double[][] output;

	public double c;

	public int[] numUnits;
	public int numLayers;
	public int inputDimension;

	int numLabels;
	String[] label;
	public boolean symmetric = false;

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

	public void feedNetwork(SparseVector point) {
		int unit, j;

		unit = j = 0;
		while (unit < numUnits[0] && j < point.length) {
			if (point.index[j] == unit) {
				output[0][unit] = point.value[j];
				j++;
				unit++;
			} else {
				/*
				 * if(this.symmetric) output[0][unit] = -1.0; else
				 */
				output[0][unit] = 0.0;
				unit++;
			}
		}
		while (unit < numUnits[0]) {
			/*
			 * if(this.symmetric) output[0][unit] = -1.0; else
			 */
			output[0][unit] = 0.0;
			unit++;
		}
	}

	public double activationFunction(double x) {
		if (this.symmetric) {
			double e = Math.exp(1.33333333333 * x);
			return 1.7159 * (e - 1) / (e + 1);
		} else {
			return 1.0 / (1.0 + Math.exp(-c * x));
		}
	}

	public String classify(SparseVector v) {
		int j, predictedClass;
		double max;
		feedNetwork(v);
		feedForward();

		predictedClass = -1;
		max = Double.NEGATIVE_INFINITY;
		for (j = 0; j < numUnits[numLayers]; j++) {
			System.out.print(this.label[j] + ":"
					+ ((float) output[numLayers][j]) + " ");
			if (max < output[numLayers][j]) {
				max = output[numLayers][j];
				predictedClass = j;
			}

		}
		System.out.print("\nwin" + this.label[predictedClass] + ":"
				+ output[numLayers][predictedClass] + " ");
		System.out.println();
		// IO.println("sum: "+sum+"\n",1);

		return label[predictedClass];
	}

	public int getIndexLabel(SparseVector v) {
		int j, predictedClass;
		double max;
		feedNetwork(v);
		feedForward();

		predictedClass = -1;
		max = Double.NEGATIVE_INFINITY;
		for (j = 0; j < numUnits[numLayers]; j++) {
			if (max < output[numLayers][j]) {
				max = output[numLayers][j];
				predictedClass = j;
			}
			// System.out.print(v.name+":"+output[numLayers][j]+" ");
		}
		// System.out.println();
		// IO.println("sum: "+sum+"\n",1);

		return predictedClass;
	}

	public int getNumLabels() {
		return numLabels;
	}

	public void read(String filename) throws IOException {
		StringTokenizer st;
		int layer, unit, k;
		BufferedReader filein;

		filein = new BufferedReader(new FileReader(filename));

		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		filein.readLine();
		symmetric = filein.readLine().equals("tanh");

		filein.readLine();
		c = IO.atof(filein.readLine());

		filein.readLine();
		numLayers = IO.atoi(filein.readLine());
		numUnits = new int[numLayers + 1];

		filein.readLine(); // fileout.writeBytes("NUM_UNITS:\n");
		st = new StringTokenizer(filein.readLine(), " \n");
		for (int i = 0; i <= numLayers; i++) {
			numUnits[i] = IO.atoi(st.nextToken());
		}
		inputDimension = numUnits[0];

		filein.readLine(); // fileout.writeBytes("LABELS:\n");
		st = new StringTokenizer(filein.readLine(), " \n");
		numLabels = st.countTokens();
		label = new String[numLabels];
		for (int i = 0; i < numLabels; i++) {
			label[i] = st.nextToken();
		}

		filein.readLine(); // fileout.writeBytes("WEIGTHS:\n");

		weigth = new double[numLayers + 1][][];
		output = new double[numLayers + 1][];

		output[0] = new double[inputDimension];
		for (layer = 1; layer <= numLayers; layer++) {
			filein.readLine();

			weigth[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];
			output[layer] = new double[numUnits[layer]];

			for (unit = 0; unit < numUnits[layer]; unit++) {
				st = new StringTokenizer(filein.readLine(), " \n");
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					weigth[layer][k][unit] = IO.atof(st.nextToken());
				}
			}
		}

		filein.close();
	}

	public static void main(String[] argv) throws Exception {
		NeuralNetworkModel csvm;
		Data data = null;
		long runtime;
		int error = 0;
		String label = null;

		csvm = new NeuralNetworkModel();
		csvm.read(argv[1]);

		try {
			data = SparseVector.readData(argv[0]);
		} catch (Exception e) {
			System.out.println("Error Format in file " + argv[0]);
			System.exit(1);
		}

		runtime = -System.currentTimeMillis();
		for (int i = 0; i < data.l; i++) {
			label = csvm.classify((SparseVector) data.point[i]);
			if (!data.label[i].equals(label)) {
				// System.out.println(data.label[i]+" : "+label);
				error++;
			}
		}
		runtime += System.currentTimeMillis();
		if (csvm.symmetric) {
			System.out.println("activation function: tanh");
		}
		System.out.println("error: " + error + "/" + data.l + " ("
				+ (float) ((100.0 * error) / data.l) + "%)");
		System.out.println("runtime: " + (float) ((runtime) / 1000.0) + " Sec");
		System.out.println((float) ((1.0 * runtime) / data.l)
				+ " mSec./character");
		System.out.println((float) ((1000.0 * data.l) / runtime)
				+ " character/sec");
	}
}

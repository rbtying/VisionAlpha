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
package mlp;

/**
 * <p>\u00DCberschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author unascribed
 * @version 1.0
 */

import svm.Data;
import svm.IO;
import svm.SparseVector;

public class RPropSymmetric extends RProp {

	public RPropSymmetric(Data data, int[] numUnits, double gammaMin_,
			double gammaMax_, double d_, double u_) {
		super(data, numUnits, gammaMin_, gammaMax_, d_, u_);
		this.activationFunction = "tanh";
	}

	public RPropSymmetric(Data data, double gammaMin_, double gammaMax_,
			double d_, double u_) {
		super(data, gammaMin_, gammaMax_, d_, u_);
		this.activationFunction = "tanh";
	}

	public RPropSymmetric(Data data, double factor, double gammaMin_,
			double gammaMax_, double d_, double u_) {
		super(data, factor, gammaMin_, gammaMax_, d_, u_);
		this.activationFunction = "tanh";
	}

	public RPropSymmetric(Data data, int[] numUnits) {
		this(data, numUnits, 1.0e-6, 50.0, 0.5, 1.2);
		this.activationFunction = "tanh";
	}

	@Override
	public double activationFunction(double x) {
		double e = Math.exp(1.333333333333 * x);
		return 1.7159 * (e - 1) / (e + 1);
	}

	@Override
	public double activationFunctionDerivative(double x) {
		return 2.57385 * (1.7159 + x) * (1.7159 - x);
	}

	@Override
	public double squaredTrainError() {
		double error, e;
		int i, j;

		error = 0.0;
		for (i = 0; i < l; i++) {
			feedNetwork(point[i]);
			feedForward();

			for (j = 0; j < numUnits[numLayers]; j++) {
				if (indexClass[i] == j) {
					e = output[numLayers][j] - 1.0;
				} else {
					e = output[numLayers][j] + 1.0;
				}

				error += e * e;
			}
		}

		return error / (l * numUnits[numLayers]);
	}

	@Override
	public double squaredTestError() {
		double error, e;
		int i, j;

		error = 0.0;
		for (i = 0; i < test.length; i++) {
			feedNetwork(test[i]);
			feedForward();

			for (j = 0; j < numUnits[numLayers]; j++) {
				if (test[i].name.equals(label[j])) {
					e = output[numLayers][j] - 1.0;
				} else {
					e = output[numLayers][j] + 1.0;
				}

				error += e * e;
			}
		}

		return error / (test.length * numUnits[numLayers]);
	}

	@Override
	public void train(boolean stochastic) {
		double vError, vaError, tError = Double.POSITIVE_INFINITY;
		boolean criterion = true;
		int i, j, k, glCounter, glClassificationCounter;
		long inittime;
		iterations = 0;

		this.trainingError = optimalError = Double.POSITIVE_INFINITY;
		this.trainingClassificationError = optimalClassificationError = Double.POSITIVE_INFINITY;
		glCounter = 0;
		glClassificationCounter = 0;

		inittime = -System.currentTimeMillis();
		while (criterion) {// && Math.abs(tError - this.trainingError) > 1e-8) {

			this.trainingError = 0.0;

			initializeDeltaWeight();
			for (k = 0; k < l; k++) {
				if (stochastic) {
					i = (int) (l * Math.random());
				} else {
					i = k;
				}

				// initializeDeltaWeight();
				feedNetwork(point[i]);
				feedForward();

				for (j = 0; j < numUnits[numLayers]; j++) {
					if (indexClass[i] == j) {
						e[j] = output[numLayers][j] - 1.0;
						if (!point[i].name.equals(this.label[j])) {
							System.out.println("*************");
						}
					} else {
						e[j] = output[numLayers][j] + 1;
					}

					this.trainingError += e[j] * e[j];
				}

				backPropagation();
				this.updateDeltaWeight();
				// this.updateWeights();
			}

			this.updateWeights();
			tError = this.trainingError;
			this.trainingError = this.trainingError / (l * numUnits[numLayers]);
			vaError = this.squaredTestError();
			// this.optimalError = Math.min(optimalError,vaError);
			if (this.optimalError >= vaError) {
				this.optimalError = vaError;
				if (this.optimalWeightSquared)
					this.updateOptimalWeight();
			}
			this.validationError = vaError;
			this.generalizationLoss = 100.0 * (validationError / optimalError - 1.0);

			// trError = classificationTrainError();
			// vaError = classificationTestError();

			if (generalizationLoss > this.glParameter)
				glCounter++;
			vError = this.classificationTestError();
			tError = this.classificationTrainError();
			this.validationClassificationError = vError;
			// this.optimalClassificationError =
			// Math.min(this.optimalClassificationError,vError);
			if (this.optimalClassificationError >= vError) {
				this.optimalClassificationError = vError;
				if (!this.optimalWeightSquared)
					this.updateOptimalWeight();
			}
			this.generalizationClassificationLoss = 100 * (vError
					/ optimalClassificationError - 1);
			if (generalizationClassificationLoss > this.glParameter)
				glClassificationCounter++;

			System.out.println("RPropSymmetric");
			System.out
					.println("Iteration: " + iterations + "/" + maxIterations);
			// System.out.println(" glCounter: "+glCounter+"/"+glCounterMax);
			System.out.print("Squared Error:");
			if (this.squaredLoss)
				System.out.print("*");
			if (this.optimalWeightSquared)
				System.out.print("#");
			System.out.println();
			System.out.println("\tglCounter:\t" + glCounter + "/"
					+ glCounterMax);
			System.out.println("\tTrainig:\t" + (100.0 * trainingError));
			System.out.println("\tValidation:\t" + (100.0 * validationError));
			System.out.println("\tOptimal:\t" + (100.0 * optimalError));
			System.out.println("\tLoss:\t\t" + (generalizationLoss));
			System.out.print("Classification Error:");
			if (this.classificationLoss)
				System.out.print("*");
			if (!this.optimalWeightSquared)
				System.out.print("#");
			System.out.println();
			System.out.println("\tglCounter:\t" + glClassificationCounter + "/"
					+ glCounterMax);
			System.out.println("\tTraining:\t" + (100.0 * tError) + "% ");
			System.out.println("\tValidation:\t" + (100.0 * vError) + "%");
			System.out.println("\tOptimal:\t"
					+ (100.0 * optimalClassificationError) + "%");
			System.out.println("\tLoss:\t\t"
					+ (generalizationClassificationLoss));
			System.out
					.println("Iteration:"
							+ (float) ((System.currentTimeMillis() + inittime - runtime) / 1000.0)
							+ " Sec");
			runtime = System.currentTimeMillis() + inittime;
			// System.out.println("this"+toString());
			System.out.println("Current runtime: "
					+ (float) ((runtime) / 1000.0) + " Sec\n");
			// this.updateWeights();
			criterion = iterations++ < maxIterations;
			if (this.squaredLoss) {
				criterion = criterion && glCounter < glCounterMax;
			}
			if (this.classificationLoss) {
				criterion = criterion && glClassificationCounter < glCounterMax;
			}
		}

		// System.out.println("Current runtime: "+(float)((runtime)/1000.0)+" Sec\n");
	}

	@Override
	public String toString() {
		String s;

		s = "RPropSymmetric[numLayers=" + numLayers;
		s += ", numUnits={";
		for (int i = 0; i <= numLayers; i++) {
			s += numUnits[i] + " ";
		}
		s += "}, activationFunction=sigmoid, c=" + c;
		s += ", gammaMin=" + gammaMin;
		s += ", gammaMax=" + gammaMax;
		s += ", d=" + d;
		s += ", u=" + u;
		s += ", iterations=" + iterations + ", runtime=" + runtime + ", error="
				+ error + "]";
		return s;
	}

	public static void main(String[] argv) throws Exception {
		RPropSymmetric nn = null;
		Data data = null;
		Data test = null;

		double C = 1.0;

		double u = 1.2;
		double d = 0.5;

		double glParameter = 5;

		double gammaMin = 1e-6;
		double gammaMax = 50.0;

		boolean type = false;
		String filename = null;
		String testfilename = null;
		String filenameout = null;
		int[] numUnits = null;
		int numLayers;
		int max = 750;
		int cross = max;
		int repeat = 1;
		double factor = -1;
		boolean loss = true;

		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'a':
					glParameter = IO.atof(argv[i]);
					break;
				case 'm':
					max = IO.atoi(argv[i]);
					break;
				case 'f':
					factor = IO.atof(argv[i]);
					break;
				case 'g':
					gammaMin = IO.atof(argv[i]);
					break;
				case 'G':
					gammaMax = IO.atof(argv[i]);
					break;
				case 'd':
					d = IO.atof(argv[i]);
					break;
				case 'c':
					C = IO.atof(argv[i]);
					break;
				case 'C':
					cross = IO.atoi(argv[i]);
					break;
				case 'u':
					u = IO.atof(argv[i]);
					break;
				case 'i':
					filename = argv[i];
					break;
				case 'o':
					filenameout = argv[i];
					break;
				case 'v':
					IO.setVerbosity(IO.atoi(argv[i]));
					break;
				case 'r':
					repeat = IO.atoi(argv[i]);
					break;
				case 's':
					type = argv[i].equals("1");
					break;
				case 't':
					testfilename = argv[i];
					break;
				case 'l':
					numLayers = IO.atoi(argv[i++]);
					numUnits = new int[numLayers];
					for (int k = 0; k < numLayers; k++) {
						numUnits[k] = IO.atoi(argv[i++]);
					}
					i--;
					break;
				case 'L':
					loss = argv[i].equals("1");
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

		// try {
		data = SparseVector.readData(filename);
		/*
		 * } catch(RuntimeException e) {
		 * System.err.println("data = SparseVector.readData(filename);");
		 * System.exit(1); }
		 */

		try {
			// System.err.println("test = SparseVector.readData(testfilename); "+testfilename);
			test = SparseVector.readData(testfilename);
		} catch (RuntimeException e) {
			System.err.println("test = SparseVector.readData(testfilename);");
			System.exit(1);
		}
		if (numUnits != null) {
			nn = new RPropSymmetric(data, numUnits, gammaMin, gammaMax, d, u);
		} else if (factor > 0) {
			nn = new RPropSymmetric(data, factor, gammaMin, gammaMax, d, u);
		} else {
			nn = new RPropSymmetric(data, gammaMin, gammaMax, d, u);
		}

		nn.test = (SparseVector[]) test.point;
		nn.c = C;
		nn.maxIterations = max;
		nn.glCounterMax = cross;
		nn.glParameter = glParameter;
		nn.squaredLoss = loss;
		nn.classificationLoss = false;

		System.out.println(nn);

		for (i = 0; i < repeat; i++) {
			nn.initialize();
			nn.train(type);
			if (filenameout != null) {
				nn.writeModel(filenameout + "_" + i);
			}
		}

		// System.out.println("\n"+csvm.toString());
	}
}

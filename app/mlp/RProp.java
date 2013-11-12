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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import svm.Data;
import svm.IO;
import svm.SparseVector;

public class RProp extends BackPropagation {
	public double[][][] deltaWeigthOld;
	public double[][][] optimalWeigth;
	public double[][][] gamma;
	public double gammaMin = 1.0e-6;
	public double gammaMax = 50.0;
	public double d = 0.5;
	public double u = 1.2;

	public double trainingError;
	public double validationError;
	public double optimalError;
	public double generalizationLoss;
	public double trainingClassificationError;
	public double validationClassificationError;
	public double optimalClassificationError;
	public double generalizationClassificationLoss;
	public double glParameter = 5.0;
	public int glCounterMax;
	public int glClassificationCounterMax;
	public double min, max;
	public boolean squaredLoss = true;
	public boolean classificationLoss = true;
	public boolean optimalWeightSquared = false;

	public RProp(Data data, int[] numUnits, double gammaMin_, double gammaMax_,
			double d_, double u_) {
		super(data, numUnits, 1.0);

		gammaMin = gammaMin_;
		gammaMax = gammaMax_;
		d = d_;
		u = u_;
	}

	public RProp(Data data, double gammaMin_, double gammaMax_, double d_,
			double u_) {
		super(data, 1.0);

		gammaMin = gammaMin_;
		gammaMax = gammaMax_;
		d = d_;
		u = u_;
	}

	public RProp(Data data, double factor, double gammaMin_, double gammaMax_,
			double d_, double u_) {
		super(data, factor, 1.0);

		gammaMin = gammaMin_;
		gammaMax = gammaMax_;
		d = d_;
		u = u_;
	}

	public RProp(Data data, int[] numUnits) {
		this(data, numUnits, 1.0e-6, 50.0, 0.5, 1.2);
	}

	@Override
	public void initialize(double zeta) {
		super.initialize(zeta);
		int layer, unit, k;

		deltaWeigthOld = new double[numLayers + 1][][];
		optimalWeigth = new double[numLayers + 1][][];
		gamma = new double[numLayers + 1][][];

		for (layer = 1; layer <= numLayers; layer++) {
			deltaWeigthOld[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];
			optimalWeigth[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];
			gamma[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];

			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					deltaWeigthOld[layer][k][unit] = optimalWeigth[layer][k][unit] = 0.0;
					gamma[layer][k][unit] = 0.1;
				}
			}
		}
	}

	@Override
	public void train() {
		train(false);
	}

	@Override
	public void train(boolean stochastic) {
		double vError, vaError, tError = Double.POSITIVE_INFINITY;
		boolean criterion = true;
		int i, j, k, glCounter, glClassificationCounter;
		long inittime;
		// int ambiguity;
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
						e[j] = output[numLayers][j];
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

			System.out.println("RProp");
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
	public void backPropagation() {
		int layer, unit, k;

		for (unit = 0; unit < numUnits[numLayers]; unit++) {
			delta[numLayers][unit] = e[unit]
					* activationFunctionDerivative(output[numLayers][unit]);
		}

		for (layer = numLayers - 1; layer >= 1; layer--) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				delta[layer][unit] = 0.0;
				for (k = 0; k < numUnits[layer + 1]; k++) {
					delta[layer][unit] += weigth[layer + 1][unit][k]
							* delta[layer + 1][k];
				}

				delta[layer][unit] *= activationFunctionDerivative(output[layer][unit]);
			}
		}
	}

	@Override
	public void updateWeights() {
		double gradientProduct;
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					gradientProduct = deltaWeigth[layer][k][unit]
							* deltaWeigthOld[layer][k][unit];

					if (gradientProduct > 0.0) {
						gamma[layer][k][unit] = Math.min(u
								* gamma[layer][k][unit], gammaMax);
						weigth[layer][k][unit] -= gamma[layer][k][unit]
								* sign(deltaWeigth[layer][k][unit]);
						deltaWeigthOld[layer][k][unit] = deltaWeigth[layer][k][unit];
					} else if (gradientProduct < 0.0) {
						gamma[layer][k][unit] = Math.max(d
								* gamma[layer][k][unit], gammaMin);
						deltaWeigthOld[layer][k][unit] = 0.0;
					} else {
						weigth[layer][k][unit] -= gamma[layer][k][unit]
								* sign(deltaWeigth[layer][k][unit]);
						deltaWeigthOld[layer][k][unit] = deltaWeigth[layer][k][unit];
					}
				}
			}
		}
	}

	@Override
	public void updateDeltaWeight() {
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k < numUnits[layer - 1]; k++) {
					deltaWeigth[layer][k][unit] += delta[layer][unit]
							* output[layer - 1][k];
				}

				deltaWeigth[layer][k][unit] += delta[layer][unit];
			}
		}
	}

	public void updateOptimalWeight() {
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k < numUnits[layer - 1]; k++) {
					optimalWeigth[layer][k][unit] = weigth[layer][k][unit];
				}

				optimalWeigth[layer][k][unit] = weigth[layer][k][unit];
			}
		}
	}

	public double sign(double x) {
		if (x > 0.0) {
			return 1.0;
		} else if (x < 0.0) {
			return -1.0;
		} else {
			return 0.0;
		}
	}

	@Override
	public void writeModel(String filename) throws IOException {
		int layer, unit, k;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		fileout.writeBytes("ALGORITHM:\n" + this.toString() + "\n");
		fileout.writeBytes("SQUARED_TRAIN_ERROR:\n" + this.trainingError + "\n");
		// fileout.writeBytes("SQUARED_VALIDATION_ERROR:\n"+this.validationError+"\n");
		fileout.writeBytes("SQUARED_VALIDATION_ERROR:\n" + this.optimalError
				+ "\n");
		fileout.writeBytes("CLASSIFICATION_TRAIN_ERROR:\n"
				+ (100.0 * this.classificationTrainError()) + "%\n");
		// fileout.writeBytes("CLASSIFICATION_VALIDATION_ERROR:\n"+(100.0*this.classificationTestError())+"%\n");
		fileout.writeBytes("CLASSIFICATION_VALIDATION_ERROR:\n"
				+ (100.0 * this.optimalClassificationError) + "%\n");
		fileout.writeBytes("ITERATIONS:\n" + iterations + "\n");
		fileout.writeBytes("RUNTIME:\n" + (runtime / 1000.0) + " seconds\n");
		fileout.writeBytes("ACTIVATION_FUNCTION:\n" + this.activationFunction
				+ "\n");
		fileout.writeBytes("ACTIVATION_FUNCTION_PARAMETERS:\n" + c + "\n");
		fileout.writeBytes("NUM_LAYERS:\n" + numLayers + "\n");
		fileout.writeBytes("NUM_UNITS:\n");
		for (int i = 0; i <= numLayers; i++) {
			fileout.writeBytes(numUnits[i] + " ");
		}
		fileout.writeBytes("\n");
		fileout.writeBytes("LABELS:\n");
		for (int i = 0; i < numLabels; i++) {
			fileout.writeBytes(label[i] + " ");
		}
		fileout.writeBytes("\n");
		fileout.writeBytes("WEIGTHS:\n");
		for (layer = 1; layer <= numLayers; layer++) {
			fileout.writeBytes("LAYER " + layer + ":\n");
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					// fileout.writeBytes((float)weigth[layer][k][unit]+" ");
					fileout.writeBytes((float) optimalWeigth[layer][k][unit]
							+ " ");
				}
				fileout.writeBytes("\n");
			}
		}

		fileout.close();
	}

	@Override
	public String toString() {
		String s;

		s = "RProp[numLayers=" + numLayers;
		s += ", numUnits={";
		for (int i = 0; i <= numLayers; i++) {
			s += numUnits[i] + " ";
		}
		s += "}, activationFunction=" + this.activationFunction + ", c=" + c;
		s += ", gammaMin=" + gammaMin;
		s += ", gammaMax=" + gammaMax;
		s += ", d=" + d;
		s += ", u=" + u;
		s += ", iterations=" + iterations + ", runtime=" + runtime + ", error="
				+ error + "]";
		return s;
	}

	public static void main(String[] argv) throws Exception {
		RProp nn = null;
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
					type = IO.atoi(argv[i]) == 1;
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
			nn = new RProp(data, numUnits, gammaMin, gammaMax, d, u);
		} else if (factor > 0) {
			nn = new RProp(data, factor, gammaMin, gammaMax, d, u);
		} else {
			nn = new RProp(data, gammaMin, gammaMax, d, u);
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

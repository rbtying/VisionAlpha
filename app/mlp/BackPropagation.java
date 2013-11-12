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

public class BackPropagation extends NeuralNetwork {
	public SparseVector[] test = null;

	public double[][][] deltaWeigth;
	public double gamma;
	public double alpha;
	public double c = 1.0;
	public int maxIterations = 50;
	public int iterations = 0;
	public long runtime = -1;
	public double error;

	public double trainingError;
	public double validationError;
	public double optimalError;
	public double generalizationLoss;
	public double glParameter = 5.0;
	public int glCounterMax;
	public double min, max;

	public BackPropagation(Data data, int[] numUnits, double gamma_) {
		super(data, numUnits);
		gamma = gamma_;
	}

	public BackPropagation(Data data, double gamma_) {
		super(data);
		gamma = gamma_;
	}

	public BackPropagation(Data data, double factor, double gamma_) {
		super(data, factor);
		gamma = gamma_;
	}

	@Override
	public void initialize(double zeta) {
		super.initialize(zeta);
		int layer;

		deltaWeigth = new double[numLayers + 1][][];

		for (layer = 1; layer <= numLayers; layer++) {
			deltaWeigth[layer] = new double[numUnits[layer - 1] + 1][numUnits[layer]];
		}
	}

	@Override
	public void train() {
		train(false);
	}

	public void train(boolean stochastic) {
		double vError, vaError, tError = Double.POSITIVE_INFINITY;
		int i, j, k, glCounter;
		long inittime;
		iterations = 0;

		this.trainingError = optimalError = Double.POSITIVE_INFINITY;
		glCounter = 0;

		inittime = -System.currentTimeMillis();
		while (iterations++ < maxIterations && glCounter < glCounterMax) {// &&
																			// Math.abs(tError
																			// -
																			// this.trainingError)
																			// >
																			// 1e-8)
																			// {

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
			this.optimalError = Math.min(optimalError, vaError);
			this.validationError = vaError;
			this.generalizationLoss = 100.0 * (validationError / optimalError - 1.0);

			// trError = classificationTrainError();
			// vaError = classificationTestError();

			if (generalizationLoss > this.glParameter)
				glCounter++;
			vError = this.classificationTestError();
			tError = this.classificationTrainError();

			System.out.println("BackPropagation");
			System.out.print("Iteration: " + iterations + "/" + maxIterations);
			System.out.println(" glCounter: " + glCounter + "/" + glCounterMax);
			System.out.println("Trainig squared error: "
					+ (100.0 * trainingError));
			System.out.println("Validation squared error: "
					+ (100.0 * validationError));
			System.out.println("Trainig error: " + (100.0 * tError) + "% ");
			System.out.println("Validation error: " + (100.0 * vError) + "%");
			System.out
					.println("Optimal error: " + (100.0 * optimalError) + "%");
			System.out.println("GeneralizationLoss: " + (generalizationLoss));
			System.out
					.println("Iterationtime: "
							+ (float) ((System.currentTimeMillis() + inittime - runtime) / 1000.0)
							+ " Sec");
			runtime = System.currentTimeMillis() + inittime;
			// System.out.println("this"+toString());
			System.out.println("Current runtime: "
					+ (float) ((runtime) / 1000.0) + " Sec\n");
			// this.updateWeights();
		}

		// System.out.println("Current runtime: "+(float)((runtime)/1000.0)+" Sec\n");
	}

	/*
	 * public void train(boolean stochastic) { int i, j, k; long inittime;
	 * iterations = 0; double validationErrorOld = 1.0;
	 * 
	 * inittime = -System.currentTimeMillis(); while(iterations < maxIterations)
	 * { error = 0.0; //initializeDeltaWeight(); for(k = 0; k < l; k++) {
	 * if(stochastic) { i = (int)(l*Math.random()); } else { i = k; }
	 * 
	 * initializeDeltaWeight(); feedNetwork(point[i]); feedForward();
	 * 
	 * for(j = 0; j < numUnits[numLayers]; j++) { if(indexClass[i] == j) { e[j]
	 * = output[numLayers][j] - 1.0; } else { e[j] = output[numLayers][j]; }
	 * 
	 * error += e[j]*e[j]; }
	 * 
	 * backPropagation(); this.updateDeltaWeight(); this.updateWeights(); }
	 * error = 100.0*error/(l*numUnits[numLayers]); iterations++;
	 * 
	 * System.out.println("Iteration: "+iterations);
	 * System.out.println("Classification error: "
	 * +(float)(100.0*classificationTrainError())+"%");
	 * System.out.println("Cuadratic error: "+(float)error);
	 * System.out.println("Validation error: "
	 * +(float)(100.0*classificationTestError())+"%");
	 * System.out.println("Iterationtime: "
	 * +(float)((System.currentTimeMillis()+inittime-runtime)/1000.0)+" Sec");
	 * runtime = System.currentTimeMillis()+inittime;
	 * System.out.println("Current runtime: "
	 * +(float)((runtime)/1000.0)+" Sec\n"); //this.updateWeights();
	 * validationErrorOld = 0.0; }
	 * 
	 * 
	 * //System.out.println("Current runtime: "+(float)((runtime)/1000.0)+" Sec\n"
	 * ); }
	 */
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
					e = output[numLayers][j];
				}

				error += e * e;
			}
		}

		return error / (l * numUnits[numLayers]);
	}

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
					e = output[numLayers][j];
				}

				error += e * e;
			}
		}

		return error / (test.length * numUnits[numLayers]);
	}

	public double classificationTrainError() {
		double error, max;
		int i, j, predictedClass, count;

		error = 0.0;
		count = 0;
		for (i = 0; i < l; i++) {
			if (indexClass[i] == -1)
				continue;

			feedNetwork(point[i]);
			feedForward();

			predictedClass = -1;
			max = Double.NEGATIVE_INFINITY;
			for (j = 0; j < numUnits[numLayers]; j++) {
				if (max < output[numLayers][j]) {
					max = output[numLayers][j];
					predictedClass = j;
				}
			}

			if (predictedClass != indexClass[i]) {
				error++;
			}
			count++;
		}

		return error / count;
	}

	public double classificationTestError() {
		if (test == null)
			return -1;
		double error, max;
		int i, j, predictedClass, count;

		error = 0.0;
		count = 0;
		for (i = 0; i < test.length; i++) {
			if (test[i].name.equals("no_name"))
				continue;

			feedNetwork(test[i]);
			feedForward();

			predictedClass = -1;
			max = Double.NEGATIVE_INFINITY;
			for (j = 0; j < numUnits[numLayers]; j++) {
				if (max < output[numLayers][j]) {
					max = output[numLayers][j];
					predictedClass = j;
				}
			}

			if (!label[predictedClass].equals(test[i].name)) {
				error++;
			}
			count++;
		}

		return error / count;
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
				output[0][unit] = 0.0;
				unit++;
			}
		}
		while (unit < numUnits[0]) {
			output[0][unit] = 0.0;
			unit++;
		}
	}

	@Override
	public double activationFunction(double x) {
		return 1.0 / (1.0 + Math.exp(-c * x));
	}

	@Override
	public double activationFunctionDerivative(double x) {
		return c * x * (1.0 - x);
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
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					weigth[layer][k][unit] += deltaWeigth[layer][k][unit];
				}
			}
		}
	}

	public void initializeDeltaWeight() {
		int layer, unit, k;

		for (layer = 1; layer <= numLayers; layer++) {
			for (unit = 0; unit < numUnits[layer]; unit++) {
				for (k = 0; k <= numUnits[layer - 1]; k++) {
					deltaWeigth[layer][k][unit] = 0.0;
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
					deltaWeigth[layer][k][unit] -= gamma * delta[layer][unit]
							* output[layer - 1][k] - alpha
							* deltaWeigth[layer][k][unit];
				}

				deltaWeigth[layer][k][unit] -= gamma * delta[layer][unit]
						- alpha * deltaWeigth[layer][k][unit];
			}
		}
	}

	@Override
	public void writeModel(String filename) throws IOException {
		int layer, unit, k;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		fileout.writeBytes("ALGORITHM:\n" + this.toString() + "\n");
		fileout.writeBytes("SQUARED_TRAIN_ERROR:\n" + this.trainingError + "\n");
		fileout.writeBytes("SQUARED_VALIDATION_ERROR:\n" + this.validationError
				+ "\n");
		fileout.writeBytes("CLASSIFICATION_TRAIN_ERROR:\n"
				+ (100.0 * this.classificationTrainError()) + "%\n");
		fileout.writeBytes("CLASSIFICATION_VALIDATION_ERROR:\n"
				+ (100.0 * this.classificationTestError()) + "%\n");
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
					fileout.writeBytes((float) weigth[layer][k][unit] + " ");
				}
				fileout.writeBytes("\n");
			}
		}

		fileout.close();
	}

	/*
	 * public void writeModel(String filename) throws IOException { int layer,
	 * unit, k; DataOutputStream fileout;
	 * 
	 * fileout = new DataOutputStream(new FileOutputStream(filename));
	 * 
	 * fileout.writeBytes("ALGORITHM:\n"+this.toString()+"\n");
	 * fileout.writeBytes("CUADRATIC_ERROR:\n"+error+"\n");
	 * fileout.writeBytes("CLASSIFICATION_TRAIN_ERROR:\n"
	 * +(100.0*this.classificationTrainError())+"%\n");
	 * fileout.writeBytes("CLASSIFICATION_TEST_ERROR:\n"
	 * +(100.0*this.classificationTestError())+"%\n");
	 * fileout.writeBytes("ITERATIONS:\n"+iterations+"\n");
	 * fileout.writeBytes("RUNTIME:\n"+(runtime/1000.0)+" seconds\n");
	 * fileout.writeBytes("ACTIVATION_FUNCTION:\nsigmoid\n");
	 * fileout.writeBytes("ACTIVATION_FUNCTION_PARAMETERS:\n"+c+"\n");
	 * fileout.writeBytes("NUM_LAYERS:\n"+numLayers+"\n");
	 * fileout.writeBytes("NUM_UNITS:\n"); for(int i = 0; i <= numLayers; i++) {
	 * fileout.writeBytes(numUnits[i]+" "); } fileout.writeBytes("\n");
	 * fileout.writeBytes("LABELS:\n"); for(int i = 0; i < numLabels; i++) {
	 * fileout.writeBytes(label[i]+" "); } fileout.writeBytes("\n");
	 * fileout.writeBytes("WEIGTHS:\n"); for(layer = 1; layer <= numLayers;
	 * layer++) { fileout.writeBytes("LAYER "+layer+":\n"); for(unit = 0; unit <
	 * numUnits[layer]; unit++) { for(k = 0; k <= numUnits[layer - 1]; k++) {
	 * fileout.writeBytes((float)weigth[layer][k][unit]+" "); }
	 * fileout.writeBytes("\n"); } }
	 * 
	 * fileout.close(); }
	 */
	@Override
	public String toString() {
		for (int i = 0; i <= numLayers; i++) {
		}
		return "BackPropagation";
	}

	public static void main(String[] argv) throws Exception {
		BackPropagation nn;
		Data data = null;
		Data test = null;

		double C = 1.0;
		double factor = 0.5;
		boolean type = false;
		String filename = null;
		String testfilename = null;
		String filenameout = null;
		int[] numUnits = null;
		int numLayers;
		int max = 5000;

		double alpha = 0.5;

		int i, cross = 3;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'm':
					max = IO.atoi(argv[i]);
					break;
				case 'f':
					factor = IO.atof(argv[i]);
					break;
				case 'g':
					factor = IO.atof(argv[i]);
					break;
				case 'b':
					IO.atof(argv[i]);
					break;
				case 'c':
					C = IO.atof(argv[i]);
					break;
				case 'a':
					alpha = IO.atof(argv[i]);
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
				case 't':
					testfilename = argv[i];
					break;
				case 's':
					type = IO.atoi(argv[i]) == 1;
					break;
				case 'C':
					cross = IO.atoi(argv[i]);
					break;
				case 'l':
					numLayers = IO.atoi(argv[i++]);
					numUnits = new int[numLayers];
					for (int k = 0; k < numLayers; k++) {
						numUnits[k] = IO.atoi(argv[i++]);
					}
					i--;
					break;
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

		try {
			data = SparseVector.readData(filename);
		} catch (RuntimeException e) {
			System.err.println("data = SparseVector.readData(filename);");
			System.exit(1);
		}

		try {
			// System.err.println("test = SparseVector.readData(testfilename); "+testfilename);
			test = SparseVector.readData(testfilename);
		} catch (RuntimeException e) {
			System.err.println("test = SparseVector.readData(testfilename);");
			System.exit(1);
		}

		nn = new BackPropagation(data, numUnits, factor);
		nn.test = (SparseVector[]) test.point;
		// System.out.println(csvm.toString()+"\n");

		nn.initialize();
		nn.c = C;
		nn.maxIterations = max;
		nn.alpha = alpha;
		nn.glCounterMax = cross;
		nn.train(type);

		if (filenameout != null) {
			nn.writeModel(filenameout);
		}

		// System.out.println("\n"+csvm.toString());
	}
}

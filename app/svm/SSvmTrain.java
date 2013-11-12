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
 * Copyright: Copyright (c) Ernesto Tapia<p>
 * Organisation: FU Berlin<p>
 * @author Ernesto Tapia
 * @version 1.0
 */
package svm;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SSvmTrain {
	InnerProductSpace[] point;
	public String dataType;
	double[] alpha;
	double[] b;
	int l;

	double C;
	double tau;
	double eps;

	private int type;
	private int degree;
	private double factor;
	private double bias;

	private int size;

	private int evaluations = 0;

	int numLabels;
	String[] label;
	int[] numElements;
	int[] classStart;
	int[] indexClass;
	int[] numSupport;
	boolean[] isSupport;

	int support = 0;

	long runtime = -1;

	boolean header = true;

	public SSvmTrain(Data data, double C_, double tau_, double eps_, int type_,
			int degree_, double factor_, double bias_, int size_) {
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
		point = new InnerProductSpace[l];

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

		dataType = data.type;

		C = C_;
		tau = tau_;
		eps = eps_;
		size = size_;

		type = type_;
		degree = degree_;
		factor = factor_;
		bias = bias_;

		size = size_;
	}

	public void train() {
		SSmo smo;
		Kernel kernel;
		InnerProductSpace[] nodePoint;
		int p, n, h;
		int numnodes, count;

		numnodes = count = numLabels;

		b = new double[numLabels];
		alpha = new double[l];

		evaluations = 0;
		runtime = -System.currentTimeMillis();
		for (p = 0; p < numLabels; p++) {
			{

				nodePoint = new InnerProductSpace[numElements[p]];

				for (h = 0; h < numElements[p]; h++) {
					nodePoint[h] = point[classStart[p] + h];
				}

				IO.println(
						"Nodes to be trained: " + (count--) + "/" + numnodes, 1);
				IO.println("Training Class " + label[p], 1);

				kernel = new Kernel(nodePoint, type, degree, factor, bias, size);
				smo = new SSmo(kernel, C, tau, eps);

				smo.train();

				System.arraycopy(smo.alpha, 0, alpha, classStart[p],
						numElements[p]);

				b[p] = smo.b - 1;
				evaluations += kernel.getEvaluations();

				IO.println("\n", 1);
			}
		}
		runtime += System.currentTimeMillis();

		numSupport = new int[numLabels];
		isSupport = new boolean[l];
		for (p = 0; p < numLabels; p++) {
			numSupport[p] = 0;
			for (n = classStart[p]; n < classStart[p] + numElements[p]; n++) {
				isSupport[n] = false;
				if (alpha[n] > tau) {
					numSupport[p]++;
					isSupport[n] = true;
				}
			}
			support += numSupport[p];
		}

		IO.println("\n", 1);
		IO.println("Total support ArrayLists:        " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Training time in seconds:     " + (runtime / 1000.0), 2);
		IO.println("Total kernel evaluations:     " + evaluations, 3);
	}

	public void writeModel(String filename) throws IOException {
		int p, i;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		if (header) {
			fileout.writeBytes("ALGORITHM:\n" + this.toString() + "\n");
			fileout.writeBytes("KERNEL:\n" + type + "\n");
			fileout.writeBytes("DEGREE:\n" + degree + "\n");
			fileout.writeBytes("FACTOR:\n" + factor + "\n");
			fileout.writeBytes("BIAS:\n" + bias + "\n");

			fileout.writeBytes("LABELS:\n");

			for (i = 0; i < numLabels; i++)
				fileout.writeBytes(label[i] + " ");

			fileout.writeBytes("\nNUMSUPPORT:\n");
			for (i = 0; i < numLabels; i++)
				fileout.writeBytes(numSupport[i] + " ");

			fileout.writeBytes("\nTHRESHOLD:\n");
			for (i = 0; i < numLabels; i++)
				fileout.writeBytes((float) b[i] + " ");
			fileout.writeBytes("\n");
		}
		fileout.writeBytes(dataType + " " + support + "\n");
		for (p = 0; p < l; p++) {
			if (isSupport[p]) {
				this.point[p].write(fileout);
			}
		}

		fileout.close();
	}

	@Override
	public String toString() {
		String s;

		s = "SSvmTrain["
				+ (new Kernel(null, type, degree, factor, bias, size))
						.toString();
		s += ", C=" + C + ", tau=" + tau + ", eps=" + eps;
		if (support > 0) {
			s += ", support_ArrayLists=" + support + "/" + l + " ("
					+ ((float) (100.0 * support) / l) + "%)";
		}
		if (runtime > 0) {
			s += ", runtime=" + (runtime / 1000.0) + " Sec";
		}
		if (evaluations > 0) {
			s += ", kernel_evaluations=" + evaluations;
		}

		s += "]";

		return s;
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	public static void main(String[] argv) throws Exception {
		SSvmTrain svm;
		Data data = null;

		double C = 0.5;
		double tau = 1E-8;
		double eps = 0.001;

		int type = Kernel.RBF;
		int degree = 1;
		double factor = 0.01;
		double bias = 0.0;
		int size = 40;

		String filename = null;
		String filenameout = null;
		boolean header = false;

		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'h':
					header = 1 == atoi(argv[i]);
					break;
				case 'k':
					type = atoi(argv[i]);
					break;
				case 'd':
					degree = atoi(argv[i]);
					break;
				case 'f':
					factor = atof(argv[i]);
					break;
				case 'g':
					factor = atof(argv[i]);
					break;
				case 'b':
					bias = atof(argv[i]);
					break;
				case 'n':
					C = atof(argv[i]);
					break;
				case 't':
					tau = atof(argv[i]);
					break;
				case 'e':
					eps = atof(argv[i]);
					break;
				case 'i':
					filename = argv[i];
					break;
				case 'o':
					filenameout = argv[i];
					break;
				case 's':
					size = atoi(argv[i]);
					break;
				case 'v':
					IO.setVerbosity(atoi(argv[i]));
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
			data = StepFunction.readData(filename);
		}

		svm = new SSvmTrain(data, C, tau, eps, type, degree, factor, bias, size);
		svm.header = header;
		svm.train();

		if (filenameout != null) {
			svm.writeModel(filenameout);
		} else {
			svm.writeModel(filename + ".mod");
		}
	}
}

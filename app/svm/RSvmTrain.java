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

import java.io.*;

public class RSvmTrain {
	// RSmo1 smo;
	RSmo smo;
	InnerProductSpace[] point;
	public String dataType;
	Kernel kernel = null;
	double[] label;
	double[] alpha;
	double[] alpha_s;
	double b;
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

	boolean[] isSupport;

	int support = 0;

	long runtime = -1;

	boolean header = true;

	public RSvmTrain(Data data, double C_, double tau_, double eps_, int type_,
			int degree_, double factor_, double bias_, int size_) {
		int i;

		l = data.l;

		System.out.println("data.l: " + data.l);

		label = new double[l];
		point = new InnerProductSpace[l];

		for (i = 0; i < l; i++) {
			label[i] = Double.parseDouble(data.label[i]);
			point[i] = data.point[i];
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
		int p;

		b = 0;
		alpha = new double[l];

		evaluations = 0;
		runtime = -System.currentTimeMillis();

		kernel = new Kernel(point, type, degree, factor, bias, size);
		// smo = new RSmo1(kernel,label,C,tau,eps);
		smo = new RSmo(kernel, label, C, tau, eps);

		smo.train();

		b = smo.b;
		evaluations += kernel.getEvaluations();
		IO.println("\n", 1);

		runtime += System.currentTimeMillis();

		isSupport = new boolean[l];
		System.out.println("l: " + l);
		for (p = 0; p < l; p++) {
			isSupport[p] = false;
			alpha[p] = smo.alpha[p];
			// alpha_s[p] = smo.alpha[p];
			// if(Math.abs(alpha[p] - alpha_s[p]) > tau) {
			if (Math.abs(alpha[p]) > tau) {
				isSupport[p] = true;
				support++;
			}
		}

		IO.println("\n", 1);
		IO.println("Total support ArrayLists:        " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Training time in seconds:     " + (runtime / 1000.0), 2);
		IO.println("Total kernel evaluations:     " + evaluations, 3);
	}

	public void writeModel(String filename) throws IOException {
		int p;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		fileout.writeBytes("ALGORITHM:\n" + this.toString() + "\n");
		fileout.writeBytes("KERNEL:\n" + type + "\n");
		fileout.writeBytes("DEGREE:\n" + degree + "\n");
		fileout.writeBytes("FACTOR:\n" + factor + "\n");
		fileout.writeBytes("BIAS:\n" + bias + "\n");

		fileout.writeBytes("LABELS:\n");
		fileout.writeBytes("no labels (regression)");

		fileout.writeBytes("\nNUMSUPPORT:\n");
		fileout.writeBytes(support + " ");

		fileout.writeBytes("\nTHRESHOLD:\n");
		fileout.writeBytes((float) b + "\n");

		fileout.writeBytes("ALPHA:\n");
		for (p = 0; p < l; p++) {
			if (isSupport[p]) {
				// fileout.writeBytes(((float)smo.alpha[p])+" "+((float)smo.alpha_s[p])+"\n");
				fileout.writeBytes(((float) smo.alpha[p]) + "\n");
			}
		}

		fileout.writeBytes("\n" + dataType + " " + support + "\n");
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

		s = "RSvmTrain["
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

	double classify(InnerProductSpace x) {
		double val;
		int i;

		val = -b;

		for (i = 0; i < l; i++) {
			if (Math.abs(alpha[i]) > tau) {
				val += alpha[i] * kernel.value(this.point[i], x);
			}
		}

		return val;
	}

	double cuadraticError(Data data) {
		double error, val;
		int i;

		error = 0;
		for (i = 0; i < data.l; i++) {
			val = Double.parseDouble(data.label[i])
					- this.classify(data.point[i]);
			error += val * val;
		}

		return (Math.sqrt(error) / l);
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	public static void main(String[] argv) throws Exception {
		RSvmTrain svm;
		Data data = null, test = null;

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
		String filenametest = null;
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
				case 'c':
					C = atof(argv[i]);
					break;
				case 'T':
					filenametest = argv[i];
					break;
				default:
					System.err.print("unknown option: " + argv[i - 1] + "\n");
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

		svm = new RSvmTrain(data, C, tau, eps, type, degree, factor, bias, size);
		svm.header = header;
		System.out.println(svm.toString());
		svm.train();

		if (filenameout != null) {
			svm.writeModel(filenameout);
		} else {
			svm.writeModel(filename + ".mod");
		}

		if (filenametest != null) {
			System.out.println("\nReading Test File:");
			test = SparseVector.readData(filenametest);
			System.out.println("\nCuadratic Test Error: "
					+ svm.cuadraticError(test));
		}
	}
}

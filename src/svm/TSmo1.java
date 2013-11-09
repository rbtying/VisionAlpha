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

public class TSmo1 {
	Kernel kernel;

	double[] error_cache;
	double tau;
	double eps;

	double[] alpha;
	double b;
	double b_up;
	double b_low;
	int i_up;
	int i_low;
	int l;

	int support;
	int positiv;
	int negativ;
	int error;

	public TSmo1(Kernel k, double tau_, double eps_) {
		int i, i_rnd;
		kernel = k;
		l = kernel.l;

		error_cache = new double[l];
		alpha = new double[l];

		b = 0.0;
		i_rnd = (int) (l * Math.random());
		// i_rnd = 0;

		alpha[i_rnd] = 1;
		kernel_i1 = kernel.getColumn(i_rnd);
		b_up = Double.POSITIVE_INFINITY;
		for (i = 0; i < l; i++) {
			error_cache[i] = kernel_i1[i];
			if (i != i_rnd && error_cache[i] < b_up) {
				b_up = error_cache[i];
				i_up = i;
			}
		}

		b_low = error_cache[i_rnd];
		i_low = i_rnd;
		/*
		 * i_low = i_up; i_up = i_rnd;
		 */
		System.out.println("b_up=" + b_up + " b_low=" + b_low);

		// b_up = error_cache[0];
		// i_up = 0;

		tau = tau_;
		eps = eps_;
	}

	double alph1, alph2;
	double E1, E2;
	double s;
	double k11, k12, k22;
	double eta;
	double a1, a2;
	double delta;
	double C1, C2;
	int k;

	double[] kernel_i1;
	double[] kernel_i2;

	int takeStep(int i1, int i2) {

		// System.out.print("@");

		if (i1 == i2)
			return 0;

		/* Lagrange multiplier for i1 */
		alph1 = alpha[i1];
		alph2 = alpha[i2];

		E1 = error_cache[i1];
		E2 = error_cache[i2];

		delta = alph2 + alph1;

		kernel_i1 = kernel.getColumn(i1);
		kernel_i2 = kernel.getColumn(i2);
		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		a2 = alph2;

		eta = k11 + k22 - 2.0 * k12;
		// System.out.println("eta="+eta);
		if (eta > 0) {
			a2 = alph2 + (E1 - E2) / eta;
			if (a2 < tau) {
				a2 = 0;
			}

			// System.out.println("a2="+a2);
		} else {
			/* Objetive function at a2 = l, H */
			C1 = C2 = 0.0;
			for (k = 0; k < l; k++) {
				if (k != i1 && k != i2 && alpha[k] > tau) {
					/*
					 * C1 += alpha[k]*kernel.value(i1,k); C2 +=
					 * alpha[k]*kernel.value(i2,k);
					 */
					C1 += alpha[k] * kernel_i1[k];
					C2 += alpha[k] * kernel_i2[k];
				}
			}
		}

		if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps))
			return 0;

		a1 = delta - a2;
		/*
		 * System.out.println("alph1="+alph1+" a1="+a1);
		 * System.out.println("alph2="+alph2+" a2="+a2);
		 * System.out.println("E1="+E1+" E2="+E2); System.out.println();
		 */
		/* Update error cache using new Lagrange multipliers */
		b_low = Double.NEGATIVE_INFINITY;
		i_low = -1;
		b_up = Double.POSITIVE_INFINITY;
		i_up = -1;
		for (k = 0; k < l; k++) {
			error_cache[k] += (a1 - alph1) * kernel_i1[k] + (a2 - alph2)
					* kernel_i2[k];
			if (alpha[k] > tau) {
				if (b_low < error_cache[k]) {
					b_low = error_cache[k];
					i_low = k;
				}
			} else {
				if (b_up > error_cache[k]) {
					b_up = error_cache[k];
					i_up = k;
				}
			}
		}

		/* Store a1, a2 in alph array */
		alpha[i1] = a1;
		alpha[i2] = a2;

		return 1;
	}

	double tmp, tmax;

	int examineExample(int i2) {
		int i1 = i2;

		alph2 = alpha[i2];

		// SVM output on point[i2] - y2
		E2 = error_cache[i2];

		// Check optimality using current b_low and b_up
		// If violated find index i1 to do joint optimization with i2

		int optimality = 1;

		if (alph2 <= tau) {
			if (b_low - E2 > 2.0 * eps) {
				optimality = 0;
				i1 = i_low;
			}
		} else {
			if (E2 - b_up > 2.0 * eps) {
				optimality = 0;
				i1 = i_up;
			}
		}

		if (optimality == 1) {
			return 0;
		}

		// For i2 in I0 choose the better i1
		/*
		 * if(alph2 > tau) { if(b_low - E2 > E2 - b_up) { i1 = i_low; } else {
		 * i1 = i_up; } }
		 */

		return takeStep(i1, i2);
	}

	int kernelEvaluations() {
		return kernel.evaluations;
	}

	double getAlpha(int i) {
		return alpha[i];
	}

	void train() {
		int numChanged;
		int examineAll;
		int innerLoopSuccess;
		int i;
		numChanged = 0;
		examineAll = 1;

		while (numChanged > 0 || examineAll == 1) {
			numChanged = 0;
			if (examineAll == 1) {
				/* Loop over all training examples */
				System.out.print("*");
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
				// System.out.println("Iteration: "+(++iter)+"\tnumChanged: "+numChanged);
			} else {
				/* Loop over examples where alpha is not 0 & not C */
				/*
				 * System.out.print("."); for(i=0; i<l; i++) { if(alpha[i] >
				 * tau) { numChanged += examineExample(i); if(b_up > b_low -
				 * 2.0*eps) { numChanged = 0; break; } }
				 * //System.out.println("Iteration: "
				 * +(++iter)+"\tnumChanged: "+numChanged); } }
				 */

				// This is the code for Modification 2 from Keerthi et al.'s
				// paper
				innerLoopSuccess = 1;
				numChanged = 0;
				while ((b_up < b_low - 2 * eps) && (innerLoopSuccess > 0)) {
					IO.print("_", 2);
					innerLoopSuccess = takeStep(i_up, i_low);
				}
			}
			if (examineAll == 1)
				examineAll = 0;
			else if (numChanged == 0)
				examineAll = 1;
		}

		error = 0;
		support = 0;
		positiv = 0;
		negativ = 0;
		for (i = 0; i < l; i++) {
			if (alpha[i] > tau) {
				support++;
				// positiv += (target[i]==1)? 1 : 0;
				// negativ += (target[i]==-1)? 1 : 0;
			}
			System.out.println(alpha[i] + " " + (error_cache[i] - b_low + 1));
			// error += ((error_cache[i]+target[i])>tau)? 0 : 1;
		}
		b = b_low;
		System.out.println();
		System.out.println("Support ArrayLists:\t\t" + support + "/" + l + " ("
				+ ((float) (100.0 * support) / l) + "%)");
		// System.out.println("Positiv Support ArrayLists:\t"+positiv+"/"+l+" ("+((float)(100.0*positiv)/l)+"%)");
		// System.out.println("Negativ Support ArrayLists:\t"+negativ+"/"+l+" ("+((float)(100.0*negativ)/l)+"%)");
		System.out.println("Missclassifications:\t\t" + error + "/" + l + " ("
				+ ((float) (100.0 * error) / l) + "%)\n");
		System.out.println("Kernel Evaliations:\t\t" + kernelEvaluations()
				+ "\n");
		System.out.println("b_up=" + b_up + " b_low=" + b_low);
		System.out.println("b=" + b);
		getR();
		System.out.println();
	}

	public double getR() {
		int i, j, k;
		double w;

		for (i = 0; i < l; i++) {
			if (tau < alpha[i]) {
				break;
			}
		}

		w = kernel.value(i, i);

		for (j = 0; j < l; j++) {
			if (tau < alpha[j]) {
				w += -2 * alpha[j] * kernel.value(j, i);
			}
		}

		for (j = 0; j < l; j++) {
			if (tau < alpha[j]) {
				for (k = 0; k < l; k++) {
					if (tau < alpha[k]) {
						if (j != k) {
							w += alpha[j] * alpha[k] * kernel.value(j, k);
						}
					}
				}
			}
		}

		return w;
	}

	public double getRC(double C) {
		double w;
		int i;

		w = 0;
		for (i = 0; i < l; i++) {
			if (tau < alpha[i]) {
				w -= alpha[i] * (1 - alpha[i]);
			}
		}

		return w / (C * C);
	}

	public double getRs() {
		int j, k;
		double w;
		w = 0;

		for (j = 0; j < l; j++) {
			if (tau < alpha[j]) {
				for (k = 0; k < l; k++) {
					if (tau < alpha[k]) {
						if (j != k) {
							w += alpha[j] * alpha[k] * kernel.value(j, k)
									* kernel.distanceSquared(j, k);
						}
					}
				}
			}
		}

		return w;
	}

	/*
	 * double learned(Data x) { double sum = -b; for(int i = 0; i < l; i++)
	 * if(alpha[i] > tau) sum += target[i]*alpha[i]*kernel(point[i],x); return
	 * sum; }
	 */

	public static void main(String[] argv) throws Exception {
		TSmo1 rsmo;
		Data data = null;

		double tau = 1E-8;
		double eps = 0.001;

		int type = Kernel.RBF;
		int degree = 3;
		double factor = 0.0001;
		double bias = 0.0;
		int size = 40;

		String filename = null;
		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-') {
					break;
				}
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'k':
					type = Integer.parseInt(argv[i]);
					break;
				case 'd':
					degree = Integer.parseInt(argv[i]);
					break;
				case 'f':
					factor = Double.parseDouble(argv[i]);
					break;
				case 'g':
					factor = Double.parseDouble(argv[i]);
					break;
				case 'b':
					bias = Double.parseDouble(argv[i]);
					break;
				case 'c':
					Double.parseDouble(argv[i]);
					break;
				case 't':
					tau = Double.parseDouble(argv[i]);
					break;
				case 'e':
					eps = Double.parseDouble(argv[i]);
					break;
				case 'i':
					filename = argv[i];
					break;
				case 'o':
					break;
				case 'O':
					break;
				case 's':
					size = Integer.parseInt(argv[i]);
					break;
				case 'v':
					IO.setVerbosity(Integer.parseInt(argv[i]));
					break;
				default:
					System.err.print("unknown option: " + argv[i - 1].charAt(1)
							+ "");
					System.exit(1);
				}
			}
		} catch (Exception e) {
			// System.err.print(e.toString());
			// System.exit(1);
		}

		try {
			data = SparseVector.readData(filename);
		} catch (RuntimeException e) {
			data = StepFunction.readData(filename);
		}

		Kernel kernel = new Kernel(data.point, type, degree, factor, bias, size);

		rsmo = new TSmo1(kernel, tau, eps);

		rsmo.train();
	}

}

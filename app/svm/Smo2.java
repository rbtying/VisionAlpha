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
 * Copyright: Copyright (c) Ernesto Tapia<p>
 * Organisation: FU Berlin<p>
 * @author Ernesto Tapia
 * @version 1.0
 */
package svm;

// THERE ARE TODOs!!

class Smo2 {
	Kernel kernel;

	double[] error_cache;
	double[] alpha;
	double[] target;
	double b;
	double b_low;
	double b_up;
	int i_low;
	int i_up;
	int l;

	double C;
	double tau;
	double eps;

	int support = 0;
	int positiv = 0;
	int negativ = 0;
	int atbound = 0;
	int error = 0;

	public Smo2(Kernel k, double t[], double C_, double tau_, double eps_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];

		for (int i = 0; i < l; i++) {
			error_cache[i] = -target[i];
			alpha[i] = 0.0;
			if (target[i] == 1.0)
				i_up = i;
			else
				i_low = i;
		}

		b = 0;

		b_up = -1.0;
		b_low = 1.0;

		C = C_;
		tau = tau_;
		eps = eps_;
	}

	double alph1, alph2;
	double y1, y2;
	double E1, E2;
	double s;
	double L, H;
	double[] kernel_i1;
	double[] kernel_i2;
	double k11, k12, k22;
	double eta;
	double a1, a2;
	double v1, v2;
	double Lobj, Hobj;
	int k;
	double error_k;

	int takeStep(int i1, int i2) {
		// System.out.println(".");

		if (i1 == i2)
			return 0;

		// Lagrange multiplier for i1
		alph1 = alpha[i1];
		y1 = target[i1];

		alph2 = alpha[i2];
		y2 = target[i2];

		// SVM output on point[i1] - y1
		E1 = error_cache[i1];
		E2 = error_cache[i2];

		s = y1 * y2;

		// Compute L, H
		if (y1 != y2) {
			L = Math.max(0, alph2 - alph1);
			H = Math.min(C, C + alph2 - alph1);
		} else {
			L = Math.max(0, alph2 + alph1 - C);
			H = Math.min(C, alph2 + alph1);
		}

		if (L == H)
			return 0;

		kernel_i1 = kernel.getColumn(i1);
		kernel_i2 = kernel.getColumn(i2);

		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		eta = 2.0 * k12 - k11 - k22;

		if (eta < 0) {
			a2 = alph2 - y2 * (E1 - E2) / eta;
			if (a2 < L) {
				a2 = L;
			} else if (a2 > H) {
				a2 = H;
			}
		} else {
			Lobj = objectiveFunctionAt(L, i1, i2);
			Hobj = objectiveFunctionAt(H, i1, i2);
			if (Lobj > Hobj + eps) {
				a2 = L;
			} else if (Lobj < Hobj - eps) {
				a2 = H;
			} else {
				a2 = alph2;
			}
		}

		if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps)) {
			return 0;
		}

		a2 = (a2 < tau) ? 0 : a2;
		a2 = (a2 > C - tau) ? C : a2;

		a1 = alph1 + s * (alph2 - a2);

		a1 = (a1 < tau) ? 0 : a1;
		a1 = (a1 > C - tau) ? C : a1;

		alpha[i1] = a1;
		alpha[i2] = a2;

		b_low = Double.NEGATIVE_INFINITY;
		b_up = Double.POSITIVE_INFINITY;
		for (k = 0; k < l; k++) {
			// Update error_cache using new lagrange multipliers
			error_k = error_cache[k] + y1 * (a1 - alph1) * kernel_i1[k] + y2
					* (a2 - alph2) * kernel_i2[k];

			// Update b_up, b_low, i_up and i_low
			if (isI0(k) || isI3(k) || isI4(k)) {
				if (error_k > b_low) {
					b_low = error_k;
					i_low = k;
				}
			}
			if (isI0(k) || isI1(k) || isI2(k)) {
				if (error_k < b_up) {
					b_up = error_k;
					i_up = k;
				}
			}

			error_cache[k] = error_k;
		}

		return 1;
	}

	int examineExample(int i2) {
		int i1 = i2;

		y2 = target[i2];
		alph2 = alpha[i2];

		// SVM output on point[i2] - y2
		E2 = error_cache[i2];

		// Check optimality using current b_low and b_up
		// If violated find index i1 to do joint optimization with i2

		int optimality = 1;

		if (isI0(i2) || isI1(i2) || isI2(i2)) {
			if (b_low - E2 > 2.0 * eps) {
				optimality = 0;
				i1 = i_low;
			}
		}
		if (isI0(i2) || isI3(i2) || isI4(i2)) {
			if (E2 - b_up > 2.0 * eps) {
				optimality = 0;
				i1 = i_up;
			}
		}

		if (optimality == 1)
			return 0;

		// For i2 in I0 choose the better i1
		if (isI0(i2)) {
			if (b_low - E2 > E2 - b_up) {
				i1 = i_low;
			} else {
				i1 = i_up;
			}
		}

		if (takeStep(i1, i2) == 1)
			return 1;

		return 0;
	}

	void train() {
		int numChanged;
		int examineAll;
		int i;
		int iter = 0;

		numChanged = 0;
		examineAll = 1;

		while (numChanged > 0 || examineAll == 1) {
			numChanged = 0;
			if (examineAll == 1) {
				// Loop over all training examples
				IO.println("Iteration: " + (++iter) + "\tnumChanged: "
						+ numChanged, 4);
				IO.print("*", 2);
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
			} else {
				// Loop over examples where alpha is not 0 & not C
				IO.println("Iteration: " + (++iter) + "\tnumChanged: "
						+ numChanged, 4);
				IO.print(".", 2);
				for (i = 0; i < l; i++) {
					if (isI0(i)) {
						numChanged += examineExample(i);

						// It is easy optimality on I0 is attained
						if (b_up > b_low - 2.0 * eps) {
							numChanged = 0;
							break;
						}
					}
				}

				// This is the code for Modification 2 from Keerthi et al.'s
				// paper
				/*
				 * innerLoopSuccess = 1; numChanged = 0; while ((b_up < b_low -
				 * 2*eps) && (innerLoopSuccess > 0)) { IO.print(".",2);
				 * innerLoopSuccess = takeStep(i_up, i_low); }
				 */
			}

			if (examineAll == 1)
				examineAll = 0;
			else if (numChanged == 0)
				examineAll = 1;
		}

		b = (b_low + b_up) / 2.0;

		for (i = 0; i < l; i++) {
			if (alpha[i] > tau) {
				if (target[i] > 0)
					positiv++;
				else if (target[i] < 0)
					negativ++;
			}
			if (alpha[i] > C - tau) {
				atbound++;
			}
			error += (target[i] * (error_cache[i] - b) > -1 + tau) ? 0 : 1;
		}

		support = positiv + negativ;

		IO.print("\n", 2);
		IO.println("Number of support ArrayLists:    " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Positiv support ArrayLists:      " + positiv + "/" + l
				+ " (" + ((float) (100.0 * positiv) / l) + "%)", 2);
		IO.println("Negativ support ArrayLists:      " + negativ + "/" + l
				+ " (" + ((float) (100.0 * negativ) / l) + "%)", 2);
		IO.println("Support ArrayLists at bound:     " + atbound + "/" + l
				+ " (" + ((float) (100.0 * atbound) / l) + "%)", 2);
		IO.println("Missclassifications:          " + error + "/" + l + " ("
				+ ((float) (100.0 * error) / l) + "%)", 3);
		IO.println("Number of kernel evaluations: " + kernelEvaluations(), 3);
	}

	/*
	 * double learned(Data x) { double sum = -b; for(int i = 0; i < l; i++)
	 * if(alpha[i] > tau) sum += target[i]*alpha[i]*kernel(point[i],x); return
	 * sum; }
	 */

	int kernelEvaluations() {
		return kernel.evaluations;
	}

	double objectiveFunctionAt(double L, int i1, int i2) {
		double g, h;

		v1 = 0.0;
		v2 = 0.0;

		// TO DO:
		// - Check if it it possible to reduce the runtime if one
		// uses v1 and v2 as global variables, because they have
		// the same value for the evaluation of Hobj and Lobj.
		// - Check it is possible to eliminate the following cicle
		// by using the values stored in error_cache[].
		for (k = 0; k < l; k++) {
			if (k != i1 && k != i2 && alpha[k] > tau) {
				v1 += target[k] * alpha[k] * kernel_i1[k];
				v2 += target[k] * alpha[k] * kernel_i2[k];
			}
		}

		// TO DO:
		// - Chek if it is logic to use both variables g and h.
		g = alph1 + s * alph2;
		h = (g - s * L);

		return (g - s * L + L - 0.5 * k11 * h * h - 0.5 * k22 * L * L - s * k12
				* h * L - y1 * h * v1 - y2 * L * v2);
	}

	boolean isI0(int i) {
		return ((alpha[i] > tau) && (alpha[i] < C - tau));
	}

	boolean isI1(int i) {
		return ((target[i] == 1.0) && (alpha[i] < tau));
	}

	boolean isI2(int i) {
		return ((target[i] == -1.0) && (alpha[i] > C - tau));
	}

	boolean isI3(int i) {
		return ((target[i] == 1.0) && (alpha[i] > C - tau));
	}

	boolean isI4(int i) {
		return ((target[i] == -1.0) && (alpha[i] < tau));
	}
}

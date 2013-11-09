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

public class Smo {
	Kernel kernel;

	double[] error_cache;
	double C;
	double tau;
	double eps;

	double[] alpha;
	double[] target;
	double b;
	int l;

	int support;
	int positiv;
	int negativ;
	int error;

	/*
	 * public Smo(Parameters par, Problem pro, int size, double C_, double tau_,
	 * double eps_) { kernel = new Kernel(par,pro,size); l = pro.l; target =
	 * pro.label;
	 * 
	 * error_cache = new double[l]; alpha = new double[l];
	 * 
	 * for(int i = 0; i < l; i++) { error_cache[i] = -target[i]; alpha[i] = 0.0;
	 * }
	 * 
	 * b = 0;
	 * 
	 * C = C_; tau = tau_; eps = eps_; }
	 */

	public Smo(Kernel k, double t[], double C_, double tau_, double eps_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];

		for (int i = 0; i < l; i++) {
			error_cache[i] = -target[i];
			alpha[i] = 0.0;
		}

		b = 0;

		C = C_;
		tau = tau_;
		eps = eps_;
	}

	int takeStep(int i1, int i2) {
		double alph1, alph2;
		double y1, y2;
		double E1, E2;
		double s;
		double L, H;
		double k11, k12, k22;
		double eta;
		double a1, a2;
		double Lobj, Hobj;
		int k;

		// System.out.println(".");

		if (i1 == i2)
			return 0;

		/* Lagrange multiplier for i1 */
		alph1 = alpha[i1];
		y1 = target[i1];

		alph2 = alpha[i2];
		y2 = target[i2];

		/* SVM output on point[i1] - y1 */
		E1 = error_cache[i1];
		E2 = error_cache[i2];

		s = y1 * y2;

		/* Compute L, H */
		if (y1 != y2) {
			L = Math.max(0, alph2 - alph1);
			H = Math.min(C, C + alph2 - alph1);
		} else {
			L = Math.max(0, alph2 + alph1 - C);
			H = Math.min(C, alph2 + alph1);
		}

		if (L == H)
			return 0;

		double[] kernel_i1 = kernel.getColumn(i1);
		double[] kernel_i2 = kernel.getColumn(i2);
		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		/*
		 * k11 = kernel.value(i1,i1); k12 = kernel.value(i1,i2); k22 =
		 * kernel.value(i2,i2);
		 */

		eta = 2.0 * k12 - k11 - k22;

		if (eta < 0) {
			a2 = alph2 - y2 * (E1 - E2) / eta;
			if (a2 < L)
				a2 = L;
			else if (a2 > H)
				a2 = H;
		} else {
			/* Objetive function at a2 = l, H */
			double v1 = 0.0, v2 = 0.0, g = alph1 + s * alph2;

			for (k = 0; k < l; k++)
				if (k != i1 && k != i2 && alpha[k] > tau) {
					/*
					 * v1 += target[k]*alpha[k]*kernel.value(i1,k); v2 +=
					 * target[k]*alpha[k]*kernel.value(i2,k);
					 */
					v1 += target[k] * alpha[k] * kernel_i1[k];
					v2 += target[k] * alpha[k] * kernel_i2[k];
				}

			Lobj = g - s * L + L - 0.5 * k11 * (g - s * L) * (g - s * L) - 0.5
					* k22 * L * L - s * k12 * (g - s * L) * L - y1
					* (g - s * L) * v1 - y2 * L * v2;
			Hobj = g - s * H + H - 0.5 * k11 * (g - s * H) * (g - s * H) - 0.5
					* k22 * H * H - s * k12 * (g - s * H) * H - y1
					* (g - s * H) * v1 - y2 * H * v2;
			if (Lobj > Hobj + eps)
				a2 = L;
			else if (Lobj < Hobj - eps)
				a2 = H;
			else
				a2 = alph2;
		}

		if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps))
			return 0;

		a2 = (a2 < tau) ? 0 : a2;
		a2 = (a2 > C - tau) ? C : a2;

		a1 = alph1 + s * (alph2 - a2);

		double b1, b2, bold;

		bold = b;

		b1 = E1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12 + b;
		if (a1 > tau && a1 < C - tau)
			b = b1;
		else {
			b2 = E2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22 + b;
			if (a2 > tau && a2 < C - tau)
				b = b2;
			else
				b = 0.5 * (b1 + b2);
		}

		/* Update error cache using new Lagrange multipliers */

		for (k = 0; k < l; k++)
			/*
			 * error_cache[k] += y1*(a1 - alph1)*kernel.value(i1,k) +y2*(a2 -
			 * alph2)*kernel.value(i2,k) + bold - b;
			 */
			error_cache[k] += y1 * (a1 - alph1) * kernel_i1[k] + y2
					* (a2 - alph2) * kernel_i2[k] + bold - b;
		/* Store a1, a2 in alph array */
		alpha[i1] = a1;
		alpha[i2] = a2;

		return 1;
	}

	int examineExample(int i2) {
		double alph2, y2;
		double E2, E1, tmp, tmax;
		double r2;

		y2 = target[i2];
		alph2 = alpha[i2];

		/* SVM output on point[i2] - y2 */
		E2 = error_cache[i2];
		r2 = E2 * y2;

		if ((r2 < -eps && alph2 < C - tau) || (r2 > eps && alph2 > tau)) {
			/* Number of non-zero & non-C alpha > 1 */
			int k, i1, irand;

			i1 = -1;
			tmax = 0;
			for (k = 0; k < l; k++)
				if (alpha[k] > tau && alpha[k] < C - tau) {
					E1 = error_cache[k];
					tmp = Math.abs(E1 - E2);
					if (tmax < tmp) {
						tmax = tmp;
						i1 = k;
					}
				}

			if (i1 > -1) {
				/* Result of second choice heuristic */
				if (takeStep(i1, i2) == 1)
					return 1;
			}

			/* Loop over all non-zero non-C alpha, starting at random */
			irand = (int) (l * Math.random());

			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				if (alpha[i1] > tau && alpha[i1] < C - tau) {
					if (takeStep(i1, i2) == 1)
						return 1;
				}
			}

			/* Loop over all posibles i1, starting at random */
			irand = (int) (l * Math.random());

			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				if (takeStep(i1, i2) == 1)
					return 1;
			}
		}

		return 0;
	}

	void train() {
		int numChanged;
		int examineAll;
		int i;
		numChanged = 0;
		examineAll = 1;

		while (numChanged > 0 || examineAll == 1) {
			numChanged = 0;
			if (examineAll == 1) {
				/* Loop over all training examples */
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
				// System.out.println("Iteration: "+(++iter)+"\tnumChanged: "+numChanged);
				System.out.print("*");
			} else {
				/* Loop over examples where alpha is not 0 & not C */
				for (i = 0; i < l; i++)
					if (alpha[i] > tau && alpha[i] < C - tau) {
						numChanged += examineExample(i);
					}
				// System.out.println("Iteration: "+(++iter)+"\tnumChanged: "+numChanged);
				System.out.print(".");
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
				positiv += (target[i] == 1) ? 1 : 0;
				negativ += (target[i] == -1) ? 1 : 0;
			}
			error += (target[i] * (error_cache[i] + target[i]) > tau) ? 0 : 1;
		}

		System.out.println();
		System.out.println("Support ArrayLists:\t\t" + support + "/" + l + " ("
				+ ((float) (100.0 * support) / l) + "%)");
		// System.out.println("Positiv Support ArrayLists:\t"+positiv+"/"+l+" ("+((float)(100.0*positiv)/l)+"%)");
		// System.out.println("Negativ Support ArrayLists:\t"+negativ+"/"+l+" ("+((float)(100.0*negativ)/l)+"%)");
		System.out.println("Missclassifications:\t\t" + error + "/" + l + " ("
				+ ((float) (100.0 * error) / l) + "%)\n");
		System.out.println("Kernel Evaliations:\t\t" + kernelEvaluations()
				+ "\n");
	}

	/*
	 * double learned(Data x) { double sum = -b; for(int i = 0; i < l; i++)
	 * if(alpha[i] > tau) sum += target[i]*alpha[i]*kernel(point[i],x); return
	 * sum; }
	 */

	int kernelEvaluations() {
		return kernel.evaluations;
	}
}

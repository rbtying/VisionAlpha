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
import java.util.Random;

class RSmo1 {
	Kernel kernel;

	double[] error_cache;
	double[] alpha;
	double[] target;
	double b;
	int l;

	double C;
	double tau;
	double eps;

	int support = 0;
	int positiv = 0;
	int negativ = 0;
	int atbound = 0;
	int error = 0;

	public RSmo1(Kernel k, double t[], double C_, double tau_, double eps_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];

		for (int i = 0; i < l; i++) {
			error_cache[i] = -target[i];
			alpha[i] = 0;
		}

		b = 0.0;

		C = C_;
		tau = tau_;
		eps = eps_;
	}

	double y1, y2;
	double alpha1, alpha2;
	double alpha1old, alpha2old;
	double b1, b2, b_old;
	double U, V;
	double[] kernel_i1;
	double[] kernel_i2;
	double k11, k12, k22;
	double E1, E2, delta_E;
	double kappa, gamma;
	double a1, a2;
	double Uobj, Vobj;
	int case1, case2, case3, case4, finished;
	int current_case;
	boolean alphaChanged;
	int k;
	double v1, v2;
	double obj_opt, obj_at_a2, a2_opt, a1_opt;

	int takeStep(int i1, int i2) {
		if (i1 == i2)
			return 0;

		y1 = target[i1];
		y2 = target[i2];

		/* Lagrange multiplier for i1 */
		alpha1 = alpha[i1];
		alpha2 = alpha[i2];

		/* SVM output on point[i1] - y1 */
		E1 = error_cache[i1];
		E2 = error_cache[i2];

		kernel_i1 = kernel.getColumn(i1);
		kernel_i2 = kernel.getColumn(i2);

		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		kappa = k11 + k22 - 2.0 * k12;
		gamma = alpha1 + alpha2;

		if (kappa > 0) {
			case1 = case2 = case3 = case4 = finished = 0;

			alpha1old = alpha1;
			alpha2old = alpha2;

			delta_E = E1 - E2;

			alphaChanged = false;
			obj_opt = Double.NEGATIVE_INFINITY;
			a1_opt = alpha1old;
			a2_opt = alpha2old;

			while (finished == 0) {
				// this loop is passes at most trhee times
				// case varibles needed to avoid attempting small changes twice
				if (case1 == 0 && alpha1 >= 0 && alpha2 >= 0) {
					// compute U, V (alpha1,alpha2)
					U = Math.max(0, gamma - C);
					V = Math.min(C, gamma);

					if (U < V) {
						a2 = alpha2 + delta_E / kappa;
						a2 = Math.min(a2, V);
						a2 = Math.max(U, a2);
					} else if (U == V) {
						// finished = 1;
					}

					case1 = 1;
					current_case = 1;
				} else if (case2 == 0 && alpha1 >= 0 && alpha2 <= 0) {
					// compute U, V (alpha1,alpha2)
					U = Math.max(-C, gamma - C);
					V = Math.min(0, gamma);

					if (U < V) {
						a2 = alpha2
								+ (delta_E - eps * (sng(alpha2) - sng(alpha1)))
								/ kappa;
						a2 = Math.min(a2, V);
						a2 = Math.max(U, a2);
					} else if (U == V) {
						// finished = 1;
					}

					case2 = 1;
					current_case = 2;
				} else if (case3 == 0 && alpha1 <= 0 && alpha2 >= 0) {
					// compute U, V (alpha1,alpha2)
					U = Math.max(0, gamma);
					V = Math.min(C, gamma + C);

					if (U < V) {
						a2 = alpha2
								+ (delta_E - eps * (sng(alpha2) - sng(alpha1)))
								/ kappa;
						a2 = Math.min(a2, V);
						a2 = Math.max(U, a2);
					} else if (U == V) {
						// finished = 1;
					}

					case3 = 1;
					current_case = 3;
				} else if (case4 == 0 && alpha1 <= 0 && alpha2 <= 0) {
					// compute U, V (alpha1,alpha2)
					U = Math.max(-C, gamma);
					V = Math.min(0, gamma + C);

					if (U < V) {
						a2 = alpha2 + delta_E / kappa;
						a2 = Math.min(a2, V);
						a2 = Math.max(U, a2);
					} else if (U == V) {
						// finished = 1;
					}

					case4 = 1;
					current_case = 4;
				} else {
					finished = 1;
				}

				// update alpha1, alpha2 and delta_E if change is larger thas
				// some threshold
				IO.print("\ncurrent_case: " + current_case + "\n", 4);
				IO.print("finished: " + finished + "\n", 4);
				IO.print("Update1: " + a2 + " " + alpha2old + " " + kappa + " "
						+ delta_E + "\n", 4);

				if ((obj_at_a2 = this.objectiveFunctionAt(a2)) > obj_opt) {
					obj_opt = obj_at_a2;
					IO.print("Update1\n", 4);
					a2_opt = (a2 < tau - C) ? -C : a2;
					a2_opt = (a2 > C - tau) ? C : a2;

					// delta_E = delta_E + (alpha2 - a2)*(k22 - k11);

					a1_opt = alpha1 + alpha2 - a2;
					alphaChanged = true;
				}
				/*
				 * if(this.objectiveFunctionAt(a2) > obj_opt) {
				 * //if(Math.abs(a2-alpha2) > tau*(a2 + alpha2 + tau)) {
				 * IO.print("Update1\n",4); a2 = (a2 < tau - C)? -C : a2; a2 =
				 * (a2 > C - tau)? C : a2;
				 * 
				 * delta_E = delta_E + (alpha2 - a2)*(k22 - k11);
				 * 
				 * alpha1 = alpha1 + alpha2 - a2;
				 * 
				 * alpha1 = (alpha1 < tau - C)? -C : alpha1; alpha1 = (alpha1 >
				 * C - tau)? C : alpha1;
				 * 
				 * alpha2 = a2; alphaChanged = true;
				 * 
				 * }
				 */
			} // END while
			IO.print("END while\n", 4);
		} // END kappa > 0

		/*
		 * if(Math.abs(alpha2-alpha2old) < tau*(alpha2 + alpha2old + tau)) {
		 * //if(!alphaChanged) { return 0; }
		 */

		/*
		 * if(!alphaChanged) { return 0; } else {
		 */
		alpha1 = a1_opt;
		alpha2 = a2_opt;
		// }

		if (Math.abs(alpha2 - alpha2old) < tau * (alpha2 + alpha2old + tau)) {
			return 0;
		}

		IO.print("Changed", 4);

		// update lagrange multipliers
		alpha[i1] = alpha1;
		alpha[i2] = alpha2;

		// update threshold
		b_old = b;
		b1 = E1 + (alpha1 - alpha1old) * k11 + (alpha2 - alpha2old) * k12
				+ b_old + eps;
		if ((alpha1 > tau && alpha1 < C - tau)) {
			b = b1;
		} else {
			b2 = E2 + (alpha1 - alpha1old) * k12 + (alpha2 - alpha2old) * k22
					+ b_old + eps;
			if ((alpha2 > tau && alpha2 < C - tau)) {
				b = b2;
			} else {
				b = 0.5 * (b1 + b2);
			}
		}

		// update error cache
		for (k = 0; k < l; k++) {
			error_cache[k] += (alpha1 - alpha1old) * kernel_i1[k]
					+ (alpha2 - alpha2old) * kernel_i2[k] + b_old - b;
		}

		return 1;
	}

	int examineExample(int i2) {
		int i1;

		alpha2 = alpha[i2];

		/* SVM output on point[i2] - y2 */
		E2 = error_cache[i2];

		if ((alpha2 < C && E2 < eps) || (alpha2 > -C && E2 > -eps)) {
			IO.print("Violating\n", 4);
			int k, irand, sng1, sng2;
			double max, tmp;

			i1 = -1;
			max = 0;
			sng2 = sng(alpha2);
			for (k = 0; k < l; k++) {
				// alpha1
				sng1 = sng(alpha[k]);
				if (alpha[k] * sng1 > 0 && alpha[k] * sng1 < C) {
					E1 = error_cache[k];
					// alpha2
					tmp = Math.abs(E1 - E2 - eps * (sng2 - sng1));
					if (max < tmp) {
						max = tmp;
						i1 = k;
					}
				}
			}

			if (i1 != -1) {
				if (takeStep(i1, i2) == 1) {
					IO.print("Euristic 1\n", 4);
					return 1;
				}
			}

			// Loop over all non-zero non-C alpha, starting at random
			irand = (int) (l * Math.random());
			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				sng1 = sng(alpha[i1]);
				if (alpha[i1] * sng1 > 0 && alpha[i1] * sng1 < C) {
					IO.print("Euristic 2\n", 4);
					if (takeStep(i1, i2) == 1) {
						return 1;
					}
				}
			}

			// Loop over all posibles i1, starting at random
			irand = (int) (l * Math.random());
			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				// IO.print("Euristic 3 "+alpha[i1]+" "+i1+"\n",2);
				if (takeStep(i1, i2) == 1) {
					return 1;
				}
			}
		}

		return 0;
	}

	void train() {
		int numChanged;
		int examineAll;
		int LoopCounter;
		int i;
		int sng;

		numChanged = 0;
		examineAll = 1;
		LoopCounter = 0;

		while ((numChanged > 0 || examineAll == 1) && LoopCounter < 500) {
			LoopCounter++;
			numChanged = 0;
			support = 0;
			atbound = 0;
			if (examineAll == 1) {
				// Loop over all training examples
				// IO.println("Iteration: "+(++LoopCounter)+"\tnumChanged: "+numChanged,4);
				IO.print("*", 2);
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
			} else {
				// Loop over examples where alpha is not 0 & not C
				// IO.println("Iteration: "+(++LoopCounter)+"\tnumChanged: "+numChanged,4);
				IO.print(".", 2);
				for (i = 0; i < l; i++) {
					sng = sng(alpha[i]);
					if (alpha[i] * sng > 0 && alpha[i] * sng < C) {
						// if(Math.abs(alpha[i]) > 0 && Math.abs(alpha[i]) < C)
						// {
						support++;
						numChanged += examineExample(i);
					} else if (Math.abs(alpha[i]) == C) {
						atbound++;
					}
				}
			}

			if (examineAll == 1) {
				examineAll = 0;
			}
			// else if(numChanged <= 2) {
			else if (numChanged == 0) {
				examineAll = 1;
			}
			System.out.println("LoopCounter: " + LoopCounter + " numChanged:"
					+ numChanged + " examineAll:" + examineAll);
			System.out.println("support: " + support);
			System.out.println("atbount: " + atbound);

		}

		double error = 0;
		support = 0;
		atbound = 0;
		for (i = 0; i < l; i++) {
			if (alpha[i] != 0) {
				support++;
			}
			if (alpha[i] > C - tau || alpha[i] < C + tau) {
				atbound++;
			}
			error += error_cache[i] * error_cache[i];
		}

		error = Math.sqrt(error) / l;

		IO.print("\n", 2);
		IO.println("Number of support ArrayLists:    " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Support ArrayLists at bound:     " + atbound + "/" + l
				+ " (" + ((float) (100.0 * negativ) / l) + "%)", 2);
		IO.println("Cuadratic Error:              " + ((float) error), 3);
		IO.println("Number of kernel evaluations: " + kernel.getEvaluations(),
				3);
	}

	double objectiveFunctionAt(double a2) {
		v1 = E1 + y1 - alpha1 * k11 - alpha2 * k12 + b;
		v2 = E2 + y2 - alpha1 * k12 - alpha2 * k22 + b;

		return (y1 * gamma - y1 * a2 + y2 * a2 - eps
				* (Math.abs(alpha1) + Math.abs(a2)) - 0.5 * k11 * (gamma - a2)
				* (gamma - a2) - 0.5 * k22 * a2 * a2 - k12 * (gamma - a2) * a2
				- (gamma - a2) * v1 - a2 * v2);
	}

	int sng(double x) {
		return ((x < 0) ? -1 : ((x == 0) ? 0 : 1));
	}

	public static void main(String[] argv) throws Exception {
		DataOutputStream fileout;
		Random r;

		fileout = new DataOutputStream(new FileOutputStream(
				"c:/svm/classes/data/regression1.dat"));
		r = new Random();

		fileout.writeBytes("SparseVector 2000\n");
		for (float x, y, count = 1; count <= 2000; count++) {
			x = 10 * (2 * r.nextFloat() - 1);
			y = 0.02f * x * x * x + 0.05f * x * x - x
					+ (float) r.nextGaussian();
			// y = 0.02f*x*x*x + 0.05f*x*x - x;
			// y = (float)Math.sin(x);
			fileout.writeBytes(y + " 0:" + x + "\n");
		}

		fileout.close();
	}
}

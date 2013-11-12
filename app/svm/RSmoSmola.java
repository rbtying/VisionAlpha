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

class RSmoSmola {
	Kernel kernel;

	double[] error_cache;
	double[] alpha;
	double[] alpha_s;
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

	public RSmoSmola(Kernel k, double t[], double C_, double tau_, double eps_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];
		alpha_s = new double[l];

		for (int i = 0; i < l; i++) {
			error_cache[i] = target[i];
			alpha[i] = 0.0;
			alpha_s[i] = 0.0;
		}

		b = 0.0;

		C = C_;
		tau = tau_;
		eps = eps_;
	}

	// double y2;
	double alpha1, alpha2, alpha1_s, alpha2_s;
	double alpha1old, alpha2old, alpha1old_s, alpha2old_s;
	double b1, b2, b_old;
	double L, H;
	double[] kernel_i1;
	double[] kernel_i2;
	double k11, k12, k22;
	double phi1, phi2, delta_phi;
	double eta, gamma;
	double a1, a2;
	double Lobj, Hobj;
	int case1, case2, case3, case4, finished;
	int current_case;

	int takeStep(int i1, int i2) {
		boolean alphaChanged = false;
		int k;

		if (i1 == i2)
			return 0;

		/* Lagrange multiplier for i1 */
		alpha1 = alpha[i1];
		alpha1_s = alpha_s[i1];

		alpha2 = alpha[i2];
		alpha2_s = alpha_s[i2];

		/* SVM output on point[i1] - y1 */
		phi1 = error_cache[i1];
		phi2 = error_cache[i2];

		kernel_i1 = kernel.getColumn(i1);
		kernel_i2 = kernel.getColumn(i2);
		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		/*
		 * k11 = kernel.value(i1,i1); k12 = kernel.value(i1,i2); k22 =
		 * kernel.value(i2,i2);
		 */

		eta = -2.0 * k12 + k11 + k22;
		gamma = alpha1 - alpha1_s + alpha2 - alpha2_s;
		if (eta > 0) {
			case1 = case2 = case3 = case4 = finished = 0;

			alpha1old = alpha1;
			alpha1old_s = alpha1_s;
			alpha2old = alpha2;
			alpha2old_s = alpha2_s;

			delta_phi = phi1 - phi2;

			while (finished == 0) {
				// this loop is passes at most trhee times
				// case varibles needed to avoid attempting small changes twice
				if (case1 == 0
						&& (alpha1 > 0 || (alpha1_s == 0 && delta_phi > 0))
						&& (alpha2 > 0 || (alpha2_s == 0 && delta_phi < 0))) {
					// compute L, H (alpha1,alpha2)
					L = Math.max(0, gamma - C);
					H = Math.min(C, gamma);

					if (L < H) {
						a2 = alpha2 - delta_phi / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1 - (a2 - alpha2);

						// update alpha1, alpha2 if change is larger thas some
						// threshold
						if (Math.abs(a1 - alpha1) > tau * (a1 + alpha1 + tau)) {
							alpha1 = a1;
							alphaChanged = true;
						}
						if (Math.abs(a2 - alpha2) > tau * (a2 + alpha2 + tau)) {
							alpha2 = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case1 = 1;
				} else if (case2 == 0
						&& (alpha1 > 0 || (alpha1_s == 0 && delta_phi > 2 * eps))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 2 * eps))) {
					// compute L, H (alpha1,alpha2_s)
					L = Math.max(0, gamma);
					H = Math.min(C, C + gamma);

					if (L < H) {
						a2 = alpha2_s - (delta_phi - 2 * eps) / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1 - (a2 - alpha2_s);

						// update alpha1, alpha2_p if change is larger thas some
						// threshold
						if (Math.abs(a1 - alpha1) > tau * (a1 + alpha1 + tau)) {
							alpha1 = a1;
							alphaChanged = true;
						}
						if (Math.abs(a2 - alpha2_s) > tau
								* (a2 + alpha2_s + tau)) {
							alpha2_s = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case2 = 1;
				} else if (case3 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < 2 * eps))
						&& (alpha2 > 0 || (alpha2_s == 0 && delta_phi < 2 * eps))) {
					// compute L, H (alpha1_s,alpha2)
					L = Math.max(0, -gamma);
					H = Math.min(C, C - gamma);

					if (L < H) {
						a2 = alpha2 - (delta_phi - 2 * eps) / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1_s - (a2 - alpha2);

						// update alpha1_s, alpha2 if change is larger thas some
						// threshold
						if (Math.abs(a1 - alpha1_s) > tau
								* (a1 + alpha1_s + tau)) {
							alpha1 = a1;
							alphaChanged = true;
						}
						if (Math.abs(a2 - alpha2) > tau * (a2 + alpha2 + tau)) {
							alpha2_s = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case3 = 1;
				} else if (case4 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < 0))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 0))) {
					// compute L, H (alpha1_s,alpha2_s)
					L = Math.max(0, -C - gamma);
					H = Math.min(C, -gamma);

					if (L < H) {
						a2 = alpha2_s - delta_phi / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1_s - (a2 - alpha2_s);

						// update alpha1_s, alpha2_s if change is larger thas
						// some threshold
						if (Math.abs(a1 - alpha1_s) > tau
								* (a1 + alpha1_s + tau)) {
							alpha1 = a1;
							alphaChanged = true;
						}
						if (Math.abs(a2 - alpha2_s) > tau
								* (a2 + alpha2_s + tau)) {
							alpha2_s = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case4 = 1;
				} else {
					finished = 1;
				}
				// update delta_phi

				if (alpha1 < -tau || alpha1_s < -tau) {
					System.out.println("case: " + current_case + " alpha1: "
							+ alpha1 + " " + alpha1_s + " alpha2: " + alpha2
							+ " " + alpha2_s);
					System.out.println("L: " + L + " H: " + H);
					// System.exit(1);
				}
				if (alpha2 < -tau || alpha2_s < -tau) {
					System.out.println("case: " + current_case + " alpha1: "
							+ alpha1 + " " + alpha1_s + " alpha2: " + alpha2
							+ " " + alpha2_s);
					System.out.println("L: " + L + " H: " + H);
					// System.exit(1);
				}
				if (alpha1 > C + tau || alpha1_s > C + tau) {
					System.out.println("case: " + current_case + " alpha1: "
							+ alpha1 + " " + alpha1_s + " alpha2: " + alpha2
							+ " " + alpha2_s);
					System.out.println("L: " + L + " H: " + H);
					// System.exit(1);
				}
				if (alpha2 > C + tau || alpha2_s > C + tau) {
					System.out.println("case: " + current_case + " alpha1: "
							+ alpha1 + " " + alpha1_s + " alpha2: " + alpha2
							+ " " + alpha2_s);
					System.out.println("L: " + L + " H: " + H);
					// System.exit(1);
				}

				delta_phi = delta_phi - eta
						* ((alpha1 - alpha1_s) - (alpha1old - alpha1old_s));
			} // END while
		} // END eta > 0
		else {
			IO.print("#:" + eta + ":2*k12:" + (2 * k12) + ":k11:" + k11
					+ ":k22:" + k22 + "\n", 2);

			case1 = case2 = case3 = case4 = finished = 0;

			alpha1old = alpha1;
			alpha1old_s = alpha1_s;
			alpha2old = alpha2;
			alpha2old_s = alpha2_s;

			delta_phi = phi1 - phi2;

			while (finished == 0) {
				// this loop is passes at most trhee times
				// case varibles needed to avoid attempting small changes twice
				if (case1 == 0
						&& (alpha1 > 0 || (alpha1_s == 0 && delta_phi > 0))
						&& (alpha2 > 0 || (alpha2_s == 0 && delta_phi < 0))) {
					// compute L, H (alpha1,alpha2)
					L = Math.max(0, gamma - C);
					H = Math.min(C, gamma);

					if (L < H) {
						Lobj = this.objectiveFunction(alpha1, alpha1_s, L,
								alpha2_s, i1, i2);
						Hobj = this.objectiveFunction(alpha1, alpha1_s, H,
								alpha2_s, i1, i2);

						if (-tau < Lobj - Hobj) {
							a2 = L;
							alphaChanged = true;
						} else if (-tau < Hobj - Lobj) {
							a2 = H;
							alphaChanged = true;
						} else {
							a2 = alpha2old;
						}

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1 - (a2 - alpha2);
					} else {
						finished = 1;
					}

					case1 = 1;
				} else if (case2 == 0
						&& (alpha1 > 0 || (alpha1_s == 0 && delta_phi > 2 * eps))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 2 * eps))) {
					// compute L, H (alpha1,alpha2_s)
					L = Math.max(0, gamma);
					H = Math.min(C, C + gamma);

					if (L < H) {
						Lobj = this.objectiveFunction(alpha1, alpha1_s, alpha2,
								L, i1, i2);
						Hobj = this.objectiveFunction(alpha1, alpha1_s, alpha2,
								H, i1, i2);

						if (-tau < Lobj - Hobj) {
							a2 = L;
							alphaChanged = true;
						} else if (-tau < Hobj - Lobj) {
							a2 = H;
							alphaChanged = true;
						} else {
							a2 = alpha2old_s;
						}

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1 - (a2 - alpha2_s);
					} else {
						finished = 1;
					}

					case2 = 1;
				} else if (case3 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < 2 * eps))
						&& (alpha2 > 0 || (alpha2_s == 0 && delta_phi < 2 * eps))) {
					// compute L, H (alpha1_s,alpha2)
					L = Math.max(0, -gamma);
					H = Math.min(C, C - gamma);

					if (L < H) {
						Lobj = this.objectiveFunction(alpha1, alpha1_s, L,
								alpha2_s, i1, i2);
						Hobj = this.objectiveFunction(alpha1, alpha1_s, H,
								alpha2_s, i1, i2);

						if (-tau < Lobj - Hobj) {
							a2 = L;
							alphaChanged = true;
						} else if (-tau < Hobj - Lobj) {
							a2 = H;
							alphaChanged = true;
						} else {
							a2 = alpha2old;
						}

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1_s - (a2 - alpha2);
					} else {
						finished = 1;
					}

					case3 = 1;
				} else if (case4 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < 0))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 0))) {
					// compute L, H (alpha1_s,alpha2_s)
					L = Math.max(0, -C - gamma);
					H = Math.min(C, -gamma);

					if (L < H) {
						Lobj = this.objectiveFunction(alpha1, alpha1_s, alpha2,
								L, i1, i2);
						Hobj = this.objectiveFunction(alpha1, alpha1_s, alpha2,
								H, i1, i2);

						if (-tau < Lobj - Hobj) {
							a2 = L;
							alphaChanged = true;
						} else if (-tau < Hobj - Lobj) {
							a2 = H;
							alphaChanged = true;
						} else {
							a2 = alpha2old_s;
						}

						a2 = (a2 < tau) ? 0 : a2;
						a2 = (a2 > C - tau) ? C : a2;

						a1 = alpha1_s - (a2 - alpha2_s);
					} else {
						finished = 1;
					}

					case4 = 1;
				} else {
					finished = 1;
				}
				// update delta_phi
				delta_phi = delta_phi - eta
						* ((alpha1 - alpha1_s) - (alpha1old - alpha1old_s));
			} // END while
		}

		if (!alphaChanged) {
			return 0;
		}

		// update lagrange multipliers
		alpha[i1] = alpha1;
		alpha_s[i1] = alpha1_s;

		alpha[i2] = alpha2;
		alpha_s[i2] = alpha2_s;

		// update threshold
		b_old = b;
		b1 = phi1 - b_old - ((alpha1 - alpha1_s) - (alpha1old - alpha1old_s))
				* k11 - ((alpha2 - alpha2_s) - (alpha2old - alpha2old_s)) * k12;
		if ((alpha1 > tau && alpha1 < C - tau)) {
			b = b1 - eps;
		} else if ((alpha1_s > tau && alpha1_s < C - tau)) {
			b = b1 + eps;
		} else {
			b2 = phi2 - b_old
					- ((alpha1 - alpha1_s) - (alpha1old - alpha1old_s)) * k12
					- ((alpha2 - alpha2_s) - (alpha2old - alpha2old_s)) * k22;
			if ((alpha2 > tau && alpha2 < C - tau)) {
				b = b2 - eps;
			} else if ((alpha2_s > tau && alpha2_s < C - tau)) {
				b = b2 + eps;
			} else {
				b = 0.5 * (b1 + b2);
			}
		}

		// update error cache
		for (k = 0; k < l; k++) {
			error_cache[k] += ((alpha1old - alpha1old_s) - (alpha1 - alpha1_s))
					* kernel_i1[k]
					+ ((alpha2old - alpha2old_s) - (alpha2 - alpha2_s))
					* kernel_i2[k] + b - b_old;
		}

		return 1;
	}

	double objectiveFunction(double a1, double a1_s, double a2, double a2_s,
			int i1, int i2) {
		double obj, v1, v2, d1, d2;
		int i;

		v1 = this.target[i1] + b;
		v2 = this.target[i2] + b;
		for (i = 0; i < l; i++) {
			if (i != i1 && i != i2) {
				v1 -= (alpha[i] - alpha_s[i]) * kernel_i1[i];
				v2 -= (alpha[i] - alpha_s[i]) * kernel_i2[i];
			}
		}

		d1 = a1 - a1_s;
		d2 = a2 - a2_s;

		obj = -0.5 * (d1 * (d1 * k11 + d2 * k12) + d2 * (d1 * k12 + d2 * k22));
		obj += v1 * d1 + v2 * d2;
		obj -= eps * (a1 + a1_s + a2 + a2_s);

		return obj;
	}

	int examineExample(int i2) {
		int i1;

		// y2 = target[i2];
		alpha2 = alpha[i2];
		alpha2_s = alpha_s[i2];

		/* SVM output on point[i2] - y2 */
		phi2 = error_cache[i2];

		if ((phi2 > eps && alpha2_s < C) || (phi2 < eps && alpha2_s > 0)
				|| (-phi2 > eps && alpha2 < C) || (-phi2 > eps && alpha2 > 0)) {
			int k, irand;
			double max, tmp;

			i1 = -1;
			max = 0;
			for (k = 0; k < l; k++) {
				// alpha1
				if (alpha[k] > tau && alpha[k] < C - tau) {
					phi1 = error_cache[k];
					// alpha2
					tmp = Math.abs(phi1 - phi2);
					if (max < tmp) {
						max = tmp;
						i1 = k;
					}
					// alpha2_s
					tmp = Math.abs(phi1 - phi2 - 2.0 * eps);
					if (max < tmp) {
						max = tmp;
						i1 = k;
					}
				}

				// alpha1_s
				if (alpha_s[k] > tau && alpha_s[k] < C - tau) {
					phi1 = error_cache[k];
					// alpha2
					tmp = Math.abs(phi1 - phi2 + 2 * eps);
					if (max < tmp) {
						max = tmp;
						i1 = k;
					}
					// alpha2_s
					tmp = Math.abs(phi1 - phi2);
					if (max < tmp) {
						max = tmp;
						i1 = k;
					}
				}
			}

			if (i1 != -1) {
				if (takeStep(i1, i2) == 1) {
					return 1;
				}
			}

			// Loop over all non-zero non-C alpha, starting at random
			irand = (int) (l * Math.random());
			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				if ((alpha[i1] > tau && alpha[i1] < C - tau)
						|| (alpha_s[i1] > tau && alpha_s[i1] < C - tau)) {
					if (takeStep(i1, i2) == 1) {
						return 1;
					}
				}
			}

			// Loop over all posibles i1, starting at random
			irand = (int) (l * Math.random());
			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
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
		int MinimumNumChanged;
		int i;
		numChanged = 0;
		examineAll = 1;
		LoopCounter = 0;

		while ((numChanged > 0 || examineAll == 1)) {
			LoopCounter++;
			numChanged = 0;
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
					if ((alpha[i] > tau && alpha[i] < C - tau)
							|| (alpha_s[i] > tau && alpha_s[i] < C - tau)) {
						numChanged += examineExample(i);
					}
				}
			}

			if (LoopCounter % 2 == 0) {
				MinimumNumChanged = (int) Math.max(1, 0.1 * l);
			} else {
				MinimumNumChanged = 1;
			}

			if (examineAll == 1) {
				examineAll = 0;
			} else if (numChanged < MinimumNumChanged) {
				// else if(numChanged == 0) {
				examineAll = 1;
			}
			System.out.println("LoopCounter: " + LoopCounter + " numChanged:"
					+ numChanged + " MinimumNumChanged:" + MinimumNumChanged
					+ " examineAll:" + examineAll);
		}

		for (i = 0; i < l; i++) {
			if (alpha[i] - alpha_s[i] > tau) {
				support++;
			}
			if (alpha[i] > C - tau || alpha_s[i] > C - tau) {
				atbound++;
			}
			error += (target[i] * (error_cache[i] - b + target[i]) > tau) ? 0
					: 1;
		}

		IO.print("\n", 2);
		IO.println("Number of support ArrayLists:    " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Support ArrayLists at bound:     " + atbound + "/" + l
				+ " (" + ((float) (100.0 * negativ) / l) + "%)", 2);
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

	public static void main(String[] argv) throws Exception {
		DataOutputStream fileout;
		Random r;

		fileout = new DataOutputStream(new FileOutputStream(
				"c:/svm/classes/data/regression1.dat"));
		r = new Random();

		fileout.writeBytes("SparseVector 2000\n");
		for (float x, y, count = 1; count <= 2000; count++) {
			x = (float) (2.0 * Math.PI * r.nextFloat());
			y = 0.02f * x * x * x + 0.05f * x * x - x
					+ (float) r.nextGaussian();
			// y = 0.02f*x*x*x + 0.05f*x*x - x;
			// y = (float)Math.sin(x);
			fileout.writeBytes(y + " 0:" + x + "\n");
		}

		fileout.close();
	}

	/*
	 * public static void main(String[] argv) { Parameters par = new
	 * Parameters(); par.type = Kernel.RBF; par.degree = 6.0; par.factor = 1.0;
	 * par.gamma = 1.0; par.bias = 0.0;
	 * 
	 * double C = 10.0; double tau = 1e-8; double eps = 0.001;
	 * 
	 * int size = 20; String filename = "z:/data/ernestos.dat"; int h = 1, k =
	 * 0; boolean partition = false; String lab = "1"; try { for(int
	 * i=0;i<argv.length;i++) { if(argv[i].charAt(0) != '-') break; ++i;
	 * switch(argv[i-1].charAt(1)) { case 'l': lab = argv[i]; break; case 'k':
	 * par.type = atoi(argv[i]); break; case 'd': par.degree = atof(argv[i]);
	 * break; case 'f': par.factor = atof(argv[i]); break; case 'g': par.gamma =
	 * atof(argv[i]); break; case 'b': par.bias = atof(argv[i]); break; case
	 * 'c': C = atof(argv[i]); break; case 't': tau = atof(argv[i]); break; case
	 * 'e': eps = atof(argv[i]); break; case 'i': filename = argv[i]; break;
	 * case 's': size = atoi(argv[i]); break; case '1': h = atoi(argv[i]);
	 * break; case '2': k = atoi(argv[i]); break; case 'p': h = atoi(argv[i]);
	 * partition = true; break; default: System.err.print("unknown option\n"); }
	 * } } catch(Exception e) { System.err.print(e.toString()); }
	 * 
	 * Kernel kernel = null; double[] label = null; if(k != 0) {
	 * switch(par.type) { case Kernel.LIN: kernel = new
	 * Kernel(StepFunction.readData(filename),size); break; case Kernel.POL:
	 * kernel = new
	 * Kernel(StepFunction.readData(filename),(int)par.degree,size); break; case
	 * Kernel.RBF: kernel = new
	 * Kernel(StepFunction.readData(filename),par.gamma,size); break; }
	 * 
	 * label = new double[kernel.l];
	 * 
	 * for(int i = 0; i < kernel.l; i++) { label[i] =
	 * (lab.equals(StepFunction.label[i]))? 1.0: -1.0; } } else {
	 * switch(par.type) { case Kernel.LIN: kernel = new
	 * Kernel(SparseVector.readData(filename),size); break; case Kernel.POL:
	 * kernel = new
	 * Kernel(SparseVector.readData(filename),(int)par.degree,size); break; case
	 * Kernel.RBF: kernel = new
	 * Kernel(SparseVector.readData(filename),par.gamma,size); break; }
	 * 
	 * label = new double[kernel.l];
	 * 
	 * for(int i = 0; i < kernel.l; i++) { label[i] =
	 * (lab.equals(SparseVector.label[i]))? 1.0: -1.0; } }
	 * 
	 * //Kernel kernel = new Kernel(par,prob,size); RSmoSmola smo= new
	 * RSmoSmola(kernel,label,C,tau,eps);
	 * 
	 * smo.train();
	 * 
	 * }
	 */

}

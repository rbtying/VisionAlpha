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

class RSmo {
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
	double error;

	public RSmo(Kernel k, double t[], double C_, double tau_, double eps_) {
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

	double alph1, alph2;
	double y1, y2;
	double E1, E2;
	// double deltaE;
	double L, H;
	double k11, k12, k22;
	double eta;
	double a1, a2, a2opt;
	double gamma;
	double Lobj, Hobj, objopt;
	double v1, v2;
	int k;

	int takeStep(int i1, int i2) {
		// System.out.println(".");

		if (i1 == i2) {
			return 0;
		}

		/* Lagrange multiplier for i1 ans i2 */
		alph1 = alpha[i1];
		alph2 = alpha[i2];

		y1 = target[i1];
		y2 = target[i2];

		/* SVM output on point[i1] - y1 */
		E1 = error_cache[i1];
		E2 = error_cache[i2];

		double[] kernel_i1 = kernel.getColumn(i1);
		double[] kernel_i2 = kernel.getColumn(i2);
		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		gamma = alph1 + alph2;
		eta = 2 * k12 - k11 - k22;
		// eta = k11 + k22 - 2* k12;
		// System.out.println("eta=" + eta);

		v1 = -(E1 - alph1 * k11 - alph2 * k12);
		v2 = -(E2 - alph1 * k12 - alph2 * k22);

		a2opt = alph2;
		a2 = alph2;
		System.out.println("E1=" + error_cache[i1] + " E2=" + error_cache[i2]);
		if (eta < 0) {
			// deltaE = E1 - E2;

			objopt = Double.NEGATIVE_INFINITY;

			if (alph1 >= 0 && alph2 >= 0) {
				L = Math.max(0, gamma - C);
				H = Math.min(C, gamma);

				if (L < H) {
					a2 = alph2 + (E1 - E2) / eta;

					a2 = Math.max(L, a2);
					a2 = Math.min(H, a2);
					// System.out.println("objopt="+objopt+"\nobjectiveFuntionAt(a2)="+objectiveFuntionAt(a2));
					// System.out.println("a1=" + (gamma - a1)+ " a2=" + a2);
					// System.out.println("L=" + L + " H=" + H);
					// System.out.println("E1=" + E1 +" E2="+E2);
					// System.out.println();
					if (objopt < objectiveFuntionAt(a2)) {
						objopt = objectiveFuntionAt(a2);
						a2opt = a2;
					}
				}
			} // end if case1
			if (alph1 >= 0 && alph2 <= 0) {
				L = Math.max(-C, gamma - C);
				H = Math.min(0, gamma);

				if (L < H) {
					a2 = alph2 + (E1 - E2 + 2 * eps) / eta;

					a2 = Math.max(L, a2);
					a2 = Math.min(H, a2);
					// System.out.println("objopt="+objopt+"\nobjectiveFuntionAt(a2)="+objectiveFuntionAt(a2));
					// System.out.println("a1=" + (gamma - a1)+ " a2=" + a2);
					// System.out.println("L=" + L + " H=" + H);
					// System.out.println("E1=" + E1 +" E2="+E2);
					// System.out.println();
					if (objopt < objectiveFuntionAt(a2)) {
						objopt = objectiveFuntionAt(a2);
						a2opt = a2;
					}
				}
			} // end if case2
			if (alph1 <= 0 && alph2 >= 0) {
				L = Math.max(0, gamma);
				H = Math.min(C, gamma + C);

				if (L < H) {
					a2 = alph2 + (E1 - E2 - 2 * eps) / eta;

					a2 = Math.max(L, a2);
					a2 = Math.min(H, a2);
					// System.out.println("objopt="+objopt+"\nobjectiveFuntionAt(a2)="+objectiveFuntionAt(a2));
					// System.out.println("a1=" + (gamma - a1)+ " a2=" + a2);
					// System.out.println("L=" + L + " H=" + H);
					// System.out.println("E1=" + E1 +" E2="+E2);
					// System.out.println();
					if (objopt < objectiveFuntionAt(a2)) {
						objopt = objectiveFuntionAt(a2);
						a2opt = a2;
					}
				}
			} // end if case3
			if (alph1 <= 0 && alph2 <= 0) {
				L = Math.max(-C, gamma);
				H = Math.min(0, gamma + C);

				if (L < H) {
					a2 = alph2 + (E1 - E2) / eta;

					a2 = Math.max(L, a2);
					a2 = Math.min(H, a2);
					// System.out.println("objopt="+objopt+"\nobjectiveFuntionAt(a2)="+objectiveFuntionAt(a2));
					// System.out.println("a1=" + (gamma - a1)+ " a2=" + a2);
					// System.out.println("L=" + L + " H=" + H);
					// System.out.println("E1=" + E1 +" E2="+E2);
					// System.out.println();
					if (objopt < objectiveFuntionAt(a2)) {
						objopt = objectiveFuntionAt(a2);
						a2opt = a2;
					}
				}

			} // end if case4
			a2 = a2opt;
			// System.out.println(" a2opt=" + a2opt);
		} // end if eta < 0
		else {
			/* Objetive function at a2 = L, H */
			Lobj = objectiveFuntionAt(L);
			Hobj = objectiveFuntionAt(H);

			if (Lobj > Hobj + tau) {
				a2 = L;
			} else if (Lobj < Hobj - tau) {
				a2 = H;
			} else {
				a2 = alph2;
			}
		}

		if (Math.abs(a2 - alph2) < tau * (a2 + alph2 + tau)) {
			return 0;
		}

		/*
		 * a2 = (a2 < tau - C) ? -C : a2; a2 = ( -tau < a2 && a2 < tau) ? 0 :
		 * a2; a2 = (a2 > C - tau) ? C : a2;
		 */

		a1 = gamma - a2;

		double b1, b2, bold;

		bold = b;

		b1 = b - E1 - (a1 - alph1) * k11 - (a2 - alph2) * k12;
		if (a1 > tau && a1 < C - tau) {
			b = b1;
		} else {
			b2 = b - E2 - (a1 - alph1) * k12 - (a2 - alph2) * k22;
			if (a2 > tau && a2 < C - tau) {
				b = b2;
			} else {
				b = 0.5 * (b1 + b2);
			}
		}

		/* Update error cache using new Lagrange multipliers */
		for (k = 0; k < l; k++) {
			error_cache[k] += (a1 - alph1) * kernel_i1[k] + (a2 - alph2)
					* kernel_i2[k] + b - bold;
		}

		System.out.println("E1=" + error_cache[i1] + " E2=" + error_cache[i2]);
		// System.out.println();
		// System.out.println();
		/* Store a1, a2 in alph array */
		alpha[i1] = a1;
		alpha[i2] = a2;

		return 1;
	}

	double tmp, tmax;

	int examineExample(int i2) {
		alph2 = alpha[i2];

		/* SVM output on point[i2] - y2 */
		E2 = error_cache[i2];

		if ((0 < alph2 && -eps < E2) || (alph2 == 0 && (E2 < -eps || eps < E2))
				|| (alph2 < 0 && E2 < eps)) {
			/* Number of non-zero & non-C alpha > 1 */
			int i1, irand;

			i1 = -1;
			tmax = 0;
			for (k = 0; k < l; k++) {
				alph1 = alpha[k];
				if ((alph1 > tau && alph1 < C - tau)
						|| (-C + tau < alph1 && alph1 < -tau)) {
					E1 = error_cache[k];
					tmp = Math.abs(E1 - E2);
					if (tmax < tmp) {
						tmax = tmp;
						i1 = k;
					}
				}
			}

			if (i1 > -1) {
				/* Result of second choice heuristic */
				if (takeStep(i1, i2) == 1) {
					return 1;
				}
			}

			/* Loop over all non-zero non-C alpha, starting at random */
			irand = (int) (l * Math.random());

			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				if (alpha[i1] > tau && alpha[i1] < C - tau) {
					if (takeStep(i1, i2) == 1) {
						return 1;
					}
				}
			}

			/* Loop over all posibles i1, starting at random */
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

	double objectiveFuntionAt(double a) {
		return y1 * gamma - y1 * a + y2 * a - eps
				* (Math.abs(gamma - a) + Math.abs(a)) - 0.5 * k11 * (gamma - a)
				* (gamma - a) - 0.5 * k22 * a * a - k12 * (gamma - a) * a
				- (gamma - a) * v1 - a * v2;
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
				System.out.print("*");
			} else {
				/* Loop over examples where alpha is not 0 & not C */
				for (i = 0; i < l; i++) {
					if ((tau < alpha[i] && alpha[i] < C - tau)
							|| (-C + tau < alpha[i] && alpha[i] < -tau)) {
						numChanged += examineExample(i);
					}
				}
				System.out.print(".");
			}

			if (examineAll == 1) {
				examineAll = 0;
			} else if (numChanged == 0) {
				examineAll = 1;
			}
		}

		error = 0;
		support = 0;
		for (i = 0; i < l; i++) {
			if (alpha[i] > tau) {
				support++;
			}
			error += error_cache[i] * error_cache[i];
		}

		error = Math.sqrt(error) / l;

		System.out.println();
		System.out.println("Support ArrayLists:\t\t" + support + "/" + l + " ("
				+ ((float) (100.0 * support) / l) + "%)");
		System.out.println("Cuadratic Error:\t\t" + error + "/" + l + " ("
				+ ((float) (100.0 * error) / l) + "%)\n");
		System.out.println("Kernel Evaliations:\t\t" + kernelEvaluations()
				+ "\n");
	}

	int kernelEvaluations() {
		return kernel.evaluations;
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	public static void main(String[] argv) throws Exception {
		RSmo rsmo;
		Data data = null;

		double C = 100.0;
		double tau = 1E-8;
		double eps = 0.001;

		int type = Kernel.POL;
		int degree = 3;
		double factor = 0.01;
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
				case 'c':
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
					break;
				case 'O':
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
			// System.err.print(e.toString());
			// System.exit(1);
		}

		try {
			data = SparseVector.readData(filename);
		} catch (RuntimeException e) {
			data = StepFunction.readData(filename);
		}

		Kernel kernel = new Kernel(data.point, type, degree, factor, bias, size);
		double t[];
		t = new double[data.l];
		for (i = 0; i < data.l; i++) {
			t[i] = Double.parseDouble(data.label[i]);
		}
		rsmo = new RSmo(kernel, t, C, tau, eps);

		rsmo.train();
	}
}

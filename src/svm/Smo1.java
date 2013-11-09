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

// THERE ARE TODOs!!

class Smo1 {
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

	double[] out;
	double A;
	double B;
	double prior1;
	double prior0;
	int iterations = 100;

	double C;
	double tau;
	double eps;

	public Smo1(Kernel k, double t[], double C_, double tau_, double eps_,
			int it_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];

		prior1 = prior0 = 0;
		for (int i = 0; i < l; i++) {
			error_cache[i] = -target[i];
			alpha[i] = 0.0;
			if (target[i] == 1.0) {
				i_up = i;
				prior1++;
			} else {
				i_low = i;
				prior0++;
			}
		}

		b = 0;

		b_up = -1.0;
		b_low = 1.0;

		C = C_;
		tau = tau_;
		eps = eps_;
		iterations = it_;
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
		if (i1 == i2) {
			return 0;
		}

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
			IO.print("#", 4);
			Lobj = objectiveFunctionAt(L);
			Hobj = objectiveFunctionAt(H);
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

		if (optimality == 1) {
			return 0;
		}

		// For i2 in I0 choose the better i1
		if (isI0(i2)) {
			if (b_low - E2 > E2 - b_up) {
				i1 = i_low;
			} else {
				i1 = i_up;
			}
		}

		return takeStep(i1, i2);
	}

	int support = 0;
	int positiv = 0;
	int negativ = 0;
	int atbound = 0;
	int error = 0;
	int iter = 0;

	void train() {
		int numChanged;
		int examineAll;
		int i;

		numChanged = 0;
		examineAll = 1;

		while (numChanged > 0 || examineAll == 1) {
			numChanged = 0;
			if (examineAll == 1) {
				IO.println("Iteration: " + (++iter) + "\tnumChanged: "
						+ numChanged, 4);
				IO.print("*", 2);

				// Loop over all training examples
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
			} else {
				IO.println("Iteration: " + (++iter) + "\tnumChanged: "
						+ numChanged, 4);

				// Loop over examples where alpha is not 0 & not C
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
				 * 2*eps) && (innerLoopSuccess > 0)) { IO.print("_",2);
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
		IO.println("Number of kernel evaluations: " + kernel.getEvaluations(),
				3);
	}

	public double output(InnerProductSpace x) {
		double f;

		f = -b;
		for (int i = 0; i < l; i++) {
			if (alpha[i] > tau) {
				f += target[i] * alpha[i] * kernel.value(x, i);
			}
		}

		return f;
	}

	public void trainSigmoidParameters(InnerProductSpace[] x, double[] xl) {
		int l;
		double[] tar;
		double scale;
		double diff;
		double p;
		double det;
		double err;
		double oldB;
		double oldA;
		double d2;
		double d1;
		double t;
		int i;
		double a, b, c, d, e;
		int count;
		int it;
		double[] pp;
		double olderr;
		double lambda;
		double loTarget;
		double hiTarget;

		A = 0.0;
		B = Math.log((prior0 + 1.0) / (prior1 + 1.0));

		hiTarget = (prior1 + 1.0) / (prior1 + 2.0);
		loTarget = 1 / (prior0 + 2.0);

		lambda = 1e-3;
		olderr = 1e300;

		l = x.length;

		IO.print("\n", 1);
		IO.print("Sigmoid Training* " + l + " ", 1);

		out = new double[l];
		pp = new double[l];
		tar = new double[l];

		for (i = 0; i < l; i++) {
			out[i] = output(x[i]);
			pp[i] = (prior1 + 1.0) / (prior0 + prior1 + 2.0);
			// tar[i] = (xl[i] == 1)? (1.0 - 0.01*(2*Math.random() - 1)) :
			// (0.01*(2*Math.random() - 1));
			tar[i] = (out[i] > 0) ? (1.0) : (0.0);
			// tar[i] = xl[i];
		}

		count = 0;
		for (it = 0; it < iterations; it++) {
			IO.print(".", 2);
			a = b = c = d = e = 0.0;
			// First, compute Hessian & gradient of error function
			// with respecto to A & B
			for (i = 0; i < l; i++) {
				if (tar[i] == 1.0) {
					t = hiTarget;
				} else {
					t = loTarget;
				}

				d1 = pp[i] - t;
				d2 = pp[i] * (1 - pp[i]);
				a += out[i] * out[i] * d2;
				b += d2;
				c += out[i] * d2;
				d += out[i] * d1;
				e += d1;
			}

			// If gradient is really tiny, then stop
			if (Math.abs(d) < 1e-9 && Math.abs(e) < 1e-9) {
				break;
			}

			oldA = A;
			oldB = B;
			err = 0.0;

			// Loop until goodness of fit increases
			while (true) {
				det = (a + lambda) * (b + lambda) - c * c;

				if (det == 0.0) {
					// If determinant of Hessian is zero,
					// increase stabilizer
					lambda *= 10.0;
					continue;
				}

				A = oldA + ((b + lambda) * d - c * e) / det;
				B = oldB + ((a + lambda) * e - c * d) / det;

				// now compute goddness of fit
				err = 0;
				for (i = 0; i < l; i++) {
					p = 1.0 / (1.0 + Math.exp(out[i] * A + B));
					pp[i] = p;

					// t = (tar[i] == 1)? (1.0 - 0.01*Math.random()) :
					// (0.01*Math.random());
					t = tar[i];

					// At this step, make sure log(0) returns -200
					if (p > 0) {
						err -= t * Math.log(p) + (1.0 - t) * Math.log(1.0 - p);
					} else {
						err -= -200 * t;
					}
				}
				if (err < olderr * (1.0 + 1.0e-7)) {
					lambda *= 0.1;
					break;
				}
				// error did not decrease: increase stabilizer by factor of 10
				// & try again
				lambda *= 10.0;
				if (lambda > 1.0e6) {
					break;
				}
			}

			diff = err - olderr;
			scale = 0.5 * (err + olderr + 1.0);
			if (diff > -1.0e-3 * scale && diff < 1.0e-7 * scale) {
				count++;
			} else {
				count = 0;
			}
			if (count == 3) {
				break;
			}
		}
		count = 0;
		l = this.l;
		for (i = 0; i < l; i++) {
			p = 1.0 / (1.0 + Math.exp((error_cache[i] - this.b) * A + B));
			if (p > 0.5 && target[i] == -1) {
				count++;
			} else if (p < 0.5 && target[i] == 1.0) {
				count++;
			}
		}
		IO.print("\n", 2);
		IO.print("Error: " + count + "/" + l + " (" + ((1.0f * count) / l)
				+ ")\n", 2);
		IO.print("Sigmoid paramters A=" + A + " B=" + B, 2);
		IO.print("\n", 1);
	}

	public void trainSigmoidParameters() {
		double[] tar;
		double scale;
		double diff;
		double p;
		double det;
		double err;
		double oldB;
		double oldA;
		double d2;
		double d1;
		double t;
		int i;
		double a, b, c, d, e;
		int count;
		int it;
		double[] pp;
		double olderr;
		double lambda;
		double loTarget;
		double hiTarget;

		IO.print("\n", 1);
		IO.print("Sigmoid Training", 1);

		A = 0.0;
		B = Math.log((prior0 + 1.0) / (prior1 + 1.0));

		hiTarget = (prior1 + 1.0) / (prior1 + 2.0);
		loTarget = 1 / (prior0 + 2.0);

		lambda = 1e-3;
		olderr = 1e300;

		out = new double[l];
		pp = new double[l];
		tar = new double[l];

		for (i = 0; i < l; i++) {
			out[i] = error_cache[i] - this.b;
			pp[i] = (prior1 + 1.0) / (prior0 + prior1 + 2.0);
			// tar[i] = (target[i] == 1)? (1.0 - 0.01*(2*Math.random() - 1)) :
			// (0.01*(2*Math.random() - 1));
			tar[i] = (target[i] == 1) ? (1.0) : (0.0);
		}

		count = 0;
		for (it = 0; it < iterations; it++) {
			IO.print(".", 2);
			a = b = c = d = e = 0.0;
			// First, compute Hessian & gradient of error function
			// with respecto to A & B
			for (i = 0; i < l; i++) {
				if (target[i] == 1.0) {
					t = hiTarget;
				} else {
					t = loTarget;
				}

				d1 = pp[i] - t;
				d2 = pp[i] * (1 - pp[i]);
				a += out[i] * out[i] * d2;
				b += d2;
				c += out[i] * d2;
				d += out[i] * d1;
				e += d1;
			}

			// If gradient is really tiny, then stop
			if (Math.abs(d) < 1e-9 && Math.abs(e) < 1e-9) {
				break;
			}

			oldA = A;
			oldB = B;
			err = 0.0;

			// Loop until goodness of fit increases
			while (true) {
				det = (a + lambda) * (b + lambda) - c * c;

				if (det == 0.0) {
					// If determinant of Hessian is zero,
					// increase stabilizer
					lambda *= 10.0;
					continue;
				}

				A = oldA + ((b + lambda) * d - c * e) / det;
				B = oldB + ((a + lambda) * e - c * d) / det;

				// now compute goddness of fit
				err = 0;
				for (i = 0; i < l; i++) {
					p = 1.0 / (1.0 + Math.exp(out[i] * A + B));
					pp[i] = p;

					// if(target[i] == 1.0) {
					// t = hiTarget;
					// }
					// else {
					// t = loTarget;
					// }

					// t = (target[i] == 1)? (1.0 - 0.01*Math.random()) :
					// (0.01*Math.random());
					t = tar[i];

					// At this step, make sure log(0) returns -200
					if (p > 0) {
						err -= t * Math.log(p) + (1.0 - t) * Math.log(1.0 - p);
					} else {
						err -= -200 * t;
					}
				}
				if (err < olderr * (1.0 + 1.0e-7)) {
					lambda *= 0.1;
					break;
				}
				// error did not decrease: increase stabilizer by factor of 10
				// & try again
				lambda *= 10.0;
				if (lambda > 1.0e6) {
					break;
				}
			}

			diff = err - olderr;
			scale = 0.5 * (err + olderr + 1.0);
			if (diff > -1.0e-3 * scale && diff < 1.0e-7 * scale) {
				count++;
			} else {
				count = 0;
			}
			if (count == 3) {
				break;
			}
		}
		count = 0;
		for (i = 0; i < l; i++) {
			if (pp[i] > 0.5 && target[i] < 0) {
				count++;
			} else if (pp[i] < 0.5 && target[i] > 0) {
				count++;
			}
		}
		IO.print("\n", 2);
		IO.print("Error: " + count + "/" + l + " (" + ((1.0f * count) / l)
				+ ")\n", 2);
		IO.print("Sigmoid paramters A=" + A + " B=" + B, 2);
		IO.print("\n", 1);
	}

	double objectiveFunctionAt(double a) {
		double h;

		// TO DO:
		// - Check if it it possible to reduce the runtime if one
		// uses v1 and v2 as global variables, because they have
		// the same value for the evaluation of Hobj and Lobj.
		// - Check it is possible to eliminate the following cicle
		// by using the values stored in error_cache[] and the
		// current values of the parameters of the algorithm.
		// v1 = 0.0;
		// v2 = 0.0;
		// for(k = 0; k < l; k++) {
		// if(k != i1 && k != i2 && alpha[k] > tau) {
		// v1 += target[k]*alpha[k]*kernel_i1[k];
		// v2 += target[k]*alpha[k]*kernel_i2[k];
		// }
		// }

		v1 = E1 + b + y1 - y1 * alph1 * k11 - y2 * alph2 * k12;
		v2 = E2 + b + y2 - y1 * alph1 * k12 - y2 * alph2 * k22;

		// TO DO:
		// - Chek if it is optimal to use both variables g and h.
		// g = alph1 + s*alph2;
		// h = (g-s*a);

		h = alph1 + s * (alph2 - a);

		return (h + a - 0.5 * k11 * h * h - 0.5 * k22 * a * a - s * k12 * h * a
				- y1 * h * v1 - y2 * a * v2);
	}

	boolean isI0(int i) {
		return ((alpha[i] > tau) && (alpha[i] < C - tau));
	}

	boolean isI1(int i) {
		return ((target[i] == 1.0) && (alpha[i] <= tau));
	}

	boolean isI2(int i) {
		return ((target[i] == -1.0) && (alpha[i] >= C - tau));
	}

	boolean isI3(int i) {
		return ((target[i] == 1.0) && (alpha[i] >= C - tau));
	}

	boolean isI4(int i) {
		return ((target[i] == -1.0) && (alpha[i] <= tau));
	}
}

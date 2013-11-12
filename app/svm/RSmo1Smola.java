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

import java.util.*;
import java.io.*;

class RSmo1Smola {
	Kernel kernel;

	double[] error_cache;
	double[] alpha;
	double[] alpha_s;
	double[] target;
	char[] index_type;
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
	double error = 0;

	public RSmo1Smola(Kernel k, double t[], double C_, double tau_, double eps_) {
		kernel = k;
		l = kernel.l;
		target = t;

		error_cache = new double[l];
		alpha = new double[l];
		alpha_s = new double[l];

		index_type = new char[l];

		for (int i = 0; i < l; i++) {
			error_cache[i] = target[i];
			index_type[i] = 'a';
			alpha[i] = 0.0;
			alpha_s[i] = 0.0;
		}

		b_up = target[0] + eps;
		b_low = target[0] - eps;

		i_up = i_low = 0;

		b = 0;

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
	double F1, F2, delta_phi;
	double eta, gamma;
	double a1, a2;
	double Lobj, Hobj;
	int case1, case2, case3, case4, finished;
	int k;
	double error_k;
	boolean alphaChanged;
	double delta_alpha1, delta_alpha1old;
	int current_case;

	int takeStep(int i1, int i2) {
		if (i1 == i2)
			return 0;

		/* Lagrange multiplier for i1 */
		alpha1 = alpha[i1];
		alpha1_s = alpha_s[i1];

		alpha2 = alpha[i2];
		alpha2_s = alpha_s[i2];

		/* SVM output on point[i1] - y1 */
		F1 = error_cache[i1];
		F2 = error_cache[i2];

		kernel_i1 = kernel.getColumn(i1);
		kernel_i2 = kernel.getColumn(i2);

		k11 = kernel_i1[i1];
		k12 = kernel_i1[i2];
		k22 = kernel_i2[i2];

		eta = -2.0 * k12 + k11 + k22;
		gamma = alpha1 - alpha1_s + alpha2 - alpha2_s;

		case1 = case2 = case3 = case4 = finished = 0;

		alpha1old = alpha1;
		alpha1old_s = alpha1_s;
		alpha2old = alpha2;
		alpha2old_s = alpha2_s;

		delta_phi = F1 - F2;
		delta_alpha1 = delta_alpha1old = alpha1 - alpha1_s;

		alphaChanged = false;
		if (eta > 0) {
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

						a2 = (a2 <= tau) ? 0 : a2;
						a2 = (a2 >= C - tau) ? C : a2;

						a1 = alpha1 - (a2 - alpha2);

						/*
						 * a1 = (a1 <= tau)? 0 : a1; a1 = (a1 >= C - tau)? C :
						 * a1;
						 */

						// update alpha1, alpha2 if change is larger thas some
						// threshold
						if (a1 > C || a1 < 0 || a2 > C || a2 < 0) {
							a1 = alpha1old;
							a2 = alpha2old;
						} else if (Math.abs(a2 - alpha2) > tau
								* (a2 + alpha2 + tau)) {
							alpha1 = a1;
							alpha2 = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case1 = 1;
					current_case = 1;
				} else if (case2 == 0
						&& (alpha1 > 0 || (alpha1_s == 0 && delta_phi > 2 * eps))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 2 * eps))) {
					// compute L, H (alpha1,alpha2_s)
					L = Math.max(0, gamma);
					H = Math.min(C, C + gamma);
					// L = Math.max(-C,gamma - C);
					// H = Math.min(0,gamma);

					if (L < H) {
						a2 = alpha2_s + (delta_phi - 2 * eps) / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 <= tau) ? 0 : a2;
						a2 = (a2 >= C - tau) ? C : a2;

						a1 = alpha1 + (a2 - alpha2_s);

						/*
						 * a1 = (a1 <= tau)? 0 : a1; a1 = (a1 >= C - tau)? C :
						 * a1;
						 */

						// update alpha1, alpha2_s if change is larger thas some
						// threshold
						if (a1 > C || a1 < 0 || a2 > C || a2 < 0) {
							a1 = alpha1old;
							a2 = alpha2old_s;
						} else if (
						// (a1 >= 0) && (a1 <= C) &&
						Math.abs(a2 - alpha2_s) > tau * (a2 + alpha2_s + tau)) {
							alpha1 = a1;
							alpha2_s = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case2 = 1;
					current_case = 2;
				} else if (case3 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < -2
								* eps))
						&& (alpha2 > 0 || (alpha2_s == 0 && delta_phi < -2
								* eps))) {
					// compute L, H (alpha1_s,alpha2)
					L = Math.max(0, -gamma);
					H = Math.min(C, -gamma + C);
					// L = Math.max(0,gamma);
					// H = Math.min(C,gamma + C);

					if (L < H) {
						a2 = alpha2 - (delta_phi + 2 * eps) / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 <= tau) ? 0 : a2;
						a2 = (a2 >= C - tau) ? C : a2;

						a1 = alpha1_s + (a2 - alpha2);

						/*
						 * a1 = (a1 <= tau)? 0 : a1; a1 = (a1 >= C - tau)? C :
						 * a1;
						 */

						// update alpha1_s, alpha2 if change is larger thas some
						// threshold
						if (a1 > C || a1 < 0 || a2 > C || a2 < 0) {
							a1 = alpha1old_s;
							a2 = alpha2old;
						} else if (
						// (a1 >= 0) && (a1 <= C) &&
						Math.abs(a2 - alpha2) > tau * (a2 + alpha2 + tau)) {
							alpha1_s = a1;
							alpha2 = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case3 = 1;
					current_case = 3;
				} else if (case4 == 0
						&& (alpha1_s > 0 || (alpha1 == 0 && delta_phi < 0))
						&& (alpha2_s > 0 || (alpha2 == 0 && delta_phi > 0))) {
					// compute L, H (alpha1_s,alpha2_s)
					L = Math.max(0, -gamma - C);
					H = Math.min(C, -gamma);
					// L = Math.max(-C,gamma);
					// H = Math.min(0,gamma + C);

					if (L < H) {
						a2 = alpha2_s + delta_phi / eta;
						a2 = Math.min(a2, H);
						a2 = Math.max(L, a2);

						a2 = (a2 <= tau) ? 0 : a2;
						a2 = (a2 >= C - tau) ? C : a2;

						a1 = alpha1_s - (a2 - alpha2_s);

						/*
						 * a1 = (a1 <= tau)? 0 : a1; a1 = (a1 >= C - tau)? C :
						 * a1;
						 */

						// update alpha1_s, alpha2_s if change is larger thas
						// some threshold
						if (a1 > C || a1 < 0 || a2 > C || a2 < 0) {
							a1 = alpha1old_s;
							a2 = alpha2old_s;
						} else if (Math.abs(a2 - alpha2_s) > tau
								* (a2 + alpha2_s + tau)) {
							alpha1_s = a1;
							alpha2_s = a2;
							alphaChanged = true;
						}
					} else {
						finished = 1;
					}

					case4 = 1;
					current_case = 4;
				} else {
					finished = 1;
				}

				/*
				 * if(alpha1 < -tau || alpha1_s < -tau) {
				 * System.out.println("case: "
				 * +current_case+"\n alpha1: "+alpha1+
				 * " "+alpha1_s+" alpha2: "+alpha2+" "+alpha2_s);
				 * System.out.println("L: "+L+" H: "+H); //System.exit(1); }
				 * if(alpha2 < -tau || alpha2_s < -tau) {
				 * System.out.println("case: "
				 * +current_case+"\n alpha1: "+alpha1+
				 * " "+alpha1_s+" alpha2: "+alpha2+" "+alpha2_s);
				 * System.out.println("L: "+L+" H: "+H); //System.exit(1); }
				 * if(alpha1 > C + tau || alpha1_s > C + tau) {
				 * System.out.println
				 * ("case: "+current_case+"\n alpha1: "+alpha1+
				 * " "+alpha1_s+" alpha2: "+alpha2+" "+alpha2_s);
				 * System.out.println("L: "+L+" H: "+H); //System.exit(1); }
				 * if(alpha2 > C + tau || alpha2_s > C + tau) {
				 * System.out.println
				 * ("case: "+current_case+"\n alpha1: "+alpha1+
				 * " "+alpha1_s+" alpha2: "+alpha2+" "+alpha2_s);
				 * System.out.println("L: "+L+" H: "+H); //System.exit(1); }
				 */

				// System.out.println("eta*(delta_alpha1 - delta_alpha1old) = "+(eta*(delta_alpha1
				// - delta_alpha1old)));
				// System.out.println("eta = "+(eta));

				// if(alphaChanged) {
				// update delta_phi and delta:alpha1old;
				delta_alpha1 = alpha1 - alpha1_s;
				delta_phi = delta_phi - eta * (delta_alpha1 - delta_alpha1old);
				delta_alpha1old = delta_alpha1;
				// }
			} // END while
		} // END eta > 0

		if (!alphaChanged) {
			return 0;
		}

		// update lagrange multipliers
		alpha[i1] = alpha1;
		alpha_s[i1] = alpha1_s;

		alpha[i2] = alpha2;
		alpha_s[i2] = alpha2_s;

		// Update error cache using new Lagrange multipliers.
		this.actualizeIndexType();

		b_low = Double.NEGATIVE_INFINITY;
		b_up = Double.POSITIVE_INFINITY;
		for (k = 0; k < l; k++) {
			// if(isI0(k))
			error_k = error_cache[k]
					+ ((alpha1old - alpha1old_s) - (alpha1 - alpha1_s))
					* kernel_i1[k]
					+ ((alpha2old - alpha2old_s) - (alpha2 - alpha2_s))
					* kernel_i2[k];

			// Update b_up, b_low, i_up, i_low.
			if (isI0(k) || isI1(k) || isI2(k)) {
				if ((isI0b(k) || isI2(k)) && error_k + eps > b_low) {
					b_low = error_k + eps;
					i_low = k;
				}
				if ((isI0a(k) || isI1(k)) && error_k - eps > b_low) {
					b_low = error_k - eps;
					i_low = k;
				}
			}
			if (isI0(k) || isI1(k) || isI3(k)) {
				if ((isI0a(k) || isI3(k)) && error_k - eps < b_up) {
					b_up = error_k - eps;
					i_up = k;
				}
				if ((isI0b(k) || isI1(k)) && error_k + eps < b_up) {
					b_up = error_k + eps;
					i_up = k;
				}
			}

			error_cache[k] = error_k;
		}

		return 1;
	}

	int examineExample(int i2) {
		int i1 = i2, optimality;

		// Lagrange multipliers for i2
		alpha2 = alpha[i2];
		alpha2_s = alpha_s[i2];

		F2 = error_cache[i2];

		/*
		 * if(isI1(i2)) { if(F2 + eps < b_up) { b_up = F2 + eps; i_up = i2; }
		 * else if(F2 - eps > b_low) { b_low = F2 - eps; i_low = i2; } } else
		 * if(isI2(i2) && (F2 + eps > b_low)) { b_low = F2 + eps; i_low = i2; }
		 * else if(isI2(i2) && (F2 - eps < b_up)) { b_up = F2 - eps; i_up = i2;
		 * }
		 */

		optimality = 1;

		// case 1
		if (isI0a(i2)) {
			if (b_low - (F2 - eps) > 2 * tau) {
				optimality = 0;
				i1 = i_low;
				if ((F2 - eps) - b_up > b_low - (F2 - eps)) {
					i1 = i_up;
				}
			} else if ((F2 - eps) - b_up > 2 * tau) {
				optimality = 0;
				i1 = i_up;
				if (b_low - (F2 - eps) > (F2 - eps) - b_up) {
					i1 = i_low;
				}
			}
		}

		// case 2
		if (isI0b(i2)) {
			if (b_low - (F2 + eps) > 2 * tau) {
				optimality = 0;
				i1 = i_low;
				if ((F2 + eps) - b_up > b_low - (F2 + eps)) {
					i1 = i_up;
				}
			} else if ((F2 + eps) - b_up > 2 * tau) {
				optimality = 0;
				i1 = i_up;
				if (b_low - (F2 + eps) > (F2 + eps) - b_up) {
					i1 = i_low;
				}
			}
		}

		// case 3
		if (isI1(i2)) {
			if (b_low - (F2 + eps) > 2 * tau) {
				optimality = 0;
				i1 = i_low;
				if ((F2 + eps) - b_up > b_low - (F2 + eps)) {
					i1 = i_up;
				}
			} else if ((F2 - eps) - b_up > 2 * tau) {
				optimality = 0;
				i1 = i_up;
				if (b_low - (F2 - eps) > (F2 - eps) - b_up) {
					i1 = i_low;
				}
			}
		}

		// case 4
		if (isI2(i2)) {
			if ((F2 + eps) - b_up > 2 * tau) {
				optimality = 0;
				i1 = i_up;
			}
		}

		// case 5
		if (isI3(i2)) {
			if (b_low - (F2 - eps) > 2 * tau) {
				optimality = 0;
				i1 = i_low;
			}
		}

		if (optimality == 1) {
			return 0;
		}

		return takeStep(i1, i2);
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

	void train() {
		int numChanged;
		int examineAll;
		int LoopCounter;
		int innerLoopSuccess;
		int i;
		numChanged = 0;
		examineAll = 1;
		LoopCounter = 0;

		while ((numChanged > 0 || examineAll == 1) && LoopCounter < 2000) {
			numChanged = 0;
			LoopCounter++;
			if (examineAll == 1) {
				// Loop over all training examples
				// IO.println("Iteration: "+(++LoopCounter)+"\tnumChanged: "+numChanged,4);
				IO.print("*", 2);
				for (i = 0; i < l; i++) {
					numChanged += examineExample(i);
				}
			} else {
				// Loop over examples where alpha is not 0 & not C
				IO.println("Iteration: " + (++LoopCounter) + "\tnumChanged: "
						+ numChanged, 4);
				/*
				 * IO.print(".",2); for(i=0; i<l; i++) { if(isI0(i)) {
				 * numChanged += examineExample(i);
				 * 
				 * // It is easy optimality on I0 is attained if(b_up > b_low -
				 * 2*tau) { numChanged = 0; break; } } }
				 */

				// This is the code for Modification 2 from Keerthi et al.'s
				// paper
				innerLoopSuccess = 1;
				numChanged = 0;
				while ((b_up < b_low - 2 * tau) && (innerLoopSuccess > 0)) {
					IO.print(":", 2);
					innerLoopSuccess = takeStep(i_up, i_low);
				}
			}

			if (examineAll == 1)
				examineAll = 0;
			else if (numChanged == 0)
				examineAll = 1;
		}

		b = (b_low + b_up) / 2.0;

		for (i = 0; i < l; i++) {
			if (Math.abs(alpha[i] - alpha_s[i]) > 0) {
				support++;
			}
			if (alpha[i] == C || alpha_s[i] == C) {
				atbound++;
			}
			error += (error_cache[i] - b) * (error_cache[i] - b);
		}

		error = Math.sqrt(error) / l;

		IO.print("\n", 2);
		IO.println("Number of support ArrayLists:    " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Support ArrayLists at bound:     " + atbound + "/" + l
				+ " (" + ((float) (100.0 * negativ) / l) + "%)", 2);
		IO.println("Cuadratic error:          " + error, 3);
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

	boolean isI0a(int i) {
		// return ((alpha[i] > 0) && (alpha[i] < C));
		return (index_type[i] == 'a');
	}

	boolean isI0b(int i) {
		// return ((alpha_s[i] > 0) && (alpha_s[i] < C));
		return (index_type[i] == 'b');
	}

	boolean isI0(int i) {
		return (isI0a(i) || isI0b(i));
	}

	boolean isI1(int i) {
		// return ((alpha[i] == 0) && (alpha_s[i] == 0));
		return (index_type[i] == '1');
	}

	boolean isI2(int i) {
		// return ((alpha[i] == 0) && (alpha_s[i] == C));
		return (index_type[i] == '2');
	}

	boolean isI3(int i) {
		// return ((alpha[i] == C) && (alpha_s[i] == 0));
		return (index_type[i] == '3');
	}

	boolean isNot(int i) {
		return alpha[i] > C || alpha[i] < 0 || alpha_s[i] > C || alpha[i] < 0;
	}

	void actualizeIndexType() {
		int i;

		for (i = 0; i < l; i++) {
			if ((alpha[i] > 0) && (alpha[i] < C))
				index_type[i] = 'a';
			if ((alpha_s[i] > 0) && (alpha_s[i] < C))
				index_type[i] = 'b';
			if ((alpha[i] == 0) && (alpha_s[i] == 0))
				index_type[i] = '1';
			if ((alpha[i] == 0) && (alpha_s[i] == C))
				index_type[i] = '2';
			if ((alpha[i] == C) && (alpha_s[i] == 0))
				index_type[i] = '3';
		}
	}

	public static void main(String[] argv) throws Exception {
		DataOutputStream fileout;
		Random r;

		fileout = new DataOutputStream(new FileOutputStream(
				"c:/svm/classes/data/regression1.dat"));
		r = new Random();

		fileout.writeBytes("SparseVector 2000\n");
		for (float x, y, count = 1; count <= 2000; count++) {
			// x = (float)(2.0*Math.PI*r.nextFloat());
			// x = (float)(r.nextFloat());
			x = (float) (10 * (2 * Math.PI * r.nextFloat() - 1));
			// y = 0.02f*x*x*x + 0.05f*x*x - x + (float)r.nextGaussian();
			y = 0.02f * x * x * x + 0.05f * x * x - x;
			// y = (float)r.nextGaussian();
			fileout.writeBytes(y + " 0:" + x + "\n");
		}

		fileout.close();
	}
}

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
package svm;

/**
 * <p>
 * \u00DCberschrift:
 * </p>
 * <p>
 * Beschreibung:
 * </p>
 * <p>
 * Copyright: Copyright (c)
 * </p>
 * <p>
 * Organisation:
 * </p>
 * 
 * @author unascribed
 * @version 1.0
 */

public class SvmTuner {
	Smo smo;
	double C;
	double sigma;
	double u1, u2;
	double R_squared;
	double norm_squared;
	double dC;
	double dsigma;
	double R_squared_dC;
	double norm_squared_C;
	double norm_squared_dsigma;
	double R_squared_dsigma;

	Kernel kernel;

	double[] error_cache;
	double C_old;
	double tau;
	double eps;

	double[] beta;
	double rho;
	int l;

	int support;
	int positiv;
	int negativ;
	int error;

	public SvmTuner(Kernel k, /* double nu, */double tau_, double eps_) {
		kernel = k;
		l = kernel.l;

		error_cache = new double[l];
		beta = new double[l];

		/*
		 * int l_nu = (int)(l*nu);
		 * 
		 * for(int i = 0; i < l_nu; i++) { beta[i] = 1/(l*nu); }
		 * 
		 * beta[l_nu] = 1.0 - l_nu/(l*nu);
		 * 
		 * for(int i = l_nu+1; i < l; i++) { beta[i] = 0.0; }
		 */
		for (int i = 0; i < l; i++) {
			beta[i] = 0.0;
		}

		rho = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < l; i++) {
			error_cache[i] = 0.0;
			for (int j = 0; j < l; j++) {
				error_cache[i] += beta[j] * kernel.value(i, j);
			}
			if (beta[i] > 0.0) {
				rho = Math.max(rho, error_cache[i]);
			}
		}

		/* C_old = 1/(nu*l); */
		C_old = Double.POSITIVE_INFINITY;
		tau = tau_;
		eps = eps_;
	}

	public SvmTuner(Smo smo) {
		this.smo = smo;
		kernel = smo.kernel;
		l = kernel.l;

		error_cache = new double[l];
		beta = new double[l];

		for (int i = 0; i < l; i++) {
			beta[i] = 0.0;
		}

		rho = 0.0;

		for (int i = 0; i < l; i++) {
			error_cache[i] = 0.0;
		}

		C_old = Double.POSITIVE_INFINITY;
		tau = smo.tau;
		eps = smo.eps;
	}

	int takeStep(int i1, int i2) {
		double beta1, beta2;
		double E1, E2;
		double L, H;
		double k11, k12, k22;
		double eta;
		double b1, b2;
		double delta;
		double Lobj, Hobj;
		double C1, C2;
		int k;

		// System.out.print("@");

		if (i1 == i2)
			return 0;

		/* Lagrange multiplier for i1 */
		beta1 = beta[i1];
		beta2 = beta[i2];

		E1 = error_cache[i1];
		E2 = error_cache[i2];

		delta = beta2 + beta1;

		/* Compute L, H */
		L = Math.max(0, delta - C_old);
		H = Math.min(C_old, delta);

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

		eta = k11 + k22 - 2.0 * k12;

		if (eta < 0) {
			b2 = beta2 + (E1 - E2) / eta;
			if (b2 < L)
				b2 = L;
			else if (b2 > H)
				b2 = H;
		} else {
			/* Objetive function at a2 = l, H */
			C1 = C2 = 0.0;
			for (k = 0; k < l; k++) {
				if (k != i1 && k != i2 && beta[k] > tau) {
					/*
					 * C1 += beta[k]*kernel.value(i1,k); C2 +=
					 * beta[k]*kernel.value(i2,k);
					 */
					C1 += beta[k] * kernel_i1[k];
					C2 += beta[k] * kernel_i2[k];
				}
			}

			Lobj = 0.5 * (delta - L) * (delta - L) * k11 + (delta - L) * L
					* k12 + 0.5 * L * L * k22 + (delta - L) * C1 + L * C2;
			Hobj = 0.5 * (delta - H) * (delta - H) * k11 + (delta - H) * H
					* k12 + 0.5 * H * H * k22 + (delta - H) * C1 + H * C2;
			if (Lobj > Hobj + eps)
				b2 = L;
			else if (Lobj < Hobj - eps)
				b2 = H;
			else
				b2 = beta2;
		}

		if (Math.abs(b2 - beta2) < eps * (b2 + beta2 + eps))
			return 0;

		b2 = (b2 < tau) ? 0 : b2;
		b2 = (b2 > C_old - tau) ? C_old : b2;

		b1 = delta - b2;

		double rho1, rho2, bold;

		bold = rho;

		rho1 = E1 + (b1 - beta1) * k11 + (b2 - beta2) * k12 + rho;
		if (b1 > tau && b1 < C_old - tau) {
			rho = rho1;
		} else {
			rho2 = E2 + (b1 - beta1) * k12 + (b2 - beta2) * k22 + rho;
			if (b2 > tau && b2 < C_old - tau) {
				rho = rho2;
			} else {
				rho = 0.5 * (rho1 + rho2);
			}
		}

		/* Update error cache using new Lagrange multipliers */

		for (k = 0; k < l; k++) {
			/*
			 * error_cache[k] += (b1 - beta1)*kernel.value(i1,k) + (b2 -
			 * beta2)*kernel.value(i2,k) + bold - b;
			 */
			error_cache[k] += (b1 - beta1) * kernel_i1[k] + (b2 - beta2)
					* kernel_i2[k] + bold - rho;
		}
		/* Store a1, b2 in alph array */
		beta[i1] = b1;
		beta[i2] = b2;

		return 1;
	}

	int examineExample(int i2) {
		double beta2;
		double E2, E1, tmp, tmax;
		// System.out.print("%");
		beta2 = beta[i2];

		/* SVM output on point[i2] - y2 */
		E2 = error_cache[i2];

		// beta2 violates the Karush-kuhn-Tucker conditions
		if ((E2 - rho) * beta2 > tau || (rho - E2) * (C_old - beta2) > tau) {
			/* Number of non-zero & non-C beta > 1 */
			int k, i1, irand;

			i1 = -1;
			tmax = 0.0;
			for (k = 0; k < l; k++) {
				if (beta[k] > tau && beta[k] < C_old - tau) {
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
				if (takeStep(i1, i2) == 1)
					return 1;
			}

			/* Loop over all non-zero non-C beta, starting at random */
			irand = (int) (l * Math.random());

			for (k = irand; k < l + irand; k++) {
				i1 = k % l;
				if (beta[i1] > tau && beta[i1] < C_old - tau) {
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
				/* Loop over examples where beta is not 0 & not C */
				for (i = 0; i < l; i++)
					if (beta[i] > tau && beta[i] < C_old - tau) {
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
			if (beta[i] > tau) {
				support++;
				// positiv += (target[i]==1)? 1 : 0;
				// negativ += (target[i]==-1)? 1 : 0;
			}
			// error += ((error_cache[i]+target[i])>tau)? 0 : 1;
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
	 * if(beta[i] > tau) sum += target[i]*beta[i]*kernel(point[i],x); return
	 * sum; }
	 */

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
		SvmTuner csvm;
		Kernel kernel;
		Data data = null;

		double C_old = 100.0;
		double eps = 0.001;

		int type = Kernel.RBF;
		int degree = 1;
		double factor = 0.01;
		double bias = 0.0;
		int size = 40;

		String filename = null;
		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
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
					C_old = atof(argv[i]);
					break;
				case 't':
					atof(argv[i]);
					break;
				case 'e':
					eps = atof(argv[i]);
					break;
				case 'i':
					filename = argv[i];
					break;
				case 'o':
					break;
				case 's':
					size = atoi(argv[i]);
					break;
				case 'v':
					IO.setVerbosity(atoi(argv[i]));
					break;
				default:
					System.err.print("unknown option\n");
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

		kernel = new Kernel(data.point, type, degree, factor, bias, size);
		csvm = new SvmTuner(kernel, C_old, eps);

		// System.out.println(csvm.toString()+"\n");

		csvm.train();

		// System.out.println("\n"+csvm.toString());
	}

}

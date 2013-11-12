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
 * �berschrift:
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
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

public class TuneSvm {

	public static void main(String[] argv) throws Exception {
		double C = 1;
		double tau = 1E-8;
		double eps = 0.001;

		int type = Kernel.RBF;
		int degree = 3;
		double factor = 1;
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
					C = Double.parseDouble(argv[i]);
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

		Data data = null;

		try {
			data = SparseVector.readData(filename);
		} catch (RuntimeException e) {
			data = StepFunction.readData(filename);
		}
		TSmo1 tsmo;
		CSmo1 csmo;
		Kernel kernel;
		String lab;
		double label[];
		double w, R, ws, Rs, wc, Rc, f, fold, fc, fs, u, v;
		int l, it = 0;

		l = data.l;
		label = new double[data.l];
		lab = data.label[0];
		for (i = 0; i < data.l; i++) {
			label[i] = (lab.equals(data.label[i])) ? 1.0 : -1.0;
		}

		f = 0;
		fold = 1;
		u = Math.log(C);
		// v = 1.0/((SparseVector) data.point[0]).length;
		v = Math.log(factor);
		while (Math.abs(f - fold) > 1e-5 * f) {
			C = Math.exp(u);
			factor = Math.exp(v);
			fold = f;
			it++;
			System.out.println("C=" + C);
			System.out.println("factor=" + factor);
			System.out.println("u=" + u);
			System.out.println("v=" + v);

			kernel = new Kernel(data.point, type, degree, factor, bias, size);
			csmo = new CSmo1(kernel, label, C, tau, eps);
			tsmo = new TSmo1(kernel, tau, eps);

			csmo.train();
			tsmo.train();

			w = csmo.getW();
			wc = csmo.getWC();
			ws = csmo.getWs();

			R = tsmo.getR();
			Rc = tsmo.getRC(C);
			Rs = tsmo.getRs();

			fc = Math.exp(u) * (wc * R + w * Rc) / l;
			fs = Math.exp(v) * (ws * R + w * Rs) / l;
			f = R * w / l;

			// u -= Math.exp(-1.0/it)*fc;
			// v -= Math.exp(-1.0/it)*fs;

			u -= fc;
			v -= fs;

			System.out.println("fc=" + fc);
			System.out.println("fs=" + fs);
			System.out.println("fold=" + fold);
			System.out.println("f=" + f);
			System.out.println("u=" + u);
			System.out.println("v=" + v);
			System.out.println("iteration=" + it);
			System.out.println();
		}
	}
}

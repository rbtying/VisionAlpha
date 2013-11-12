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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class RSvmModel {
	Kernel kernel;
	InnerProductSpace[] point;
	SparseVector[][] alpha;
	double[] b;
	int l;

	private int type;
	private int degree;
	private double factor;
	private double bias;

	int numLabels;
	String[] label;
	int[] numElements;
	int[] classStart;

	long runtime = -1;

	private int bIndex(int p, int n) {
		return (n - p + p * numLabels - p * (p + 1) / 2 - 1);
	}

	public void read(String filename) throws IOException {
		BufferedReader filein;
		String line;
		StringTokenizer st;
		int n, p, i;

		filein = new BufferedReader(new FileReader(filename));

		filein.readLine();
		filein.readLine();

		filein.readLine();
		type = IO.atoi(filein.readLine());

		filein.readLine();
		degree = IO.atoi(filein.readLine());

		filein.readLine();
		factor = IO.atof(filein.readLine());

		filein.readLine();
		bias = IO.atof(filein.readLine());

		filein.readLine();
		line = filein.readLine();
		st = new StringTokenizer(line, " \n\t\r\f");
		numLabels = st.countTokens();
		label = new String[numLabels];
		for (i = 0; i < numLabels; i++)
			label[i] = new String(st.nextToken());

		filein.readLine();
		line = filein.readLine();
		st = new StringTokenizer(line, " \n\t\r\f");
		numElements = new int[numLabels];
		for (i = 0; i < numLabels; i++)
			numElements[i] = atoi(st.nextToken());

		classStart = new int[numLabels];
		classStart[0] = 0;
		for (i = 1; i < numLabels; i++)
			classStart[i] = classStart[i - 1] + numElements[i - 1];

		for (i = 0, l = 0; i < numLabels; i++)
			l += numElements[i];

		filein.readLine();
		line = filein.readLine();
		st = new StringTokenizer(line, ",: \n\t\r\f");
		b = new double[numLabels * (numLabels - 1) / 2];
		for (i = 0; i < numLabels * (numLabels - 1) / 2; i++)
			b[i] = atof(st.nextToken());

		filein.readLine();
		alpha = new SparseVector[numLabels][numLabels];
		for (p = 0; p < numLabels - 1; p++) {
			for (n = p + 1; n < numLabels; n++) {
				alpha[p][n] = SparseVector.read(filein);
				alpha[n][p] = SparseVector.read(filein);
			}
		}

		point = new InnerProductSpace[l];

		String s = filein.readLine();
		if (s.equals("SparseVector")) {
			for (p = 0; p < l; p++) {
				point[p] = SparseVector.read(filein);
			}
		} else if (s.equals("StepFunction")) {
			for (p = 0; p < l; p++) {
				point[p] = StepFunction.read(filein);
			}
		}

		kernel = new Kernel(null, type, degree, factor, bias, -1);

		filein.close();
	}

	public String classify(InnerProductSpace x) {
		int p, n;
		int i;

		p = 0;
		n = numLabels - 1;

		for (i = 1; i < numLabels; i++) {

			if (decisionFunction(x, p, n) == 1)
				n--;
			else
				p++;
		}

		// System.out.println(p+","+n+" ");

		return label[p];
	}

	public int getIndexLabel(InnerProductSpace x) {
		int p, n;
		int i;

		p = 0;
		n = numLabels - 1;

		for (i = 1; i < numLabels; i++) {

			if (decisionFunction(x, p, n) == 1)
				n--;
			else
				p++;
		}

		// System.out.println(p+","+n+" ");

		return p;
	}

	public int getNumLabels() {
		return numLabels;
	}

	/*
	 * double sigmoid(double x) { //if(bipolar) // return
	 * 2.0/(1.0+Math.exp(-c*x)) - 1.0; //else return 1.0/(1.0+Math.exp(-x)); }
	 */

	public int decisionFunction(InnerProductSpace x, int p, int n) {
		double f_pn = 0.0;
		int i, j;

		f_pn -= b[bIndex(p, n)];

		for (j = classStart[p], i = 0; j < classStart[p] + numElements[p]
				&& i < alpha[p][n].length; j++)
			if (alpha[p][n].index[i] == j) {
				f_pn += alpha[p][n].value[i] * kernel.value(x, point[j]);
				i++;
			}

		for (j = classStart[n], i = 0; j < classStart[n] + numElements[n]
				&& i < alpha[n][p].length; j++)
			if (alpha[n][p].index[i] == j) {
				f_pn += alpha[n][p].value[i] * kernel.value(x, point[j]);
				i++;
			}

		return (f_pn > 0) ? 1 : -1;
	}

	@Override
	public String toString() {
		String s;

		s = "SvmModel[" + kernel.toString();

		s += "]";

		return s;
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	public static void main(String[] argv) throws Exception {
		SvmModel csvm;
		Data data = null;
		long runtime;
		int error = 0;
		String label = null;

		csvm = new SvmModel();
		csvm.read(argv[1]);

		try {
			data = SparseVector.readData(argv[0]);
		} catch (RuntimeException e) {

			data = StepFunction.readData(argv[0]);
		}

		runtime = -System.currentTimeMillis();
		for (int i = 0; i < data.l; i++) {
			label = csvm.classify(data.point[i]);
			if (!data.label[i].equals(label)) {
				System.out.println(data.label[i] + " : " + label);
				error++;
			}
		}
		runtime += System.currentTimeMillis();
		System.out.println("error: " + error + "/" + data.l + " ("
				+ (float) ((100.0 * error) / data.l) + "%)");
		System.out.println("runtime: " + (float) ((runtime) / 1000.0) + " Sec");
		System.out.println((float) ((1.0 * runtime) / data.l)
				+ " mSec./character");
		System.out.println((float) ((1000.0 * data.l) / runtime)
				+ " character/sec");
	}
}

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
 * Copyright: Copyright (c) <p>
 * Organisation: <p>
 * @author
 * @version 1.0
 */
package svm;

import java.io.*;
import java.net.*;
import java.util.*;

public class SvmModel {
	Kernel kernel;
	InnerProductSpace[] point;
	SparseVector[][] alpha;
	double[] b;
	double[] A;
	double[] B;
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

	double out[];
	double f;

	static boolean DAGClassifier = true;
	static boolean sigmoidClassifier = false;
	static boolean voteClassifier = false;

	private int bIndex(int p, int n) {
		return (n - p + p * numLabels - p * (p + 1) / 2 - 1);
	}

	public void read(String filename) throws IOException {
		if (filename != null) {
			InputStream st = getClass().getResourceAsStream(filename);
			if (st != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(st));
				read(br);
			} else {
				System.err.println("Could not find " + filename
						+ " in classpath");
				// System.exit(1);
			}
		}
	}

	public void read(BufferedReader filein) throws IOException {
		String line;
		StringTokenizer st;
		int n, p, i;

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
		for (i = 0; i < numLabels; i++) {
			label[i] = new String(st.nextToken());

		}
		filein.readLine();
		line = filein.readLine();
		st = new StringTokenizer(line, " \n\t\r\f");
		numElements = new int[numLabels];
		for (i = 0; i < numLabels; i++) {
			numElements[i] = atoi(st.nextToken());

		}
		classStart = new int[numLabels];
		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];

		}
		for (i = 0, l = 0; i < numLabels; i++) {
			l += numElements[i];

		}
		filein.readLine();
		line = filein.readLine();
		st = new StringTokenizer(line, ",: \n\t\r\f");
		b = new double[numLabels * (numLabels - 1) / 2];
		for (i = 0; i < numLabels * (numLabels - 1) / 2; i++) {
			b[i] = atof(st.nextToken());

		}
		line = filein.readLine();
		if (line.equals("SigmoidParameters:")) {
			st = new StringTokenizer(filein.readLine(), ",: \n\t\r\f");
			A = new double[numLabels * (numLabels - 1) / 2];
			B = new double[numLabels * (numLabels - 1) / 2];
			for (i = 0; i < numLabels * (numLabels - 1) / 2; i++) {
				A[i] = atof(st.nextToken());
				B[i] = atof(st.nextToken());
			}
			filein.readLine();
		}

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

		out = new double[numLabels];

		filein.close();
	}

	public void read(URL url) throws IOException {
		read(new BufferedReader(new InputStreamReader(url.openStream())));
		/*
		 * BufferedReader filein; String line; StringTokenizer st; int n, p, i,
		 * j, index;
		 * 
		 * filein = new BufferedReader(new InputStreamReader(url.openStream()));
		 * 
		 * filein.readLine(); filein.readLine();
		 * 
		 * filein.readLine(); type = IO.atoi(filein.readLine());
		 * 
		 * filein.readLine(); degree = IO.atoi(filein.readLine());
		 * 
		 * filein.readLine(); factor = IO.atof(filein.readLine());
		 * 
		 * filein.readLine(); bias = IO.atof(filein.readLine());
		 * 
		 * filein.readLine(); line = filein.readLine(); st = new
		 * StringTokenizer(line, " \n\t\r\f"); numLabels = st.countTokens();
		 * label = new String[numLabels]; for(i = 0; i < numLabels; i++) {
		 * label[i] = new String(st.nextToken());
		 * 
		 * } filein.readLine(); line = filein.readLine(); st = new
		 * StringTokenizer(line, " \n\t\r\f"); numElements = new int[numLabels];
		 * for(i = 0; i < numLabels; i++) { numElements[i] =
		 * atoi(st.nextToken());
		 * 
		 * } classStart = new int[numLabels]; classStart[0] = 0; for(i = 1; i <
		 * numLabels; i++) { classStart[i] = classStart[i - 1] + numElements[i -
		 * 1];
		 * 
		 * } for(i = 0, l = 0; i < numLabels; i++) { l += numElements[i];
		 * 
		 * } filein.readLine(); line = filein.readLine(); st = new
		 * StringTokenizer(line, ",: \n\t\r\f"); b = new double[numLabels *
		 * (numLabels - 1) / 2]; for(i = 0; i < numLabels * (numLabels - 1) / 2;
		 * i++) { b[i] = atof(st.nextToken());
		 * 
		 * } filein.readLine(); alpha = new SparseVector[numLabels][numLabels];
		 * for(p = 0; p < numLabels - 1; p++) { for(n = p + 1; n < numLabels;
		 * n++) { alpha[p][n] = SparseVector.read(filein); alpha[n][p] =
		 * SparseVector.read(filein); } }
		 * 
		 * point = new InnerProductSpace[l];
		 * 
		 * String s = filein.readLine(); if(s.equals("SparseVector")) { for(p =
		 * 0; p < l; p++) { point[p] = SparseVector.read(filein); } } else
		 * if(s.equals("StepFunction")) { for(p = 0; p < l; p++) { point[p] =
		 * StepFunction.read(filein); } }
		 * 
		 * kernel = new Kernel(null, type, degree, factor, bias, -1);
		 * 
		 * out = new double[numLabels];
		 * 
		 * filein.close();
		 */
	}

	public String classify(InnerProductSpace x) {
		if (SvmModel.DAGClassifier) {
			return this.classifyDAG(x);
		} else if (SvmModel.voteClassifier) {
			return this.classifyVote(x);
		} else if (SvmModel.sigmoidClassifier) {
			return this.classifySigmoid(x);
		}

		return "no_name";
	}

	public String classifyDAG(InnerProductSpace x) {
		int p, n;
		int i;

		p = 0;
		n = numLabels - 1;

		for (i = 1; i <= numLabels - 1; i++) {
			// f = this.sigmoidOutput(x, p, n);
			// if(f >= 0.5) {
			f = this.output(x, p, n);
			if (f >= 0.0) {
				n--;
			} else {
				p++;
			}
		}

		return label[p];
	}

	public String classifySigmoid(InnerProductSpace x) {
		double aux;
		int p, n;

		for (n = 0; n < numLabels; n++) {
			out[n] = 0;
		}

		// System.err.print(((SparseVector)x).name);
		for (p = 0; p < numLabels; p++) {
			for (n = p + 1; n < numLabels; n++) {
				aux = this.sigmoidOutput(x, p, n);
				/*
				 * if(aux != 1) { out[n] += Math.log(1.0 - aux)/Math.E; } else {
				 * out[n] += -200; } if(aux != 0) { out[p] +=
				 * Math.log(aux)/Math.E; } else { out[p] += -200; }
				 */
				out[n] += 1.0 - aux;
				out[p] += aux;
				// System.err.print(" "+(count++)+":"+aux);
			}
		}

		// System.err.println();

		aux = Double.NEGATIVE_INFINITY;
		p = -1;
		for (n = 0; n < numLabels; n++) {
			out[n] /= numLabels;
			if (out[n] >= aux) {
				aux = out[n];
				p = n;
			}
		}

		/*
		 * out[p] = Double.NEGATIVE_INFINITY; for(n = 0; n < numLabels; n++) {
		 * //System.out.print(out[n]+ " "); if(out[n] >= aux2) { aux2 = out[n];
		 * } }
		 */

		// System.out.println(label[p]+":"+"\n"+aux+" "+aux2+" "+(aux-aux2)+"\n");

		return label[p];
	}

	public String classifyVote(InnerProductSpace x) {
		double aux;
		int p, n;

		for (n = 0; n < numLabels; n++) {
			out[n] = 0;
		}

		for (p = 0; p < numLabels; p++) {
			for (n = p + 1; n < numLabels; n++) {
				aux = this.decisionFunction(x, p, n);
				out[n] += -aux;
				out[p] += aux;
			}
		}

		aux = Double.NEGATIVE_INFINITY;
		p = -1;
		for (n = 0; n < numLabels; n++) {
			out[n] /= numLabels;
			if (out[n] > aux) {
				aux = out[n];
				p = n;
			}
		}

		return label[p];
	}

	public int maxnumSupport() {
		int p, n;
		int i, max;

		p = 0;
		n = numLabels - 1;
		max = 0;
		for (i = 1; i < numLabels; i++) {
			max += this.numSupport(p, n);
			if (this.numSupport(p, n - 1) > this.numSupport(p + 1, n)) {
				n--;
			} else {
				p++;
			}
		}

		return max;
	}

	public int minnumSupport() {
		int p, n;
		int i, min;

		p = 0;
		n = numLabels - 1;
		min = 0;
		for (i = 1; i < numLabels; i++) {
			min += this.numSupport(p, n);
			System.out
					.println("numLabels:" + numLabels + " p:" + p + " n:" + n);
			if (this.numSupport(p, n - 1) < this.numSupport(p + 1, n)) {
				n--;
			} else {
				p++;
			}
		}
		System.out.println("numLabels:" + numLabels + " p:" + p + " n:" + n);

		return min;
	}

	public int getIndexLabel(InnerProductSpace x) {
		int p, n;
		int i;

		p = 0;
		n = numLabels - 1;

		for (i = 1; i < numLabels; i++) {

			if (decisionFunction(x, p, n) == 1) {
				n--;
			} else {
				p++;
			}
		}

		return p;
	}

	public int getNumLabels() {
		return numLabels;
	}

	public int decisionFunction(InnerProductSpace x, int p, int n) {
		double f_pn = 0.0;
		int i, j;

		f_pn -= b[bIndex(p, n)];

		for (j = classStart[p], i = 0; j < classStart[p] + numElements[p]
				&& i < alpha[p][n].length; j++) {
			if (alpha[p][n].index[i] == j) {
				f_pn += alpha[p][n].value[i] * kernel.value(x, point[j]);
				i++;
			}
		}

		for (j = classStart[n], i = 0; j < classStart[n] + numElements[n]
				&& i < alpha[n][p].length; j++) {
			if (alpha[n][p].index[i] == j) {
				f_pn += alpha[n][p].value[i] * kernel.value(x, point[j]);
				i++;
			}
		}

		return (f_pn >= 0) ? 1 : -1;
	}

	public double output(InnerProductSpace x, int p, int n) {
		double f_pn = 0.0;
		int i, j;

		f_pn -= b[bIndex(p, n)];

		for (j = classStart[p], i = 0; j < classStart[p] + numElements[p]
				&& i < alpha[p][n].length; j++) {
			if (alpha[p][n].index[i] == j) {
				f_pn += alpha[p][n].value[i] * kernel.value(x, point[j]);
				i++;
			}
		}

		for (j = classStart[n], i = 0; j < classStart[n] + numElements[n]
				&& i < alpha[n][p].length; j++) {
			if (alpha[n][p].index[i] == j) {
				f_pn += alpha[n][p].value[i] * kernel.value(x, point[j]);
				i++;
			}
		}

		return f_pn;
	}

	public double sigmoidOutput(InnerProductSpace x, int p, int n) {
		int i = bIndex(p, n);
		return 1.0 / (1.0 + Math.exp(output(x, p, n) * A[i] + B[i]));
	}

	public int numSupport(int p, int n) {
		if (p == n) {
			return 0;
		}
		return (alpha[p][n].length + alpha[n][p].length);
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
		String modelfilename = null;
		String datafilename = null;

		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-') {
					break;
				}
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'd':
					SvmModel.DAGClassifier = true;
					SvmModel.voteClassifier = false;
					SvmModel.sigmoidClassifier = false;
					break;
				case 'i':
					datafilename = argv[i];
					break;
				case 'm':
					modelfilename = argv[i];
					break;
				case 's':
					SvmModel.DAGClassifier = false;
					SvmModel.voteClassifier = false;
					SvmModel.sigmoidClassifier = true;
					break;
				case 'v':
					SvmModel.DAGClassifier = false;
					SvmModel.voteClassifier = true;
					SvmModel.sigmoidClassifier = false;
					break;
				default:
					System.err.print("unknown option: " + argv[i - 1].charAt(1)
							+ "");
					showHelp();
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.err.print(e.toString());
			System.exit(1);
		}

		SvmModel csvm;
		Data data = null;
		long runtime;
		int error = 0;
		String label = null;

		csvm = new SvmModel();
		csvm.read(modelfilename);

		try {
			data = SparseVector.readData(datafilename);
		} catch (RuntimeException e) {
			data = StepFunction.readData(argv[0]);
		}

		runtime = -System.currentTimeMillis();
		for (i = 0; i < data.l; i++) {
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

	static void showHelp() {
	}
}

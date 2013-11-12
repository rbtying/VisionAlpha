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

import java.io.*;

public class SvmTrain {
	InnerProductSpace[] point;
	public String dataType;
	double[][] alpha;
	double[] b;
	double[] A;
	double[] B;
	int l;
	static boolean sigmoidTraining = true;

	double C;
	double tau;
	double eps;
	int iterations;

	private int type;
	private int degree;
	private double factor;
	private double bias;

	private int size;

	private int evaluations = 0;

	int numLabels;
	String[] label;
	int[] numElements;
	int[] classStart;
	int[] indexClass;
	int[] numSupport;
	boolean[] isSupport;

	int support = 0;

	long runtime = -1;

	boolean[][] linearSeparable;
	int minSupport;
	int maxSupport;

	Data test = null;

	public SvmTrain(Data data, double C_, double tau_, double eps_, int type_,
			int degree_, double factor_, double bias_, int size_, int it_) {
		int i, j;
		int maxLabels = 10;

		l = data.l;

		label = new String[maxLabels];
		numElements = new int[maxLabels];

		indexClass = new int[l];

		numLabels = 0;
		System.out.println("numLabels = 0;");
		for (i = 0; i < l; i++) {
			String labelTaked = data.label[i];

			for (j = 0; j < numLabels; j++)
				if (labelTaked.equals(label[j]))
					break;

			if (j == numLabels) {
				if (j == maxLabels) {
					maxLabels += maxLabels;
					String[] ArrayList = new String[maxLabels];
					System.arraycopy(label, 0, ArrayList, 0, label.length);
					label = ArrayList;
					int[] iArrayList = new int[maxLabels];
					System.arraycopy(numElements, 0, iArrayList, 0,
							numElements.length);
					numElements = iArrayList;
				}
				label[j] = labelTaked;
				numLabels++;
			}

			indexClass[i] = j;
			numElements[j]++;
		}

		classStart = new int[numLabels];
		point = new InnerProductSpace[l];

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		for (i = 0; i < l; i++) {
			point[classStart[indexClass[i]]] = data.point[i];
			++classStart[indexClass[i]];
		}

		classStart[0] = 0;
		for (i = 1; i < numLabels; i++) {
			classStart[i] = classStart[i - 1] + numElements[i - 1];
		}

		dataType = data.type;

		C = C_;
		tau = tau_;
		eps = eps_;
		size = size_;

		type = type_;
		degree = degree_;
		factor = factor_;
		bias = bias_;

		size = size_;

		iterations = it_;
	}

	public void train() {
		Smo1 smo;
		Kernel kernel;
		InnerProductSpace[] nodePoint;
		double[] nodeTarget;
		int p, n, h;
		int numnodes, count;

		numnodes = count = numLabels * (numLabels - 1) / 2;

		b = new double[numnodes];
		alpha = new double[numLabels][l];

		if (sigmoidTraining) {
			A = new double[numnodes];
			B = new double[numnodes];
		}

		evaluations = 0;
		runtime = -System.currentTimeMillis();
		linearSeparable = new boolean[numLabels][numLabels];
		for (p = 0; p < numLabels - 1; p++) {
			for (n = p + 1; n < numLabels; n++) {

				nodePoint = new InnerProductSpace[numElements[p]
						+ numElements[n]];
				nodeTarget = new double[numElements[p] + numElements[n]];

				for (h = 0; h < numElements[p]; h++) {
					nodePoint[h] = point[classStart[p] + h];
					nodeTarget[h] = 1.0;
				}

				for (h = 0; h < numElements[n]; h++) {
					nodePoint[numElements[p] + h] = point[classStart[n] + h];
					nodeTarget[numElements[p] + h] = -1.0;
				}

				IO.println(
						"Nodes to be trained: " + (count--) + "/" + numnodes, 1);
				IO.println("Training Class '" + label[p] + "' vs. '" + label[n]
						+ ": ", 1);

				kernel = new Kernel(nodePoint, type, degree, factor, bias, size);
				smo = new Smo1(kernel, nodeTarget, C, tau, eps, iterations);

				smo.train();

				// System.arraycopy(smo.alpha,0,alpha[j],classStart[i],numElements[i]);
				// System.arraycopy(smo.alpha,numElements[i],alpha[i],classStart[j],numElements[j]);

				System.arraycopy(smo.alpha, 0, alpha[n], classStart[p],
						numElements[p]);
				System.arraycopy(smo.alpha, numElements[p], alpha[p],
						classStart[n], numElements[n]);

				b[bIndex(p, n)] = smo.b;
				evaluations += kernel.getEvaluations();
				linearSeparable[p][n] = smo.error == 0;

				if (sigmoidTraining) {
					if (this.test == null) {
						smo.trainSigmoidParameters();
					} else {
						nodePoint = new InnerProductSpace[test.numElements[p]
								+ test.numElements[n]];
						nodeTarget = new double[test.numElements[p]
								+ test.numElements[n]];

						for (h = 0; h < test.numElements[p]; h++) {
							nodePoint[h] = test.point[test.classStart[p] + h];
							nodeTarget[h] = 1.0;
						}

						for (h = 0; h < test.numElements[n]; h++) {
							nodePoint[test.numElements[p] + h] = test.point[test.classStart[n]
									+ h];
							nodeTarget[test.numElements[p] + h] = -1.0;
						}

						smo.trainSigmoidParameters(nodePoint, nodeTarget);
					}

					A[bIndex(p, n)] = smo.A;
					B[bIndex(p, n)] = smo.B;
				}

				IO.println("\n", 1);
			}
		}
		runtime += System.currentTimeMillis();

		numSupport = new int[numLabels];
		isSupport = new boolean[l];
		for (p = 0; p < numLabels; p++) {
			numSupport[p] = 0;
			for (n = classStart[p]; n < classStart[p] + numElements[p]; n++) {
				isSupport[n] = false;
				for (h = 0; h < numLabels; h++)
					if (alpha[h][n] > tau) {
						numSupport[p]++;
						isSupport[n] = true;
						break;
					}
			}
			support += numSupport[p];
		}

		IO.println("\n", 1);
		IO.println("Total support ArrayLists:        " + support + "/" + l
				+ " (" + ((float) (100.0 * support) / l) + "%)", 1);
		IO.println("Training time in seconds:     " + (runtime / 1000.0), 2);
		IO.println("Total kernel evaluations:     " + evaluations, 3);
		IO.println("\nNon-linear separable classes:", 3);
		for (p = 0; p < numLabels - 1; p++) {
			for (n = p + 1; n < numLabels; n++) {
				if (!linearSeparable[p][n])
					IO.println(label[p] + " vs. " + label[n], 3);
			}
		}

	}

	private int bIndex(int p, int n) {
		return (n - p + p * numLabels - p * (p + 1) / 2 - 1);
	}

	public void writeModel(String filename) throws IOException {
		int n, p, i, j, index;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		fileout.writeBytes("ALGORITHM:\n" + this.toString() + "\n");
		fileout.writeBytes("KERNEL:\n" + type + "\n");
		fileout.writeBytes("DEGREE:\n" + degree + "\n");
		fileout.writeBytes("FACTOR:\n" + factor + "\n");
		fileout.writeBytes("BIAS:\n" + bias + "\n");

		fileout.writeBytes("LABELS: " + numLabels + "\n");

		for (i = 0; i < numLabels; i++)
			fileout.writeBytes(label[i] + " ");

		fileout.writeBytes("\nNUMSUPPORT: " + support + "\n");
		for (i = 0; i < numLabels; i++)
			fileout.writeBytes(numSupport[i] + " ");

		fileout.writeBytes("\nTHRESHOLD:\n");
		for (i = 0; i < numLabels * (numLabels - 1) / 2; i++)
			fileout.writeBytes((float) b[i] + " ");
		fileout.writeBytes("\n");

		if (sigmoidTraining) {
			fileout.writeBytes("SigmoidParameters:\n");
			for (i = 0; i < numLabels * (numLabels - 1) / 2; i++) {
				fileout.writeBytes(A[i] + " " + B[i] + " ");
			}
			fileout.writeBytes("\n");
		}

		fileout.writeBytes("ALPHA:\n");
		for (p = 0; p < numLabels - 1; p++) {
			for (n = p + 1; n < numLabels; n++) {

				fileout.writeBytes(".");
				index = 0;
				for (i = 0; i < p; i++) {
					index += numSupport[i];
				}
				index--;
				for (j = classStart[p]; j < classStart[p] + numElements[p]; j++) {
					for (i = 0; i < numLabels; i++) {
						if (alpha[i][j] > tau) {
							index++;
							break;
						}
					}
					if (alpha[n][j] > tau) {
						fileout.writeBytes(" " + index + ":"
								+ (float) alpha[n][j]);
					}
				}
				fileout.writeBytes("\n");

				fileout.writeBytes(".");
				index = 0;
				for (i = 0; i < n; i++) {
					index += numSupport[i];
				}
				index--;
				for (j = classStart[n]; j < classStart[n] + numElements[n]; j++) {
					for (i = 0; i < numLabels; i++) {
						if (alpha[i][j] > tau) {
							index++;
							break;
						}
					}
					if (alpha[p][j] > tau) {
						fileout.writeBytes(" " + index + ":"
								+ (float) (-alpha[p][j]));
					}
				}
				fileout.writeBytes("\n");
			}
		}

		fileout.writeBytes(dataType + "\n");
		for (p = 0; p < l; p++) {
			if (isSupport[p]) {
				this.point[p].write(fileout);
			}
		}

		fileout.close();
	}

	public void writeSupportArrayLists(String filename) throws IOException {
		int p;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		for (p = 0; p < l; p++) {
			if (isSupport[p]) {
				this.point[p].write(fileout);
			}
		}

		fileout.close();
	}

	public void writeOldModel(String filename) throws IOException {
		int i, j;
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(filename));

		fileout.writeBytes("KERNEL:\n" + type + "\n");
		fileout.writeBytes("DEGREE:\n" + degree + "\n");
		fileout.writeBytes("FACTOR:\n" + factor + "\n");
		fileout.writeBytes("GAMMA:\n" + factor + "\n");
		fileout.writeBytes("BIAS:\n" + bias + "\n");

		fileout.writeBytes("NumLabels:\n" + numLabels + "\n");

		fileout.writeBytes("Labels:\n");

		for (i = 0; i < numLabels; i++)
			fileout.writeBytes(label[i] + " ");

		fileout.writeBytes("\nNumSupport:\n");
		for (i = 0; i < numLabels; i++)
			fileout.writeBytes(numSupport[i] + " ");

		fileout.writeBytes("\nThreshold:\n");
		for (i = 0; i < numLabels * (numLabels - 1) / 2; i++)
			fileout.writeBytes(b[i] + " ");
		fileout.writeBytes("\n");

		if (sigmoidTraining) {
			fileout.writeBytes("SigmoidParameters:\n");
			for (i = 0; i < numLabels * (numLabels - 1) / 2; i++)
				fileout.writeBytes(A[i] + " " + B[i] + " ");
			fileout.writeBytes("\n");
		}

		/*
		 * for(i = 0; i < numLabels; i++) { for(j = 0; j < numLabels; j++)
		 * fileout.writeBytes(b[i][j]+" "); fileout.writeBytes("\n"); }
		 */

		fileout.writeBytes("SupportArrayLists:\n");

		for (i = 0; i < l; i++) {
			boolean isSupport = false;
			for (j = 0; j < numLabels; j++)
				if (alpha[j][i] > tau) {
					isSupport = true;
					break;
				}

			// boolean isSupport = true;
			for (j = 0; isSupport && j < numLabels; j++) {
				fileout.writeBytes(((alpha[j][i] > tau) ? ((float) alpha[j][i])
						: 0) + " ");
			}

			SparseVector p = (SparseVector) point[i];
			for (j = 0; isSupport && j < p.length; j++)
				fileout.writeBytes(p.index[j] + ":" + (float) p.value[j] + " ");

			if (isSupport)
				fileout.writeBytes("\n");
		}

		fileout.close();
	}

	/*
	 * public void load(String filename) throws IOException { BufferedReader
	 * filein = new BufferedReader(new FileReader(filename)); String line;
	 * StringTokenizer st; int i, j, k;
	 * 
	 * parameters = new Parameters();
	 * 
	 * filein.readLine(); parameters.type = atoi(filein.readLine());
	 * filein.readLine(); parameters.degree = atof(filein.readLine());
	 * filein.readLine(); parameters.factor = atof(filein.readLine());
	 * filein.readLine(); parameters.gamma = atof(filein.readLine());
	 * filein.readLine(); parameters.bias = atof(filein.readLine());
	 * 
	 * filein.readLine(); numLabels = atoi(filein.readLine());
	 * 
	 * filein.readLine(); line = filein.readLine(); st = new
	 * StringTokenizer(line,",: \n\t\r\f"); label = new String[numLabels]; for(i
	 * = 0; i < numLabels; i++) label[i] = new String(st.nextToken());
	 * 
	 * filein.readLine(); line = filein.readLine(); st = new
	 * StringTokenizer(line,",: \n\t\r\f"); numElements = new int[numLabels];
	 * for(i = 0; i < numLabels; i++) numElements[i] = atoi(st.nextToken());
	 * 
	 * classStart = new int[numLabels]; classStart[0] = 0; for(i = 1; i <
	 * numLabels; i++) classStart[i] = classStart[i-1]+numElements[i-1];
	 * 
	 * filein.readLine(); line = filein.readLine(); st = new
	 * StringTokenizer(line,",: \n\t\r\f"); b = new double[numLabels*(numLabels
	 * - 1)/2]; for(i = 0; i < numLabels*(numLabels - 1)/2; i++) b[i] =
	 * atof(st.nextToken());
	 * 
	 * //filein.readLine(); //b = new double[numLabels][numLabels]; //for(i = 0;
	 * i < numLabels; i++) { // line = filein.readLine(); // st = new
	 * StringTokenizer(line,",: \n\t\r\f"); // for(j = 0; j < numLabels; j++) //
	 * b[i][j] = atof(st.nextToken()); //}
	 * 
	 * filein.readLine();
	 * 
	 * l = 0; for(i = 0; i < numLabels; i++) l += numElements[i];
	 * 
	 * alpha = new double[numLabels][l]; point = new Data[l][];
	 * 
	 * j = 0; while((line = filein.readLine())!= null) { st = new
	 * StringTokenizer(line,",: \n\t\r\f"); for(i = 0; i < numLabels; i++) {
	 * alpha[i][j] = atof(st.nextToken()); //System.out.print(alpha[i][j]+" ");
	 * } //System.out.println(); int dim = st.countTokens()/2;
	 * 
	 * point[j] = new Data[dim]; for(k = 0; k < dim; k++) { point[j][k] = new
	 * Data(); point[j][k].index = atoi(st.nextToken()); point[j][k].value =
	 * atof(st.nextToken()); } j++; }
	 * 
	 * filein.close(); }
	 */
	/*
	 * public String classify(Data[] x) { double f_pn = 0.0; int p, n; int i, j;
	 * 
	 * p = 0; n = numLabels - 1;
	 * 
	 * for(i = 1, j = 0; i < numLabels; i++) {
	 * 
	 * if(decisionFunction(x,p,n) == 1) n--; else p++; }
	 * 
	 * //System.out.println(p+","+n+" ");
	 * 
	 * return label[p]; }
	 */
	/*
	 * public String classifyRandom(Data[] x) { double f_pn = 0.0; int[] p, n;
	 * int i, j, k;
	 * 
	 * p = new int[numLabels];
	 * 
	 * 
	 * for(i = 0; i < numLabels; i++) { p[i] = i; }
	 * 
	 * int rnd; String lab; for(i = 0; i < numLabels; i++) { rnd =
	 * (int)(numLabels*Math.random()); k = p[i]; p[i] = p[rnd]; p[rnd] = k; lab
	 * = new String(label[rnd]); label[i] = new String(label[rnd]); label[rnd] =
	 * new String(lab); }
	 * 
	 * System.out.println(); for(i = 0; i < numLabels; i++) {
	 * System.out.println(i+","+p[i]+" "); }
	 * 
	 * System.out.println(); for(k = 1, i = 0, j = numLabels - 1; k < numLabels;
	 * k++) { System.out.println(p[i]+" "+p[j]);
	 * if(decisionFunction(x,p[i],p[j]) == 1) j--; else i++; }
	 * System.out.println(p[i]+" "+p[j]); //System.out.println(p+","+n+" ");
	 * 
	 * return label[i]; }
	 */
	/*
	 * public String classify(int[] x) { double f_pn = 0.0; int p, n; int i, j;
	 * 
	 * p = 0; n = numLabels - 1;
	 * 
	 * for(i = 1; i < numLabels; i++) {
	 * 
	 * if(decisionFunction(x,p,n) == 1) n--; else p++; }
	 * 
	 * //System.out.println(p+","+n+" ");
	 * 
	 * return label[p]; }
	 */
	/*
	 * public String classify(double[] x) { double f_pn = 0.0; int p, n; int i,
	 * j;
	 * 
	 * p = 0; n = numLabels - 1;
	 * 
	 * for(i = 1; i < numLabels; i++) {
	 * 
	 * if(decisionFunction(x,p,n) == 1) n--; else p++; }
	 * 
	 * //System.out.println(p+","+n+" ");
	 * 
	 * return label[p]; }
	 */
	/*
	 * public String vote(double[] x) { double max; int max_i; double[][] f =
	 * new double[numLabels][numLabels]; double aux;
	 * 
	 * for(int i = 0; i<numLabels; i++) { for(int j = i+1; j < numLabels; j++) {
	 * f[i][j] += decisionFunction(x,i,j); } }
	 * 
	 * double[] v = new double[numLabels];
	 * 
	 * for(int i = 0; i<numLabels; i++) { v[i] = 0; for(int j = 0; j <
	 * numLabels; j++) { if(i < j) { v[i] += sigmoid(f[i][j]); } else if(i > j)
	 * { v[i] += sigmoid(-f[j][i]); } } }
	 * 
	 * max = v[0]; max_i = 0; for(int i = 0; i<numLabels; i++) {
	 * //System.err.println(label[i]+": "+f[i]); if(v[i] > max) { max = v[i];
	 * max_i = i; } }
	 * 
	 * return label[max_i]; }
	 */
	/*
	 * public String vote(Data[] x) { double max; int max_i; double[][] f = new
	 * double[numLabels][numLabels]; double aux;
	 * 
	 * for(int i = 0; i<numLabels; i++) { for(int j = i+1; j < numLabels; j++) {
	 * f[i][j] += decisionFunction(x,i,j); } }
	 * 
	 * double[] v = new double[numLabels];
	 * 
	 * for(int i = 0; i<numLabels; i++) { v[i] = 0; for(int j = 0; j <
	 * numLabels; j++) { if(i < j) { v[i] += sigmoid(f[i][j]); } else if(i > j)
	 * { v[i] += sigmoid(-f[j][i]); } } }
	 * 
	 * max = v[0]; max_i = 0; for(int i = 0; i<numLabels; i++) {
	 * //System.err.println(label[i]+": "+f[i]); if(v[i] > max) { max = v[i];
	 * max_i = i; } }
	 * 
	 * return label[max_i]; }
	 */
	/*
	 * double sigmoid(double x) { //if(bipolar) // return
	 * 2.0/(1.0+Math.exp(-c*x)) - 1.0; //else return 1.0/(1.0+Math.exp(-x)); }
	 */
	/*
	 * public int decisionFunction(Data[] x, int p, int n) { double f_pn = 0.0;
	 * int i, j;
	 * 
	 * //if(p == n) // return 0; //else if(p>n) { // int _ = p; // p = n; // n =
	 * _; //}
	 * 
	 * f_pn -= b[bIndex(p,n)];
	 * 
	 * for(j = classStart[p]; j < classStart[p]+numElements[p]; j++)
	 * if(alpha[n][j] > 0.0) f_pn +=
	 * alpha[n][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * for(j = classStart[n]; j < classStart[n]+numElements[n]; j++)
	 * if(alpha[p][j] > 0.0) f_pn -=
	 * alpha[p][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * return (f_pn>0)? 1 : -1; }
	 */
	/*
	 * public int decisionFunction(int[] x, int p, int n) { double f_pn = 0.0;
	 * int i, j;
	 * 
	 * if(p == n) return 0; else if(p>n) { int _ = p; p = n; n = _; }
	 * 
	 * f_pn -= b[bIndex(p,n)];
	 * 
	 * for(j = classStart[p]; j < classStart[p]+numElements[p]; j++)
	 * if(alpha[n][j] > 0.0) f_pn +=
	 * alpha[n][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * for(j = classStart[n]; j < classStart[n]+numElements[n]; j++)
	 * if(alpha[p][j] > 0.0) f_pn -=
	 * alpha[p][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * return (f_pn>0)? 1 : -1; }
	 */
	/*
	 * public int decisionFunction(double[] x, int p, int n) { double f_pn =
	 * 0.0; int i, j;
	 * 
	 * if(p == n) return 0; else if(p>n) { int _ = p; p = n; n = _; }
	 * 
	 * f_pn -= b[bIndex(p,n)];
	 * 
	 * for(j = classStart[p]; j < classStart[p]+numElements[p]; j++)
	 * if(alpha[n][j] > 0.0) f_pn +=
	 * alpha[n][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * for(j = classStart[n]; j < classStart[n]+numElements[n]; j++)
	 * if(alpha[p][j] > 0.0) f_pn -=
	 * alpha[p][j]*Kernel.value(x,point[j],parameters);
	 * 
	 * return (f_pn>0)? 1 : -1; }
	 */

	@Override
	public String toString() {
		String s;

		s = "SvmTrain["
				+ (new Kernel(null, type, degree, factor, bias, size))
						.toString();
		s += ", C=" + C + ", tau=" + tau + ", eps=" + eps;
		if (support > 0) {
			s += ", support_ArrayLists=" + support + "/" + l + " ("
					+ ((float) (100.0 * support) / l) + "%)";
		}
		if (runtime > 0) {
			s += ", runtime=" + (runtime / 1000.0) + " Sec";
		}
		if (evaluations > 0) {
			s += ", kernel_evaluations=" + evaluations;
		}

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
		SvmTrain csvm;
		Data data = null;

		double C = 100.0;
		double tau = 1E-8;
		double eps = 0.001;

		int type = Kernel.RBF;
		int degree = 2;
		double factor = 0.01;
		double bias = 0.0;
		int size = 40;
		int iterations = 100;

		String filename = null;
		String filenameout = null;
		String filenametest = null;
		boolean sv = true;
		int i;
		try {
			for (i = 0; i < argv.length; i++) {
				if (argv[i].charAt(0) != '-')
					break;
				++i;
				switch (argv[i - 1].charAt(1)) {
				case 'b':
					bias = atof(argv[i]);
					break;
				case 'c':
					C = atof(argv[i]);
					break;
				case 'd':
					degree = atoi(argv[i]);
					break;
				case 'e':
					eps = atof(argv[i]);
					break;
				case 'f':
					factor = atof(argv[i]);
					break;
				case 'g':
					factor = atof(argv[i]);
					break;
				case 'h':
					showHelp();
					System.exit(0);
					break;
				case 'i':
					filename = argv[i];
					break;
				case 'I':
					iterations = Integer.parseInt(argv[i]);
					break;
				case 'k':
					type = atoi(argv[i]);
					break;
				case 'o':
					filenameout = argv[i];
					break;
				case 'O':
					filenameout = argv[i];
					break;
				case 's':
					size = atoi(argv[i]);
					break;
				case 'S':
					sigmoidTraining = true;
					break;
				case 't':
					tau = atof(argv[i]);
					break;
				case 'T':
					filenametest = argv[i];
					break;
				case 'v':
					IO.setVerbosity(atoi(argv[i]));
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

		// try {
		data = SparseVector.readData(filename);
		// }
		// catch(RuntimeException e) {
		// data = StepFunction.readData(filename);
		// }

		csvm = new SvmTrain(data, C, tau, eps, type, degree, factor, bias,
				size, iterations);

		if (filenametest != null) {
			csvm.test = Data.group(SparseVector.readData(filenametest),
					csvm.label);
		}
		// System.out.println(csvm.toString()+"\n");

		csvm.train();

		if (filenameout != null) {
			csvm.writeModel(filenameout);
			// csvm.writeOldModel(filenameout+".old");
		} else {
			csvm.writeModel(filename + ".mod");
			// csvm.writeModel(filename+".mod.old");
		}

		if (sv) {
			csvm.writeSupportArrayLists(filenameout + ".dat");
		}

		// System.out.println("\n"+csvm.toString());
	}

	static void showHelp() {
		String str = "";

		str += "usage: SvmTrain [options] -i inputfile -o outputfile\n\n";
		str += "Kernel options:\n";
		str += "\t-b bias\n";
		str += "\t-t kernel type: 0 = LIN, 1 = POL, 2 = RBF, 3 = TAN\n";

		System.out.println(str);
	}
}

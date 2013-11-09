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

public class Kernel {
	static final int LIN = 0;
	static final int POL = 1;
	static final int RBF = 2;
	static final int SIG = 3;

	public int type;
	public int degree;
	public double factor;
	public double bias;

	InnerProductSpace[] point;
	private double[] norm_squared;
	int l;

	int evaluations = 0;

	int cache_size = -1;
	int size;
	int cache_bound;
	private Node head;
	private Node[] node;

	boolean debug = false;

	public Kernel(InnerProductSpace[] point_, int type_, int degree_,
			double factor_, double bias_, int size_) {
		point = point_;
		type = type_;
		degree = degree_;
		factor = factor_;
		bias = bias_;
		size = size_;

		initializeKernelCache(size_);
	}

	public Kernel(InnerProductSpace[] point_, int size_) {
		this(point_, LIN, 0, 0.0, 0.0, size_);
	}

	public Kernel(InnerProductSpace[] point_, int degree_, int size_) {
		this(point_, POL, degree_, 0.0, 0.0, size_);
	}

	public Kernel(InnerProductSpace[] point_, double factor_, int size_) {
		this(point_, RBF, 0, factor_, 0.0, size_);
	}

	public Kernel(InnerProductSpace[] point_, double factor_, double bias_,
			int size_) {
		this(point_, SIG, 0, factor_, bias_, size_);
	}

	public Kernel() {
		this(null, LIN, 0, 0.0, 0.0, -1);
	}

	public Kernel(int degree_) {
		this(null, POL, degree_, 0.0, 0.0, -1);
	}

	public Kernel(double factor_) {
		this(null, RBF, 0, factor_, 0.0, -1);
	}

	public Kernel(double factor_, double bias_) {
		this(null, SIG, 0, factor_, bias_, -1);
	}

	public void initializeKernelCache(int size) {
		if (point == null)
			return;

		l = point.length;

		if (type == RBF) {
			norm_squared = new double[l];
			for (int i = 0; i < l; i++) {
				norm_squared[i] = point[i].norm2();
			}
		}

		cache_bound = size * 1024 * 1024 / 8;
		cache_size = 0;
		node = new Node[l];

		for (int i = 0; i < l; i++) {
			node[i] = new Node();
		}

		head = new Node();
		head.next = head.prev = head;
	}

	public double value(InnerProductSpace x, int i) {
		return value(x, this.point[i]);
	}

	public double value(InnerProductSpace x, InnerProductSpace y) {
		switch (type) {
		case LIN:
			return factor * x.dot(y);
		case POL:
			double val = factor * x.dot(y) + 1.0;

			for (int k = 2; k <= degree; k++) {
				val *= val;
			}
			return val;
		case RBF:
			return Math.exp(-factor * x.distance2(y));
			// return Math.exp(-factor*factor*x.distance2(y));
			// return Math.exp(-x.distance2(y)/(2.0*factor*factor));
		case SIG:
			return Maths.tanh(factor * x.dot(y) - bias);
		default:
			System.err.println("Kernel type unrecognized!");
			return 0.0;
		}
	}

	public double value(int i, int j) {
		evaluations++;
		switch (type) {
		case LIN:
			return factor * point[i].dot(point[j]);
		case POL:
			double val = factor * point[i].dot(point[j]) + 1.0;

			for (int k = 2; k <= degree; k++) {
				val *= val;
			}
			return val;
		case RBF:
			return Math.exp(-factor
					* (norm_squared[i] + norm_squared[j] - 2.0 * point[i]
							.dot(point[j])));
			// return Math.exp(-(norm_squared[i] + norm_squared[j] -
			// 2.0*point[i].dot(point[j]))/(2.0*factor*factor));
			// return Math.exp(-distanceSquared(i,j)/(2.0*factor*factor));
			// return Math.exp(-factor*distanceSquared(i,j));
		case SIG:
			return Maths.tanh(factor * point[i].dot(point[j]) - bias);
		default:
			System.err.println("Kernel type unrecognized!");
			return 0.0;
		}
	}

	public double distanceSquared(int i, int j) {
		return (norm_squared[i] + norm_squared[j] - 2.0 * point[i]
				.dot(point[j]));
	}

	double[] getColumn(int i) {
		// return getColumn(i,l);
		Node n = node[i];

		if (n.value == null) {
			// IO.print("@",4);
			cache_size += l;
			while (cache_size > cache_bound) {
				IO.println("# cache_size: " + cache_size + ", cache_bound:"
						+ cache_bound, 5);
				Node m = head.next;
				delete(m);
				// System.out.println("Length: "+ m.value.length);
				cache_size -= l;
				m.value = null;
				// System.out.print("* "+ cache_size);
				// System.exit(0);
			}

			double[] array = new double[l];
			for (int j = 0; j < l; j++) {
				array[j] = value(i, j);
			}

			n.value = array;
		} else {
			delete(n);
		}

		insert(n);
		return n.value;
	}

	/*
	 * double[] getColumn(int i, int len) { Node n = node[i]; int nlen =
	 * (n.value==null)?0:n.value.length;
	 * 
	 * if(nlen > 0) delete(n);
	 * 
	 * if(nlen < len) {
	 * 
	 * while(cache_size <= len - nlen) { Node m = head.next; delete(m);
	 * //System.out.println("Length: "+ m.value.length); cache_size +=
	 * m.value.length; m.value = null; //System.out.print("* "+ cache_size);
	 * //System.exit(0); }
	 * 
	 * double[] array = new double[len]; if(n.value != null)
	 * System.arraycopy(n.value,0,array,0,nlen);
	 * 
	 * for(int j = nlen; j<len; j++) { array[j] = value(i,j); cache_size--; }
	 * 
	 * n.value = array; //System.out.print(". "+(++count)); }
	 * 
	 * insert(n);
	 * 
	 * return n.value; }
	 */

	public int getEvaluations() {
		return evaluations;
	}

	public void setParameter(int degree, double factor, double bias) {
		this.degree = degree;
		this.factor = factor;
		this.bias = bias;
	}

	public void setParameter(double factor) {
		this.setParameter(this.degree, factor, this.bias);
	}

	@Override
	public String toString() {
		String s;

		s = "Kernel[type=";
		switch (type) {
		case LIN:
			s += "LIN";
			break;
		case POL:
			s += "POL, degree=" + degree;
			break;
		case RBF:
			s += "RBF, gamma=" + factor;
			break;
		case SIG:
			s += "SIG, factor=" + factor + "bias" + bias;
			break;
		default:
			s += "UNKNOWN";
			break;
		}
		if (cache_size < 0) {
			s += ", cache=" + size + " MBytes";
		}
		if (evaluations > 0) {
			s += ", evaluations=" + evaluations;
		}

		s += "]";

		return s;
	}

	private void insert(Node n) {
		n.next = head;
		n.prev = head.prev;
		n.prev.next = n;
		n.next.prev = n;
	}

	private void delete(Node n) {
		n.prev.next = n.next;
		n.next.prev = n.prev;
	}

	class Node {
		Node next;
		Node prev;
		double value[];
	}

	/*
	 * public static void main(String[] argv) { Kernel ker; Parameters par;
	 * 
	 * par = new Parameters();
	 * 
	 * par.type = RBF; par.gamma = 1.0;
	 * 
	 * ker = new Kernel(par, new Problem("e:/svm/hcr.dat.li"),10);
	 * 
	 * for(int i = 0; i < ker.l; i++) { int rand =
	 * (int)(ker.l*Math.random()/10); double[] a = ker.getColumn(rand,ker.l/5);
	 * for(int j=0; j<a.length; j++) if(rand==j)
	 * System.out.println("a["+rand+"]["+j+"] = "+a[j]); }
	 * 
	 * System.out.println("Cache Size "+ker.cache_size); }
	 */
}

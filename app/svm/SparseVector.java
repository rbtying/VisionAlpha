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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SparseVector implements InnerProductSpace {
	public String name;

	public double[] value;
	public int[] index;
	public int length = -1;

	public SparseVector(double[] v, String name_) {
		int i, j;

		for (i = 0, j = 0; i < v.length; i++) {
			if (v[i] != 0.0) {
				j++;
			}
		}

		length = j;
		name = new String(name_);

		if (j != 0) {
			value = new double[j];
			index = new int[j];

			for (i = 0, j = 0; i < v.length; i++) {
				if (v[i] != 0.0) {
					value[j] = v[i];
					index[j] = i;
					j++;
				}
			}
		}
	}

	public SparseVector(double[] v, String name_, boolean withzero) {
		int i, j;

		if (withzero) {
			length = v.length;
			value = new double[length];
			index = new int[length];

			for (i = 0; i < v.length; i++) {
				value[i] = v[i];
				index[i] = i;
			}

		} else {
			for (i = 0, j = 0; i < v.length; i++) {
				if (v[i] != 0.0) {
					j++;
				}
			}

			length = j;
			if (j != 0) {
				value = new double[j];
				index = new int[j];

				for (i = 0, j = 0; i < v.length; i++) {
					if (v[i] != 0.0) {
						value[j] = v[i];
						index[j] = i;
						j++;
					}
				}
			}

		}

		name = new String(name_);

	}

	public SparseVector(double[] v) {
		this(v, "no_name");
	}

	public SparseVector(int length_, String name_) {
		length = length_;
		name = new String(name_);

		if (length_ != 0) {
			value = new double[length_];
			index = new int[length_];

			for (int i = 0; i < length; i++) {
				index[i] = i;
			}
		}
	}

	public SparseVector(int length_) {
		this(length_, "no_name");
	}

	public SparseVector(SparseVector v) {
		value = new double[length];
		index = new int[length];

		System.arraycopy(v.value, 0, value, 0, length);
		System.arraycopy(v.index, 0, index, 0, length);

		name = new String(v.name);
	}

	public SparseVector(String str) {
		StringTokenizer st = new StringTokenizer(str, ",: \n\t\r\f");
		int size = st.countTokens() / 2;

		this.value = new double[size];
		this.index = new int[size];
		this.name = new String(st.nextToken());
		this.length = size;

		for (int i = 0; i < size; i++) {
			this.index[i] = IO.atoi(st.nextToken());
			this.value[i] = IO.atof(st.nextToken());
		}
	}

	public static SparseVector contructFrom(String str) {
		SparseVector v;
		StringTokenizer st = new StringTokenizer(str, ",: \n\t\r\f");
		int size = st.countTokens() / 2;

		v = new SparseVector(size);

		for (int i = 0; i < size; i++) {
			v.index[i] = IO.atoi(st.nextToken());
			v.value[i] = IO.atof(st.nextToken());
		}

		return v;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object instanceof SparseVector) {
			SparseVector o = (SparseVector) object;

			if (o.length != this.length)
				return false;
			for (int i = 0; i < this.length; i++) {
				if (this.index[i] == o.index[i]) {
					if (this.value[i] != o.value[i]) {
						return false;
					}
				} else {
					return false;
				}
			}

			return false;
		}

		return false;

	}

	@Override
	public double dot(InnerProductSpace p) {
		SparseVector x, y;
		int h, k;
		double sum;

		x = this;
		y = (SparseVector) p;

		h = k = 0;
		sum = 0.0;
		while (h < x.length && k < y.length) {
			if (x.index[h] == y.index[k]) {
				sum += x.value[h++] * y.value[k++];
			} else if (x.index[h] < y.index[k]) {
				h++;
			} else {
				k++;
			}
		}

		return sum;
	}

	@Override
	public double norm() {
		return Math.sqrt(this.dot(this));
	}

	@Override
	public double norm2() {
		return this.dot(this);
	}

	@Override
	public double distance(InnerProductSpace p) {
		return Math.sqrt(distance2(p));
	}

	@Override
	public double distance2(InnerProductSpace p) {
		SparseVector x, y;
		int h, k;
		double sum;

		x = this;
		y = (SparseVector) p;

		h = k = 0;
		sum = 0.0;
		while (h < x.length && k < y.length) {
			if (x.index[h] == y.index[k]) {
				sum += (x.value[h] - y.value[k]) * (x.value[h] - y.value[k]);
				h++;
				k++;
			} else if (x.index[h] < y.index[k]) {
				sum += x.value[h] * x.value[h];
				h++;
			} else {
				sum += y.value[k] * y.value[k];
				k++;
			}
		}

		while (h < x.length) {
			sum += x.value[h] * x.value[h];
			h++;
		}

		while (k < y.length) {
			sum += y.value[k] * y.value[k];
			k++;
		}

		return sum;
	}

	public static SparseVector read(BufferedReader filein) throws IOException {
		StringTokenizer st = null;
		SparseVector v;
		st = new StringTokenizer(filein.readLine(), ",: \n\t\r\f");

		int size = st.countTokens() / 2;

		v = new SparseVector(size, new String(st.nextToken()));

		for (int i = 0; i < size; i++) {
			v.index[i] = IO.atoi(st.nextToken());
			v.value[i] = IO.atof(st.nextToken());
		}

		return (v);
	}

	@Override
	public void write(DataOutputStream fileout) throws IOException {
		fileout.writeBytes(name + " ");
		for (int i = 0; i < length; i++) {
			fileout.writeBytes(index[i] + ":" + value[i] + " ");
		}
		fileout.writeBytes("\n");
	}

	public String toWrite() {
		String s;
		s = name;
		for (int i = 0; i < length; i++) {
			s += " " + index[i] + ":" + (float) value[i];
		}
		s += "\n";

		return s;
	}

	public String toWrite(String name) {
		String s;
		s = name;
		for (int i = 0; i < length; i++) {
			s += " " + index[i] + ":" + (float) value[i];
		}
		s += "\n";

		return s;
	}

	/*
	 * public static Data readData(String filename) { BufferedReader filein;
	 * Data data = null; SparseVector[] point = null; String[] label = null;
	 * StringTokenizer st; String type; int l, i;
	 * 
	 * try { filein = new BufferedReader(new FileReader(filename)); st = new
	 * StringTokenizer(filein.readLine(),",: \n\t\r\f");
	 * 
	 * type = st.nextToken();
	 * 
	 * if(!type.equals("SparseVector")) { throw new
	 * RuntimeException("The header in file "
	 * +filename+" is "+type+". Needed header: SparseVector"); }
	 * 
	 * l = IO.atoi(st.nextToken());
	 * 
	 * IO.println("Reading file "+filename+":",1);
	 * 
	 * point = new SparseVector[l];
	 * 
	 * for(i = 0; i < l; i++) { point[i] = SparseVector.read(filein); if(i % 10
	 * == 0) { IO.print(".",1); } if(i % 100 == 0 && i != 0) { IO.print(""+i,1);
	 * } } IO.println("."+l+" Done.",1);
	 * 
	 * label = new String[l];
	 * 
	 * IO.println("Reading labels:",4); for(i = 0; i < l; i++) { label[i] =
	 * point[i].name; if(i % 10 == 0) { IO.print(".",4); } if(i % 100 == 0 && i
	 * != 0) { IO.print(""+i,4); } } IO.println("."+l+" Done.",4);
	 * 
	 * data = new Data(point,label,type,l);
	 * 
	 * filein.close(); } catch(FileNotFoundException fnfe) {
	 * System.err.println("File "+filename+" not found..."); System.exit(1); }
	 * catch(IOException ioe) {
	 * System.err.println("Error reading file "+filename+"..."); System.exit(1);
	 * }
	 * 
	 * return data; }
	 */

	public static Data readData(String filename) {
		BufferedReader filein;
		Data data = null;
		SparseVector[] point = null;
		ArrayList<SparseVector> v;
		String[] label = null;
		String str;
		int l, i;

		try {
			filein = new BufferedReader(new FileReader(filename));
			// st = new StringTokenizer(filein.readLine(),",: \n\t\r\f");

			v = new ArrayList<SparseVector>();

			IO.println("Reading " + filename + ":", 1);
			for (i = 0; (str = filein.readLine()) != null; i++) {
				v.add(new SparseVector(str));
				if (i % 10 == 0) {
					IO.print(".", 1);
				}
				if (i % 100 == 0 && i != 0) {
					IO.print("" + i, 1);
				}
			}
			l = v.size();
			IO.println("." + l + "\nDone.", 1);
			// IO.println("Storing data:",3);

			point = new SparseVector[l];
			label = new String[l];
			for (i = 0; i < l; i++) {
				point[i] = v.get(i);
				label[i] = point[i].name;

				// System.out.println(v.get(i)+"\n"+point[i]);
				// if(i % 10 == 0) {
				// IO.print(".",3);
				// }
				// if(i % 100 == 0 && i != 0) {
				// IO.print(""+i,1);
				// }
			}
			// IO.println("."+l+" Done.",3);

			data = new Data(point, label, "SparseVector", l);

			filein.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("File " + filename + " not found...");
			System.exit(1);
		} catch (IOException ioe) {
			System.err.println("Error reading file " + filename + "...");
			System.exit(1);
		}

		return data;
	}

	public static SparseVector[] readData(BufferedReader filein, int l) {
		SparseVector[] v = null;
		String[] label = null;
		int i;

		try {
			new StringTokenizer(filein.readLine(), ",: \n\t\r\f");

			v = new SparseVector[l];

			for (i = 0; i < l; i++) {
				v[i] = SparseVector.read(filein);
			}

			label = new String[l];

			for (i = 0; i < l; i++) {
				label[i] = v[i].name;
			}
		} catch (IOException ioe) {
			System.err.println("Error reading file");
			System.exit(1);
		}

		return v;
	}

	@Override
	public String toString() {
		String s = "SparseVector[name=" + name + ", length=" + length;
		for (int i = 0; i < length; i++) {
			s += ", " + index[i] + ":" + value[i];
		}

		s += "]";

		return s;
	}

	public static void main(String[] arg) {
		Data data = SparseVector.readData(arg[0]);

		Kernel k = new Kernel(data.point, 0);

		System.out.print("Labels:");
		for (int i = 0; i < data.l; i++) {
			System.out.print(" " + data.label[i]);
		}
		System.out.println();
		for (int i = 0; i < data.l; i++) {
			System.out.println(data.point[i].toString() + " - norm:"
					+ k.value(i, i));
		}
	}
}

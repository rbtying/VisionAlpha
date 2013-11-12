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
import java.util.StringTokenizer;

public class StepFunction implements InnerProductSpace {
	public String name;

	public double[] value;
	public double[] index;
	public int length = -1;

	public StepFunction(double[] v, String name_) {
		int i;

		value = new double[v.length];
		index = new double[v.length];
		length = v.length;

		for (i = 0; i < v.length; i++) {
			value[i] = v[i];
			index[i] = (1.0 * i + 1) / v.length;
		}

		name = new String(name_);
	}

	public StepFunction(double[] v) {
		this(v, "no_name");
	}

	public StepFunction(int length_, String name_) {
		value = new double[length_];
		index = new double[length_];
		length = length_;
		name = new String(name_);
	}

	public StepFunction(int length_) {
		this(length_, "no_name");
	}

	public StepFunction(StepFunction v) {
		value = new double[length];
		index = new double[length];

		System.arraycopy(v.value, 0, value, 0, length);
		System.arraycopy(v.index, 0, index, 0, length);

		name = new String(v.name);
	}

	/*
	 * public static StepFunction phiMap(DStroke s, String n) { StepFunction f;
	 * double len; double pos; double dist;
	 * 
	 * //s.filter();
	 * 
	 * //f = new StepFunction(2*(s.size()-1),n); f = new
	 * StepFunction(s.size()-1,n); len = s.length(); pos = 0;
	 * 
	 * //for(int i = 0; i < s.size() - 1; i++) { // dist =
	 * s.pointAt(i).distance(s.pointAt(i+1)); // pos += dist; // f.index[i] =
	 * pos/len; // f.value[i] = (s.pointAt(i+1).x - s.pointAt(i).x)/dist; //}
	 * 
	 * for(int i = 0; i < s.size() - 1; i++) { dist =
	 * s.pointAt(i).distance(s.pointAt(i+1)); pos += dist; f.index[i] = pos/len;
	 * f.value[i] = (s.pointAt(i+1).y - s.pointAt(i).y)/dist; }
	 * 
	 * return f; }
	 */
	/*
	 * public static StepFunction phiMap(DStroke s) { return
	 * phiMap(s,"no_name"); }
	 */

	public double integral() {
		int i;
		double val, a;

		val = 0.0;
		a = 0;
		for (i = 0; i < this.length; i++) {
			val += this.value[i] * (this.index[i] - a);
			a = this.index[i];
		}

		return val;
	}

	@Override
	public double dot(InnerProductSpace p) {
		StepFunction x, y;
		int h, k;
		double sum, a;

		x = this;
		y = (StepFunction) p;

		h = k = 0;
		sum = 0.0;
		a = 0.0;
		while (h < x.length && k < y.length) {
			if (x.index[h] == y.index[k]) {
				sum += x.value[h] * y.value[k] * (x.index[h] - a);
				a = x.index[h];
				h++;
				k++;
			} else if (x.index[h] < y.index[k]) {
				sum += x.value[h] * y.value[k] * (x.index[h] - a);
				a = x.index[h];
				h++;
			} else {
				sum += x.value[h] * y.value[k] * (y.index[k] - a);
				a = y.index[k];
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
		return (this.norm2() - 2.0 * this.dot(p) + p.norm2());
	}

	public static StepFunction read(BufferedReader filein) throws IOException {
		StringTokenizer st;
		StepFunction v;
		String s;

		s = filein.readLine();

		// System.err.println(s);
		st = new StringTokenizer(s, ": \n");
		// System.err.println(s);

		int size = st.countTokens() / 2;

		v = new StepFunction(size, new String(st.nextToken()));

		for (int i = 0; i < size; i++) {
			v.index[i] = IO.atof(st.nextToken());
			v.value[i] = IO.atof(st.nextToken());
		}

		return (v);
	}

	@Override
	public void write(DataOutputStream fileout) throws IOException {
		fileout.writeBytes(name);
		for (int i = 0; i < length; i++) {
			fileout.writeBytes(" " + (float) index[i] + ":" + (float) value[i]);
		}
		fileout.writeBytes("\n");
	}

	public String toWrite() {
		String s;
		s = name;
		for (int i = 0; i < length; i++) {
			s += " " + index[i] + ":" + value[i];
		}
		s += "\n";

		return s;
	}

	public static Data readData(String filename) {
		BufferedReader filein;
		Data data = null;
		StepFunction[] point = null;
		String[] label = null;
		StringTokenizer st;
		String type;
		int l, i;

		try {
			filein = new BufferedReader(new FileReader(filename));
			st = new StringTokenizer(filein.readLine(), ",: \n\t\r\f");

			type = st.nextToken();

			if (!type.equals("StepFunction")) {
				filein.close();
				throw new RuntimeException("The header in file " + filename
						+ " is " + type + ". Needed header: StepFunction");
			}

			l = IO.atoi(st.nextToken());

			IO.println("Reading file " + filename + ":", 1);

			point = new StepFunction[l];

			for (i = 0; i < l; i++) {
				point[i] = StepFunction.read(filein);
				if (i % 10 == 0) {
					IO.print(".", 1);
				}
				if (i % 100 == 0 && i != 0) {
					IO.print("" + i, 1);
				}
			}
			IO.println("." + l + " Done.", 1);

			label = new String[l];

			IO.println("Reading labels:", 4);
			for (i = 0; i < l; i++) {
				label[i] = point[i].name;
				if (i % 10 == 0) {
					IO.print(".", 4);
				}
				if (i % 100 == 0 && i != 0) {
					IO.print("." + i, 4);
				}
			}
			IO.println("" + l + " Done.", 4);

			data = new Data(point, label, type, l);

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

	public static StepFunction[] readData(BufferedReader filein, int l) {
		StepFunction[] v = null;
		String[] label = null;
		int i;

		try {
			new StringTokenizer(filein.readLine(), ",: \n\t\r\f");

			v = new StepFunction[l];

			for (i = 0; i < l; i++) {
				v[i] = StepFunction.read(filein);
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
		String s = "StepFunction[name=" + name + ", length=" + length;
		for (int i = 0; i < length; i++) {
			s += ", " + (float) index[i] + ":" + (float) value[i];
		}

		s += "]";

		return s;
	}

	public static void main(String[] arg) {
		Data data = StepFunction.readData(arg[0]);

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

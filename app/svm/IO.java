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

import java.io.DataOutputStream;
import java.io.IOException;

public class IO {
	public static int verbosity = 3;

	private static boolean SIZE = true;

	public static void writeSize(boolean b) {
		SIZE = b;
	}

	public static void setVerbosity(int v) {
		verbosity = v;
	}

	public static void print(String s, int v) {
		if (v <= verbosity) {
			System.out.print(s);
		}
	}

	public static void println(String s, int v) {
		if (v <= verbosity) {
			System.out.println(s);
		}
	}

	public static void println() {
		System.out.println();
	}

	public static void write(DataOutputStream fileout, double[] v)
			throws IOException {
		int i;
		int len;

		len = v.length;

		if (SIZE)
			fileout.writeBytes(len + " ");
		for (i = 0; i < len; i++)
			fileout.writeBytes(v[i] + " ");
		fileout.writeBytes("\n");
	}

	public static void write(DataOutputStream fileout, int[] v)
			throws IOException {
		int i;
		int len;

		len = v.length;

		if (SIZE)
			fileout.writeBytes(len + " ");
		for (i = 0; i < len; i++)
			fileout.writeBytes(v[i] + " ");
		fileout.writeBytes("\n");
	}

	public static void write(DataOutputStream fileout, int[] u, int[] v)
			throws IOException {
		int i;
		int len;

		len = v.length;

		if (SIZE)
			fileout.writeBytes(len + " ");
		for (i = 0; i < len; i++)
			fileout.writeBytes(u[i] + " " + v[i] + " ");
		fileout.writeBytes("\n");
	}

	/*
	 * public static void write(DataOutputStream fileout, ArrayList v) throws
	 * IOException { int i; int len;
	 * 
	 * len = v.size();
	 * 
	 * if(SIZE) fileout.writeBytes(len+" "); for(i = 0; i < len; i++) { DPoint p
	 * = (DPoint)v.get(i); fileout.writeBytes(p.x+" "+p.y+" "); }
	 * fileout.writeBytes("\n"); }
	 */

	public static int atoi(String s) {
		return Integer.parseInt(s);
	}

	public static long atol(String s) {
		return Long.parseLong(s);
	}

	public static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

}

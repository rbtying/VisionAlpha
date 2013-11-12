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
package ocr;

import java.awt.*;

public class Stroke {

	// the absolute timestamp of point 0
	final long first_time;
	// in times[i] is the timestamp of point i relative to point 0
	// times[0] is always 0
	final long[] times;
	// in xs[i] is the absolue x-coordinate of point i
	final int[] xs;
	// in ys[i] is the absolue y-coordinate of point i
	final int[] ys;

	private int findmin(int[] arr) {
		int min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min)
				min = arr[i];
		}
		return min;
	}

	private int findmax(int[] arr) {
		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max)
				max = arr[i];
		}
		return max;
	}

	public Point getLowerLeft() {
		return (new Point(findmin(xs), findmax(ys)));
	}

	public Point getTopRight() {
		return (new Point(findmax(xs), findmin(ys)));
	}

	public Point getTopLeft() {
		return (new Point(findmin(xs), findmin(ys)));
	}

	public Point getLowerRight() {
		return (new Point(findmax(xs), findmax(ys)));
	}

	public Stroke(int size, long first_time) {
		this.first_time = first_time;
		times = new long[size];
		xs = new int[size];
		ys = new int[size];
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Stroke s = new Stroke(this);

		sb.append("[length=" + s.xs.length + ", ");
		sb.append("first_time=" + s.first_time + ", ");
		for (int i = 0; i < s.xs.length; i++)
			sb.append("(" + s.times[i] + "," + s.xs[i] + "," + s.ys[i] + "), ");
		sb.delete(sb.length() - 2, sb.length());
		sb.append("]\n");
		return sb.toString();
	}

	/**
	 * copy constructor
	 */
	public Stroke(Stroke s) {

		first_time = s.first_time;

		times = new long[s.times.length];
		System.arraycopy(s.times, 0, times, 0, times.length);

		xs = new int[s.xs.length];
		System.arraycopy(s.xs, 0, xs, 0, xs.length);

		ys = new int[s.ys.length];
		System.arraycopy(s.ys, 0, ys, 0, ys.length);
	}

}

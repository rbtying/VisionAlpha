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
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
package hfr;

import java.util.*;

import java.awt.*;

import hfr.graph.*;
import ocr.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SymbolList extends ArrayList<SymbolNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1270981666539943839L;

	public SymbolList() {
		super();
	}

	public SymbolList(SymbolList sl) {
		super();
		for (int i = 0; i < sl.size(); i++) {
			this.add(sl.symbolNodeAt(i));
		}
	}

	public SymbolNode symbolNodeAt(int i) {
		return this.get(i);
	}

	public void sort() {
		Collections.sort(this);
	}

	public int getMinX() {
		int m = Integer.MAX_VALUE;

		for (int i = 0; i < this.size(); i++) {
			m = Math.min(m, this.symbolNodeAt(i).minX);
		}

		return m;
	}

	public int getMinY() {
		int m = Integer.MAX_VALUE;

		for (int i = 0; i < this.size(); i++) {
			m = Math.min(m, this.symbolNodeAt(i).minY);
		}

		return m;
	}

	public int getMaxX() {
		int m = Integer.MIN_VALUE;

		for (int i = 0; i < this.size(); i++) {
			m = Math.max(m, this.symbolNodeAt(i).maxX);
		}

		return m;
	}

	public int getMaxY() {
		int m = Integer.MIN_VALUE;

		for (int i = 0; i < this.size(); i++) {
			m = Math.max(m, this.symbolNodeAt(i).maxY);
		}

		return m;
	}

	public int getWidth() {
		return this.getMaxX() - this.getMinX();
	}

	public int getHeight() {
		return this.getMaxY() - this.getMinY();
	}

	SymbolNode getLastSymbol() {
		int idx = this.size() - 1;
		if (idx >= 0) {
			return this.get(idx);
		} else {
			return null;
		}
	}

	public void setWalls() {
		/*
		 * for(int i = 0; i < this.size(); i++) { this.symbolNodeAt(i).upWall =
		 * this.getMinY(); this.symbolNodeAt(i).downWall = this.getMaxY();
		 * this.symbolNodeAt(i).leftWall = this.getMinX();
		 * this.symbolNodeAt(i).rightWall = this.getMaxX(); }
		 */
		SymbolNode sn;
		for (int i = 0; i < this.size() - 1; i++) {
			sn = this.symbolNodeAt(i);
			if (sn.isVariableRange()) {
				sn.rightWall = this.symbolNodeAt(i + 1).maxX;
			}
		}
	}

	public SymbolList getRows(double factor) {
		SymbolList rows = null;
		SymbolNode sn;
		ArrayList interval, newInterval;
		Point p;
		int column[], min, i, j;
		double space;

		min = this.getMinY();
		column = new int[this.getMaxY() - this.getMinY() + 1];
		for (i = 0; i < this.size(); i++) {
			sn = this.symbolNodeAt(i);
			for (j = sn.minY; j <= sn.maxY; j++) {
				column[j - min] = 1;
			}
		}

		interval = new ArrayList();
		j = -1;
		for (i = 1; i < column.length; i++) {
			if (column[i] - column[i - 1] < 0) {
				interval.add(new Point(i, -1));
				j++;
			} else if (column[i] - column[i - 1] > 0) {
				((Point) interval.get(j)).y = i;
			}
		}

		if (interval.isEmpty()) {
			return this;
		}

		space = 0.0;
		for (i = 0; i < interval.size(); i++) {
			p = (Point) interval.get(i);
			space += p.y - p.y;
		}

		space = space / interval.size();
		newInterval = new ArrayList();
		space = factor * space;
		for (i = 0; i < interval.size(); i++) {
			p = (Point) interval.get(i);
			if (p.y - p.y > space) {
				newInterval.add(new Point(0, (p.x + p.y) / 2));
			}
		}

		return rows;
	}

	public void setHorizontalWalls() {
		if (this.isEmpty()) {
			return;
		}

		if (this.symbolNodeAt(0).isVariableRange()) {
			this.symbolNodeAt(0).leftThreshold = this.getMinX();
		}

		for (int i = 1; i < this.size(); i++) {
			this.symbolNodeAt(i - 1).rightWall = this.symbolNodeAt(i).minX;
		}
	}

	public void setHorizontalWalls(SymbolList sl) {
		if (this.isEmpty()) {
			return;
		}

		if (this.symbolNodeAt(0).isVariableRange()) {
			this.symbolNodeAt(0).leftThreshold = sl.getMinX();
		}

		for (int i = 1; i < this.size(); i++) {
			this.symbolNodeAt(i - 1).rightWall = this.symbolNodeAt(i).minX;
		}
	}

	public boolean isFirst(String name) {
		return name.equals(this.symbolNodeAt(0).name);
	}

	public void add(SymbolList sl) {
		for (int i = 0; i < sl.size(); i++) {
			this.add(sl.symbolNodeAt(i));
		}
	}

	public void addSpaces(String str, double factor) {
		double space;
		int i, count;

		space = 0.0;
		count = 0;
		for (i = 0; i < this.size(); i++) {
			if (!this.symbolNodeAt(i).name.equals("-")
					&& !this.symbolNodeAt(i).name.equals("\\sqrt")) {
				space += this.symbolNodeAt(i).width;
				count++;
			}
		}

		space = space / count;

		if (space != 0.0) {
			factor = factor * space;

			for (i = 0; i < this.size() - 1; i++) {
				if ((this.symbolNodeAt(i + 1).minX - this.symbolNodeAt(i).maxX) > factor) {
					this.symbolNodeAt(i).name = this.symbolNodeAt(i).name + str;
				}
			}
		}
	}

	public SymbolList dominated() {
		SymbolList sl;
		SymbolNode sn;
		int i;

		sl = new SymbolList();
		sl.add(this);
		sl.sort();

		for (i = sl.size() - 1; i > 0; i--) {
			if (sl.symbolNodeAt(i).dominates(sl.symbolNodeAt(i - 1))) {
				sn = sl.symbolNodeAt(i);
				sl.set(i, sl.symbolNodeAt(i - 1));
				sl.set(i - 1, sn);
			}
			// else if(sl.symbolNodeAt(i - 1).dominates(sl.symbolNodeAt(i))) {
			// sn = sl.symbolNodeAt(i - 1);
			// sl.set(sl.symbolNodeAt(i),i - 1);
			// sl.set(sn,i);
			// }
		}

		return sl;
	}

	public void draw(Graphics2D g2) {
		SymbolNode sn;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			// System.out.println("i: "+i);
			sn = this.symbolNodeAt(i);
			/*
			 * if(sn.someDown || sn.someUp) { g2.setColor(Color.blue);
			 * g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
			 * BasicStroke.JOIN_ROUND)); g2.drawRect(sn.minX, sn.hbMinY,
			 * sn.width, sn.hbMaxY - sn.hbMinY); }
			 */

			try {
				sn.draw(g2, this.symbolNodeAt(i + 1), "", Color.red);
			} catch (ArrayIndexOutOfBoundsException ex) {
			}

			if (!sn.rows.isEmpty()) {
				SymbolList row;
				for (j = 0; j < sn.rows.size(); j++) {
					row = sn.rows.get(j);
					sn.draw(g2, row.symbolNodeAt(0), "", Color.magenta);
					row.draw(g2);
				}
			}
			/*
			 * if(sn.someInDownRegion || sn.someInUpRegion) {
			 * //System.out.println(sn); g2.setStroke(new BasicStroke(0.5f,
			 * BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
			 * g2.setColor(Color.blue); g2.drawRect(sn.minX, sn.hbMinY,
			 * sn.width, sn.hbMaxY - sn.hbMinY); }
			 */
			if (!sn.upleft.isEmpty()) {
				sn.draw(g2, sn.upleft.symbolNodeAt(0), "", Color.green);
				sn.upleft.draw(g2);
			}
			if (!sn.up.isEmpty()) {
				sn.draw(g2, sn.up.symbolNodeAt(0), "", Color.green);
				sn.up.draw(g2);
			}
			if (!sn.superscript.isEmpty()) {
				sn.draw(g2, sn.superscript.symbolNodeAt(0), "", Color.green);
				sn.superscript.draw(g2);
			}
			if (!sn.subexpression.isEmpty()) {
				sn.draw(g2, sn.subexpression.symbolNodeAt(0), "", Color.magenta);
				sn.subexpression.draw(g2);
			}
			if (!sn.subscript.isEmpty()) {
				sn.draw(g2, sn.subscript.symbolNodeAt(0), "", Color.blue);
				sn.subscript.draw(g2);
			}
			if (!sn.down.isEmpty()) {
				sn.draw(g2, sn.down.symbolNodeAt(0), "", Color.blue);
				sn.down.draw(g2);
			}
			if (!sn.downleft.isEmpty()) {
				sn.draw(g2, sn.downleft.symbolNodeAt(0), "", Color.blue);
				sn.downleft.draw(g2);
			}
			if (!sn.right.isEmpty()) {
				sn.draw(g2, sn.right.symbolNodeAt(0), "", Color.orange);
				sn.right.draw(g2);
			}

		}
	}

	public void drawSomeUpDown(Graphics2D g2) {
		SymbolNode sn;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			// System.out.println("i: "+i);
			sn = this.symbolNodeAt(i);
			if (sn.someInDownRegion && sn.someInUpRegion) {
				// System.out.println(sn);
				g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_ROUND));
				g2.setColor(Color.red);
				g2.drawRect(sn.minX, sn.hbMinY, sn.width, sn.hbMaxY - sn.hbMinY);
			} else if (sn.someInDownRegion) {
				// System.out.println(sn);
				g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_ROUND));
				g2.setColor(Color.red);
				g2.drawRect(sn.minX, sn.minY, sn.width, sn.hbMaxY - sn.minY);
			} else if (sn.someInUpRegion) {
				// System.out.println(sn);
				g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_ROUND));
				g2.setColor(Color.red);
				g2.drawRect(sn.minX, sn.hbMinY, sn.width, sn.maxY - sn.hbMinY);
			}
			if (!sn.rows.isEmpty()) {
				SymbolList row;
				for (j = 0; j < sn.rows.size(); j++) {
					row = sn.rows.get(j);
					sn.draw(g2, row.symbolNodeAt(0), "", Color.magenta);
					row.draw(g2);
				}
			}

			if (!sn.upleft.isEmpty()) {
				// sn.draw(g2, sn.upleft.symbolNodeAt(0), "", Color.green);
				sn.upleft.drawSomeUpDown(g2);
			}
			if (!sn.up.isEmpty()) {
				// sn.draw(g2, sn.up.symbolNodeAt(0), "", Color.green);
				sn.up.drawSomeUpDown(g2);
			}
			if (!sn.superscript.isEmpty()) {
				// sn.draw(g2, sn.superscript.symbolNodeAt(0), "", Color.green);
				sn.superscript.drawSomeUpDown(g2);
			}
			if (!sn.subexpression.isEmpty()) {
				// sn.draw(g2, sn.subexpression.symbolNodeAt(0), "",
				// Color.magenta);
				sn.subexpression.drawSomeUpDown(g2);
			}
			if (!sn.subscript.isEmpty()) {
				// sn.draw(g2, sn.subscript.symbolNodeAt(0), "", Color.blue);
				sn.subscript.drawSomeUpDown(g2);
			}
			if (!sn.down.isEmpty()) {
				// sn.draw(g2, sn.down.symbolNodeAt(0), "", Color.blue);
				sn.down.drawSomeUpDown(g2);
			}
			if (!sn.downleft.isEmpty()) {
				// sn.draw(g2, sn.downleft.symbolNodeAt(0), "", Color.blue);
				sn.downleft.drawSomeUpDown(g2);
			}
			if (!sn.right.isEmpty()) {
				// sn.draw(g2, sn.right.symbolNodeAt(0), "", Color.orange);
				sn.right.drawSomeUpDown(g2);
			}

		}

	}

	public void draw(Graphics2D g2, Color c, String label) {
		SymbolNode sn;
		int i;

		for (i = 0; i < this.size(); i++) {
			// System.out.println("i: "+i);
			sn = this.symbolNodeAt(i);
			try {
				sn.draw(g2, this.symbolNodeAt(i + 1), label, c);
			} catch (ArrayIndexOutOfBoundsException ex) {
			}
		}
	}

	public void drawRight(Graphics2D g2) {
		SymbolNode sn;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			sn = this.symbolNodeAt(i);
			for (j = 0; j < sn.right.size(); j++) {
				sn.draw(g2, sn.right.symbolNodeAt(j), "");
			}
		}
	}

	public void drawRight(Graphics2D g2, Color c) {
		SymbolNode sn;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			sn = this.symbolNodeAt(i);
			for (j = 0; j < sn.right.size(); j++) {
				sn.draw(g2, sn.right.symbolNodeAt(j), "", c);
			}
		}
	}

	public void drawRightVariableRange(Graphics2D g2, Color c) {
		SymbolNode sn;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			sn = this.symbolNodeAt(i);
			if (sn.isVariableRange()) {
				for (j = 0; j < sn.right.size(); j++) {
					sn.draw(g2, sn.right.symbolNodeAt(j), "", c);
				}
			}
		}
	}

	public SymbolNode startSymbolNode() {
		SymbolList sl;
		int n;

		sl = new SymbolList();
		sl.add(this);

		while (sl.size() > 1) {
			n = sl.size() - 1;
			if (sl.symbolNodeAt(n).dominates(sl.symbolNodeAt(n - 1))) {
				sl.remove(n - 1);
			} else {
				sl.remove(n);
			}
		}

		// System.out.println("");
		return sl.symbolNodeAt(0);
	}

	public String toSequenceString() {
		String str = "";
		int i;

		for (i = 0; i < this.size(); i++) {
			str += this.symbolNodeAt(i).name;
			if (i < this.size() - 1) {
				str += " ";
			}
		}

		return str;
	}

	public String toStringRight() {
		String str = "";
		int i;

		for (i = 0; i < this.size(); i++) {
			str += this.symbolNodeAt(i).name + ": ";
			/*
			 * for(j = 0; j <this.symbolNodeAt(i).right.size(); j++) { str +=
			 * this.symbolNodeAt(i).right.symbolNodeAt(j)+ " "; }
			 */
			str += this.symbolNodeAt(i).right + "\n";
		}

		return str;
	}

	/*
	 * public double[] verticalProyection() { return verticalProyection(6); }
	 */

	public double[] verticalProyection(int filter) {
		SymbolList sl = new SymbolList();
		SymbolNode sn;
		double hist[], histf[], mu;
		int i, j, h, hp, wp;

		this.sort();

		hp = wp = 0;
		for (i = 0; i < this.size(); i++) {
			sl.add(this.symbolNodeAt(i));
			hp += this.symbolNodeAt(i).height;
			wp += this.symbolNodeAt(i).weight;
		}
		hp = hp / this.size();
		wp = wp / this.size();

		hist = new double[sl.getMaxY() - sl.getMinY() + 1];

		for (i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);
			h = (sn.isHorizontalBar() && sn.width > 0.8 * wp) ? ((8 * hp) / 10)
					: sn.height;
			mu = (sn.superThreshold + sn.subscThreshold) / 2;

			// hist[mu - sl.getMinY()]=(!sn.isRoot()) ? (1.0 * h * sn.width) :
			// 0;

			// for(j = sl.getMinY(); j <=sl.getMaxY(); j++) {
			for (j = (int) (mu - 1.5 * h); j <= mu + 3 * h / 2; j++) {
				if (h == 0) {
					continue;
				}
				try {
					hist[j - sl.getMinY()] +=
					// (sn.minX <= j && j <= sn.maxX)? 1 : 0;
					((!sn.isRoot()) ? (1.0 * h * sn.width) : 0) // ;
							// ((!sn.isRoot()) ? (1.0 * h ) : 0) //;
							* Math.exp(-10.0 * (j - mu) * (j - mu) / (h * h));
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}

			/*
			 * for(j = -h*2+mu; j <=h*2+mu; j++) { try { hist[j - sl.getMinY()]
			 * += //1; ((!sn.isRoot()) ? (1.0 * h * sn.width) : 0)*
			 * Math.exp(-2*(j- sl.getMinY())*(j- sl.getMinY())/(h*h)); }
			 * catch(ArrayIndexOutOfBoundsException e) { } }
			 */
			/*
			 * for(j = 0; j < h; j++) { if(sn.isHorizontalBar() && sn.width >
			 * 0.8*wp) { try { hist[(sn.minY + sn.maxY)/2 - ((8*hp)/10)/2 -
			 * sl.getMinY() + j] += //1.0; //(1.0*sn.height)/this.getHeight();
			 * ((!sn.isRoot()) ? (0.8 * hp * sn.width) : 0)Math.exp(-0.5*(j
			 * -mu+hp/2)*(j-mu+hp/2)/(hp*hp)); }
			 * catch(ArrayIndexOutOfBoundsException e) {} } else { hist[sn.minY
			 * - sl.getMinY() + j] += //1.0; //(1.0*sn.height)/this.getHeight();
			 * ((!sn.isRoot()) ? (1.0 * sn.height * sn.width) : 0)
			 * Math.exp(-0.5*(j -mu+h/2)*(j-mu+h/2)/(h*h)); } }
			 */
		}

		histf = new double[sl.getMaxY() - sl.getMinY() + 1];
		for (i = 1; i <= filter; i++) {
			for (j = 0; j < histf.length; j++) {
				histf[j] = 0;
				try {
					histf[j] += hist[j - 1] + hist[j] + hist[j + 1];
					histf[j] = histf[j] / 3;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
				}
			}
			System.arraycopy(histf, 0, hist, 0, hist.length);
		}

		return hist;
	}

	public int[] getMaxima(double[] a, int w) {
		// double v[], vn[], dx, dy;
		int indexes[], i, j;
		boolean extremal, extremar;

		/*
		 * v = new double[a.length]; vn = new double[a.length]; for(i = 1; i <
		 * v.length; i++) { vn[i] = a[i] - a[i - 1]; } vn[0] = v[1]; for(i = 2;
		 * i < v.length - 2; i++) { v[i] = vn[i-2] + 4.0*vn[i-1] + 6.0*vn[i] +
		 * 4.0*v[i+1] + v[i+2]; v[i] = v[i]/16.0; }
		 */

		/*
		 * for(i = 1; i < v.length - 1; i++) { v[i] = 0.5*vn[i-1] + 0.5*vn[i] +
		 * 0.25*v[i+1]; }
		 */

		indexes = new int[a.length];
		/*
		 * if(v[0] < v[1]) { indexes[0] = 0; }
		 */
		for (i = 0; i < indexes.length; i++) {
			try {
				extremal = true;
				for (j = 1; j <= w; j++) {
					extremal &= a[i] >= a[i - j];
				}
				for (j = 1; j <= w; j++) {
					extremal &= a[i] > a[i + j];
					// extremal &= a[i] >= a[i + j];
				}

				extremar = true;
				for (j = 1; j <= w; j++) {
					extremar &= a[i] > a[i - j];
				}
				for (j = 1; j <= w; j++) {
					extremar &= a[i] >= a[i + j];
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				extremal = extremar = false;
			}

			if (extremal || extremar) {
				indexes[i] = 1;
			}
		}

		return indexes;
	}

	public void drawDad(Graphics2D g) {

		for (int i = 0; i < this.size(); i++) {
			g.setColor(Color.black);
			// g.drawString(this.symbolNodeAt(i).name,this.symbolNodeAt(i).maxX,this.symbolNodeAt(i).maxY);
			if (this.symbolNodeAt(i).dad == null) {
				continue;
			}
			// if(this.symbolNodeAt(i).dad.name.equals("[") //||
			// this.symbolNodeAt(i).name.equals("[")
			// )
			// g.setColor(Color.red);
			// else
			g.setColor(Color.blue);
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_ROUND));
			g.drawLine(this.symbolNodeAt(i).dad.centroidX,
					this.symbolNodeAt(i).dad.centroidY,
					this.symbolNodeAt(i).centroidX,
					this.symbolNodeAt(i).centroidY);

			g.fillRect(this.symbolNodeAt(i).dad.centroidX - 1,
					this.symbolNodeAt(i).dad.centroidY - 1, 3, 3);
			g.fillRect(this.symbolNodeAt(i).centroidX - 1,
					this.symbolNodeAt(i).centroidY - 1, 3, 3);
		}

	}

	public void printDadMatrix() {
		SymbolNode sn, dad;
		int i, j;

		for (i = 0; i < this.size(); i++) {
			dad = this.symbolNodeAt(i);
			for (j = 0; j < this.size(); j++) {
				sn = this.symbolNodeAt(j);
				if (sn.dad == null) {
					System.out.print(".");
				} else if (sn.dad.equals(dad)) {
					System.out.print("*");
				} else {
					System.out.print(".");
				}
			}
			System.out.println();
		}
	}

	public SymbolList drawVerticalProyection(Graphics2D g, Color c, double minf) {
		SymbolList sl = new SymbolList();
		DPoint p;
		double hist[]; // = verticalProyection(3);
		int indexes[]; // = getMaxima(hist,10);
		double min, max, f;
		int i, count, width;
		SymbolNode sn;

		this.sort();

		count = 1;
		// System.out.println("this.symbolNodeAt(0): "+this.symbolNodeAt(0));
		sn = new SymbolNode(this.symbolNodeAt(0));
		// sn = new
		// SymbolNode(this.getMinX(),this.getMinY(),this.getMinX(),this.getMaxY(),"[");
		sn.atractors.clear();
		width = sn.width;
		for (i = 1; i < this.size(); i++) {
			sl.add(new SymbolNode(this.symbolNodeAt(i)));
		}

		hist = sl.verticalProyection(0);
		indexes = sl.getMaxima(hist, 10);

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		for (i = 0; i < hist.length; i++) {
			if (hist[i] > 0) {
				min = Math.min(min, hist[i]);
				max = Math.max(max, hist[i]);
			}
		}

		g.setColor(new Color(0f, 1f, 0f, .4f));
		g.fillRect(sn.minX + (int) (width * minf), sl.getMinY(), // ((int)(i*sn.height/(1.0*this.getHeight()))),
				1, hist.length);

		for (i = 0; i < hist.length; i++) {
			f = (hist[i] - min) / (max - min);
			g.setColor(new Color(1f, 0f, 0f, .4f));
			g.fillRect(sn.minX, sl.getMinY() + i, // ((int)(i*sn.height/(1.0*this.getHeight()))),
					(int) (width * f), 1);
			if (indexes[i] == 1) {
				// System.out.print("Extrema: hist["+i+"] = "+f);
				if (f <= minf) {
					// System.out.println();
					continue;
				} else {
					// System.out.println("*");
				}
				g.fillRect(sn.minX, sl.getMinY() + i, // ((int)(i*sn.height/(1.0*this.getHeight()))),
						(int) (width * f), 1);
				g.setColor(Color.blue);
				g.drawString("" + count, sn.minX + (int) (width * f),
						sl.getMinY() + i); // ((int)(i*sn.height/(1.0*this.getHeight()))));
				count++;
				// p = new DPoint(this.getMinX() - width*(1 - f),this.getMinY()
				// + i);
				// p = new DPoint(this.getMinX()+sn.width,sl.getMinY() +
				// i);//((int)(i*sn.height/(1.0*this.getHeight()))));
				p = new DPoint(sl.getMinX(), sl.getMinY() + i); // ((int)(i*sn.height/(1.0*this.getHeight()))));
				// p.xf = (1 - f)*width;
				// sn.atractors.add(new DPoint((int)(this.getMinX() - width*(1 -
				// f)),this.getMinY() + i));
				sn.atractors.add(p);

			}

		}

		sl.sort();
		SymbolList inTree = new SymbolList();
		// SymbolEdge.setUsingAtractors(true);

		// SymbolEdge.setDistanceFactors(false);

		inTree.add(sn);
		SymbolList tree = MSTPrim.construct(sl, inTree);
		tree.drawDad(g);
		// SymbolEdge.setDistanceFactors(false);
		for (i = 0; i < tree.size(); i++) {
			System.out.println(tree.symbolNodeAt(i) + "->"
					+ tree.symbolNodeAt(i).dad);
		}

		sn.addRows(tree);

		return sl;
	}

	public SymbolList setVerticalProyection(SymbolNode sn, double minf) {
		if (this.isEmpty()) {
			return null;
		}
		SymbolList sl;
		DPoint p;
		double hist[]; // = verticalProyection(3);
		int indexes[]; // = getMaxima(hist,10);
		double min, max, f;
		int i;

		sl = new SymbolList(this);
		sl.sort();
		// System.out.println("this.symbolNodeAt(0): "+this.symbolNodeAt(0));
		// sn = new SymbolNode(this.symbolNodeAt(0));
		// sn = new
		// SymbolNode(this.getMinX(),this.getMinY(),this.getMinX(),this.getMaxY(),"[");
		sn.atractors.clear();
		for (i = 1; i < this.size(); i++) {
			sl.add(new SymbolNode(this.symbolNodeAt(i)));
		}

		hist = sl.verticalProyection(3);
		indexes = sl.getMaxima(hist, 10);

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		for (i = 0; i < hist.length; i++) {
			if (hist[i] > 0) {
				min = Math.min(min, hist[i]);
				max = Math.max(max, hist[i]);
			}
		}

		for (i = 0; i < hist.length; i++) {
			f = (hist[i] - min) / (max - min);
			if (indexes[i] == 1) {
				// System.out.print("Extrema: hist[" + i + "] = " + f);
				if (f <= minf) {
					// System.out.println();
					continue;
				} else {
					// System.out.println("*");
				}
				// p = new DPoint(this.getMinX() - width*(1 - f),this.getMinY()
				// + i);
				// p = new DPoint(this.getMinX()+sn.width,sl.getMinY() +
				// i);//((int)(i*sn.height/(1.0*this.getHeight()))));
				p = new DPoint(sl.getMinX(), sl.getMinY() + i); // ((int)(i*sn.height/(1.0*this.getHeight()))));
				// p.xf = (1 - f)*width;
				// sn.atractors.add(new DPoint((int)(this.getMinX() - width*(1 -
				// f)),this.getMinY() + i));
				sn.atractors.add(p);

			}

		}

		// sl.add(sn);
		// System.out.println(sn);
		sl.sort();
		SymbolList inTree = new SymbolList();
		SymbolEdge.setUsingAtractors(true);
		// SymbolEdge.setHorizontalFactors(true);
		SymbolEdge.setDistanceFactors(false);
		// System.out.println("sl: "+sl);
		inTree.add(sn);
		// System.out.println("inTree: "+inTree);
		SymbolList tree = MSTPrim.construct(sl, inTree);
		// SymbolEdge.setHorizontalFactors(false);
		SymbolEdge.setDistanceFactors(true);
		// for(i = 0; i < tree.size(); i++) {
		// System.out.print("[node=" + tree.symbolNodeAt(i) + ", dad=" +
		// tree.symbolNodeAt(i).dad + "] ");
		// }

		// System.out.println("\nchldren: " +
		// sn.getProperChildrenSymbolList(tree));
		sn.addRows(tree);
		// System.out.println(sn.rowsToString());

		return sl;
	}

	public static void main(String[] arg) {
		SymbolList sl = new SymbolList();

		for (char i = 'A'; i < '}'; i++) {
			sl.add(new SymbolNode((int) (100 * Math.random()), 0, 0, 0, "" + i));
		}

		// System.out.println(sl);

		sl.sort();

		// System.out.println(sl);
	}
}

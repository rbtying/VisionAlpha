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

import hfr.graph.*;
import ocr.*;

public class BaselineStructureTree {
	public final static int LATEX = 0;
	public final static int MATHEMATICA = 1;
	public final static int MINIMATICA = 2;
	public final static int BASIC = 3;
	public static int interpreter = 0;
	public SymbolList symbolList;
	public SymbolNode root;
	public static String dot = "dot";
	String digits = "0123456789.=";
	public static boolean checkSpaces = true;

	public BaselineStructureTree(SymbolList symbolList) {
		this.symbolList = symbolList;
	}

	public void buildTree() {
		/*
		 * SymbolList tree; SymbolNode sn; tree =
		 * checkHorizontalBar(symbolList); for(int i = 0; i < tree.size(); i++)
		 * { symbolList.symbolNodeAt(i).hbMaxY = tree.symbolNodeAt(i).hbMaxY;
		 * symbolList.symbolNodeAt(i).hbMinY = tree.symbolNodeAt(i).hbMinY;
		 * symbolList.symbolNodeAt(i).someInDownRegion =
		 * tree.symbolNodeAt(i).someInDownRegion;
		 * symbolList.symbolNodeAt(i).someInUpRegion =
		 * tree.symbolNodeAt(i).someInUpRegion; }
		 */
		extractBaselineMST(symbolList);
	}

	public void extractBaseline(SymbolList sl) {
		if (sl.isEmpty()) {
			return;
		}

		SymbolList horizontal;
		SymbolNode sn = null;
		int i, j;

		// the list is ordered
		sl.sort();
		sl.setWalls();

		horizontal = new SymbolList();
		horizontal.add(this.horizontal(sl));
		horizontal.setHorizontalWalls(sl);

		SymbolMinimumSpanningTree.construct(sl, horizontal);

		for (i = 0; i < horizontal.size(); i++) {
			sn = horizontal.symbolNodeAt(i);
			for (j = 0; j < sl.size(); j++) {
				sn.addNotHorizontal(sl.symbolNodeAt(j));
			}
		}

		checkVariableRange(horizontal);

		sl.clear();
		sl.add(horizontal);

		for (i = 0; i < horizontal.size(); i++) {
			extractBaseline(horizontal.symbolNodeAt(i).up);
			extractBaseline(horizontal.symbolNodeAt(i).superscript);
			extractBaseline(horizontal.symbolNodeAt(i).subexpression);
			extractBaseline(horizontal.symbolNodeAt(i).subscript);
			extractBaseline(horizontal.symbolNodeAt(i).down);
		}
	}

	public void extractBaselineTree(SymbolList sl) {
		if (sl.isEmpty()) {
			return;
		}

		SymbolList horizontal, tree;
		int i;

		// the list is ordered
		sl.sort();
		sl.setWalls();

		horizontal = this.horizontal(sl);

		tree = SymbolMinimumSpanningTree.construct(sl, horizontal);

		for (i = 0; i < horizontal.size(); i++) {
			addRight(tree.symbolNodeAt(i));
		}

		sl.clear();

		for (i = 0; i < horizontal.size(); i++) {
			sl.add(tree.symbolNodeAt(i));
		}

		for (i = 0; i < sl.size(); i++) {
			extractBaselineTree(sl.symbolNodeAt(i).upleft);
			extractBaselineTree(sl.symbolNodeAt(i).up);
			extractBaselineTree(sl.symbolNodeAt(i).superscript);
			extractBaselineTree(sl.symbolNodeAt(i).subexpression);
			extractBaselineTree(sl.symbolNodeAt(i).subscript);
			extractBaselineTree(sl.symbolNodeAt(i).down);
			extractBaselineTree(sl.symbolNodeAt(i).downleft);
		}
	}

	public void extractBaselineMST(SymbolList sl) {
		if (sl.isEmpty()) {
			return;
		}

		SymbolList hor, tree, row, aux;
		SymbolNode sn = null;
		int i, j;

		// the list is ordered
		sl.sort();
		// sl.setWalls();
		aux = checkHorizontalBar(sl);
		sl.clear();
		for (i = 0; i < aux.size(); i++) {
			sl.add(new SymbolNode(aux.symbolNodeAt(i)));
		}
		sl.sort();
		/*
		 * for(i = 0; i < sl.size(); i++) { sn = sl.symbolNodeAt(i); sn.hbMaxY =
		 * aux.symbolNodeAt(i).hbMaxY; sn.hbMinY = aux.symbolNodeAt(i).hbMinY;
		 * sn.someInDownRegion = aux.symbolNodeAt(i).someInDownRegion;
		 * sn.someInUpRegion = aux.symbolNodeAt(i).someInUpRegion; }
		 */

		hor = this.horizontal(sl);
		for (i = 0; i < hor.size(); i++) {
			sn = hor.symbolNodeAt(i);
			if (sn.isVariableRange() && i < hor.size() - 1) {
				sn.rightWall = hor.symbolNodeAt(i + 1).minX;
			}
			sn.inMainBaseline = true;
		}

		tree = MSTPrim.construct(sl, hor);

		checkMatrix(tree);

		for (i = 0; i < tree.size(); i++) {
			sn = tree.symbolNodeAt(i);
			sn.rightWall = Integer.MAX_VALUE;
			addDadMST(sn);
		}

		sl.clear();

		for (i = 0; i < tree.size(); i++) {
			sn = tree.symbolNodeAt(i);
			if (sn.dad == null) {
				sl.add(sn);
			}
		}

		sl.sort();

		for (i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);
			if (sn.name.equals("[")) {
				this.setVerticalProyection(sn, 0.05);
			}
			for (j = 0; j < sn.rows.size(); j++) {
				row = sn.rows.get(j);
				System.out.println(row);
				if (checkSpaces) {
					addSpaces(row, 1.15);
				}
				extractBaselineMST(row);
			}
			extractBaselineMST(sn.upleft);
			extractBaselineMST(sn.up);
			extractBaselineMST(sn.superscript);
			extractBaselineMST(sn.subexpression);
			extractBaselineMST(sn.subscript);
			extractBaselineMST(sn.down);
			extractBaselineMST(sn.downleft);
		}
	}

	public void extractOneBaselineMST(SymbolList sl) {
		if (sl.isEmpty()) {
			return;
		}

		SymbolList hor, tree, aux;
		SymbolNode sn = null;
		int i;

		// the list is ordered
		sl.sort();
		// sl.setWalls();
		aux = checkHorizontalBar(sl);
		sl.clear();
		for (i = 0; i < aux.size(); i++) {
			sl.add(new SymbolNode(aux.symbolNodeAt(i)));
		}

		/*
		 * for(i = 0; i < sl.size(); i++) { sn = sl.symbolNodeAt(i); sn.hbMaxY =
		 * aux.symbolNodeAt(i).hbMaxY; sn.hbMinY = aux.symbolNodeAt(i).hbMinY;
		 * sn.someInDownRegion = aux.symbolNodeAt(i).someInDownRegion;
		 * sn.someInUpRegion = aux.symbolNodeAt(i).someInUpRegion; }
		 */

		hor = this.horizontal(sl);
		for (i = 0; i < hor.size(); i++) {
			sn = hor.symbolNodeAt(i);
			if (sn.isVariableRange() && i < hor.size() - 1) {
				sn.rightWall = hor.symbolNodeAt(i + 1).minX;
			}
			sn.inMainBaseline = true;
		}

		tree = MSTPrim.construct(sl, hor);

		checkMatrix(tree);

		for (i = 0; i < tree.size(); i++) {
			addDadMST(tree.symbolNodeAt(i));
		}

		sl.clear();

		for (i = 0; i < tree.size(); i++) {
			if (tree.symbolNodeAt(i).dad == null) {
				sl.add(tree.symbolNodeAt(i));
			}
		}

		sl.sort();
	}

	public void addSpaces(SymbolList sl, double factor) {
		SymbolList nsl = new SymbolList();
		double space;
		int i, count;
		sl.sort();
		space = 0.0;
		count = 0;
		for (i = 0; i < sl.size(); i++) {
			if (!sl.symbolNodeAt(i).name.equals("-")
					&& !sl.symbolNodeAt(i).name.equals("\\sqrt")) {
				space += sl.symbolNodeAt(i).width;
				count++;
			}
		}

		space = space / count;

		if (space != 0.0) {
			factor = factor * space;
			// last = null;
			count = 0;
			// fsl = nsl;
			for (i = 0; i < sl.size() - 1; i++) {
				nsl.add(sl.symbolNodeAt(i));
				if ((sl.symbolNodeAt(i + 1).minX - sl.symbolNodeAt(i).maxX) > factor
						&& !sl.symbolNodeAt(i).isVariableRange()) {
					nsl.add(new SymbolNode(
							(sl.symbolNodeAt(i + 1).minX + sl.symbolNodeAt(i).maxX) / 2,
							sl.getMinY(),
							(sl.symbolNodeAt(i + 1).minX + sl.symbolNodeAt(i).maxX) / 2,
							sl.getMaxY(), "\\matrix_space"));
					count++;
				}
				if (count == 0) {
					// fsl = nsl;
				}
			}
			nsl.add(sl.symbolNodeAt(sl.size() - 1));

			sl.clear();
			sl.add(nsl);
		}
	}

	public static SymbolList checkHorizontalBar(SymbolList sl) {
		SymbolList tree;
		SymbolNode sn, sd;
		MSTMatrix m;
		int i, j;
		boolean chb, ua;

		chb = MSTPrim.checkHorizontalBar;
		ua = SymbolEdge.isUsingAtractors();

		MSTPrim.checkHorizontalBar = true;
		SymbolEdge.checkHorizontalBar = true;
		SymbolEdge.setUsingAtractors(false);
		tree = MSTPrim.construct(sl);
		SymbolEdge.checkHorizontalBar = false;
		MSTPrim.checkHorizontalBar = chb;
		SymbolEdge.setUsingAtractors(ua);
		m = new MSTMatrix(tree);

		for (i = 0; i < m.size(); i++) {
			sn = tree.symbolNodeAt(i);
			sn.hbMaxY = sn.maxY;
			sn.hbMinY = sn.minY;
			sn.someInDownRegion = sn.someInUpRegion = false;
		}

		for (i = 0; i < m.size(); i++) {
			sn = tree.symbolNodeAt(i);
			for (j = i + 1; j < m.size(); j++) {
				if (m.get(i, j) == 1) {
					sd = tree.symbolNodeAt(j);
					if (sn.isHorizontalBar()) {
						if (sn.dominates(sd)) {
							if (sn.up(sd)) {
								// sn.hbMinY = Math.min(sn.hbMinY,
								// sd.superThreshold);
								sn.hbMinY = Math.min(sn.hbMinY, sd.minY);
								sn.someInUpRegion = true;
							}
							if (sn.down(sd)) {
								// sn.hbMaxY = Math.max(sn.hbMaxY,
								// sd.subscThreshold);
								sn.hbMaxY = Math.max(sn.hbMaxY, sd.maxY);
								sn.someInDownRegion = true;
							}
						}
					}
					if (sd.isHorizontalBar()) {
						if (sd.dominates(sn)) {
							if (sd.up(sn)) {
								// sd.hbMinY = Math.min(sd.hbMinY,
								// sn.superThreshold);
								sd.hbMinY = Math.min(sd.hbMinY, sn.minY);
								sd.someInUpRegion = true;
							}
							if (sd.down(sn)) {
								// sd.hbMaxY = Math.max(sd.hbMaxY,
								// sn.subscThreshold);
								sd.hbMaxY = Math.max(sd.hbMaxY, sn.maxY);
								sd.someInDownRegion = true;
							}
						}
					}
				}
			}
		}

		/*
		 * for(i = 0; i < tree.size(); i++) { tree.symbolNodeAt(i).dad = null; }
		 */

		return tree;
	}

	public static void checkMatrix(SymbolList sl) {
		SymbolList left = new SymbolList();
		SymbolNode snleft, snright;
		int i, j;

		sl.sort();
		// //System.out.println("sl: " + sl);
		for (i = 0; i < sl.size(); i++) {
			// System.out.println(sl.symbolNodeAt(i).name + " ");
			if (sl.symbolNodeAt(i).name.equals("[")) {
				// //System.out.println("sl.symbolNodeAt(i).name.equals(\"[\"): "
				// +
				// sl.symbolNodeAt(i).name.equals("["));
				// //System.out.println("left: " + left);
				left.add(sl.symbolNodeAt(i));
			} else if (sl.symbolNodeAt(i).name.equals("]")) {
				// //System.out.println("sl.symbolNodeAt(i).name.equals(\"]\"): "
				// +
				// sl.symbolNodeAt(i).name.equals("]"));
				// //System.out.println("left: " + left);
				snleft = left.getLastSymbol();
				snright = sl.symbolNodeAt(i);
				if (snleft.rightCloseBracket(snright)
						&& snright.leftCloseBracket(snleft)) {
					snleft.rightWall = snright.centroidX;
					snleft.rightBracket = snright;
					// snleft.minY = Math.min(snleft.minY,snright.minY);
					// snleft.maxY = Math.max(snleft.maxY,snright.maxY);
					left.remove(left.size() - 1);
				}
			}
		}

		// //System.out.println("--------");

		for (i = 0; i < sl.size(); i++) {
			snleft = sl.symbolNodeAt(i);
			// //System.out.println("snleft: "+snleft);
			if (snleft.name.equals("[")) {
				for (j = i + 1; j < sl.size(); j++) {
					// //System.out.println("sl.symbolNodeAt(j): "+sl.symbolNodeAt(j));
					// //System.out.println("snleft.rightBracket: "+snleft.rightBracket);
					if (snleft.row(sl.symbolNodeAt(j))
					// && !snleft.rightBracket.equals(sl.symbolNodeAt(j))
					) {
						sl.symbolNodeAt(j).dad = snleft;
					}
				}
			}
		}
	}

	public void addDadMST(SymbolNode sn) {
		SymbolNode d;

		// //System.out.println(sn+":");

		d = sn;
		while (d.dad != null) {
			d = d.dad;
			// //System.out.println(dad+"->"+sn);
			// if(dad.equals(sn)) return;
		}

		// //System.out.print(d+"->"+sn+"\n");

		if (d.equals(sn)) {
			return;
		}

		if (d.name.equals("[")) {
			d.rows.add(sn);
		} else if (d.isHorizontalBar()) {
			if (sn.centroidY < d.centroidY) {
				d.up.add(sn);
			} else {
				d.down.add(sn);
			}
		} else if (d.name.equals("\\prod")) {
			d.add(sn);
		} else if (d.isVariableRange()) {
			if (sn.centroidY < d.centroidY) {
				d.superscript.add(sn);
			} else {
				d.subscript.add(sn);
			}
		} else if (d.isVariable()) {
			if (sn.centroidY < d.subscThreshold) {
				d.superscript.add(sn);
			} else {
				d.subscript.add(sn);
			}
		} else {
			d.add(sn);
		}
	}

	String rightToString(SymbolList sl) {
		String str = "";
		int i;

		for (i = 0; i < sl.size(); i++) {
			str += sl.symbolNodeAt(i);
			if (!sl.symbolNodeAt(i).right.isEmpty()) {
				str += " & " + rightToString(sl.symbolNodeAt(i).right);
			}
		}

		return str;
	}

	public SymbolList setVerticalProyection(SymbolNode sn, double minf) {
		if (sn.rows.isEmpty()) {
			return null;
		}
		SymbolList sl;
		DPoint p;
		double hist[];
		int indexes[];
		double min, max, f, w;
		int i;

		sl = new SymbolList(sn.rows);
		sn.rows.clear();
		sl.sort();
		sn.atractors.clear();

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

		/*
		 * w = 0.0; count = 0; for(i = 1; i < sl.size(); i++) {
		 * if(sl.symbolNodeAt(i).minX - sl.symbolNodeAt(i - 1).maxX > 0) { w +=
		 * sl.symbolNodeAt(i).minX - sl.symbolNodeAt(i - 1).maxX; count++; } } w
		 * = w/count;
		 */

		w = Math.abs(sl.getMinX() - sn.maxX);

		for (i = 0; i < hist.length; i++) {
			f = (hist[i] - min) / (max - min);
			if (indexes[i] == 1) {
				if (f > minf) {
					p = new DPoint(sl.getMinX() - (1 - f) * w, sl.getMinY() + i);
					// p = new DPoint(sl.getMinX(), sl.getMinY() + i);
					// //((int)(i*sn.height/(1.0*this.getHeight()))));
					sn.atractors.add(p);
				}
			}

		}

		sl.sort();
		SymbolList inTree = new SymbolList();
		// SymbolEdge.setUsingAtractors(true);
		SymbolEdge.setHorizontalFactors(true);
		// SymbolEdge.setTwosideDominace(true);
		SymbolEdge.setDominaceHorizontalFactors(true);
		SymbolEdge.setDistanceFactors(false);
		System.out.println("horf" + SymbolEdge.horf);
		inTree.add(sn);
		SymbolList tree = MSTPrim.construct(sl, inTree);
		SymbolEdge.setHorizontalFactors(false);
		// SymbolEdge.setTwosideDominace(false);
		SymbolEdge.setDominaceHorizontalFactors(false);
		SymbolEdge.setDistanceFactors(true);

		sn.addRows(tree);

		// System.out.println(sn.rowsToString());

		return sl;
	}

	public void addRows(SymbolNode sn) {
		SymbolList sl = new SymbolList(sn.rows);
		SymbolList cl = sn.getProperChildrenSymbolList(sl);

		sn.rows.clear();
		SymbolNode.yCompareTo = true;
		cl.sort();
		SymbolNode.yCompareTo = false;
		if (!cl.isEmpty()) {
			SymbolList row;
			int j;

			for (j = 0; j < cl.size(); j++) {
				row = new SymbolList();
				row.add(new SymbolNode(cl.symbolNodeAt(j)));
				row.add(cl.symbolNodeAt(j).getChildrenSymbolList(sl));
				// row.addSpaces(" & ",1.2);
				// System.out.println("row " + j + ": " + row);
				sn.rows.add(j, (SymbolNode) row);
			}
		}
	}

	public SymbolNode lastInRow(SymbolList sl) {
		SymbolNode sn = sl.getLastSymbol();
		if (sn.right.isEmpty()) {
			return sn;
		} else {
			return lastInRow(sn.right);
		}
	}

	public String rowToString(SymbolList sl) {
		String str = "";
		if (!sl.isEmpty()) {
			SymbolNode sn = lastInRow(sl);
			str += sl + " & " + rowToString(sn.right);
		}

		return str + "\\\\\n";
	}

	public static void findSpaces(SymbolList sl, double f) {
		SymbolList rsl = new SymbolList(), lsl = new SymbolList();
		// SymbolNode sn;
		double space, diff;
		int numspaces, i;
		// int j;

		if (sl.isEmpty()) {
			return;
		}

		space = 0;
		numspaces = 0;
		for (i = 1; i < sl.size(); i++) {
			diff = sl.symbolNodeAt(i).minX - sl.symbolNodeAt(i - 1).maxX;
			if (diff > 0) {
				space += diff;
				numspaces++;
			}
		}

		space = space / numspaces;

		for (i = 1; i < sl.size(); i++) {
			diff = sl.symbolNodeAt(i).minX - sl.symbolNodeAt(i - 1).maxX;
			if (diff > f) {
				numspaces = i;
				break;
			}
		}

		for (i = 0; i < numspaces; i++) {
			lsl.add(sl.symbolNodeAt(i));
		}

		for (i = numspaces; i < sl.size(); i++) {
			rsl.add(sl.symbolNodeAt(i));
		}

		sl.clear();
		findSpaces(rsl, f);
		lsl.getLastSymbol().right.add(rsl);
		sl.add(lsl);
	}

	public void addRight(SymbolNode sn) {
		SymbolList r;
		int i;

		r = BaselineStructureTree.getRightChild(sn);
		// r = sn.right;

		if (sn.isHorizontalBar()) {
			for (i = 0; i < r.size(); i++) {
				if (r.symbolNodeAt(i).centroidY < sn.centroidY) {
					sn.up.add(r.symbolNodeAt(i));
				} else {
					sn.down.add(r.symbolNodeAt(i));
				}
			}
		} else if (sn.name.equals("\\prod")) {
			for (i = 0; i < r.size(); i++) {
				sn.add(r.symbolNodeAt(i));
			}
		} else if (sn.isVariableRange()) {
			for (i = 0; i < r.size(); i++) {
				if (r.symbolNodeAt(i).centroidY < sn.centroidY) {
					sn.superscript.add(r.symbolNodeAt(i));
				} else {
					sn.subscript.add(r.symbolNodeAt(i));
				}
			}
		} else {
			for (i = 0; i < r.size(); i++) {
				sn.add(r.symbolNodeAt(i));
			}
		}

		r.clear();
	}

	/*
	 * public boolean horizontal(SymbolNode s1, SymbolNode s2) {
	 * if(s1.equals(s2)) return false; boolean isHorizontal = false; int s1Sup,
	 * s1Sub, s2Sup, s2Sub; // We intent to make a more flexible definition of
	 * horizontal. if(s1.isOpenBracket()) { s1Sup = s1.minY; s1Sub = s1.maxY; }
	 * else { s1Sup = s1.superThreshold; s1Sub = s1.subscThreshold; }
	 * if(s2.isVariableRange()) { s2Sup = s2.superThreshold; s2Sub =
	 * s2.subscThreshold; } else { s2Sup = s2.minY; s2Sub = s2.maxY; } // Other
	 * definition for horizontal. Which one is the correct? isHorizontal =
	 * (s1Sub > s2.centroidY && s1Sup < s2.centroidY) || (s2Sub > s1.centroidY
	 * && s2Sup < s1.centroidY); // The relation is defined as flexible as
	 * possible, because // we only want to list candidates of horizontal
	 * relation int angle1 = (int)(180*Math.atan2(s2.centroidY -
	 * s1.centroidY,s2.centroidX - s1.centroidX)/Math.PI); int angle2 =
	 * (int)(180*Math.atan2(s2.centroidY - s1.centroidY,-s2.centroidX +
	 * s1.centroidX)/Math.PI); if(s1.isHorizontalBar()) { //isHorizontal = -25 <
	 * angle1 && angle1 < 25; isHorizontal = true; if(s1.dominates(s2)) {
	 * isHorizontal = false; } } else if(s1.isDot()) { isHorizontal = -15 <
	 * angle1 && angle1 < 15; } else if(s1.isRoot() && s1.dominates(s2)) {
	 * isHorizontal = false; } else if(s1.isNonScripted() || s1.isOpenBracket())
	 * { isHorizontal = true; } if(s2.isHorizontalBar()) { isHorizontal =
	 * isHorizontal || -25 < angle2 && angle2 < 25; if((s1.isVariable() ||
	 * s1.isDigit()) && s1.subscript(s2) && !s2.dominates(s1)) { isHorizontal =
	 * true; } } else if(s1.isNonScripted() && s2.isDot()) { isHorizontal = -15
	 * < angle2 && angle2 < 15; } return isHorizontal && s1.maxX < s2.centroidX;
	 * }
	 */

	/*
	 * // 22.07.03 public boolean horizontal(SymbolNode s1, SymbolNode s2) {
	 * if(s1.equals(s2)) { return false; } boolean isHorizontal = false; int
	 * s1Sup, s1Sub, s2Sup, s2Sub; // We intent to make a more flexible
	 * definition of horizontal. if(s1.isOpenBracket()) { s1Sup = s1.minY; s1Sub
	 * = s1.maxY; } else { s1Sup = s1.superThreshold; s1Sub = s1.subscThreshold;
	 * } if(s2.isVariableRange()) { s2Sup = s2.superThreshold; s2Sub =
	 * s2.subscThreshold; } else { s2Sup = s2.minY; s2Sub = s2.maxY; } // Other
	 * definition for horizontal. Which one is the correct? isHorizontal =
	 * (s1Sub > s2.centroidY && s1Sup < s2.centroidY) || (s2Sub > s1.centroidY
	 * && s2Sup < s1.centroidY); // The relation is defined as flexible as
	 * possible, because // we only want to list candidates of horizontal
	 * relation // int angle = (int)(180*Math.atan2(s2.centroidY -
	 * s1.centroidY,s2.centroidX - s1.centroidX)/Math.PI); int angle = (int)
	 * (180 * Math.atan2(s2.centroidY - s1.centroidY, s2.centroidX - s1.maxX) /
	 * Math.PI); if(s1.isHorizontalBar()) { if(s2.isHorizontalBar() ||
	 * s2.isDot()) { isHorizontal = isHorizontal || ( -25 < angle && angle <
	 * 25); } //isHorizontal = true; if(s1.dominates(s2)) { isHorizontal =
	 * false; } } else if(s1.isDot()) { isHorizontal = isHorizontal || -15 <
	 * angle && angle < 15;
	 * ////System.out.println("s1.isDot()! isHorizontal="+isHorizontal
	 * +" angle="+angle); } else if(s1.isRoot() && s1.dominates(s2)) {
	 * isHorizontal = false; } //else if(s1.isNonScripted() ||
	 * s1.isOpenBracket()) { // isHorizontal = true; //} //angle =
	 * (int)(180*Math.atan2(s2.centroidY - s1.centroidY,s2.centroidX -
	 * s1.centroidX)/Math.PI); angle = (int) (180 * Math.atan2(s2.centroidY -
	 * s1.centroidY, s2.minX - s1.centroidX) / Math.PI); if(s2.isHorizontalBar()
	 * && !s2.dominates(s1)) { //isHorizontal = isHorizontal || ( -25 < angle &&
	 * angle < 25); //if((s1.isVariable() || s1.isDigit()) && s1.subscript(s2)
	 * && !s2.dominates(s1)) { // isHorizontal = true; //}
	 * if(!s1.isVariableRange() && s1.subscThreshold < s2.centroidY) {
	 * isHorizontal = true; } } else if(s1.isNonScripted() && s2.isDot()) {
	 * isHorizontal = -25 < angle && angle < 25; } return isHorizontal &&
	 * s1.maxX < s2.centroidX; }
	 */

	// Definition 21.07.03
	public boolean horizontal(SymbolNode s1, SymbolNode s2) {
		if (s1.equals(s2)) {
			return false;
		}
		// if(s1.name.equals("\\matrix_space") ||
		// s2.name.equals("\\matrix_space"))
		// return true;
		boolean isHorizontal = false;
		int s1Sup, s1Sub, s2Sup, s2Sub;
		// We intent to make a more flexible definition of horizontal.
		if (s1.isOpenBracket()) {
			s1Sup = s1.minY;
			s1Sub = s1.maxY;
		} else {
			s1Sup = s1.superThreshold;
			s1Sub = s1.subscThreshold;
		}

		if (s2.isVariableRange()) {
			s2Sup = s2.superThreshold;
			s2Sub = s2.subscThreshold;
		} else {
			s2Sup = s2.minY;
			s2Sub = s2.maxY;
		}
		// Other definition for horizontal. Which one is the correct?
		isHorizontal = (s1Sub > s2.centroidY && s1Sup < s2.centroidY)
				|| (s2Sub > s1.centroidY && s2Sup < s1.centroidY);
		// The relation is defined as flexible as possible, because
		// we only want to list candidates of horizontal relation
		// int angle = (int)(180*Math.atan2(s2.centroidY -
		// s1.centroidY,s2.centroidX - s1.centroidX)/Math.PI);
		int angle = (int) (180 * Math.atan2(s2.centroidY - s1.centroidY,
				s2.centroidX - s1.centroidX) / Math.PI);
		if (s1.isHorizontalBar()) {
			isHorizontal = isHorizontal || (-35 < angle && angle < 35);
			// isHorizontal = true;
			if (s1.dominates(s2)) {
				isHorizontal = false;
			}
		} else if (s1.isDot()) {
			isHorizontal = isHorizontal || -15 < angle && angle < 15;
			// //System.out.println("s1.isDot()! isHorizontal="+isHorizontal+" angle="+angle);
		} else if (s1.isRoot() && s1.dominates(s2)) {
			isHorizontal = false;
		}
		// else if(s1.isNonScripted() || s1.isOpenBracket()) {
		// isHorizontal = true;
		// }
		// angle = (int)(180*Math.atan2(s2.centroidY - s1.centroidY,s2.centroidX
		// - s1.centroidX)/Math.PI);
		angle = (int) (180 * Math.atan2(s2.centroidY - s1.centroidY, s2.minX
				- s1.centroidX) / Math.PI);
		/*
		 * if(s2.isHorizontalBar() && !s2.dominates(s1)) { //isHorizontal =
		 * isHorizontal || ( -25 < angle && angle < 25); //if((s1.isVariable()
		 * || s1.isDigit()) && s1.subscript(s2) && !s2.dominates(s1)) { //
		 * isHorizontal = true; //} if(!s1.isVariableRange() &&
		 * s1.subscThreshold < s2.centroidY) { isHorizontal = true; } }
		 */
		if ((s2.someInUpRegion || s2.someInDownRegion) && !s2.dominates(s1)) {
			if (s1.isNonScripted()) {
				isHorizontal = isHorizontal
						|| (s1.centroidY > s2.hbMinY && s1.centroidY < s2.hbMaxY);
			} else if (!s1.isVariableRange()) {
				isHorizontal = (isHorizontal || (s1.centroidY > s2.hbMinY && s1.centroidY < s2.centroidY))
						&& s2.centroidY > s1.superThreshold;
			}
		} else if (s2.isDot() && s1.isNonScripted()) {
			isHorizontal = -25 < angle && angle < 25;
		}
		return isHorizontal && s1.maxX < s2.centroidX;
	}

	/*
	 * public boolean horizontal(SymbolNode s1, SymbolNode s2) {
	 * if(s1.equals(s2)) return false; boolean isHorizontal = false; int s1Sup,
	 * s1Sub, s2Sup, s2Sub; // We intent to make a more flexible definition of
	 * horizontal. if(s1.isOpenBracket()) { s1Sup = s1.minY; s1Sub = s1.maxY; }
	 * else { s1Sup = s1.superThreshold; s1Sub = s1.subscThreshold; }
	 * if(s2.isVariableRange()) { s2Sup = s2.superThreshold; s2Sub =
	 * s2.subscThreshold; } else { s2Sup = s2.minY; s2Sub = s2.maxY; } // Other
	 * definition for horizontal. Which one is the correct? isHorizontal =
	 * (s1Sub > s2.centroidY && s1Sup < s2.centroidY) || (s2Sub > s1.centroidY
	 * && s2Sup < s1.centroidY); // The relation is defined as flexible as
	 * possible, because // we only want to list candidates of horizontal
	 * relation int angle1 = (int)(180*Math.atan2(s2.centroidY -
	 * s1.centroidY,s2.centroidX - s1.centroidX)/Math.PI); int angle2 =
	 * (int)(180*Math.atan2(s2.centroidY - s1.centroidY,-s2.centroidX +
	 * s1.centroidX)/Math.PI); if(s1.isHorizontalBar()) { isHorizontal = -25 <
	 * angle1 && angle1 < 25; //isHorizontal = true; if(s1.dominates(s2)) {
	 * isHorizontal = false; } } else if(s1.isDot()) { isHorizontal = -15 <
	 * angle1 && angle1 < 15; } else if(s1.isRoot() && s1.dominates(s2)) {
	 * isHorizontal = false; } //else if(s1.isNonScripted() ||
	 * s1.isOpenBracket()) { // isHorizontal = true; //}
	 * if(s2.isHorizontalBar()) { isHorizontal = isHorizontal || -25 < angle2 &&
	 * angle2 < 25; if((s1.isVariable() || s1.isDigit()) && s1.subscript(s2) &&
	 * !s2.dominates(s1)) { isHorizontal = true; } } else if(s1.isNonScripted()
	 * && s2.isDot()) { isHorizontal = -15 < angle2 && angle2 < 15; } return
	 * isHorizontal && s1.maxX < s2.centroidX; }
	 */

	public SymbolList horizontal(SymbolList sl1, SymbolList sl2) {
		// if(sl2.isEmpty()) return sl1;

		SymbolNode ss = sl1.get(sl1.size() - 1), sj, si;
		SymbolList hor, noHor;
		int i, j;
		// int lenhor;

		hor = new SymbolList();
		noHor = new SymbolList();

		for (i = 0; i < sl2.size(); i++) {
			si = sl2.symbolNodeAt(i);
			if (horizontal(ss, sl2.symbolNodeAt(i))) {
				hor.add(new SymbolNode(si));
			} else if (si.minX >= ss.minX) {
				noHor.add(new SymbolNode(si));
			}
		}

		for (j = noHor.size() - 1; j > -1; j--) {
			sj = noHor.symbolNodeAt(j);
			if (sj.isFractionBar() && sj.centroidY < ss.superThreshold) {
				for (i = hor.size() - 1; i > -1; i--) {
					si = hor.symbolNodeAt(i);
					if (sj.dominates(si) && si.height < ss.height) {
						hor.remove(i);
					}
				}
			}
		}

		// //System.out.println("sl1: "+sl1);
		// //System.out.println("hor: "+hor);
		// //System.out.println("sl2:"+sl2+"\n");

		if (hor.isEmpty()) {
			return sl1;
		}

		SymbolNode startSymbol = new SymbolNode(hor.startSymbolNode());

		/*
		 * if(!ss.isDot() && startSymbol.isHorizontalBar() && ss.subscThreshold
		 * < startSymbol.centroidY) { boolean someUp = false; for(i = 0; i <
		 * hor.size(); i++) { if( //startSymbol.dominates(hor.symbolNodeAt(i))
		 * //&& startSymbol.up(hor.symbolNodeAt(i)) //&&
		 * //(hor.symbolNodeAt(i).isHorizontalBar() &&
		 * ss.right(hor.symbolNodeAt(i))) ) { someUp = true; break; } }
		 * if(someUp) { sl1.add(startSymbol); } else {
		 * sl2.removeElement(startSymbol); } } else {
		 */
		sl1.add(startSymbol);
		// }
		return horizontal(sl1, sl2);
		// return horizontal(sl1, noHor);
	}

	public SymbolList horizontal(SymbolList sl) {
		SymbolNode ss;
		SymbolList sl1, sl2;

		ss = new SymbolNode(sl.startSymbolNode());

		sl1 = new SymbolList();
		sl1.add(ss);
		sl2 = new SymbolList();
		sl2.add(sl);

		// //System.out.println("sl1: "+sl1);
		// //System.out.println("sl2:"+sl2+"\n");

		return horizontal(sl1, sl2);
	}

	/*
	 * public void checkVariableRange(SymbolNode sn, SymbolNode vr) {
	 * if(!vr.isVariableRange()) return; if(!sn.superscript.isEmpty()) { boolean
	 * found = false; int i; for(i = 0; i < vr.superscript.size(); i++) {
	 * if(found = vr.up(vr.superscript.symbolNodeAt(i))) { break; } } if(found)
	 * { } } }
	 */

	public void checkVariableRange(SymbolNode sn, SymbolNode vr) {
		if (!vr.isVariableRange()) {
			return;
		}
		if (!sn.superscript.isEmpty() || !sn.subscript.isEmpty()) {
			SymbolList nodes = new SymbolList();
			SymbolList inTree = new SymbolList();
			SymbolList tree;
			SymbolList snchild, vrchild;
			SymbolNode snAux;

			int i;

			nodes.add(sn);
			nodes.add(vr);

			inTree.add(nodes);

			nodes.add(vr.superscript);
			nodes.add(vr.subscript);

			nodes.add(sn.superscript);
			nodes.add(sn.subscript);

			// //System.out.println("nodes!:"+nodes);
			// //System.out.println("inTree!:"+inTree);

			tree = SymbolMinimumSpanningTree.construct(nodes, inTree);

			vr.superscript.clear();
			vr.subscript.clear();

			sn.superscript.clear();
			sn.subscript.clear();

			// //System.out.println("tree!:\n"+tree.toStringRight());

			vrchild = getRightChild(tree.symbolNodeAt(1));
			snchild = getRightChild(tree.symbolNodeAt(0));

			// //System.out.println(tree.toStringRight());
			// //System.out.println(toStrigRightChilds(tree));
			// //System.out.println("vrchild:"+tree.symbolNodeAt(1)+" -> "+vrchild);
			// //System.out.println("snchild:"+tree.symbolNodeAt(0)+" -> "+snchild);

			for (i = 0; i < vrchild.size(); i++) {
				snAux = vrchild.symbolNodeAt(i);
				if (snAux.centroidY < vr.superThreshold) {
					vr.superscript.add(snAux);
				} else if (snAux.centroidY > vr.subscThreshold) {
					vr.subscript.add(snAux);
				} else {
					snchild.add(snAux);
				}
			}

			// //System.out.println("snchild:"+snchild);

			for (i = 0; i < snchild.size(); i++) {
				// sn.add(snchild.symbolNodeAt(i));
				snAux = snchild.symbolNodeAt(i);
				if (snAux.centroidY < sn.superThreshold) {
					sn.superscript.add(snAux);
				} else if (snAux.centroidY > sn.subscThreshold) {
					sn.subscript.add(snAux);
				}
			}

			/*
			 * if(sn.isVariableRange()) { for(i = 0; i < snchild.size(); i++) {
			 * sn.add(snchild.symbolNodeAt(i)); } } else { for(i = 0; i <
			 * snchild.size(); i++) { snAux = snchild.symbolNodeAt(i);
			 * if(snAux.centroidY < vr.minY) { vr.superscript.add(snAux); } else
			 * if(snAux.centroidY > vr.maxY) { vr.subscript.add(snAux); } else {
			 * sn.add(snAux); } } }
			 */
		}

	}

	public void checkVariableRange(SymbolList sl) {
		if (sl.isEmpty()) {
			return;
		}
		SymbolNode sn = sl.symbolNodeAt(0);

		if (sn.isVariableRange()) {
			sn.superscript.add(sn.upleft);
			sn.upleft.clear();
			sn.superscript.add(sn.up);
			sn.up.clear();

			sn.subscript.add(sn.downleft);
			sn.downleft.clear();
			sn.subscript.add(sn.down);
			sn.down.clear();
		}

		for (int i = 1; i < sl.size(); i++) {
			this.checkVariableRange(sl.symbolNodeAt(i - 1), sl.symbolNodeAt(i));
		}
	}

	/*
	 * public void checkVariableRange(SymbolNode sn, SymbolNode vr) {
	 * if(!vr.isVariableRange()) return; if(!sn.superscript.isEmpty() ||
	 * !vr.up.isEmpty()) { SymbolList nodes = new SymbolList(); SymbolList
	 * inTree = new SymbolList(); SymbolList tree; SymbolList snchild, vrchild;
	 * SymbolNode snAux; int i; nodes.add(sn); nodes.add(vr); inTree.add(nodes);
	 * nodes.add(vr.superscript); nodes.add(vr.up); nodes.add(sn.superscript);
	 * //System.out.println(" nodes:"+nodes);
	 * //System.out.println("inTree:"+inTree); tree =
	 * SymbolMinimumSpanningTree.construct(nodes,inTree);
	 * vr.superscript.clear(); vr.up.clear(); sn.superscript.clear(); vrchild =
	 * getRightChild(tree.symbolNodeAt(1)); snchild =
	 * getRightChild(tree.symbolNodeAt(0));
	 * //System.out.println(tree.toStringRight());
	 * //System.out.println(toStrigRightChilds(tree));
	 * //System.out.println("vrchild:"+tree.symbolNodeAt(1)+" -> "+vrchild);
	 * //System.out.println("snchild:"+tree.symbolNodeAt(2)+" -> "+snchild);
	 * for(i = 0; i < vrchild.size(); i++) { snAux = vrchild.symbolNodeAt(i);
	 * if(snAux.centroidY < vr.superThreshold) { vr.superscript.add(snAux); }
	 * else { snchild.add(snAux); } } //System.out.println("snchild:"+snchild);
	 * for(i = 0; i < snchild.size(); i++) { sn.add(snchild.symbolNodeAt(i)); }
	 * } if(!sn.subscript.isEmpty() || !sn.down.isEmpty()) { SymbolList nodes =
	 * new SymbolList(); SymbolList inTree = new SymbolList(); SymbolList tree;
	 * SymbolList snchild, vrchild; SymbolNode snAux; int i; nodes.add(sn);
	 * nodes.add(vr); inTree.add(nodes); nodes.add(vr.subscript);
	 * nodes.add(vr.down); nodes.add(sn.subscript);
	 * //System.out.println(" nodes:"+nodes);
	 * //System.out.println("inTree:"+inTree); tree =
	 * SymbolMinimumSpanningTree.construct(nodes,inTree); vr.subscript.clear();
	 * vr.down.clear(); sn.subscript.clear(); vrchild =
	 * getRightChild(tree.symbolNodeAt(1)); snchild =
	 * getRightChild(tree.symbolNodeAt(0));
	 * //System.out.println(tree.toStringRight());
	 * //System.out.println(toStrigRightChilds(tree));
	 * //System.out.println("vrchild:"+tree.symbolNodeAt(1)+" -> "+vrchild);
	 * //System.out.println("snchild:"+tree.symbolNodeAt(2)+" -> "+snchild);
	 * for(i = 0; i < vrchild.size(); i++) { snAux = vrchild.symbolNodeAt(i);
	 * if(snAux.centroidY > vr.subscThreshold) { vr.subscript.add(snAux); } else
	 * { snchild.add(snAux); } } //System.out.println("snchild:"+snchild); for(i
	 * = 0; i < snchild.size(); i++) { sn.add(snchild.symbolNodeAt(i)); } } }
	 */

	public static SymbolList getRightChild(SymbolNode sn) {
		SymbolList sl = new SymbolList();

		if (!sn.right.isEmpty()) {
			for (int i = 0; i < sn.right.size(); i++) {
				sl.add(getRightChild(sn.right.symbolNodeAt(i)));
				sl.add(sn.right.symbolNodeAt(i));
			}
		}

		return sl;
	}

	static public String toStrigRightChilds(SymbolList sl) {
		String str = "";

		for (int i = 0; i < sl.size(); i++) {
			str += sl.symbolNodeAt(i) + " -> "
					+ getRightChild(sl.symbolNodeAt(i)) + "\n";
		}

		return str;
	}

	public SymbolList hor(SymbolList sl1, SymbolList sl2) {
		if (sl1.isEmpty()) {
			return sl1;
		}

		SymbolList remainigSymbols;
		SymbolList slNew;
		SymbolNode sn;

		sn = new SymbolNode(sl1.get(sl1.size() - 1));
		remainigSymbols = new SymbolList();
		for (int i = 0; i < sl2.size(); i++) {
			if (!sn.addNotHorizontal(sl2.symbolNodeAt(i))) {
				remainigSymbols.add(sl2.symbolNodeAt(i));
			}
		}

		sl1.set(sl1.size() - 1, sn);

		if (remainigSymbols.isEmpty()) {
			return sl1;
		}

		if (sn.type.equals("NON_SCRIPTED")) {
			slNew = new SymbolList();

			slNew.add(sl1);
			slNew.add(startSymbolNode(remainigSymbols));
			return hor(slNew, remainigSymbols);
		}

		SymbolList sl = new SymbolList();
		SymbolNode s;
		sl.add(remainigSymbols);

		while (!sl.isEmpty()) {
			s = sl.symbolNodeAt(0);

			if (sn.right(s)) {
				slNew = new SymbolList();

				slNew.add(sl1);
				slNew.add(checkDominance(s, remainigSymbols));
				return hor(slNew, remainigSymbols);
			}

			sl.remove(0);
		}

		sl1.add(sn);

		return sl1;
	}

	public SymbolNode checkDominance(SymbolNode sn, SymbolList sl) {
		SymbolList slnew = new SymbolList();

		slnew.add(sl);
		slnew.add(sn);

		return slnew.startSymbolNode();
	}

	public SymbolNode startSymbolNode(SymbolList sl) {
		if (sl.isEmpty()) {
			return null;
		}
		if (sl.size() == 1) {
			return sl.symbolNodeAt(0);
		}

		SymbolList remaining = new SymbolList();
		int n = sl.size() - 1;

		remaining.add(sl);
		/*
		 * if( sl.symbolNodeAt(n).overlaps(sl.symbolNodeAt(n - 1)) ||
		 * sl.symbolNodeAt(n).subexpression(sl.symbolNodeAt(n - 1)) )
		 */
		if (sl.symbolNodeAt(n - 1).dominates(sl.symbolNodeAt(n))) {
			remaining.remove(n);
		} else {
			remaining.remove(n - 1);
		}

		return startSymbolNode(remaining);
	}

	public String interpret(SymbolList sl) {
		String str = "";

		if (sl.isEmpty()) {
			return str;
		}

		SymbolNode sn;
		// String name;
		for (int i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);
			/*
			 * if(sn.name.equals("-")) { if(!sn.up.isEmpty() &&
			 * !sn.down.isEmpty()) { str +=
			 * "("+interpret(sn.up)+"/"+interpret(sn.down)+")"; } }
			 */

			if (sn.name.equals(dot) && i > 0) {
				str += "*";
				sn.name = "*";
			}
			// else if(!sn.subexpression.isEmpty() && sn.name.equals("\\sqrt"))
			// {
			// str += "("+interpret(sn.subexpression)+")^(0.5)";
			// //str += "\\sqrt{"+interpret(sn.subexpression)+"}";
			// }
			else if (!sn.up.isEmpty() && !sn.down.isEmpty()
					&& sn.name.equals("-")) {
				str += "(" + interpret(sn.up) + ")/(" + interpret(sn.down)
						+ ")";
				// str +=
				// "\\frac{"+interpret(sn.up)+"}{"+interpret(sn.down)+"}";
			} else if (!sn.up.isEmpty()
					&& sn.name.equals("\\iota")
					&& (sn.up.isFirst("-") || sn.up.isFirst(dot) || sn.up
							.isFirst("/"))) {
				str += "i";
				// str +=
				// "\\frac{"+interpret(sn.up)+"}{"+interpret(sn.down)+"}";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
				// str +=
				// "\\frac{"+interpret(sn.up)+"}{"+interpret(sn.down)+"}";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
				// str +=
				// "\\frac{"+interpret(sn.up)+"}{"+interpret(sn.down)+"}";
			} else if (!sn.name.equals("\\sqrt")) {
				str += sn.name;
			}

			if (!sn.subexpression.isEmpty()) {
				str += "((" + interpret(sn.subexpression) + ")^(0.5))";
				// str += "\\sqrt{"+interpret(sn.subexpression)+"}";
			}
			if (!sn.superscript.isEmpty() && sn.type.equals("NON_SCRIPTED")) {
				// sn.up.add(sn.superscript);
				// str += "^{"+interpret(sn.superscript)+"}";
				str += interpret(sn.superscript);
			} else if (!sn.superscript.isEmpty() && sn.name.equals(dot)) {
				// sn.up.add(sn.superscript);
				// str += "^{"+interpret(sn.superscript)+"}";
				str += interpret(sn.superscript);
			} else if (!sn.superscript.isEmpty()) {
				// sn.up.add(sn.superscript);
				// str += "^{"+interpret(sn.superscript)+"}";
				str += "^(" + interpret(sn.superscript) + ")";
			}
			if (!sn.subscript.isEmpty() && sn.type.equals("NON_SCRIPTED")) {
				str += interpret(sn.subscript);
			} else if (!sn.subscript.isEmpty() && sn.subscript.isFirst(dot)) {
				str += ".";
			} else if (!sn.subscript.isEmpty()) {
				// sn.down.add(sn.subscript);
				// str += "_{"+interpret(sn.subscript)+"}";
				str += "_(" + interpret(sn.subscript) + ")";
				// str += interpret(sn.subscript);
			}

		}

		return str;
	}

	public String interpretLaTeX(SymbolList sl) {
		String str = "";

		if (sl.isEmpty()) {
			return str;
		}

		SymbolNode sn;
		// String name;
		for (int i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);

			if (sn.isOpenBracket()) {
				sn.name = "\\left" + sn.name;
			} else if (sn.isCloseBracket()) {
				sn.name = "\\right" + sn.name;
			}
			if (sn.name.equals("\\matrix_space")) {
				sn.name = " & ";
			}
			if (sn.name.equals("\\arrow")) {
				sn.name = "\\to";
			}
			if (sn.name.equals("comma")) {
				sn.name = "\\prime";
			}
			if (sn.name.equals(dot)) {
				if (i > 0) {
					str += "*";
					sn.name = "*";
				}
			} else if (!sn.up.isEmpty() && !sn.down.isEmpty()
					&& sn.name.equals("-")) {
				str += "\\frac{" + interpretLaTeX(sn.up) + "}{"
						+ interpretLaTeX(sn.down) + "}";
			} else if (!sn.down.isEmpty() && sn.name.equals("\\arrow")) {
				str += "\\vec{" + interpretLaTeX(sn.down) + "}";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("\\arrow")) {
				str += "\\vec{" + sn.name + "}";
			}
			/*
			 * else if(sn.up.size() == 1 && sn.name.equals("1") &&
			 * (sn.up.isFirst("-") || sn.up.isFirst(dot) || sn.up.isFirst("/")))
			 * { str += "i"; sn.up.clear(); } else if(sn.up.size() == 1 &&
			 * sn.name.equals("/") && (sn.up.isFirst("-") || sn.up.isFirst(dot)
			 * || sn.up.isFirst("/"))) { str += "i"; sn.up.clear(); } else
			 * if(sn.up.size() == 1 && sn.name.equals("\\iota") &&
			 * (sn.up.isFirst("-") || sn.up.isFirst(dot) || sn.up.isFirst("/")))
			 * { str += "i"; sn.up.clear(); } else if(sn.superscript.size() == 1
			 * && sn.name.equals("1") && (sn.superscript.isFirst("-") ||
			 * sn.superscript.isFirst(dot) || sn.superscript.isFirst("/"))) {
			 * str += "i"; sn.superscript.clear(); } else
			 * if(sn.superscript.size() == 1 && sn.name.equals("/") &&
			 * (sn.superscript.isFirst("-") || sn.superscript.isFirst(dot) ||
			 * sn.superscript.isFirst("/"))) { str += "i";
			 * sn.superscript.clear(); } else if(sn.superscript.size() == 1 &&
			 * sn.name.equals("\\iota") && (sn.superscript.isFirst("-") ||
			 * sn.superscript.isFirst(dot) || sn.superscript.isFirst("/"))) {
			 * str += "i"; sn.superscript.clear(); } else if(sn.up.size() == 1
			 * && sn.name.equals("\\left(") && (sn.up.isFirst("-") ||
			 * sn.up.isFirst(dot) || sn.up.isFirst("/"))) { str += "i";
			 * sn.up.clear(); } else if(sn.superscript.size() == 1 &&
			 * sn.name.equals("\\left(") && (sn.superscript.isFirst("-") ||
			 * sn.superscript.isFirst(dot) || sn.superscript.isFirst("/"))) {
			 * str += "i"; sn.superscript.clear(); } else if(sn.up.size() == 1
			 * && sn.name.equals("\\right)") && (sn.up.isFirst("-") ||
			 * sn.up.isFirst(dot) || sn.up.isFirst("/"))) { str += "j";
			 * sn.up.clear(); } else if(sn.superscript.size() == 1 &&
			 * sn.name.equals("\\right)") && (sn.superscript.isFirst("-") ||
			 * sn.superscript.isFirst(dot) || sn.superscript.isFirst("/"))) {
			 * str += "j"; sn.superscript.clear(); } else if(sn.up.size() == 1
			 * && sn.name.equals("3") && (sn.up.isFirst("-") ||
			 * sn.up.isFirst(dot) || sn.up.isFirst("/"))) { str += "j";
			 * sn.up.clear(); } else if(sn.superscript.size() == 1 &&
			 * sn.name.equals("3") && (sn.superscript.isFirst("-") ||
			 * sn.superscript.isFirst(dot) || sn.superscript.isFirst("/"))) {
			 * str += "j"; sn.superscript.clear(); }
			 */

			else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("<")
					&& sn.name.equals("-")) {
				str += "\\leq ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst(">")
					&& sn.name.equals("-")) {
				str += "\\geq ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("<")) {
				str += "\\leq ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals(">")) {
				str += "\\geq ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("+")
					&& sn.name.equals("-")) {
				str += "\\pm ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("+")) {
				str += "\\mp ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("+")
					&& sn.name.equals("-")) {
				str += "\\mp ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("+")) {
				str += "\\pm ";
			} else if (sn.up.isEmpty() && !sn.down.isEmpty()
					&& sn.name.equals("-")) {
				str += "\\overline{" + interpretLaTeX(sn.down) + "}";
			} else if (!sn.up.isEmpty() && sn.down.isEmpty()
					&& sn.name.equals("-")) {
				str += "\\underline{" + interpretLaTeX(sn.up) + "}";
			} else if (!sn.name.equals("\\sqrt") && !sn.name.equals("\\prod")) {
				str += sn.name;
				if (sn.name.charAt(0) == '\\') {
					str += " ";
				}
			}

			if (!sn.rows.isEmpty()) {
				str += "\\begin{matrix}\n";
				// str += "\\pmatrix{";
				for (int j = 0; j < sn.rows.size(); j++) {
					str += interpretLaTeX((sn.rows.get(j)));
					// str += "\\cr\n";
					str += "\\\\\n";
				}
				// str += "}";
				str += "\\end{matrix}";
			}

			if (sn.name.equals("\\prod")) {
				str += "\\sideset{^{" + interpretLaTeX(sn.upleft) + "}_{"
						+ interpretLaTeX(sn.downleft) + "}}" + "{^{"
						+ interpretLaTeX(sn.superscript) + "}_{"
						+ interpretLaTeX(sn.subscript) + "}}" + "\\prod ";
				continue;
			}

			if (!sn.subexpression.isEmpty() && sn.name.equals("\\sqrt")) {
				str += "\\sqrt{" + interpretLaTeX(sn.subexpression) + "}";
			}

			if (!sn.superscript.isEmpty() && sn.superscript.isFirst(dot)) {
				str += "^{."
						+ interpretLaTeX(sn.superscript.get(0).superscript)
						+ "}";
			} else if (!sn.superscript.isEmpty() && sn.isDot()) {
				str += "." + interpretLaTeX(sn.superscript);
			} else if (!sn.superscript.isEmpty() && sn.isNonScripted()) {
				str += interpretLaTeX(sn.superscript);
			} else if (!sn.superscript.isEmpty()) {
				str += "^{" + interpretLaTeX(sn.superscript) + "}";
			}

			if (!sn.subscript.isEmpty()
					&& sn.subscript.get(sn.subscript.size() - 1).isComma()) {
				sn.subscript.remove(sn.subscript.size() - 1);
				if (sn.subscript.isEmpty()) {
					str += ",";
				} else {
					str += "_{" + interpretLaTeX(sn.subscript) + "},";
				}
			} else if (!sn.subscript.isEmpty()
					&& sn.subscript.toSequenceString().equals("dot")) {
				str += ".";
			} else if (!sn.subscript.isEmpty()
					&& sn.type.equals("NON_SCRIPTED")) {
				str += interpretLaTeX(sn.subscript);
			} else if (!sn.subscript.isEmpty()) {
				str += "_{" + interpretLaTeX(sn.subscript) + "}";
			}

		}

		return str;
	}

	public String interpretBasic() {
		String output = interpretBasic(symbolList);

		output = oldReplace(output, " ", "");

		output = oldReplace(output, "[", "");
		output = oldReplace(output, "\\left", "");
		output = oldReplace(output, "&", "");

		output = oldReplace(output, "=", " = ");
		output = oldReplace(output, "+", " + ");
		output = oldReplace(output, "-", " - ");
		output = oldReplace(output, "*", " * ");
		output = oldReplace(output, "/", " / ");

		output = oldReplace(output, "n0p", " nop ");
		output = oldReplace(output, "h0p", " nop ");
		output = oldReplace(output, "let", " let ");
		output = oldReplace(output, "let i", "leti ");
		output = oldReplace(output, "g0t0", " goto ");
		output = oldReplace(output, "90t0", " goto ");
		output = oldReplace(output, "st0p", " stop ");
		output = oldReplace(output, "print", " print ");
		output = oldReplace(output, "p\\sigmaint", " print ");
		// output = oldReplace(output, "print", " PRINT ");
		output = oldReplace(output, "list", "list");
		output = oldReplace(output, "\\sigmann", "run");

		output = oldReplace(output, ".*", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}.", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}*", ":");
		output = oldReplace(output, ",*", ";");

		return output.toUpperCase();
	}

	public String interpretBasic(SymbolList sl) {
		String str = "";

		if (sl.isEmpty()) {
			return str;
		}

		SymbolNode sn;
		// String name;
		for (int i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);

			if (sn.name.equals("\\matrix_space")) {
				sn.name = " ";
			}
			if (sn.name.equals(dot)) {
				if (i > 0) {
					str += " * ";
					sn.name = "*";
				}
			} else if (!sn.up.isEmpty() && !sn.down.isEmpty()
					&& sn.name.equals("-")) {
				str += interpretBasic(sn.up) + " / " + interpretBasic(sn.down);
			} else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("-")) {
				str += " = ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("-")) {
				str += " = ";
			} else if (!sn.name.equals("\\sqrt") && !sn.name.equals("\\prod")) {
				str += sn.name;
				if (sn.name.charAt(0) == '\\') {
					str += " ";
				}
			}

			if (!sn.rows.isEmpty()) {
				for (int j = 0; j < sn.rows.size(); j++) {
					str += interpretBasic((sn.rows.get(j)));
					str += "\n";
				}
			}

			if (!sn.superscript.isEmpty() && sn.superscript.isFirst(dot)) {
				str += "^ ."
						+ interpretBasic(sn.superscript.get(0).superscript);
			} else if (!sn.superscript.isEmpty() && sn.isDot()) {
				str += "." + interpretBasic(sn.superscript);
			} else if (!sn.superscript.isEmpty() && sn.isNonScripted()) {
				str += interpretBasic(sn.superscript);
			} else if (!sn.superscript.isEmpty()) {
				str += " ^ " + interpretBasic(sn.superscript);
			}

			if (!sn.subscript.isEmpty()
					&& sn.subscript.get(sn.subscript.size() - 1).isComma()) {
				sn.subscript.remove(sn.subscript.size() - 1);
				if (sn.subscript.isEmpty()) {
					str += ",";
				} else {
					str += interpretBasic(sn.subscript) + ",";
				}
			} else if (!sn.subscript.isEmpty()
					&& sn.subscript.toSequenceString().equals("dot")) {
				str += ".";
			} else if (!sn.subscript.isEmpty()
					&& sn.type.equals("NON_SCRIPTED")) {
				str += interpretBasic(sn.subscript);
			} else if (!sn.subscript.isEmpty()) {
				str += interpretBasic(sn.subscript);
			}

		}

		return str;
	}

	public String interpret() {
		if (interpreter == LATEX) {
			return interpretLaTeX();
		}
		if (interpreter == MATHEMATICA) {
			return interpretMathematica();
		}
		if (interpreter == BASIC) {
			return interpretBasic();
		}
		return "";
	}

	public String interpretRead() {
		return interpretRead(symbolList);
	}

	/**
	 * If Java 1.4 is unavailable, the following technique may be used.
	 * 
	 * @param aInput
	 *            is the original String which may contain substring
	 *            aOldPattern.
	 * @param aOldPattern
	 *            is the substring which is to be replaced
	 * @param aNewPattern
	 *            is the replacement for aOldPattern
	 */
	public static String oldReplace(final String aInput,
			final String aOldPattern, final String aNewPattern) {

		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(aInput.substring(startIdx, idxOld));
			// add aNewPattern to take place of aOldPattern
			result.append(aNewPattern);

			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + aOldPattern.length();
		}
		// the final chunk will go to the end of aInput
		result.append(aInput.substring(startIdx));
		return result.toString();
	}

	public String interpretLaTeX() {
		String output = interpretLaTeX(symbolList);

		output = oldReplace(output, "cos", "\\cos");
		output = oldReplace(output, "cot", "\\cot");
		output = oldReplace(output, "cosh", "\\cosh");
		output = oldReplace(output, "coth", "\\coth");

		output = oldReplace(output, "sin", "\\sin");
		output = oldReplace(output, "c0s", "\\cos");
		output = oldReplace(output, "tan", "\\tan");
		output = oldReplace(output, "c0t", "\\cot");
		output = oldReplace(output, "sec", "\\sec");
		output = oldReplace(output, "csc", "\\csc");
		output = oldReplace(output, "sinh", "\\sinh");
		output = oldReplace(output, "c0sh", "\\cosh");
		output = oldReplace(output, "tanh", "\\tanh");
		output = oldReplace(output, "c0th", "\\coth");
		output = oldReplace(output, "sech", "\\sech");
		output = oldReplace(output, "csch", "\\csch");

		output = oldReplace(output, "arc\\sin", "\\arcsin");
		output = oldReplace(output, "arc\\cos", "\\arccos");
		output = oldReplace(output, "a\\tan", "\\arctan");
		output = oldReplace(output, "a\\sin", "\\arcsin");
		output = oldReplace(output, "a\\cos", "\\arccos");
		output = oldReplace(output, "a\\tan", "\\arctan");

		output = oldReplace(output, "l0g", "\\log");
		output = oldReplace(output, "log", "\\log");
		output = oldReplace(output, "lg", "\\lg");
		output = oldReplace(output, "ln", "\\ln");
		output = oldReplace(output, "det", "\\det");
		output = oldReplace(output, "dim", "\\dim");

		output = oldReplace(output, "max", "\\max");
		output = oldReplace(output, "min", "\\min");

		output = oldReplace(output, "lim", "\\lim");

		output = oldReplace(output, ".*", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}.", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}*", ":");
		output = oldReplace(output, ",*", ";");

		return output;
	}

	public String interpretRead(SymbolList sl) {
		String str = "";
		int i, j;

		if (sl.isEmpty()) {
			return str;
		}

		SymbolNode sn;
		// String name;
		// sl.addSpaces("\\;",0.85);
		for (i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);

			if (sn.name.equals("\\infty")) {
				sn.name = "infinito ";
			}
			if (sn.name.equals("(")) {
				sn.name = "abre parentesis ";
			}
			if (sn.name.equals(")")) {
				sn.name = "cierra parentesis ";
			}
			if (sn.name.equals("\\sum")) {
				sn.name = "la suma ";
			}
			if (sn.name.equals("\\int")) {
				sn.name = "la integral ";
			}
			if (sn.name.equals("d")) {
				// sn.name = "\\)\\[DifferentialD]";
				sn.name = "con respecto a ";
			}
			if (sn.name.equals("\\partial")) {
				sn.name = "la derivada parcial ";
			}
			if (sn.name.equals("e")) {
				sn.name = "eee ";
			}
			if (sn.name.equals("+")) {
				sn.name = "mas ";
			}
			if (sn.name.equals("-")) {
				sn.name = "menos ";
			}
			if (sn.name.equals("*")) {
				sn.name = "por ";
			}
			if (sn.name.equals("/")) {
				sn.name = "entre ";
			}
			if (sn.name.equals("=")) {
				sn.name = "igual a ";
			}
			if (sn.name.equals("<")) {
				sn.name = "menor que ";
			}
			if (sn.name.equals(">")) {
				sn.name = "mayor que ";
			}
			/*
			 * if (sn.name.equals("\\pi")) { sn.name = "pi "; }
			 */
			if (sn.name.equals(dot)) {
				if (i == 0) {
					sn.name = ".";
				} else {
					sn.name = "por ";
				}
			}

			// else if(!sn.subexpression.isEmpty() && sn.name.equals("\\sqrt"))
			// {
			// str += "\\sqrt{"+interpretLaTeX(sn.subexpression)+"}";
			// }
			if (!sn.up.isEmpty() && !sn.down.isEmpty()
					&& sn.name.equals("menos ")) {
				str += "el cociente de, " + interpretRead(sn.up) + ", entre, "
						+ interpretRead(sn.down) + ", ";
			} else if (!sn.up.isEmpty()
					&& sn.up.symbolNodeAt(0).name.equals("-")
					&& sn.name.equals("menos ")) {
				str += "igual a, ";
			} else if (!sn.down.isEmpty()
					&& sn.down.symbolNodeAt(0).name.equals("-")
					&& sn.name.equals("menos ")) {
				str += "igual a, ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("<")
					&& sn.name.equals("menos ")) {
				str += "menor o igual a, ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst(">")
					&& sn.name.equals("menos ")) {
				str += "mayor o igual a, ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("<")) {
				str += "menor o igual a, ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals(">")) {
				str += "mayor o igual a, ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("+")
					&& sn.name.equals("menos ")) {
				str += "mas o menos, ";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("+")) {
				str += "menos o mas, ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("+")
					&& sn.name.equals("menos ")) {
				str += "mas o menos, ";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("+")) {
				str += "menos o mas, ";
			} else if (!sn.isRoot()) {
				if (this.digits.lastIndexOf(sn.name) == -1) {
					str += " ";
					// //System.out.println("SpaceAdded");
				}
				for (j = 0; j < sn.name.length(); j++) {
					if (sn.name.charAt(j) != '\\') {
						str += sn.name.charAt(j);
					}
				}
				if (this.digits.lastIndexOf(sn.name) == -1) {
					str += " ";
					// //System.out.println("SpaceAdded");
				}
			}

			if (!sn.subexpression.isEmpty()) {
				str += " la raiz cuadrada de "
						+ interpretRead(sn.subexpression) + ", ";
			}

			if (sn.name.equals("la integral ")) {
				if (!sn.subscript.isEmpty()) {
					str += "de, " + interpretRead(sn.subscript) + ", ";
				}
				if (!sn.superscript.isEmpty()) {
					str += "a, " + interpretRead(sn.superscript) + ", de ";
				}
				// str += "\\(";
				continue;
			}

			if (sn.name.equals("la suma ")) {
				if (!sn.subscript.isEmpty()) {
					str += "de, " + interpretRead(sn.subscript) + ", ";
				}
				if (!sn.superscript.isEmpty()) {
					str += "hasta, " + interpretRead(sn.superscript) + ", de ";
				}
				// str += "\\(";
				continue;
			}

			if (!sn.subscript.isEmpty() && sn.subscript.isFirst(dot)) {
				str += ".";
			} else if (!sn.subscript.isEmpty()
					&& sn.type.equals("NON_SCRIPTED")) {
				str += " " + interpretRead(sn.subscript);
			} else if (!sn.subscript.isEmpty() && digits.indexOf(sn.name) > -1) {
				str += " " + interpretRead(sn.subscript);
			} else if (!sn.subscript.isEmpty()
					&& sn.name.equals("la derivada parcial ")) {
				str += "con respecto a, " + interpretRead(sn.subscript)
						+ ", de, ";
			} else if (!sn.subscript.isEmpty()) {
				str += "sub, " + interpretRead(sn.subscript) + ", ";
			}

			if (!sn.superscript.isEmpty() && sn.type.equals("NON_SCRIPTED")) {
				str += " " + interpretRead(sn.superscript);
			} else if (!sn.superscript.isEmpty()
					&& (sn.name.equals(dot) || sn.name.equals("."))) {
				str += interpretRead(sn.superscript);
			} else if (!sn.superscript.isEmpty()) {
				str += "elevado a, " + interpretRead(sn.superscript) + ", ";
			}
		}

		return str;
	}

	public String interpretMathematica(SymbolList sl) {
		String str = "";

		if (sl.isEmpty()) {
			return str;
		}

		SymbolNode sn;
		// String name;
		// sl.addSpaces("\\;",0.85);
		for (int i = 0; i < sl.size(); i++) {
			sn = sl.symbolNodeAt(i);

			if (sn.name.equals("\\infty")) {
				sn.name = "\\[Infinity]";
			}
			if (sn.name.equals("(")) {
				sn.name = "\\(";
			}
			if (sn.name.equals(")")) {
				sn.name = "\\)";
			}
			if (sn.name.equals("\\sum")) {
				sn.name = "\\[Sum]";
			}
			if (sn.name.equals("\\int")) {
				sn.name = "\\[Integral]";
			}
			if (sn.name.equals("d")) {
				sn.name = "\\[DifferentialD]";
			}
			if (sn.name.equals("\\partial")) {
				sn.name = "\\[PartialD]";
			}
			if (sn.name.equals("e")) {
				sn.name = "\\[ExponentialE]";
			}
			if (sn.name.equals("\\pi")) {
				sn.name = "\\[Pi]";
			}
			if (sn.name.equals(dot)) {
				if (i == 0) {
					sn.name = ".";
				}
				/*
				 * else if(sl.symbolNodeAt(i).name.equals("]") &&
				 * sl.symbolNodeAt(i + 1).name.equals("[")) { sn.name = "."; }
				 */
				else {
					sn.name = "*";
				}
			}

			if (!sn.rows.isEmpty()) {
				// System.out.println("i=" + i + " sl.size()=" + sl.size() +
				// " sl=" +
				// sl.symbolNodeAt(i + 1).superscript.toSequenceString());
				if (sn.rows.size() == 1) {
					str += "[" + interpretMathematica((sn.rows.get(0))) + "]";
					str = oldReplace(str, "\\matrix_space", " ");
				} else if (sl.symbolNodeAt(i + 1).name.equals("]")
						&& sl.symbolNodeAt(i + 1).superscript
								.toSequenceString().equals("- 1")) {
					str += "Inverse[{";
					for (int j = 0; j < sn.rows.size(); j++) {
						str += "{" + interpretMathematica((sn.rows.get(j)))
								+ "}";
						if (j < sn.rows.size() - 1) {
							str += ", ";
						}
					}
					str += "}] ";
					sl.symbolNodeAt(i + 1).superscript.clear();
					str = oldReplace(str, "\\matrix_space", ", ");
				} else {
					str += "{";
					for (int j = 0; j < sn.rows.size(); j++) {
						str += "{" + interpretMathematica((sn.rows.get(j)))
								+ "}";
						if (j < sn.rows.size() - 1) {
							str += ", ";
						}
					}
					str += "}";
				}
				str = oldReplace(str, "\\matrix_space", ", ");
			}

			if (!sn.up.isEmpty() && !sn.down.isEmpty() && sn.name.equals("-")) {
				str += "\\(" + interpretMathematica(sn.up) + "\\)\\/\\("
						+ interpretMathematica(sn.down) + "\\)";
			} else if (!sn.up.isEmpty() && sn.up.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
			} else if (!sn.down.isEmpty() && sn.down.isFirst("-")
					&& sn.name.equals("-")) {
				str += "=";
			} else if (sn.name.equals("[") || sn.name.equals("]")) {
				sn.name = "";
			} else if (!sn.name.equals("\\sqrt")) {
				str += sn.name;
				if (this.digits.lastIndexOf(sn.name) == -1) {
					str += " ";
				}
			}

			if (!sn.subexpression.isEmpty()) {
				str += " \\(" + interpretMathematica(sn.subexpression)
						+ "\\)\\^\\(1\\/2\\)";
			}

			if (sn.name.equals("\\[Integral]")) {
				if (!sn.subscript.isEmpty()) {
					str += "\\_\\(" + interpretMathematica(sn.subscript)
							+ "\\)";
				}
				if (!sn.superscript.isEmpty()) {
					str += "\\%\\(" + interpretMathematica(sn.superscript)
							+ "\\)";
				}
				continue;
			}

			if (sn.name.equals("\\[Sum]")) {
				if (!sn.subscript.isEmpty()) {
					str += "\\+\\(" + interpretMathematica(sn.subscript)
							+ "\\)";
				}
				if (!sn.superscript.isEmpty()) {
					str += "\\%\\(" + interpretMathematica(sn.superscript)
							+ "\\)";
				}
				continue;
			}

			if (!sn.superscript.isEmpty() && sn.type.equals("NON_SCRIPTED")) {
				str += " " + interpretMathematica(sn.superscript);
			}
			/*
			 * else if(!sn.superscript.isEmpty() && sn.name.equals("") &&
			 * sn.superscript.toSequenceString().equals("- 1")) { str +=
			 * "//Inverse "; }
			 */
			else if (!sn.superscript.isEmpty()
					&& (sn.name.equals(dot) || sn.name.equals("."))) {
				str += interpretMathematica(sn.superscript);
			} else if (!sn.superscript.isEmpty()) {
				// sn.up.add(sn.superscript);
				str += "\\^\\(" + interpretMathematica(sn.superscript) + "\\)";
			}

			if (!sn.subscript.isEmpty()
					&& sn.subscript.get(sn.subscript.size() - 1).isComma()) {
				sn.subscript.remove(sn.subscript.size() - 1);
				if (sn.subscript.isEmpty()) {
					str += ",";
				} else {
					str += "\\_\\(" + interpretMathematica(sn.subscript)
							+ "\\),";
				}
			} else if (!sn.subscript.isEmpty() && sn.subscript.isFirst(dot)) {
				str += ".";
			} else if (!sn.subscript.isEmpty()
					&& sn.type.equals("NON_SCRIPTED")) {
				str += " " + interpretMathematica(sn.subscript);
			} else if (!sn.subscript.isEmpty() && digits.indexOf(sn.name) > -1) {
				str += " " + interpretMathematica(sn.subscript);
			} else if (!sn.subscript.isEmpty()
					&& sn.name.equals("\\[PartialD]")) {
				// sn.down.add(sn.subscript);
				// \!\(\[PartialD]\_\(x, y\)x\ y\)
				str += "\\_\\(" + interpretMathematica(sn.subscript) + "\\) ";
				// str += interpretLaTeX(sn.subscript);
			} else if (!sn.subscript.isEmpty()) {
				// sn.down.add(sn.subscript);
				str += "\\_\\(" + interpretMathematica(sn.subscript) + "\\)";
				// str += interpretLaTeX(sn.subscript);
			}

		}

		return str;
	}

	/*
	 * public String interpretMathematica(SymbolList sl) { String str = "";
	 * if(sl.isEmpty()) return str; SymbolNode sn; String name;
	 * //sl.addSpaces("\\;",0.85); for(int i = 0; i < sl.size(); i++) { sn =
	 * sl.symbolNodeAt(i); if(sn.name.equals("\\infty")) { sn.name =
	 * "\\[Infinity]"; } else if(sn.name.equals("(")) { sn.name = "\\("; } else
	 * if(sn.name.equals(")")) { sn.name = "\\)"; } else
	 * if(sn.name.equals("\\sum")) { sn.name = "\\[Sum]"; } else
	 * if(sn.name.equals("\\int")) { sn.name = "\\[Integral]"; } else
	 * if(sn.name.equals("d")) { //sn.name = "\\)\\[DifferentialD]"; sn.name =
	 * "\\[DifferentialD]"; } else if(sn.name.equals("\\partial")) { sn.name =
	 * "\\[PartialD]"; } else if(sn.name.equals("e")) { sn.name =
	 * "\\[ExponentialE]"; } else if(sn.name.equals("\\pi")) { sn.name =
	 * "\\[Pi]"; } else if(sn.isDot() && !sn.superscript.isEmpty()) { sn.name =
	 * "."; } else if(sn.isDot()) { sn.name = "*"; } //else
	 * if(!sn.subexpression.isEmpty() && sn.name.equals("\\sqrt")) { // str +=
	 * "\\sqrt{"+interpretLaTeX(sn.subexpression)+"}"; //} if(!sn.up.isEmpty()
	 * && !sn.down.isEmpty() && sn.name.equals("-")) { str +=
	 * "\\("+interpretMathematica
	 * (sn.up)+"\\)\\/\\("+interpretMathematica(sn.down)+"\\)"; } else
	 * if(!sn.up.isEmpty() && sn.up.isFirst("-") && sn.name.equals("-")) { str
	 * += "="; } else if(!sn.down.isEmpty() && sn.down.isFirst("-") &&
	 * sn.name.equals("-")) { str += "="; } else if (!sn.name.equals("\\sqrt")){
	 * str += sn.name; if(this.digits.lastIndexOf(sn.name) == -1) str += " "; }
	 * if(!sn.subexpression.isEmpty()) { str +=
	 * " \\("+interpretMathematica(sn.subexpression)+"\\)\\^\\(1\\/2\\)"; }
	 * if(sn.name.equals("\\[Integral]")) { if(!sn.subscript.isEmpty()) { str +=
	 * "\\_\\("+interpretMathematica(sn.subscript)+"\\)"; }
	 * if(!sn.superscript.isEmpty()) { str +=
	 * "\\%\\("+interpretMathematica(sn.superscript)+"\\)"; } //str += "\\(";
	 * continue; } if(sn.name.equals("\\[Sum]")) { if(!sn.subscript.isEmpty()) {
	 * str += "\\+\\("+interpretMathematica(sn.subscript)+"\\)"; }
	 * if(!sn.superscript.isEmpty()) { str +=
	 * "\\%\\("+interpretMathematica(sn.superscript)+"\\)"; } //str += "\\(";
	 * continue; } //if(!sn.superscript.isEmpty() &&
	 * sn.superscript.isFirst(dot)) { // str +=
	 * "\\^\\(."+interpretMathematica(((
	 * SymbolNode)sn.superscript.get(0)).superscript)+"\\)"; //} // else
	 * if(!sn.superscript.isEmpty() && sn.isNonScripted()) {
	 * //sn.up.add(sn.superscript); str +=
	 * " "+interpretMathematica(sn.superscript); } //else
	 * if(!sn.superscript.isEmpty()) { // && sn.type.equals("NON_SCRIPTED")) {
	 * //sn.up.add(sn.superscript); //str +=
	 * "\\^\\("+interpretMathematica(sn.superscript)+"\\)"; //} else
	 * if(!sn.superscript.isEmpty() && sn.name.equals(".")) {
	 * //sn.up.add(sn.superscript); str +=
	 * "."+interpretMathematica(sn.superscript); } else
	 * if(!sn.superscript.isEmpty()) { //sn.up.add(sn.superscript); str +=
	 * "\\^\\("+interpretMathematica(sn.superscript)+"\\)"; }
	 * if(!sn.subscript.isEmpty() && sn.subscript.isFirst(dot)) { str += "."; }
	 * else if(!sn.subscript.isEmpty() && sn.type.equals("NON_SCRIPTED")) { str
	 * += " "+interpretMathematica(sn.subscript); } else
	 * if(!sn.subscript.isEmpty() && digits.indexOf(sn.name) > -1) { str +=
	 * " "+interpretMathematica(sn.subscript); } else if(!sn.subscript.isEmpty()
	 * && sn.name.equals("\\[PartialD]")) { //sn.down.add(sn.subscript); str +=
	 * "\\_"+interpretMathematica(sn.subscript)+"\\ "; //str +=
	 * interpretLaTeX(sn.subscript); } else if(!sn.subscript.isEmpty()) {
	 * //sn.down.add(sn.subscript); str +=
	 * "\\_\\("+interpretMathematica(sn.subscript)+"\\)"; //str +=
	 * interpretLaTeX(sn.subscript); } } return str; }
	 */

	public String interpretMathematica() {
		String output = interpretMathematica(symbolList);

		output = oldReplace(output, " ", "");

		output = oldReplace(output, "C0s", "Cos");
		output = oldReplace(output, "C0t", "Cot");
		output = oldReplace(output, "C0sh", "Cosh");
		output = oldReplace(output, "C0th", "Coth");
		output = oldReplace(output, "L0g", "Log");

		output = oldReplace(output, "sin", "Sin");
		output = oldReplace(output, "c0s", "Cos");
		output = oldReplace(output, "tan", "Tan");
		output = oldReplace(output, "c0t", "Cot");
		output = oldReplace(output, "sec", "Sec");
		output = oldReplace(output, "csc", "Csc");
		output = oldReplace(output, "sinh", "Sinh");
		output = oldReplace(output, "c0sh", "Cosh");
		output = oldReplace(output, "tanh", "Tanh");
		output = oldReplace(output, "c0th", "Coth");
		output = oldReplace(output, "sech", "Sech");
		output = oldReplace(output, "csch", "Csch");

		output = oldReplace(output, "arcSin", "ArcSin");
		output = oldReplace(output, "arcCos", "ArcCos");
		output = oldReplace(output, "arcTan", "ArcTan");
		output = oldReplace(output, "aSin", "ArcSin");
		output = oldReplace(output, "aCos", "ArcCos");
		output = oldReplace(output, "aTan", "ArcTan");

		output = oldReplace(output, "l0g", "Log");
		output = oldReplace(output, "lg", "Log");
		output = oldReplace(output, "ln", "Ln");
		output = oldReplace(output, "det", "Det");
		output = oldReplace(output, "dim", "Dim");

		output = oldReplace(output, "max", "Max");
		output = oldReplace(output, "min", "Min");

		output = oldReplace(output, "lim", "Lim");

		output = oldReplace(output, ".*", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}.", ":");
		output = oldReplace(output, "*^{.}", ":");
		output = oldReplace(output, "^{.}*", ":");
		output = oldReplace(output, ",*", ";");

		return output;
	}
}

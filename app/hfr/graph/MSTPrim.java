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
package hfr.graph;

/**
 * <p>�berschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

import hfr.SymbolList;
import hfr.SymbolNode;

import java.awt.Graphics2D;

public class MSTPrim {
	SymbolList nodes = new SymbolList();
	SymbolList inTree = new SymbolList();
	public static boolean parentRightWall = false;
	public static boolean checkHorizontalBar = true;
	Heap<SymbolNode> heap;

	public MSTPrim(SymbolList nodes, SymbolList inTree) {
		this.inTree.add(inTree);
		this.nodes.add(nodes);
	}

	public SymbolList buildMST() {
		SymbolNode sn, v;
		int i;
		double w;

		SymbolNode.weightCompareTo = true;

		nodes.removeAll(inTree);

		for (i = 0; i < nodes.size(); i++) {
			nodes.symbolNodeAt(i).weight = Integer.MAX_VALUE;
		}

		for (i = 0; i < inTree.size(); i++) {
			inTree.symbolNodeAt(i).weight = Integer.MIN_VALUE;
			inTree.symbolNodeAt(i).belonging = true;
			nodes.add(inTree.symbolNodeAt(i));
		}

		inTree.clear();

		heap = new Heap<SymbolNode>(nodes.size());
		for (i = 0; i < nodes.size(); i++) {
			try {
				heap.insert(nodes.symbolNodeAt(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while (!heap.isEmpty()) {
			sn = (SymbolNode) heap.deleteMin();
			inTree.add(sn);

			for (i = 1; i <= heap.size(); i++) {
				v = (SymbolNode) heap.get(i);
				w = SymbolEdge.getDistance(sn, v);
				/*
				 * if(sn.inMainBaseline) { System.out.println(sn + ":" + v); }
				 */

				if (checkHorizontalBar
						&& (v.someInDownRegion || v.someInUpRegion || // v.isHorizontalBar()
																		// ||
						v.isVariableRange()) && !sn.isHorizontalBar()
						&& !sn.inMainBaseline) {
					// SymbolEdge.setDistanceFactors(false);
					w = Math.min(w, SymbolEdge.getDistance(v, sn));
					// SymbolEdge.setDistanceFactors(true);
				}
				if (w < v.weight) {
					v.weight = w;
					v.dad = sn;
					sn.son = v;
					if (MSTPrim.parentRightWall) {
						v.rightWall = sn.rightWall;
					}
					heap.upHeap(i);
				}
			}
		}
		SymbolNode.weightCompareTo = false;

		return inTree;
	}

	public SymbolList buildMST(Graphics2D g2, int delay) {
		SymbolNode sn, v;
		int i;
		double w;

		SymbolNode.weightCompareTo = true;

		nodes.removeAll(inTree);

		for (i = 0; i < nodes.size(); i++) {
			nodes.symbolNodeAt(i).weight = Integer.MAX_VALUE;
		}

		for (i = 0; i < inTree.size(); i++) {
			inTree.symbolNodeAt(i).weight = Integer.MIN_VALUE;
			inTree.symbolNodeAt(i).belonging = true;
			nodes.add(inTree.symbolNodeAt(i));
		}

		inTree.clear();

		heap = new Heap<SymbolNode>(nodes.size());
		for (i = 0; i < nodes.size(); i++) {
			try {
				heap.insert(nodes.symbolNodeAt(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while (!heap.isEmpty()) {
			sn = (SymbolNode) heap.deleteMin();
			inTree.add(sn);
			for (i = 1; i <= heap.size(); i++) {
				v = (SymbolNode) heap.get(i);
				w = SymbolEdge.getDistance(sn, v);
				if (w < v.weight) {
					v.weight = w;
					v.dad = sn;
					sn.son = v;
					heap.upHeap(i);
				}
			}
		}
		SymbolNode.weightCompareTo = false;

		return inTree;
	}

	public static SymbolList construct(SymbolList nodes, SymbolList inTree) {
		MSTPrim mst = new MSTPrim(nodes, inTree);
		SymbolList t = mst.buildMST();

		// System.out.println("MST!!"+mst);

		return t;
	}

	public static SymbolList construct(SymbolList nodes) {
		MSTPrim mst = new MSTPrim(nodes, new SymbolList());
		SymbolList t = mst.buildMST();

		// System.out.println("MST!!"+mst);

		return t;
	}

	SymbolNode getDad(SymbolNode sn) {
		SymbolNode s = null;

		if (sn.dad == null) {
			return null;
		}

		s = sn;
		while (s.dad != null) {
			s = s.dad;
		}

		return s;
	}

	@Override
	public String toString() {
		String str = "";

		for (int i = 0; i < inTree.size(); i++) {
			str += "[node=" + inTree.symbolNodeAt(i) + ", dad="
					+ inTree.symbolNodeAt(i).dad + "]";
			if (i > 0) {
				str += " ";
			}
		}

		/*
		 * for(int i = 0; i < inTree.size(); i++) { str +=
		 * inTree.symbolNodeAt(i) +" => "+inTree.symbolNodeAt(i).right+"\n"; }
		 */

		return str;
	}

	public static String toString(SymbolList inTree) {
		String str = "";

		for (int i = 0; i < inTree.size(); i++) {

			str += "[node=" + inTree.symbolNodeAt(i) + ", dad="
					+ inTree.symbolNodeAt(i).dad + "]";
			if (i > 0) {
				str += " ";
			}
		}

		/*
		 * for(int i = 0; i < inTree.size(); i++) { str +=
		 * inTree.symbolNodeAt(i) +" => "+inTree.symbolNodeAt(i).right+"\n"; }
		 */

		return str;
	}

}

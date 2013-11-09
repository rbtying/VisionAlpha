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
 * <p>Copyright: Copyright (c) 2001 - 2003</p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

import java.util.*;

import DataStructures.*;
import hfr.*;

public class SymbolMinimumSpanningTree {
	SymbolList nodes = new SymbolList();
	SymbolList inTree = new SymbolList();
	SymbolList notInTree = new SymbolList();
	SymbolList tree = new SymbolList();
	int dad[];

	public SymbolMinimumSpanningTree(SymbolList nodes, SymbolList inTree) {
		this.nodes.add(nodes);
		this.inTree.add(inTree);

		/*
		 * for(i = 0; i < nodes.size(); i++) { tree.add(new
		 * SymbolNode(nodes.symbolNodeAt(i))); }
		 * 
		 * for(i = 0; i < nodes.size() - 1; i++) {
		 * tree.symbolNodeAt(i).addRight(tree.symbolNodeAt(i+1)); }
		 */

		// for(i = 0; i < inTree.size() - 1; i++) {
		// inTree.symbolNodeAt(i).addRight(inTree.symbolNodeAt(i+1));
		// }

		this.nodes.sort();
		this.inTree.sort();
		dad = new int[this.nodes.size()];
	}

	public SymbolList construct(boolean useKruskal) {
		if (useKruskal) {
			return kruskal();
		} else {
			return prim();
		}
	}

	public SymbolList construct() {
		return construct(false);
	}

	public static SymbolList construct(SymbolList nodes, SymbolList inTree) {
		SymbolMinimumSpanningTree t = new SymbolMinimumSpanningTree(nodes,
				inTree);

		return t.construct();
	}

	public static SymbolList construct(SymbolList nodes) {
		SymbolList empty = new SymbolList();
		SymbolMinimumSpanningTree t = new SymbolMinimumSpanningTree(nodes,
				empty);

		return t.construct();
	}

	SymbolList kruskal() {
		BinaryHeap h;
		SymbolEdge e;
		ArrayList<SymbolEdge> edges = new ArrayList<SymbolEdge>();
		ArrayList<SymbolEdge> edgesFound = new ArrayList<SymbolEdge>();
		int i, j;

		for (i = 0; i < nodes.size(); i++) {
			for (j = i + 1; j < nodes.size(); j++) {
				edges.add(new SymbolEdge(nodes.symbolNodeAt(i), nodes
						.symbolNodeAt(j)));
			}
		}

		// System.out.println("inTree="+inTree);
		for (i = 0; i < edges.size(); i++) {
			e = edges.get(i);
			for (j = 0; j < inTree.size() - 1; j++) {
				if ((e.nodeA.equals(inTree.symbolNodeAt(j)) && e.nodeB
						.equals(inTree.symbolNodeAt(j + 1)))
						|| (e.nodeA.equals(inTree.symbolNodeAt(j + 1)) && e.nodeB
								.equals(inTree.symbolNodeAt(j)))) {
					e.setDistance(Integer.MIN_VALUE);
				}
			}
		}

		// System.out.println("\nTREE:\n"+inTree.toStringRight());

		h = new BinaryHeap(edges.size());
		for (i = 0; i < edges.size(); i++) {
			try {
				h.insert(edges.get(i));
			} catch (Overflow of) {
				of.printStackTrace();
			}
		}

		// System.out.println("edgesFound="+edgesFound+"\n");
		while (edgesFound.size() < nodes.size() - 1 && !h.isEmpty()) {
			e = (SymbolEdge) h.deleteMin();
			if (find(e.nodeA, e.nodeB, true)) {
				// System.out.println("edgesFound.size()="+edgesFound.size());
				edgesFound.add(e);
				// System.out.println("Edge found! j="+j+" e="+e+"\n");
				// System.out.println("Edge found! e="+e+"\n");
				// System.out.println("getNodeA().right: "+e.getNodeA().right);
				// System.out.println("getNodeB().right: "+e.getNodeB().right);
				// System.out.println("getNodeB().right: "+e.getNodeB().right);
				// j++;
				/*
				 * try { Thread.sleep(1000); } catch(InterruptedException ie) {
				 * ie.printStackTrace(); }
				 */
			}
		}

		// System.out.println("edgesFound="+edgesFound+"\n");
		/*
		 * if(h.isEmpty()) { System.out.println("heap is empty!\n"); }
		 * if(edgesFound.size() == nodes.size() - 1) {
		 * System.out.println("edgesFound.size() == nodes.size() - 1\n"); }
		 */
		inTree.clear();
		for (i = 0; i < edgesFound.size(); i++) {
			e = edgesFound.get(i);
			for (j = 0; j < inTree.size(); j++) {
				if (e.nodeA.equals(inTree.symbolNodeAt(j))) {
					inTree.symbolNodeAt(j).addRight(e.nodeB);
					break;
				}
			}

			if (j == inTree.size()) {
				inTree.add(e.nodeA);
				inTree.symbolNodeAt(j).addRight(e.nodeB);
			}
		}

		// System.out.println("\nTREE:\n"+inTree.toStringRight());

		return inTree;
	}

	SymbolList prim() {
		BinaryHeap heap = new BinaryHeap(this.nodes.size() * this.nodes.size());
		SymbolEdge optEdge;
		int i, j;

		if (inTree.isEmpty())
			inTree.add(nodes.symbolNodeAt(0));

		notInTree.add(nodes);
		notInTree.removeAll(inTree);

		for (i = 0; i < inTree.size(); i++) {
			inTree.symbolNodeAt(i).right.clear();
		}

		for (i = 0; i < notInTree.size(); i++) {
			notInTree.symbolNodeAt(i).right.clear();
		}

		// System.out.println("nodes:\n  "+nodes);
		// System.out.println("inTree:\n  "+inTree);
		// System.out.println("notInTree:\n  "+notInTree);

		// System.out.println("TREE:\n  "+inTree.toStringRight());

		while (!notInTree.isEmpty()) {
			// System.out.println("inTree:\n  "+inTree);
			// System.out.println("notInTree:\n  "+notInTree);
			// System.out.println("TREE:\n  "+inTree.toStringRight());
			for (i = 0; i < inTree.size(); i++) {
				for (j = 0; j < notInTree.size(); j++) {
					try {
						heap.insert(new SymbolEdge(inTree.symbolNodeAt(i),
								notInTree.symbolNodeAt(j)));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			if (heap.isEmpty())
				break;

			optEdge = (SymbolEdge) heap.findMin();

			// System.out.println("optEdge.getNodeA().right: "+optEdge.getNodeA().right);
			// System.out.println("optEdge.getNodeB().right: "+optEdge.getNodeB().right);
			optEdge.getNodeB().right.clear();
			optEdge.getNodeA().addRight(optEdge.getNodeB());

			// System.out.println();
			// System.out.println("optEdge.getNodeA().right: "+optEdge.getNodeA().right);
			// System.out.println("optEdge.getNodeB().right: "+optEdge.getNodeB().right);
			/*
			 * for(i = 0; i < tree.size(); i++) {
			 * if(tree.symbolNodeAt(i).equals(optEdge.getNodeA())) {
			 * tree.symbolNodeAt(i).addRight(new
			 * SymbolNode(optEdge.getNodeB())); break; } }
			 */

			inTree.add(optEdge.getNodeB());
			notInTree.remove(optEdge.getNodeB());

			/*
			 * System.out.println("inTree:\n  "+inTree);
			 * System.out.println("inTree:\n  "+inTree);
			 * System.out.println("notInTree:\n  "+notInTree);
			 * System.out.println("TREE:\n  "+inTree.toStringRight());
			 * System.out.println("-------------------------");
			 */

			heap.makeEmpty();
		}
		// System.out.println("TREE:\n  "+inTree.toStringRight());
		return inTree;
		// return tree;
	}

	boolean find(SymbolNode x, SymbolNode y, boolean union) {
		SymbolNode i, j, t;

		i = x;
		while (i.dad != null) {
			i = i.dad;
		}

		j = y;
		while (j.dad != null) {
			j = j.dad;
		}

		while (x.dad != null) {
			t = x;
			x = x.dad;
			t.dad = i;
		}

		while (y.dad != null) {
			t = y;
			y = y.dad;
			t.dad = j;
		}

		// System.out.println("find: x="+x+" y="+y);
		// System.out.println("find: i="+i+" j="+j);
		// System.out.println("find: i.dad="+i.dad+" j.dad="+j.dad);
		if (union && !i.equals(j)) {
			if (j.weight < i.weight) {
				j.weight = j.weight + i.weight - 1;
				i.dad = j;
			} else {
				i.weight = i.weight + j.weight - 1;
				j.dad = i;
			}
			// System.out.println("find: i.dad="+i.dad+" j.dad="+j.dad);
		}
		// System.out.println("find: !i.equals(j)="+!i.equals(j)+"\n");

		return !i.equals(j);
	}

	/*
	 * SymbolList prim() { ArrayList edges = new ArrayList(); SymbolEdge
	 * optEdge; int i, j;
	 * 
	 * if(inTree.isEmpty()) inTree.add(nodes.symbolNodeAt(0));
	 * 
	 * notInTree.add(nodes); notInTree.removeAll(inTree);
	 * 
	 * //System.out.println("TREE:\n  "+inTree.toStringRight());
	 * 
	 * while(!notInTree.isEmpty()) { //System.out.println("inTree:\n  "+inTree);
	 * //System.out.println("notInTree:\n  "+notInTree); for(i = 0; i <
	 * inTree.size(); i++) { for(j = 0; j < notInTree.size(); j++) {
	 * edges.add(new
	 * SymbolEdge(inTree.symbolNodeAt(i),notInTree.symbolNodeAt(j))); } }
	 * 
	 * if(edges.isEmpty()) break;
	 * 
	 * Collections.sort(edges);
	 * 
	 * optEdge = (SymbolEdge) edges.firstElement();
	 * 
	 * System.out.println(edges);
	 * 
	 * optEdge.getNodeA().addRight(optEdge.getNodeB());
	 * inTree.add(optEdge.getNodeB()); notInTree.remove(optEdge.getNodeB());
	 * edges.clear();
	 * 
	 * //System.out.println("inTree:\n  "+inTree);
	 * //System.out.println("inTree:\n  "+inTree);
	 * //System.out.println("notInTree:\n  "+notInTree);
	 * //System.out.println("TREE:\n  "+inTree.toStringRight());
	 * //System.out.println(); }
	 * 
	 * return inTree; }
	 */
}

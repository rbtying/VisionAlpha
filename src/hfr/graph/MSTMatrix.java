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

import java.util.*;

import hfr.*;

public class MSTMatrix {
	public static ArrayList<MSTMatrix> MatrixMemory = new ArrayList<MSTMatrix>();
	@SuppressWarnings("rawtypes")
	public static ArrayList[] MatrixMemoryII = null;
	public static boolean checkMatrixMemory = true;
	public static boolean checkMatrixMemoryII = false;

	SymbolList tree;
	int maxStrokes = 4;
	int matrix[][];
	int strokeCounter[];

	public MSTMatrix(SymbolList tree) {
		SymbolNode sn, dad;
		// tree.sort();
		this.tree = tree;
		this.matrix = new int[tree.size()][tree.size()];
		this.strokeCounter = new int[tree.size()];

		for (int i = 0; i < tree.size(); i++) {
			dad = tree.symbolNodeAt(i);
			strokeCounter[i] = 1;
			// System.out.println(dad+":"+dad.dad);
			for (int j = i + 1; j < tree.size(); j++) {
				sn = tree.symbolNodeAt(j);
				if (sn.dad.equals(dad))
					matrix[i][j] = 1;
				else
					matrix[i][j] = 0;
			}
		}
	}

	public MSTMatrix(MSTMatrix m) {
		this.tree = m.tree;
		this.matrix = new int[m.size()][m.size()];
		this.strokeCounter = new int[m.size()];

		for (int i = 0; i < tree.size(); i++) {
			this.strokeCounter[i] = m.strokeCounter[i];
			for (int j = i + 1; j < tree.size(); j++) {
				this.matrix[i][j] = m.get(i, j);
			}
		}
	}

	public static void constructMatrixMemory(SymbolList tree) {
		MSTMatrix m = new MSTMatrix(tree);
		MatrixMemory = new ArrayList<MSTMatrix>();

		m.constructMatrixMemory();
	}

	public static void constructMatrixMemory(SymbolList tree, boolean check) {
		MSTMatrix m = new MSTMatrix(tree);

		MatrixMemory = new ArrayList<MSTMatrix>();

		/*
		 * if(check) { MSTMatrix.checkMatrixMemoryII = true;
		 * MSTMatrix.checkMatrixMemory = false; MSTMatrix.MatrixMemoryII = new
		 * ArrayList[tree.size()]; for(int i = 0; i < tree.size(); i++) {
		 * MSTMatrix.MatrixMemoryII[i] = new ArrayList(); }
		 * 
		 * MSTMatrix.MatrixMemoryII[0].add(m); } else {
		 * MSTMatrix.checkMatrixMemoryII = false; MSTMatrix.checkMatrixMemory =
		 * true; }
		 */

		m.constructMatrixMemory();
	}

	public void constructMatrixMemory() {
		MSTMatrix m = null;
		int i, j;

		// if(checkMatrixMemory) {
		if (!foundInMatrixMemory(this)) {
			MatrixMemory.add(this);
			// System.out.println(this);
		}
		// }

		for (i = 0; i < tree.size(); i++) {
			for (j = i + 1; j < tree.size(); j++) {
				m = this.addNode(i, j);
				if (m != null) {
					// if(MSTMatrix.checkMatrixMemoryII) {
					// if(!MSTMatrix.foundInMatrixMemory(m,i)) {
					// //System.out.println(m);
					// MSTMatrix.MatrixMemoryII[i].add(m);
					// }
					m.constructMatrixMemory();
				}
			}
		}
	}

	public static int getMemorySize() {
		/* if(MSTMatrix.checkMatrixMemory) { */
		return MSTMatrix.MatrixMemory.size();
		/*
		 * } else { int size = 0;
		 * 
		 * for(int i = 0; i < MSTMatrix.MatrixMemoryII.length; i++) { size +=
		 * MSTMatrix.MatrixMemoryII[i].size(); }
		 * 
		 * return size; }
		 */
	}

	static boolean foundInMatrixMemory(MSTMatrix m) {
		int i;

		for (i = MatrixMemory.size() - 1; i > -1; i--) {
			if (MatrixMemory.get(i).equals(m)) {
				return true;
			}
		}

		/*
		 * for(i = 0; i < MatrixMemory.size(); i++) { if(((MSTMatrix)
		 * MatrixMemory.get(i)).equals(m)) { return true; } }
		 */

		/*
		 * int j; i = 0; j = MatrixMemory.size() - 1; while(i <= j && i <
		 * MatrixMemory.size() && j > -1) { if(((MSTMatrix)
		 * MatrixMemory.get(i)).equals(m)) { return true; } if(((MSTMatrix)
		 * MatrixMemory.get(j)).equals(m)) { return true; } i++; j--; }
		 */

		return false;
	}

	static boolean foundInMatrixMemory(MSTMatrix m, int j) {
		int i;

		for (i = MatrixMemoryII[j].size() - 1; i > -1; i--) {
			if (((MSTMatrix) MatrixMemoryII[j].get(i)).equals(m)) {
				return true;
			}
		}

		return false;
	}

	public int get(int i, int j) {
		return matrix[i][j];
	}

	public void set(int x, int i, int j) {
		this.matrix[i][j] = x;
	}

	public int size() {
		return tree.size();
	}

	MSTMatrix addNode(int i, int j) {
		if (matrix[i][j] != 1 ||
		// strokeCounter[i] == 0 || strokeCounter[j] == 0 ||
				strokeCounter[i] + strokeCounter[j] > maxStrokes) {
			return null;
		}

		MSTMatrix m = new MSTMatrix(this);

		m.matrix[i][j] = 2;

		for (int k = j + 1; k < matrix[i].length; k++) {
			if (this.matrix[j][k] != 0) {
				m.matrix[i][k] = this.matrix[j][k];
				m.matrix[j][k] = 0;
			}
		}

		m.strokeCounter[i] += this.strokeCounter[j];
		m.strokeCounter[j] = 0;

		return m;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object instanceof MSTMatrix) {
			MSTMatrix o = (MSTMatrix) object;

			if (o.size() != this.size())
				return false;

			// for(int i = 0; i < tree.size(); i++) {
			for (int i = tree.size() - 1; i > -1; i--) {
				// for(int j = tree.size() - 1; j > i; j--) {
				for (int j = i + 1; j < tree.size(); j++) {
					if (o.get(i, j) != this.get(i, j)) {
						return false;
					}
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		String str = "\n";

		for (int i = 0; i < tree.size(); i++) {
			for (int j = 0; j < tree.size(); j++) {
				if (matrix[i][j] == 1)
					str += "+ ";
				else if (matrix[i][j] == 0)
					str += ". ";
				else
					str += "- ";
			}
			str += "\n";
		}

		return str;
	}

}

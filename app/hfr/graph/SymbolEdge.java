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
 * <p>\uFFFDberschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) 2001 - 2003</p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr\uFFFDguez
 * @version 1.0
 */

import hfr.*;
import ocr.*;

//import DataStructures.*;

public class SymbolEdge implements DataStructures.Comparable {
	// public class SymbolEdge implements Comparable {
	SymbolNode nodeA;
	SymbolNode nodeB;
	public double dist0;
	public double dist1;
	public double dist2;
	public int i, j;
	String inf = "";
	public static boolean useAtractors = true;
	public static boolean checkTwosideDominance = false;
	public static boolean checkHorizontalBar = false;
	public static boolean useDistanceFactors = true;

	public static double vrf = 4, frf = 6;
	public static double horf = 1;
	public static double dhf = 1;

	public SymbolEdge(SymbolNode a, SymbolNode b, int i, int j) {
		this(a, b);
		this.i = i;
		this.j = j;
	}

	public SymbolEdge(SymbolNode a, SymbolNode b, double d) {
		this.nodeA = a;
		this.nodeB = b;
		dist0 = dist1 = dist2 = d;
	}

	public SymbolEdge(SymbolNode a, SymbolNode b) {
		this.nodeA = a;
		this.nodeB = b;
		DPoint p;
		double d;

		this.dist0 = (a.centroidX - b.centroidX) * (a.centroidX - b.centroidX)
				/ horf + (a.centroidY - b.centroidY)
				* (a.centroidY - b.centroidY);

		if (a.equals(b)) {
			dist0 = dist1 = Integer.MAX_VALUE;
			return;
		}

		dist1 = Integer.MAX_VALUE;
		for (int i = 0; i < a.atractors.size(); i++) {
			p = a.atractors.get(i);
			d = dist2(b.center, p);
			inf += " " + d;
			dist1 = Math.min(dist1, d);
		}

		SymbolNode A, B;

		if (b.dominates(a) && (b.isVariableRange() || b.isHorizontalBar())) {
			A = b;
			B = a;

			// System.out.println(A+" "+B);

			double fac = 1;
			if (b.isHorizontalBar()) {
				fac = frf;
			}

			for (int i = 0; i < A.atractors.size(); i++) {
				p = A.atractors.get(i);
				for (int j = 0; j < B.downAtractor.size(); j++) {
					dist1 = Math.min(dist1, dist2(p, B.downAtractor.get(j))
							/ fac);
				}
			}

			for (int i = 0; i < A.atractors.size(); i++) {
				p = A.atractors.get(i);
				for (int j = 0; j < B.upAtractor.size(); j++) {
					dist1 = Math
							.min(dist1, dist2(p, B.upAtractor.get(j)) / fac);
				}
			}
		}

		A = a;
		B = b;
		if (a.dominates(b)) {
			// dist1 = Integer.MAX_VALUE;
			if (A.isVariableRange()) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						dist1 = Math.min(dist1, dist2(p, B.downAtractor.get(j))
								/ vrf);
					}
				}

				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.upAtractor.size(); j++) {
						dist1 = Math.min(dist1, dist2(p, B.upAtractor.get(j))
								/ vrf);
					}
				}
			}
			if (A.isHorizontalBar()) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						dist1 = Math.min(dist1, dist2(p, B.downAtractor.get(j))
								/ frf);
					}
				}

				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.upAtractor.size(); j++) {
						dist1 = Math.min(dist1, dist2(p, B.upAtractor.get(j))
								/ frf);
					}
				}
			} else if (A.isRoot() && A.subexpression(B)) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.atractors.size(); j++) {
						dist1 = Math.min(dist1, dist2(p, B.atractors.get(j))
								/ vrf);
					}
				}
			} else if (A.isVariable() || A.isCloseBracket() || A.isDigit()
					|| A.isRoot()) {
				// else {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					dist1 = Math.min(dist1,
							dist2(p, new DPoint(B.minX, B.centroidY)));
				}

				// dist1 = dist2(new DPoint(A.maxX,A.centroidY),new
				// DPoint(B.minX,B.centroidY));
			} else {
				p = A.center;
				for (int j = 0; j < B.leftAtractor.size(); j++) {
					dist1 = Math.min(dist1, dist2(p, B.leftAtractor.get(j)));
				}
			}
		} else {
			dist1 = dist0;
		}

		if (A.right(B)) {
			// dist1 = Integer.MIN_VALUE;
			// dist1 = Integer.MAX_VALUE;
			for (int i = 0; i < A.atractors.size(); i++) {
				p = A.atractors.get(i);
				for (int j = 0; j < B.leftAtractor.size(); j++) {
					dist1 = Math.min(dist1, dist2(p, B.leftAtractor.get(j)));
				}
			}
		} else if (B.right(A)) {
			// dist1 = Integer.MIN_VALUE;
			// dist1 = Integer.MAX_VALUE;
			for (int i = 0; i < B.atractors.size(); i++) {
				p = B.atractors.get(i);
				for (int j = 0; j < A.leftAtractor.size(); j++) {
					dist1 = Math.min(dist1, dist2(p, A.leftAtractor.get(j)));
				}
			}
		}
	}

	/*
	 * public SymbolEdge(SymbolNode a, SymbolNode b) { this.nodeA = a;
	 * this.nodeB = b; DPoint p; int d; this.dist0 = (a.centroidX -
	 * b.centroidX)*(a.centroidX - b.centroidX)/horf + (a.centroidY -
	 * b.centroidY)*(a.centroidY - b.centroidY); if(a.equals(b)) { dist0 = dist1
	 * = Integer.MAX_VALUE; return; } dist1 = Integer.MAX_VALUE; for(int i = 0;
	 * i < a.atractors.size(); i++) { p = (DPoint)a.atractors.get(i); d =
	 * dist2(b.center,p); inf += " "+d; dist1 = Math.min(dist1,d); } SymbolNode
	 * A = a, B = b; if(a.dominates(b)) { A = a; B = b; } else
	 * if(b.dominates(a)) { A = b; B = a; } dist1 = Integer.MAX_VALUE;
	 * if(A.isHorizontalBar() || A.isVariableRange()) { for(int i = 0; i <
	 * A.atractors.size(); i++) { p = (DPoint) A.atractors.get(i); for(int j =
	 * 0; j < B.downAtractor.size(); j++) { dist1 =
	 * Math.min(dist1,dist2(p,(DPoint)B.downAtractor.get(j))); } } for(int i =
	 * 0; i < A.atractors.size(); i++) { p = (DPoint) A.atractors.get(i);
	 * for(int j = 0; j < B.downAtractor.size(); j++) { dist1 =
	 * Math.min(dist1,dist2(p,(DPoint)B.upAtractor.get(j))); } } } else
	 * if(A.isVariable() || A.isCloseBracket() || A.isDigit() || A.isRoot() ||
	 * A.right(B)){ for(int i = 0; i < A.atractors.size(); i++) { p = (DPoint)
	 * A.atractors.get(i); for(int j = 0; j < B.leftAtractor.size(); j++) {
	 * dist1 = Math.min(dist1,dist2(p,(DPoint)B.leftAtractor.get(j))); } } }
	 * else { p = (DPoint) A.center; for(int j = 0; j < B.leftAtractor.size();
	 * j++) { dist1 = Math.min(dist1,dist2(p,(DPoint)B.leftAtractor.get(j))); }
	 * } if(A.right(B)) { //dist1 = Integer.MIN_VALUE; dist1 =
	 * Integer.MAX_VALUE; for(int i = 0; i < A.atractors.size(); i++) { p =
	 * (DPoint) A.atractors.get(i); for(int j = 0; j < B.leftAtractor.size();
	 * j++) { dist1 = Math.min(dist1,dist2(p,(DPoint)B.leftAtractor.get(j))); }
	 * } } else if(B.right(B)) { //dist1 = Integer.MIN_VALUE; dist1 =
	 * Integer.MAX_VALUE; for(int i = 0; i < B.atractors.size(); i++) { p =
	 * (DPoint) B.atractors.get(i); for(int j = 0; j < A.leftAtractor.size();
	 * j++) { dist1 = Math.min(dist1,dist2(p,(DPoint)A.leftAtractor.get(j))); }
	 * } } }
	 */

	public static double getDistance(SymbolNode a, SymbolNode b) {
		if (checkTwosideDominance) {
			return Math.min(getDistance(a, b, SymbolEdge.useAtractors),
					getDistance(b, a, SymbolEdge.useAtractors));
		}

		return getDistance(a, b, SymbolEdge.useAtractors);
	}

	public static double getDistance(SymbolNode a, SymbolNode b,
			boolean attractors) {
		double d0 = (a.centroidX - b.centroidX) * (a.centroidX - b.centroidX)
				/ horf + (a.centroidY - b.centroidY)
				* (a.centroidY - b.centroidY);
		DPoint p;
		double d1;
		SymbolNode A = a, B = b;
		d1 = Double.POSITIVE_INFINITY;
		if (checkHorizontalBar) {
			if (A.isHorizontalBar()) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.downAtractor.get(j)) // /frf
								);
					}
					for (int j = 0; j < B.upAtractor.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.upAtractor.get(j)) // /
																		// frf
								);
					}
				}
			}
			if (B.isHorizontalBar()) {
				A = b;
				B = a;
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.downAtractor.get(j)) // /frf
								);
					}
					for (int j = 0; j < B.upAtractor.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.upAtractor.get(j)) // /
																		// frf
								);
					}
				}
			}

			return d1 = Math.min(d1, d0);
		}

		if (!attractors) {
			return d0;
		}

		if (a.equals(b)) {
			return Double.POSITIVE_INFINITY;
		}

		/*
		 * for(int i = 0; i < a.atractors.size(); i++) { p = (DPoint)
		 * a.atractors.get(i); d = dist2(b.center, p); d1 = Math.min(d1, d); }
		 */

		if (A.name.equals("[")) {
			double daux, horfold = horf;
			d1 = Double.POSITIVE_INFINITY;
			horf = 1.0;

			for (int i = 0; i < A.atractors.size(); i++) {
				p = A.atractors.get(i);
				for (int j = 0; j < B.leftAtractor.size(); j++) {
					daux = dist2(p, B.leftAtractor.get(j));
					if (B.isVariableRange() || B.isHorizontalBar()) {
						daux = 0.5 * daux;
					}
					if (B.subscThreshold >= p.x && p.x >= B.superThreshold) {
						daux = 0.5 * daux;
					}
					if (daux < d1) {
						d1 = daux;
					}
				}
			}

			horf = horfold;
			// if(iopt != -1) {
			// A.atractors.remove(iopt);
			return d1;
			// }
			// System.out.println(A);
		} else if (B.name.equals("[")) {
			double daux, horfold = horf;
			d1 = Double.POSITIVE_INFINITY;
			horf = 1.0;

			for (int i = 0; i < B.atractors.size(); i++) {
				p = B.atractors.get(i);
				for (int j = 0; j < A.leftAtractor.size(); j++) {
					daux = dist2(p, A.leftAtractor.get(j));
					if (A.isVariableRange() || A.isHorizontalBar()) {
						daux = 0.5 * daux;
					}
					if (A.subscThreshold >= p.x && p.x >= A.superThreshold) {
						daux = 0.5 * daux;
					}

					if (daux < d1) {
						d1 = daux;
					}
				}
			}

			horf = horfold;
			// if(iopt != -1) {
			// B.atractors.remove(iopt);
			return d1;
			// }
			// System.out.println(B);
		}

		if (b.dominates(a) && (b.isVariableRange() || b.isHorizontalBar())) {
			/*
			 * A = b; B = a;
			 */
			double fac = 1;
			if (b.isHorizontalBar()) {
				fac = frf;
			}

			for (int i = 0; i < b.atractors.size(); i++) {
				p = b.atractors.get(i);
				for (int j = 0; j < a.downAtractor.size(); j++) {
					d1 = Math.min(d1, dist2(p, a.downAtractor.get(j)) / fac);
				}
				for (int j = 0; j < a.upAtractor.size(); j++) {
					d1 = Math.min(d1, dist2(p, a.upAtractor.get(j)) / fac);
				}

			}
		}

		A = a;
		B = b;
		if (a.dominates(b)) {

			// d1 = Integer.MAX_VALUE;
			if (A.isVariableRange()) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						if (B.centroidX > A.minX && B.centroidX < A.rightWall
								&& A.inMainBaseline) {
							d1 = Math.min(d1, dist2(p, B.downAtractor.get(j))
									/ vrf);
						} else {
							d1 = Math.min(d1, dist2(p, B.downAtractor.get(j)));
						}

					}
					for (int j = 0; j < B.upAtractor.size(); j++) {
						if (B.centroidX > A.minX && B.centroidX < A.rightWall
								&& A.inMainBaseline) {
							d1 = Math.min(d1, dist2(p, B.upAtractor.get(j))
									/ vrf);
						} else {
							d1 = Math.min(d1, dist2(p, B.upAtractor.get(j)));
						}

					}

					// }
				}
			}
			if (A.isHorizontalBar()) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.downAtractor.size(); j++) {
						d1 = Math
								.min(d1, dist2(p, B.downAtractor.get(j)) / frf);
					}
					for (int j = 0; j < B.upAtractor.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.upAtractor.get(j)) / frf);
					}
				}
			} else if (A.isRoot() && A.subexpression(B)) {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					for (int j = 0; j < B.atractors.size(); j++) {
						d1 = Math.min(d1, dist2(p, B.atractors.get(j)) / vrf);
					}
				}
			} else if (A.isVariable() || A.isCloseBracket() || A.isDigit()
					|| A.isRoot()) {
				// else {
				for (int i = 0; i < A.atractors.size(); i++) {
					p = A.atractors.get(i);
					d1 = Math
							.min(d1, dist2(p, new DPoint(B.minX, B.centroidY)));
					if (B.isRoot() || B.isVariableRange()
							|| B.isHorizontalBar()) {
						for (int j = 0; j < B.leftAtractor.size(); j++) {
							d1 = Math.min(d1, dist2(p, B.leftAtractor.get(j)));
						}

					}
					/*
					 * if(A.inMainBaseline && B.isHorizontalBar() && B.centroidX
					 * > A.maxX) { for(int j = 0; j < B.leftAtractor.size();
					 * j++) { d1 = Math.min(d1, 2.0*dist2(p, (DPoint)
					 * B.leftAtractor.get(j))/frf); } }
					 */

					if (B.someInDownRegion) {
						// System.out.println("someInDownRegion");
						d1 = Math.min(d1,
								dist2(p, new DPoint(B.minX, B.hbMaxY)));
					}
					if (B.someInUpRegion && !A.isDigit()) {
						// System.out.println("someInUpRegion");
						d1 = Math.min(d1,
								dist2(p, new DPoint(B.minX, B.hbMinY)));
					}
					// d1 = Math.min(d1,dist2(p,new
					// DPoint(B.centroidX,B.centroidY)));
				}

				// dist1 = dist2(new DPoint(A.maxX,A.centroidY),new
				// DPoint(B.minX,B.centroidY));
			} else {
				// p = (DPoint) A.center;
				if (A.right(B)) {
					for (int i = 0; i < A.rightAtractor.size(); i++) {
						p = A.rightAtractor.get(i);
						for (int j = 0; j < B.leftAtractor.size(); j++) {
							d1 = Math.min(d1, dist2(p, B.leftAtractor.get(j)));
						}
					}
				} else {
					for (int i = 0; i < A.atractors.size(); i++) {
						p = A.atractors.get(i);
						for (int j = 0; j < B.leftAtractor.size(); j++) {
							d1 = Math.min(d1, dist2(p, B.leftAtractor.get(j)));
						}
					}
				}
			}
		} else {
			d1 = d0;
			// d1 = Integer.MAX_VALUE;
		}

		if (A.right(B) && !A.inMainBaseline && !B.inMainBaseline
				&& !A.name.equals("[") && !B.name.equals("[")) {
			// if(A.isFractionBar())
			// System.out.println("FractionBar!: A=" + A + " B=" + B);
			/*
			 * if(A.isHorizontalBar()) { d1 = Math.min(d1, dist2(new
			 * DPoint(A.centroidX, A.centroidY), new DPoint(B.minX,
			 * B.centroidY)) / dhf); } else {
			 */
			d1 = Math.min(
					d1,
					dist2(new DPoint(A.maxX, A.centroidY), new DPoint(B.minX,
							B.centroidY))
							/ dhf);
			// }
		} else if (B.right(A) && !A.inMainBaseline && !B.inMainBaseline
				&& !B.name.equals("[") && !A.name.equals("[")) {

			if (B.isFractionBar()) {
				// System.out.println("FractionBar!: B=" + B + " A=" + A);
				d1 = Math.min(
						d1,
						dist2(new DPoint(B.centroidX, B.centroidY), new DPoint(
								A.minX, A.centroidY))
								/ dhf);
			} else {
				d1 = Math.min(
						d1,
						dist2(new DPoint(B.maxX, B.centroidY), new DPoint(
								A.minX, A.centroidY))
								/ dhf);
			}
		}

		if (A.name.equals("[")) {
			double opt = Double.POSITIVE_INFINITY;
			for (int i = 0; i < A.atractors.size(); i++) {
				p = A.atractors.get(i);
				for (int j = 0; j < B.leftAtractor.size(); j++) {
					d1 = Math.min(d1,
							(int) 1.0 * dist2(p, B.leftAtractor.get(j)));
					/*
					 * if(B.subscThreshold >= p.x && p.x >= B.superThreshold) {
					 * d1 = Math.min(d1, (int) 1.0 * dist2(p, (DPoint)
					 * B.leftAtractor.get(j))/2.0); }
					 */
					if (d1 < opt) {
						opt = d1;
					}
				}
			}
			// A.atractors.remove(iopt);
			// d1 = Math.min(d1,dist2(new DPoint(A.maxX,A.centroidY), new
			// DPoint(B.minX,B.centroidY)));
		} else if (B.name.equals("[")) {
			double opt = Double.POSITIVE_INFINITY;
			for (int i = 0; i < B.atractors.size(); i++) {
				p = B.atractors.get(i);
				for (int j = 0; j < A.leftAtractor.size(); j++) {
					d1 = Math.min(d1,
							(int) 1.0 * dist2(p, A.leftAtractor.get(j)));
					/*
					 * if(A.subscThreshold >= p.x && p.x >= A.superThreshold) {
					 * d1 = Math.min(d1, (int) 1.0 * dist2(p, (DPoint)
					 * A.leftAtractor.get(j))/2.0); }
					 */
					if (d1 < opt) {
						opt = d1;
					}
				}
			}
			// B.atractors.remove(iopt);
			// d1 = Math.min(d1,dist2(new DPoint(B.maxX,B.centroidY), new
			// DPoint(A.minX,A.centroidY)));
		}

		return d1;
	}

	public double getDistance() {
		if (SymbolEdge.isUsingAtractors()) {
			return dist1;
		} else {
			return dist0;
		}
	}

	public void setDistance(double d) {
		dist0 = dist1 = dist2 = d;
	}

	public static void setDistanceFactors(boolean set) {
		if (set) {
			vrf = 5;
			frf = 5;
		} else {
			vrf = 1;
			frf = 1;
		}

		useDistanceFactors = set;
	}

	public static void setHorizontalFactors(boolean set) {
		if (set) {
			horf = 15;
		} else {
			horf = 1;
		}
	}

	public static void setDominaceHorizontalFactors(boolean set) {
		if (set) {
			dhf = 15;
		} else {
			dhf = 1;
		}
	}

	public double getDistance2() {
		if (SymbolEdge.isUsingAtractors()) {
			return Math.sqrt(dist1);
		} else {
			return Math.sqrt(dist0);
		}
	}

	@Override
	public int compareTo(DataStructures.Comparable o) {
		return this.compareTo((SymbolEdge) o);
	}

	public int compareTo(Object o) {
		return this.compareTo((SymbolEdge) o);
	}

	public int compareTo(SymbolEdge se) {
		return (this.getDistance() < se.getDistance()) ? -1 : ((this
				.getDistance() == se.getDistance()) ? 0 : 1);
	}

	/*
	 * public void setNodes(SymbolNode a, SymbolNode b) { this.nodeA = a;
	 * this.nodeB = b; this.dist = (a.centroidX - b.centroidX)*(a.centroidX -
	 * b.centroidX)/horf + (a.centroidY - b.centroidY)*(a.centroidY -
	 * b.centroidY)/horf; }
	 */

	public SymbolNode getNodeA() {
		return nodeA;
	}

	public SymbolNode getNodeB() {
		return nodeB;
	}

	@Override
	public String toString() {
		String str = "[" + nodeA.toString() + ", " + nodeB.toString() + ", "
				+ getDistance() +
				// ","+inf+
				"]";
		return str;
	}

	static double dist2(DPoint p, DPoint q) {
		return (p.x - q.x) * (p.x - q.x) * p.xf * q.xf / horf + (p.y - q.y)
				* (p.y - q.y) * p.yf * q.yf;
	}

	public static void setUsingAtractors(boolean b) {
		useAtractors = b;
	}

	public static void setTwosideDominace(boolean b) {
		checkTwosideDominance = b;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SymbolEdge) {
			SymbolEdge o = (SymbolEdge) object;

			return (this.nodeA.equals(o.nodeA) && this.nodeB.equals(o.nodeB))
					|| (this.nodeA.equals(o.nodeB) && this.nodeB
							.equals(o.nodeA));
		}

		return false;
	}

	public static boolean isUsingAtractors() {
		return useAtractors;
	}
}

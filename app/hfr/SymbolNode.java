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
package hfr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import ocr.DPoint;

public class SymbolNode extends SymbolList implements Comparable<SymbolNode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -429583096605376795L;
	public static final int NON_SCRIPTED = 0;
	public static final int OPEN_BRACKET = 1;
	public static final int ROOT = 2;
	public static final int VARIABLE_RANGE = 3;
	public static final int ASCENDER = 4;
	public static final int DESCENDER = 5;
	public static final int CLOSE_BRACKET = 6;

	private static final String[][] NAMES = {
			// NON_SCRIPTED
			{ "NON_SCRIPTED", "+", "-", "*", "/", ".", "dot", "comma", "=",
					"<", ">", "\\arrow", "\\cdot", "\\matrix_space" },
			// OPEN_BRACKET
			{ "OPEN_BRACKET", "(", "[", "{" },
			// ROOT
			{ "ROOT", "\\sqrt" },
			// VARIABLE_RANGE
			{ "VARIABLE_RANGE", "\\int", "\\sum", "\\prod", "\\oint",
					"\\coprod", "\\bigcap", "\\bigcup", "\\bigvee",
					"\\bigwedge" },
			// ASCENDER
			{ "ASCENDER", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
					"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
					"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "Y",
					"Z", "b", "d", "f", "h", "i", "k", "l", "t", "\\Sigma",
					"\\Gamma", "\\Delta", "\\Theta", "\\Lambda", "\\Omega",
					"\\Pi", "\\delta", "\\theta", "\\lambda", "\\chi", "\\psi",
					"\\kappa", "\\phi", "^", "\\partial" },
			// DESCENDER
			{ "DESCENDER", "g", "j", "p", "q", "y", "\\beta", "\\gamma",
					"\\eta", "\\mu", "\\rho", "\\zeta", "\\xi" },
			// CLOSE_BRACKET
			{ "CLOSE_BRACKET", ")", "]", "}" }, };

	public static float c = 0.33f;
	public static float t = 0.2f;

	public static String dot = "dot";

	public int minX;
	public int maxX;
	public int minY;
	public int maxY;
	public int centroidX;
	public int centroidY;
	public int subscThreshold;
	public int superThreshold;
	public int leftThreshold;
	public int width;
	public int height;
	public int upWall = Integer.MIN_VALUE;
	public int downWall = Integer.MAX_VALUE;
	public int leftWall = Integer.MIN_VALUE;
	public int rightWall = Integer.MAX_VALUE;
	public float diagonal;
	public boolean inMainBaseline;

	public String name;
	public String type;

	public SymbolList up = new SymbolList();
	public SymbolList superscript = new SymbolList();
	public SymbolList right = new SymbolList();
	public SymbolList subexpression = new SymbolList();
	public SymbolList subscript = new SymbolList();
	public SymbolList down = new SymbolList();
	public SymbolList downleft = new SymbolList();
	public SymbolList left = new SymbolList();
	public SymbolList upleft = new SymbolList();
	public SymbolList rows = new SymbolList();

	public int hbMinY = Integer.MAX_VALUE;
	public int hbMaxY = Integer.MIN_VALUE;
	public boolean someInUpRegion = false;
	public boolean someInDownRegion = false;

	public ArrayList<DPoint> atractors = new ArrayList<DPoint>();
	public DPoint center;

	public ArrayList<DPoint> upAtractor = new ArrayList<DPoint>();
	public ArrayList<DPoint> downAtractor = new ArrayList<DPoint>();
	public ArrayList<DPoint> leftAtractor = new ArrayList<DPoint>();
	public ArrayList<DPoint> rightAtractor = new ArrayList<DPoint>();

	// public SymbolNode up = null;
	// public SymbolNode superscript = null;
	// public SymbolNode right = null;
	// public SymbolNode subexpression = null;
	// public SymbolNode subscript = null;
	// public SymbolNode down = null;

	public SymbolNode dad = null;
	public SymbolNode son = null;
	public double weight = Double.POSITIVE_INFINITY;
	public boolean belonging = false;

	public static boolean weightCompareTo = false;
	public static boolean yCompareTo = false;
	public static boolean drawAttractors = false;

	public SymbolNode rightBracket = null;
	public SymbolNode leftBracket = null;

	public SymbolNode() {
		this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
				Integer.MIN_VALUE, "no_name");
	}

	public SymbolNode(SymbolNode sn) {
		this(sn.minX, sn.minY, sn.maxX, sn.maxY, sn.name);
		this.hbMaxY = sn.hbMaxY;
		this.hbMinY = sn.hbMinY;
		this.someInDownRegion = sn.someInDownRegion;
		this.someInUpRegion = sn.someInUpRegion;
	}

	public SymbolNode(double minX, double minY, double maxX, double maxY,
			String name) {
		this((int) minX, (int) minY, (int) maxX, (int) maxY, name);
	}

	public SymbolNode(int minX, int minY, int maxX, int maxY, String name) {
		int i, j, indexClass, H;

		indexClass = -1;
		for (i = 0; i < 7; i++) {
			for (j = 1; j < SymbolNode.NAMES[i].length; j++) {
				if (name.equals(SymbolNode.NAMES[i][j])) {
					indexClass = i;
					break;
				}
			}
			if (indexClass != -1) {
				break;
			}
		}

		if (indexClass == -1) {
			this.type = "CENTERED";
			// indexClass = 6;
		} else {
			this.type = new String(SymbolNode.NAMES[indexClass][0]);
		}

		this.name = new String(name);

		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;

		this.width = maxX - minX;
		this.height = maxY - minY;

		this.centroidX = (minX + maxX) / 2;

		this.diagonal = (float) Math.sqrt(this.width * this.width + this.height
				* this.height);

		H = maxY - minY;
		if (indexClass == SymbolNode.NON_SCRIPTED) {
			this.centroidY = (minY + maxY) / 2;
			this.superThreshold = this.minY; // this.centroidY;
			this.subscThreshold = this.maxY; // this.centroidY;
			this.leftThreshold = this.maxX;
		} else if (indexClass == SymbolNode.OPEN_BRACKET) {
			this.centroidY = maxY - (int) (0.5 * H);
			// this.centroidY = maxY - (int)(c*H);
			// this.superThreshold = this.minY;
			// this.subscThreshold = this.maxY;
			this.superThreshold = this.minY + (int) (t * H);
			this.subscThreshold = this.maxY - (int) (t * H);
			this.leftThreshold = this.minX;
		} else if (indexClass == SymbolNode.CLOSE_BRACKET) {
			this.centroidY = maxY - (int) (0.5 * H);
			// this.centroidY = maxY - (int)(c*H);
			// this.superThreshold = this.minY;
			// this.subscThreshold = this.maxY;
			this.superThreshold = this.minY + (int) (t * H);
			this.subscThreshold = this.maxY - (int) (t * H);
			this.leftThreshold = this.minX;
		} else if (indexClass == SymbolNode.ROOT) {
			// this.centroidY = maxY - (int)(c*H);
			this.centroidY = maxY - (int) (0.5 * H);
			this.superThreshold = this.minY + (int) (t * H);
			this.subscThreshold = this.maxY - (int) (t * H);
			this.leftThreshold = this.maxX;
		} else if (indexClass == SymbolNode.VARIABLE_RANGE) {
			this.centroidY = maxY - (int) (0.5 * H);
			this.superThreshold = this.minY + (int) (t * H);
			this.subscThreshold = this.maxY - (int) (t * H);
			this.leftThreshold = this.minX;
		} else if (indexClass == SymbolNode.ASCENDER) {
			this.centroidY = maxY - (int) (c * H);
			this.superThreshold = this.minY + (int) (0.35 * H * (t + 1.0));
			this.subscThreshold = this.maxY - (int) (0.75 * t * H);
			// this.leftThreshold = this.minX;
			this.leftThreshold = this.maxX;
		} else if (indexClass == SymbolNode.DESCENDER) {
			this.centroidY = minY + (int) (c * H);
			this.superThreshold = this.minY + (int) (0.65 * t * H);
			this.subscThreshold = this.maxY - (int) (0.45 * H * (t + 1.0));
			// this.leftThreshold = this.minX;
			this.leftThreshold = this.maxX;
		} else {
			this.centroidY = maxY - (int) (0.5 * H);
			this.superThreshold = this.minY + (int) (t * H);
			this.subscThreshold = this.maxY - (int) (t * H);
			// this.leftThreshold = this.minX;
			this.leftThreshold = this.maxX;
		}

		// this.leftThreshold = this.maxX;

		if (this.isHorizontalBar()) {
			this.atractors.add(new DPoint(this.minX, this.centroidY));
			this.atractors.add(new DPoint(this.minX + this.width / 6.0,
					this.centroidY));
			this.atractors.add(new DPoint(this.minX + 2.0 * this.width / 6.0,
					this.centroidY));
			this.atractors.add(new DPoint(this.centroidX, this.centroidY));
			this.atractors.add(new DPoint(this.minX + 4.0 * this.width / 6.0,
					this.centroidY));
			this.atractors.add(new DPoint(this.minX + 5.0 * this.width / 6.0,
					this.centroidY));
			this.atractors.add(new DPoint(this.maxX, this.centroidY));
		} else if (this.isVariable() || this.isCloseBracket()) {
			this.atractors.add(new DPoint(this.maxX, this.minY));
			this.atractors.add(new DPoint(this.maxX, this.centroidY));
			this.atractors.add(new DPoint(this.maxX, this.maxY));
		} else if (this.isDigit() || this.isRoot()) {
			this.atractors.add(new DPoint(this.maxX, this.minY));
			this.atractors.add(new DPoint(this.maxX, this.centroidY));
		} else if (this.name.equals("\\prod")) {
			this.atractors.add(new DPoint(this.minX, this.minY));
			this.atractors.add(new DPoint(this.maxX, this.minY));
			this.atractors.add(new DPoint(this.centroidX, this.minY));
			this.atractors.add(new DPoint(this.centroidX, this.maxY));
			this.atractors.add(new DPoint(this.maxX, this.maxY));
			this.atractors.add(new DPoint(this.minX, this.maxY));
		} else if (this.isVariableRange()) {
			this.atractors.add(new DPoint(this.maxX, this.minY));
			// this.atractors.add(new DPoint(this.centroidX,this.centroidY));
			if (!this.name.equals("\\int")) {
				this.atractors.add(new DPoint(this.centroidX, this.minY));
				this.atractors.add(new DPoint(this.centroidX, this.maxY));
			}
			this.atractors.add(new DPoint(this.maxX, this.maxY));
		} else {
			this.atractors.add(new DPoint(this.centroidX, this.centroidY));
			this.atractors.add(new DPoint(this.maxX, this.centroidY));
		}

		center = new DPoint(this.centroidX, this.centroidY);

		this.upAtractor.add(new DPoint(this.minX, this.minY));
		this.upAtractor.add(new DPoint(this.centroidX, this.minY));
		this.upAtractor.add(new DPoint(this.maxX, this.minY));

		this.downAtractor.add(new DPoint(this.minX, this.maxY));
		this.downAtractor.add(new DPoint(this.centroidX, this.maxY));
		this.downAtractor.add(new DPoint(this.maxX, this.maxY));

		this.leftAtractor.add(new DPoint(this.minX, this.minY));
		this.leftAtractor.add(new DPoint(this.minX, this.centroidY));
		this.leftAtractor.add(new DPoint(this.minX, this.maxY));

		this.rightAtractor.add(new DPoint(this.maxX, this.minY));
		this.rightAtractor.add(new DPoint(this.maxX, this.centroidY));
		this.rightAtractor.add(new DPoint(this.maxX, this.maxY));
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object instanceof SymbolNode) {
			SymbolNode o = (SymbolNode) object;

			return (this.minX == o.minX && this.maxX == o.maxX
					&& this.minY == o.minY && this.maxY == o.maxY && this.name
						.equals(o.name));
		}

		return false;
	}

	public int compareTo(SymbolNode other) {
		double t, o;

		if (weightCompareTo) {
			t = this.weight;
			o = other.weight;
		} else if (yCompareTo) {
			{
				t = this.minY;
				o = other.minY;
			}

		} else {
			t = this.minX;
			o = other.minX;
		}

		return ((t < o) ? -1 : ((t == o) ? 0 : 1));
	}

	@Override
	public String toString() {
		String str = "";

		str += "[";
		str += name + ", ";
		// str += "name="+name+", ";
		// str += "type="+type+", ";
		// str += "minX="+minX+", ";
		// str += "minY="+minY+", ";
		// str += "maxX="+maxX+", ";
		// str += "maxY="+maxY;
		str += this.atractors.size() + "]";

		return str;
		// return name;
	}

	public boolean up(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (minX <= sn.centroidX &&
		// sn.centroidX < maxX &&
				sn.centroidX < this.leftThreshold && upWall < sn.centroidY && sn.centroidY <= superThreshold);
	}

	public boolean superscript(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (
		// maxX <= sn.centroidX &&
		this.leftThreshold <= sn.centroidX && sn.centroidX < rightWall
				&& upWall < sn.centroidY && sn.centroidY <= superThreshold);
	}

	public boolean right(SymbolNode sn) {
		boolean normal;
		if (this.equals(sn)) {
			return false;
		}
		// if(this.type.equals("NON_SCRIPTED")) {
		// if(this.type.equals("NON_SCRIPTED") &&
		// sn.type.equals("NON_SCRIPTED")) {
		// double angle = Math.atan2(sn.centroidX - this.centroidX, sn.centroidY
		// - this.centroidY);
		// return (-Math.PI/4.0 < angle && angle < Math.PI/4.0);// ||
		// sn.left(this);
		// }
		// else
		int angle = (int) (180 * Math.atan2(sn.centroidY - this.centroidY,
				sn.centroidX - this.centroidX) / Math.PI);
		normal = (leftThreshold <= sn.centroidX && sn.centroidX < rightWall
				&& sn.superThreshold < this.centroidY && this.centroidY <= sn.subscThreshold);

		if (this.isHorizontalBar()) {
			if (sn.isHorizontalBar() || sn.isDot()) {
				return -35 < angle && angle < 35;
			} else {
				return (leftThreshold <= sn.centroidX
						&& sn.centroidX < rightWall && sn.minY < this.centroidY && this.centroidY <= sn.maxY);
			}
		}
		if (this.name.equals("dot")) {
			if (sn.isHorizontalBar() || sn.isDot()) {
				return -15 < angle && angle < 15;
			} else {
				return normal;
			}

		} else {
			return normal;
		}
	}

	public boolean subscript(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (
		// maxX <= sn.centroidX &&
		this.leftThreshold <= sn.centroidX && sn.centroidX < rightWall
				&& subscThreshold < sn.centroidY && sn.centroidY <= downWall);
	}

	public boolean down(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (minX <= sn.centroidX &&
		// this.leftThreshold <= sn.centroidX &&
				sn.centroidX < maxX && subscThreshold < sn.centroidY && sn.centroidY <= downWall);
	}

	public boolean downleft(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (leftWall <= sn.centroidX && sn.centroidX < minX
				&& subscThreshold < sn.centroidY && sn.centroidY <= downWall);
	}

	public boolean left(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (leftWall <= sn.centroidX && sn.centroidX < minX
				&& superThreshold < sn.centroidY && sn.centroidY <= subscThreshold);
	}

	public boolean upleft(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (leftWall <= sn.centroidX && sn.centroidX < minX
				&& upWall < sn.centroidY && sn.centroidY <= superThreshold);
	}

	public boolean subexpression(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (
		// this.name.equals("\\sqrt") &&
		minX <= sn.centroidX && sn.centroidX < maxX && minY < sn.centroidY && sn.centroidY <= maxY);
	}

	public boolean row(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (
		// this.name.equals("\\sqrt") &&
		minX <= sn.centroidX && sn.centroidX < this.rightWall
				&& minY < sn.centroidY && sn.centroidY <= maxY);
	}

	public boolean rightCloseBracket(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (this.superThreshold > sn.minY && this.subscThreshold < sn.maxY && this.minX < sn.minX);
	}

	public boolean leftCloseBracket(SymbolNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		return (this.superThreshold > sn.minY && this.subscThreshold < sn.maxY && this.minX > sn.minX

		);
	}

	public String getSpatialRelation(SymbolNode s) {
		String relation = "";

		if (this.up(s)) {
			relation += "above";
		} else if (this.right(s)) {
			relation += "right";
		} else if (this.superscript(s)) {
			relation += "super_script";
		} else if (this.subscript(s)) {
			relation += "sub_script";
		} else if (this.down(s)) {
			relation += "below";
		} else if (this.downleft(s)) {
			relation += "low_left";
		} else if (this.left(s)) {
			relation += "left";
		} else if (this.upleft(s)) {
			relation += "top_left";
		} else if (this.subexpression(s)) {
			relation += "in";
		}

		return relation;
	}

	public void addRight(SymbolNode s) {
		this.right.add(s);
	}

	@Override
	public boolean add(SymbolNode s) {
		boolean added = true;

		if (this.equals(s)) {
			return false;
		}

		if (this.subexpression(s)) {
			this.subexpression.add(s);
			return true;
		} else if (this.superscript(s)) {
			this.superscript.add(s);
			return true;
		} else if (this.subscript(s)) {
			this.subscript.add(s);
			return true;
		} else if (this.up(s)) {
			up.add(s);
			return true;
		} else if (this.down(s)) {
			this.down.add(s);
			return true;
		} else if (this.downleft(s) && this.name.equals("\\prod")) {
			this.downleft.add(s);
			return true;
		} else if (this.upleft(s) && this.name.equals("\\prod")) {
			this.upleft.add(s);
			return true;
		}
		/*
		 * else if(this.right(s)) { this.right.add(s); return true; } else
		 * if(this.left(s)) { this.left.add(s); return true; }
		 */
		else {
			added = false;
		}

		return added;
	}

	public boolean addNotHorizontal(SymbolNode s) {
		boolean added = true;

		if (this.equals(s)) {
			return false;
		}

		if (this.subexpression(s)) {
			this.subexpression.add(s);
		} else if (this.superscript(s)) {
			this.superscript.add(s);
		} else if (this.subscript(s)) {
			this.subscript.add(s);
		} else if (this.up(s)) {
			up.add(s);
		} else if (this.down(s)) {
			this.down.add(s);
		} else if (this.downleft(s)) {
			this.downleft.add(s);
		} else if (this.upleft(s)) {
			this.upleft.add(s);
		} else {
			added = false;
		}

		return added;
	}

	public boolean overlaps(SymbolNode sn) {
		return (!this.equals(sn)
				&& this.type.equals("NON_SCRIPTED")
				&& this.minX <= sn.centroidX
				&& sn.centroidX < this.maxX
				&& !sn.subexpression(this)
				&& !((sn.type.equals("CLOSE_BRACKET") || sn.type
						.equals("OPEN_BRACKET")) && sn.minY <= this.centroidY && sn.centroidY < sn.maxY) && !((sn.type
				.equals("NON_SCRIPTED") || sn.type.equals("VARIABLE_RANGE")) && this.width < sn.width));
	}

	public boolean dominates(SymbolNode sn) {
		boolean dominated = false;

		if (this.equals(sn)) {
			return false;
		}
		if (sn.name.equals("\\dot")) {
			return true;
		}
		if (this.name.equals("-")) {
			dominated = this.minX <= sn.centroidX && sn.centroidX <= this.maxX;

			if (sn.name.equals("-")) {
				if (dominated) {
					dominated = sn.width <= this.width;
				}
			} else if (sn.type.equals("ROOT")) {
				if (sn.subexpression(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("VARIABLE_RANGE")) {
				if (dominated) {
					if (sn.minX < this.minX || this.maxX < sn.maxX) {
						dominated = false;
					}
				}
			}
		} // if "-"
		else if (this.type.equals("NON_SCRIPTED")
				|| this.type.equals("OPEN_BRACKET")) {
			dominated = this.minX < sn.minX;

			if (sn.name.equals("-")) {
				if (sn.minX <= this.centroidX && this.centroidX <= sn.maxX) {
					// if(sn.up(this) || sn.down(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("ROOT")) {
				if (sn.subexpression(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("VARIABLE_RANGE")) {
				if ((sn.superscript(this) || sn.subscript(this))
						&& sn.height > this.height) {
					dominated = false;
				}
			}

		} // if "NON_SCRIPTED"
		else if (this.type.equals("ROOT")) {
			dominated = this.subexpression(sn) || this.superscript(sn);

			if (sn.name.equals("-")) {
				if (sn.up(this) || sn.down(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("ROOT")) {
				if (sn.subexpression(this) && this.width < sn.width) {
					dominated = false;
				}
			} else if (sn.type.equals("VARIABLE_RANGE")) {
				if ((sn.superscript(this) || sn.subscript(this))
						&& sn.height > this.height) {
					dominated = false;
				}
			}
		} // if "ROOT"
		else if (this.type.equals("VARIABLE_RANGE")) {
			// dominated = this.superscript(sn) || this.subscript(sn);
			dominated = ((sn.centroidY < this.superThreshold || sn.centroidY > this.subscThreshold)
					&& this.height > sn.height && this.maxX < sn.centroidX)
					|| ((sn.centroidY < this.minY || sn.centroidY > this.maxY)
							&& this.height > sn.height && this.maxX >= sn.centroidX);

			if (this.name.equals("\\prod")) {
				dominated = ((sn.centroidY < this.superThreshold || sn.centroidY > this.subscThreshold) && this.height > sn.height);
			}

			if (sn.name.equals("-")) {
				if ((sn.up(this) || sn.down(this)) && sn.minX < this.minX
						&& this.maxX < sn.maxX) {
					dominated = false;
				}
			}
			if (sn.type.equals("VARIABLE_RANGE")) {
				if ((sn.superscript(this) || sn.subscript(this))
						&& sn.height > this.height) {
					dominated = false;
				}
			}

			// dominated =
			// dominated && Math.abs(sn.subscThreshold - sn.superThreshold) >
			// this.height;
		} // if "VARIABLE_RANGE"
		else { // close brackets and variables
			dominated = this.superscript(sn) || this.subscript(sn);

			if (sn.name.equals("-")) {
				if (sn.minX <= this.centroidX && this.centroidX <= sn.maxX) {
					// if(sn.up(this) || sn.down(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("ROOT")) {
				if (sn.subexpression(this)) {
					dominated = false;
				}
			} else if (sn.type.equals("VARIABLE_RANGE")) {
				if ((sn.superscript(this) || sn.subscript(this))
						&& sn.height > this.height) {
					dominated = false;
				}
			}
		} // if "CLOSE_BRACKET" or variable

		return dominated;
	}

	public boolean isAdjacent(SymbolNode sn) {
		return (!sn.type.equals("NON_SCRIPTED") && !this.equals(sn)
				&& sn.superThreshold <= this.centroidY && this.centroidY < sn.subscThreshold);
	}

	public boolean isCloseBracket() {
		return (new String(")]}")).lastIndexOf(this.name) != -1;
	}

	public boolean isComma() {
		return this.name.equals("comma");
	}

	public boolean isDigit() {
		return (new String("0123456789")).lastIndexOf(this.name) != -1;
	}

	public boolean isDot() {
		return this.name.equals("dot");
	}

	public boolean isFractionBar() {
		return this.name.equals("-") && this.someInDownRegion
				&& this.someInUpRegion;
	}

	public boolean isHorizontalBar() {
		return this.name.equals("-");
	}

	public boolean isVariable() {
		return (this.type.equals("ASCENDER") || this.type.equals("DESCENDER") || this.type
				.equals("CENTERED"))
				&& !this.isDigit()
				&& !this.isCloseBracket();
	}

	public boolean isVariableRange() {
		return this.type.equals("VARIABLE_RANGE");
	}

	public boolean isNonScripted() {
		return this.type.equals("NON_SCRIPTED");
	}

	public boolean isOpenBracket() {
		return (new String("([{")).lastIndexOf(this.name) != -1;
	}

	public boolean isOperator() {
		return this.type.equals("VARIABLE_RANGE");
	}

	public boolean isRoot() {
		return this.type.equals("ROOT");
	}

	public boolean isDad(SymbolNode sn) {
		SymbolNode dad;
		boolean dadmatches = false;

		dad = this.dad;
		while (dad != null) {
			if (dadmatches = dad.equals(sn)) {
				break;
			}
			dad = dad.dad;
		}

		return dadmatches;
	}

	public SymbolList getProperChildrenSymbolList(SymbolList sl) {
		SymbolList cl = new SymbolList();
		int i;

		for (i = 0; i < sl.size(); i++) {
			try {
				if (sl.symbolNodeAt(i).dad.equals(this)) {
					cl.add(new SymbolNode(sl.symbolNodeAt(i)));
				}
			} catch (NullPointerException npe) {

			}
		}
		// cl.sort();

		return cl;
	}

	public SymbolList getChildrenSymbolList(SymbolList sl) {
		SymbolList cl = new SymbolList();
		int i;

		for (i = 0; i < sl.size(); i++) {
			if (sl.symbolNodeAt(i).isDad(this)) {
				cl.add(new SymbolNode(sl.symbolNodeAt(i)));
			}
		}

		cl.sort();

		return cl;
	}

	public void addRows(SymbolList sl) {
		SymbolList cl = new SymbolList();// getProperChildrenSymbolList(sl);
		int i, j;

		SymbolNode.yCompareTo = true;
		for (i = 0; i < sl.size(); i++) {
			if (this.equals(sl.symbolNodeAt(i).dad)) {
				cl.add(new SymbolNode(sl.symbolNodeAt(i)));
			}
		}
		cl.sort();
		SymbolNode.yCompareTo = false;

		System.out.println("cl: " + cl + "\n--o--");

		if (!cl.isEmpty()) {

			/*
			 * SymbolList row; SymbolNode sn; for(i = 0; i < cl.size(); i++) {
			 * row = new SymbolList(); sn = new SymbolNode(cl.symbolNodeAt(i));
			 * row.add(sn); for(j = 0; j < sl.size(); j++) {
			 * if(sl.symbolNodeAt(j).isDad(sn)) { row.add(new
			 * SymbolNode(sl.symbolNodeAt(j))); } }
			 * this.rows.add((SymbolList)row); System.out.println(row); }
			 */

			SymbolList row;

			for (j = 0; j < cl.size(); j++) {
				row = new SymbolList();
				row.add(new SymbolNode(cl.symbolNodeAt(j)));
				row.add(cl.symbolNodeAt(j).getChildrenSymbolList(sl));
				// row.addSpaces(" & ",1.3);
				this.rows.add(j, (SymbolNode) row);
			}

			// this.rows.add(cl);
		}
	}

	public String rowsToString() {
		String str = "" + this + ":\n";

		for (int i = 0; i < this.rows.size(); i++) {
			str += "\t" + this.rows.get(i) + "\n";
		}

		return str;
	}

	public String getName() {
		return name;
	}

	public void drawBoundingBox(Graphics2D g2, Color c) {
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND));
		g2.setColor(c);
		g2.drawRect(this.minX, this.minY, this.width, this.height);
	}

	public void draw(Graphics2D g2, SymbolNode sn, String label) {
		draw(g2, sn, label, new Color(255, 0, 255, 120));
	}

	public void draw(Graphics2D g2, SymbolNode sn, String label, Color c) {
		FontMetrics metrics = g2.getFontMetrics();
		int width, height, x, y;

		g2.setColor(c);
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND));

		g2.drawLine(this.centroidX, this.centroidY, sn.centroidX, sn.centroidY);

		g2.setStroke(new BasicStroke(4, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND));

		g2.drawLine(this.centroidX, this.centroidY, this.centroidX,
				this.centroidY);
		g2.drawLine(sn.centroidX, sn.centroidY, sn.centroidX, sn.centroidY);

		width = metrics.stringWidth(label);
		height = metrics.getHeight();

		x = (this.centroidX + sn.centroidX) / 2;
		y = (this.centroidY + sn.centroidY) / 2;
		// g2.setColor(Color.white);
		// g2.fillRect(x - width/2, y - height/2, width, height);
		g2.setColor(new Color(255, 0, 0, 140));
		g2.drawString(label, x - width / 2, y + height / 2);
		g2.setColor(new Color(0, 255, 0, 200));
		/*
		 * if(drawAtractors) { for(int i = 0; i < sn.atractors.size(); i++) {
		 * DPoint p = (DPoint) sn.atractors.get(i); g2.draw(new
		 * Line2D.Double((float)p.x,(float)p.y,(float)p.x,(float)p.y)); } }
		 */

	}

	public static void draw(Graphics2D g2, SymbolNode sn) {
		g2.setColor(new Color(0, 0, 255, 30));
		g2.fill(new Rectangle2D.Double(sn.minX, sn.minY, sn.width,
				sn.superThreshold - sn.minY));

		g2.fill(new Rectangle2D.Double(sn.minX, sn.subscThreshold, sn.width,
				sn.maxY - sn.subscThreshold));
		g2.setColor(new Color(0, 0, 255, 200));
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND));

		g2.draw(new Line2D.Double(sn.centroidX, sn.centroidY, sn.centroidX,
				sn.centroidY));

		g2.setColor(new Color(255, 0, 0, 200));
		// System.out.println(sn);
		if (SymbolNode.drawAttractors) {
			for (int i = 0; i < sn.atractors.size(); i++) {
				DPoint p = sn.atractors.get(i);
				g2.draw(new Line2D.Double((float) p.x, (float) p.y,
						(float) p.x, (float) p.y));

			}
		}
	}
}

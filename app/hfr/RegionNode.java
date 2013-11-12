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

public class RegionNode {
	public static final int ABOVE_RIGHT = 0;
	public static final int ABOVE = 1;
	public static final int ABOVE_LEFT = 2;
	public static final int LEFT = 3;
	public static final int BELOW_LEFT = 4;
	public static final int BELOW = 5;
	public static final int BELOW_RIGHT = 6;
	public static final int RIGHT = 7;
	public static final int IN = 8;

	private static final String TYPES[] = { "ABOVE_RIGHT", "ABOVE",
			"ABOVE_LEFT", "LEFT", "BELOW_LEFT", "BELOW", "BELOW_RIGHT",
			"RIGHT", "IN" };

	private int minX = Integer.MIN_VALUE;
	private int minY = Integer.MIN_VALUE;

	private int maxX = Integer.MAX_VALUE;
	private int maxY = Integer.MAX_VALUE;

	private String name = null;
	private String type = null;

	private SymbolList symbolList = new SymbolList();
	private SymbolNode parentSymbol = null;

	public RegionNode(int minX, int minY, int maxX, int maxY, String name,
			String type) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxY;
		this.maxY = maxY;

		this.name = new String(name);
		this.type = new String(type);
	}

	public RegionNode(int minX, int minY, int maxX, int maxY, String name) {
		this(minX, minY, maxX, maxY, name, "no_type");
	}

	public RegionNode(int minX, int minY, int maxX, int maxY) {
		this(minX, minY, maxX, maxY, "no_name", "no_type");
	}

	public RegionNode() {
		this(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
				Integer.MAX_VALUE, "no_name", "no_type");
	}

	public RegionNode(int minX, int minY, int maxX, int maxY, String name,
			int type) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;

		this.name = new String(name);
		this.type = new String(TYPES[type]);
	}

	public RegionNode(int minX, int minY, int maxX, int maxY, int type) {
		this(minX, minY, maxX, maxY, "no_name", type);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RegionNode) {
			RegionNode regionNode;

			regionNode = (RegionNode) object;

			return (this.minX == regionNode.minX
					&& this.maxX == regionNode.maxX
					&& this.minY == regionNode.minY
					&& this.maxY == regionNode.maxY
					&& this.name.equals(regionNode.name) && this.type
						.equals(regionNode.type));
		}

		return false;
	}

	public int getMinX() {
		return this.minX;
	}

	public int getMinY() {
		return this.minY;
	}

	public int getMaxX() {
		return this.maxX;
	}

	public int getMaxY() {
		return this.maxY;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public void setMinX(int x) {
		this.minX = x;
	}

	public void setMinY(int y) {
		this.minY = y;
	}

	public void setMaxX(int x) {
		this.maxX = x;
	}

	public void setMaxY(int y) {
		this.maxY = y;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SymbolList getSymbolList() {
		return this.symbolList;
	}

	public SymbolNode getParentSymbol() {
		return this.parentSymbol;
	}

	public void setParentSymbol(SymbolNode symbolNode) {
		this.parentSymbol = symbolNode;
	}

	public void setSymbolList(SymbolList symbolList) {
		this.symbolList = symbolList;
	}

	public boolean isLocated(SymbolNode symbolNode) {
		return (this.minX <= symbolNode.centroidX
				&& this.maxX < symbolNode.centroidX
				&& this.minY <= symbolNode.centroidY && this.maxY < symbolNode.centroidY);
	}

	public SymbolNode symbolAt(int i) {
		return this.symbolList.get(i);
	}

	public void addSymbolNode(SymbolNode symbolNode) {
		// symbolNode.parentRegion = this;
		this.symbolList.add(symbolNode);
	}
}

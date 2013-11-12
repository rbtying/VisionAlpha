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
 * @author unascribed
 * @version 1.0
 */

import hfr.SymbolList;
import hfr.SymbolNode;

public class SymbolGraph {
	SymbolList nodes = new SymbolList();

	public SymbolGraph(SymbolList n) {
		this.nodes.add(n);
		this.nodes.sort();
	}

	SymbolNode symbolNodeAt(int i) {
		return nodes.symbolNodeAt(i);
	}
}

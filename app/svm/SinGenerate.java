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
package svm;

/**
 * �berschrift: Beschreibung: Copyright: Copyright (c) Organisation:
 * 
 * @author
 * @version 1.0
 */

public class SinGenerate {

	public static void main(String[] args) {
		for (float x = 0; x <= 1.0; x += 0.1)
			for (float y = 0; y <= 1.0; y += 0.1) {
				System.out.println(Math.sin(0.5 * (x + y) / Math.PI) + " 1:"
						+ x + " 2:" + y);
			}
	}
}

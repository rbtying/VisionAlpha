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
 * <p>�berschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */

import java.io.DataOutputStream;
import java.io.FileOutputStream;

public class RData {

	public static void main(String[] args) throws Exception {
		DataOutputStream fileout;

		fileout = new DataOutputStream(new FileOutputStream(args[0]));

		double x, y;

		for (int i = 0; i < Integer.parseInt(args[1]); i++) {
			x = 2 * Math.random() - 1;
			y = 2 * Math.random() - 1;
			fileout.writeBytes((x * x + x * y) + " 0:" + x + " 1:" + y + "\n");
		}

		fileout.close();
	}

}

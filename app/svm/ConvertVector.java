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
 * <p>\u00DCberschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) </p>
 * <p>Organisation: </p>
 * @author unascribed
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.StringTokenizer;

public class ConvertVector {
	static BufferedReader filein;
	static DataOutputStream filetrn, filetst, fileval;
	static int i, j, index;
	static StringTokenizer st;
	static String line, data;
	static float x, y;
	static int l, size;

	public static void main(String argv[]) throws Exception {
		filein = new BufferedReader(new FileReader(argv[0]));
		filetrn = new DataOutputStream(new FileOutputStream(argv[1]));

		while ((line = filein.readLine()) != null) {
			st = new StringTokenizer(line, " ");
			size = st.countTokens() - 1;
			if (size < 0)
				continue;
			data = "";
			for (i = 0; i < size; i++) {
				data += " " + i + ":" + Float.parseFloat(st.nextToken());
			}
			data = st.nextToken() + data;
			// System.out.println(data);
			filetrn.writeBytes(data + "\n");
		}

		filein.close();
		filetrn.close();

	}

}

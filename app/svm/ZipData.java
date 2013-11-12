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
 * @author unascribed
 * @version 1.0
 */

import java.util.*;
import java.io.*;

public class ZipData {
	public static void main(String argv[]) throws Exception {
		BufferedReader filein;
		DataOutputStream fileout;
		String line;
		StringTokenizer st;
		filein = new BufferedReader(new FileReader(argv[0]));
		fileout = new DataOutputStream(new FileOutputStream(argv[1]));

		int i, len;
		float x;
		while ((line = filein.readLine()) != null) {
			System.out.print(".");
			st = new StringTokenizer(line, ", ");

			if ((len = st.countTokens()) == 0)
				continue;
			fileout.writeBytes("" + ((int) Double.parseDouble(st.nextToken()))
					+ " ");

			for (i = 0; i < len - 1; i++) {
				x = Float.parseFloat(st.nextToken());
				// x = (x + 1.0f)/2.0f;
				if (x != 0.0f)
					fileout.writeBytes("" + i + ":" + x + " ");
			}
			fileout.writeBytes("\n");

		}
		filein.close();
		fileout.close();
	}
}

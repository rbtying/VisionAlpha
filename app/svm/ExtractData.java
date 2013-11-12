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
/**
 * \uFFFDberschrift: <p>
 * Beschreibung: <p>
 * Copyright: Copyright (c) <p>
 * Organisation: <p>
 * @author
 * @version 1.0
 */
package svm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class ExtractData {
	public static void main(String[] argv) {
		String line;
		StringTokenizer st;
		BufferedReader filein = null;
		DataOutputStream fileout = null;
		String digit;
		boolean found;

		int i = 1;
		try {
			filein = new BufferedReader(new FileReader(argv[0]));
			fileout = new DataOutputStream(new FileOutputStream(argv[1]));

			while ((line = filein.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}

				st = new StringTokenizer(line, " :");
				found = false;
				digit = st.nextToken();
				for (i = 2; i < argv.length; i++) {
					found |= digit.equals(argv[i]);
				}

				if (found) {
					System.out.println(digit);
					fileout.writeBytes(line + "\n");
				}
			}

			filein.close();
		} catch (FileNotFoundException fnfe1) {
			System.err.println("File " + argv[0] + " not found...");
			System.exit(1);
		} catch (IOException ioe1) {
			System.err.println("Error reading file " + argv[0] + "...");
			System.exit(1);
		}
	}

}

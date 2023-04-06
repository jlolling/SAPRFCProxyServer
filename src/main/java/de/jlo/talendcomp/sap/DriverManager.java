/**
 * Copyright 2023 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.sap;

public class DriverManager {
		
	private static DriverManager INSTANCE = null;

	private DriverManager() {
	}
	
	public static DriverManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DriverManager();
		}
		return INSTANCE;
	}
		
	/**
	 * Returns the driver implementation
	 * @return driver instance - actually a instance of DriverImpl
	 * @throws Exception
	 */
	public static Driver getDriverSAPJCO() throws Exception {
		try {
			Class.forName("com.sap.conn.jco.JCoTable");
		} catch (ClassNotFoundException e) {
			throw new Exception("Classes from sapjco3.jar not available.", e);
		}
		// this is the part we separate the implementation from the interface
		return (Driver) Class.forName("de.jlo.talendcomp.sap.sapjco.DriverImpl").getDeclaredConstructor().newInstance();
	}
	
}

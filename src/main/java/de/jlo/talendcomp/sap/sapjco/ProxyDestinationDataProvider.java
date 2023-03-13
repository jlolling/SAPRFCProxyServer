package de.jlo.talendcomp.sap.sapjco;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import de.jlo.talendcomp.sap.ConnectionProperties;

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
/**
 * This is a custom DestinationDataProvider to provide the settings given from the Talend job
 * to the DestinationManager
 * @author Jan Lolling jan.lolling@gmail.com
 *
 */
public class ProxyDestinationDataProvider implements DestinationDataProvider  {
	
	private Map<String, Properties> propertiesMap = new HashMap<>();
	private static ProxyDestinationDataProvider instance = null;
	private static Object lock = new Object();
	
	private ProxyDestinationDataProvider() {}
	
	/**
	 * Get the singleton instance
	 * @return singleton instance of ProxyDestinationDataProvider
	 */
	public static ProxyDestinationDataProvider getInstance() {
		synchronized(lock) {
			if (instance == null) {
				instance = new ProxyDestinationDataProvider();
			}
		}
		return instance;
	}
	
	/**
	 * Set here the connection properties made in the Builder methods from the ConnectionFactory
	 * @param cp 
	 */
	public void setConnectionProperties(ConnectionProperties cp) {
		if (propertiesMap.containsKey(cp.getDestinationName()) == false) {
			propertiesMap.put(cp.getDestinationName(), cp.getProperties());
		}
	}
	
	@Override
	public Properties getDestinationProperties(String destinationName) throws DataProviderException {
		return propertiesMap.get(destinationName);
	}

	@Override
	public void setDestinationDataEventListener(DestinationDataEventListener l) {
		// do nothing, not needed
	}

	@Override
	public boolean supportsEvents() {
		return false;
	}

}

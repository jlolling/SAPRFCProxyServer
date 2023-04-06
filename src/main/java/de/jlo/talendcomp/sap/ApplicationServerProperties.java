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

import java.util.Properties;

public class ApplicationServerProperties implements ConnectionProperties {

	private String user = null;
	private String password = null;
	private String language = null;
	private String host = null;
	private String client = null;
	private String systemNumber = null;
	private String destinationName = null;
	private Properties properties = null;
	private String sapGui = null;
	
	@Override
	public void build() throws Exception {
		if (user == null) {
			throw new Exception("user not set!");
		}
		if (password == null) {
			throw new Exception("password not set!");
		}
		if (language == null) {
			throw new Exception("language not set!");
		}
		if (host == null) {
			throw new Exception("host not set!");
		}
		if (client == null) {
			throw new Exception("client not set!");
		}
		if (systemNumber == null) {
			throw new Exception("systemNumber not set!");
		}
		destinationName = user + language + host + client + systemNumber;
		properties = new Properties();
        properties.setProperty(JCO_USER, user);
        properties.setProperty(JCO_PASSWD, password);
        properties.setProperty(JCO_LANG, language);
        properties.setProperty(JCO_ASHOST, host);
        properties.setProperty(JCO_CLIENT, client);
        properties.setProperty(JCO_SYSNR, systemNumber);
        if (sapGui != null) {
        	properties.setProperty(JCO_USE_SAPGUI, sapGui);
        }
	}
	
	@Override
	public String getDestinationName() {
		if (destinationName == null) {
			throw new IllegalStateException("destinationName is not initialized. Call build() before!");
		}
		return destinationName;
	}

	@Override
    public Properties getProperties() {
		if (properties == null) {
			throw new IllegalStateException("properties are not initialized. Call build() before!");
		}
        return properties;
    }

	public String getUser() {
		return user;
	}

	public ApplicationServerProperties setUser(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public ApplicationServerProperties setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public ApplicationServerProperties setLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getHost() {
		return host;
	}

	public ApplicationServerProperties setHost(String host) {
		this.host = host;
		return this;
	}

	public String getClient() {
		return client;
	}

	public ApplicationServerProperties setClient(String client) {
		this.client = client;
		return this;
	}

	public String getSystemNumber() {
		return systemNumber;
	}

	public ApplicationServerProperties setSystemNumber(String systemNumber) {
		this.systemNumber = systemNumber;
		return this;
	}

	public String getSapGui() {
		return sapGui;
	}

	public void setSapGui(String sapGui) {
		this.sapGui = sapGui;
	}

}

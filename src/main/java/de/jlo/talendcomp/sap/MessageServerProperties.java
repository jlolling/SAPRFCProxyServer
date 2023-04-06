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

public class MessageServerProperties implements ConnectionProperties {
	
	private String user = null;
	private String password = null;
	private String language = null;
	private String host = null;
	private String client = null;
	private String r3Name = null;
	private String group = null;
	private String destinationName = null;
	private Properties properties = null;
	private String sapGui = null;
	
	@Override
	public void build() throws Exception  {
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
		if (r3Name == null) {
			throw new Exception("r3Name not set!");
		}
		if (group == null) {
			throw new Exception("group not set!");
		}
		destinationName = user + language + host + client + r3Name + group;
		properties = new Properties();
        properties.setProperty(JCO_USER, user);
        properties.setProperty(JCO_PASSWD, password);
        properties.setProperty(JCO_LANG, language);
        properties.setProperty(JCO_MSHOST, host);
        properties.setProperty(JCO_CLIENT, client);
        properties.setProperty(JCO_R3NAME, r3Name);
        properties.setProperty(JCO_GROUP, group);
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

	public MessageServerProperties setUser(String user) {
		this.user = user;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public MessageServerProperties setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public MessageServerProperties setLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getHost() {
		return host;
	}

	public MessageServerProperties setHost(String host) {
		this.host = host;
		return this;
	}

	public String getClient() {
		return client;
	}

	public MessageServerProperties setClient(String client) {
		this.client = client;
		return this;
	}

	public String getR3Name() {
		return r3Name;
	}

	public MessageServerProperties setR3Name(String r3Name) {
		this.r3Name = r3Name;
		return this;
	}

	public String getGroup() {
		return group;
	}

	public MessageServerProperties setGroup(String group) {
		this.group = group;
		return this;
	}

	public String getSapGui() {
		return sapGui;
	}

	public void setSapGui(String sapGui) {
		this.sapGui = sapGui;
	}

}

package de.jlo.talendcomp.sap;

import java.util.Properties;

/**
 * Copyright 2023 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public interface ConnectionProperties {

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_AUTH_TYPE = "jco.destination.auth_type";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_AUTH_TYPE_CONFIGURED_USER = "CONFIGURED_USER";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_AUTH_TYPE_CURRENT_USER = "CURRENT_USER";

	// Field descriptor #4 Ljava/lang/String; (deprecated)
	@java.lang.Deprecated
	public static final java.lang.String JCO_USER_ID = "jco.destination.user_id";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_CLIENT = "jco.client.client";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_USER = "jco.client.user";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_ALIAS_USER = "jco.client.alias_user";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_PASSWD = "jco.client.passwd";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_LANG = "jco.client.lang";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_CODEPAGE = "jco.client.codepage";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_PCS = "jco.client.pcs";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_ASHOST = "jco.client.ashost";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SYSNR = "jco.client.sysnr";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_MSHOST = "jco.client.mshost";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_MSSERV = "jco.client.msserv";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_R3NAME = "jco.client.r3name";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_GROUP = "jco.client.group";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SAPROUTER = "jco.client.saprouter";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_MYSAPSSO2 = "jco.client.mysapsso2";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_GETSSO2 = "jco.client.getsso2";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_X509CERT = "jco.client.x509cert";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_EXTID_DATA = "jco.client.extid_data";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_EXTID_TYPE = "jco.client.extid_type";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_LCHECK = "jco.client.lcheck";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_DELTA = "jco.client.delta";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_PARTNERNAME = "jco.client.snc_partnername";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_QOP = "jco.client.snc_qop";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_MYNAME = "jco.client.snc_myname";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_MODE = "jco.client.snc_mode";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_SSO = "jco.client.snc_sso";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_SNC_LIBRARY = "jco.client.snc_lib";

	// Field descriptor #4 Ljava/lang/String; (deprecated)
	@java.lang.Deprecated
	public static final java.lang.String JCO_DEST = "jco.client.dest";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_PEAK_LIMIT = "jco.destination.peak_limit";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_POOL_CAPACITY = "jco.destination.pool_capacity";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_EXPIRATION_TIME = "jco.destination.expiration_time";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_EXPIRATION_PERIOD = "jco.destination.expiration_check_period";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_MAX_GET_TIME = "jco.destination.max_get_client_time";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_REPOSITORY_DEST = "jco.destination.repository_destination";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_REPOSITORY_USER = "jco.destination.repository.user";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_REPOSITORY_PASSWD = "jco.destination.repository.passwd";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_REPOSITORY_SNC = "jco.destination.repository.snc_mode";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_CPIC_TRACE = "jco.client.cpic_trace";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_TRACE = "jco.client.trace";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_GWHOST = "jco.client.gwhost";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_GWSERV = "jco.client.gwserv";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_TPHOST = "jco.client.tphost";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_TPNAME = "jco.client.tpname";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_TYPE = "jco.client.type";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_USE_SAPGUI = "jco.client.use_sapgui";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_DENY_INITIAL_PASSWORD = "jco.client.deny_initial_password";

	// Field descriptor #4 Ljava/lang/String;
	public static final java.lang.String JCO_REPOSITORY_ROUNDTRIP_OPTIMIZATION = "jco.destination.repository_roundtrip_optimization";

	/**
	 * the SAP connection properties according to the type of server we want to
	 * connect
	 * 
	 * @return Properties
	 */
	public Properties getProperties();

	/**
	 * Dynamically build destination name made of the essential properties
	 * 
	 * @return
	 */
	public String getDestinationName();

	/**
	 * build connection properties and destination name
	 */
	public void build() throws Exception;

}

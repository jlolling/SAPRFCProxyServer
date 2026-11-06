package de.jlo.talendcomp.sap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.jlo.talendcomp.sap.ApplicationServerProperties;
import de.jlo.talendcomp.sap.MessageServerProperties;

public class TestDestinationName {

	private ApplicationServerProperties createAppServer(String password) throws Exception {
		ApplicationServerProperties p = new ApplicationServerProperties()
				.setHost("host")
				.setClient("003")
				.setUser("user")
				.setPassword(password)
				.setLanguage("DE")
				.setSystemNumber("03");
		p.build();
		return p;
	}

	private MessageServerProperties createMessageServer(String password) throws Exception {
		MessageServerProperties p = new MessageServerProperties()
				.setHost("host")
				.setClient("003")
				.setUser("user")
				.setPassword(password)
				.setLanguage("DE")
				.setR3Name("R3")
				.setGroup("GROUP");
		p.build();
		return p;
	}

	@Test
	public void testApplicationServerPasswordInName() throws Exception {
		assertEquals(createAppServer("secret").getDestinationName(), createAppServer("secret").getDestinationName());
		assertNotEquals(createAppServer("secret").getDestinationName(), createAppServer("wrong").getDestinationName());
		assertFalse(createAppServer("secret").getDestinationName().contains("secret"));
	}

	@Test
	public void testMessageServerPasswordInName() throws Exception {
		assertEquals(createMessageServer("secret").getDestinationName(), createMessageServer("secret").getDestinationName());
		assertNotEquals(createMessageServer("secret").getDestinationName(), createMessageServer("wrong").getDestinationName());
		assertFalse(createMessageServer("secret").getDestinationName().contains("secret"));
	}

}

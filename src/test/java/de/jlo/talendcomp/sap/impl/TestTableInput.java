package de.jlo.talendcomp.sap.impl;

import static org.junit.Assert.assertTrue;

import java.util.StringTokenizer;

import org.junit.Test;

public class TestTableInput {

	@Test
	public void testSplit() {
		String s = "1100003204\b"
				+ "000010\b"
				+ "ZA02\b"
				+ "20160111\b"
				+ "000000000010135055\b"
				+ "4110080040010190  \b"
				+ "         1.000 \b"
				+ "PAK\b"
				+ "       700.000 \b"
				+ "       700.000 \b"
				+ "G  \b"
				+ "          \b"
				+ "000000\b"
				+ "          \b"
				+ "000000\b"
				+ "       83.50 \b"
				+ "       38.41 \b"
				+ "        0.00 \b"
				+ "        0.00 \b"
				+ "       38.41 \b"
				+ "         38.41 \b"
				+ "CHF  \b"
				+ "1710\b"
				+ "50\b"
				+ "000000\b"
				+ "20210908\b"
				+ "         39.53 \b"
				+ "GL01  \b"
				+ "HOBOTEC SeHoBoShr St vz 4-25TX20        \b"
				+ "          \b"
				+ "\b";
//		StringTokenizer st = new StringTokenizer(s, "\b");
//		int index = 0;
//		while (st.hasMoreElements()) {
//			System.out.println(index++ + ": " + st.nextElement());
//		}
		if (s.endsWith("\b")) { // prevent ignoring the last field is empty
			s = s + " ";
		}
		String[] array = s.split("\b");
		int index = 0;
		for (String v : array) {
			System.out.println(index++ + ": " + v);
		}		
		assertTrue("Split does not work here", index == 32);
	}
	
}

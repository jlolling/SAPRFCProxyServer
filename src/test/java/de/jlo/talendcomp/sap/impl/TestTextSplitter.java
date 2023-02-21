package de.jlo.talendcomp.sap.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.jlo.talendcomp.sap.TextSplitter;

public class TestTextSplitter {
	
	@Test
	public void test1() {
		String input = "';';T1;T2; ;T3";
		List<String> result = TextSplitter.split(input, ';');
		for (String s : result) {
			System.out.println(">" + s);
		}
		assertTrue("Count parts incorrect: " + result.size(), result.size() == 4);
	}

	@Test
	public void test2() {
		String input = "';'";
		List<String> result = TextSplitter.split(input, ';');
		for (String s : result) {
			System.out.println(">" + s);
		}
		assertTrue("Count parts incorrect: " + result.size(), result.size() == 1);
	}

	@Test
	public void test3() {
		String input = "abcd  efg;";
		List<String> result = TextSplitter.split(input, ';');
		for (String s : result) {
			System.out.println(">" + s);
		}
		assertTrue("Count parts incorrect: " + result.size(), result.size() == 1);
	}

	@Test
	public void test4() {
		String input = "";
		List<String> result = TextSplitter.split(input, ';');
		for (String s : result) {
			System.out.println(">" + s);
		}
		assertTrue("Count parts incorrect: " + result.size(), result.size() == 0);
	}

	@Test
	public void test5() {
		String input = " ; ";
		List<String> result = TextSplitter.split(input, ';');
		for (String s : result) {
			System.out.println(">" + s);
		}
		assertTrue("Count parts incorrect: " + result.size(), result.size() == 0);
	}

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

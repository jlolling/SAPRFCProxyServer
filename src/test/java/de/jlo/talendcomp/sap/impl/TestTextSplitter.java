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
		String input = "abcd  efg";
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

}

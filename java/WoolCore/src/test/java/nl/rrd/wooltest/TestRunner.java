/*
 * Copyright 2019 Roessingh Research and Development.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nl.rrd.wooltest;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


public class TestRunner {
	public static void main(String[] args) {
		System.out.println("Running tests..\n");
		Result result = JUnitCore.runClasses(TestSuite.class);
		
		System.out.println("--- OUTCOME ---");
		System.out.println(result.getRunCount()+" tests were run in "+result.getRunTime()+"ms.");
		
		if(result.wasSuccessful()) {
			System.out.println("All tests PASSED.\n");
		}
		else {
			System.out.println("Some tests FAILED.\n");
		}
			
		System.out.println("Failed tests:");
		if(result.getFailureCount() > 0) {
			for (Failure failure : result.getFailures()) {
			System.out.println("- "+failure.toString());
			}
			System.out.println();
		}
		else {
			System.out.println("- No tests have failed.\n");
		}
		
		System.out.println("Ignored tests:");
		if(result.getIgnoreCount() > 0) {
			System.out.println("- "+result.getIgnoreCount()+" tests have been ignored.");
		}
		else {
			System.out.println("- No tests were ignored.");
		}
	}
}

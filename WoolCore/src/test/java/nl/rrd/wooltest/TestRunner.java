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

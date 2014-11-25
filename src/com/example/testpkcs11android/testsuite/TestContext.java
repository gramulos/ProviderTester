package com.example.testpkcs11android.testsuite;

import java.util.ArrayList;

import android.app.Activity;

public class TestContext {	
	
	private TestStateChangeListener testStateChangeListener;
	private ArrayList<TestPKCS> tests;	
	private Activity activity;
	private Thread executionThread;
	private boolean isExecuting;
	
	public TestContext(Activity activity) {		
		tests = new ArrayList<TestPKCS>();
		testStateChangeListener = null;
		this.activity = activity;
		isExecuting = false;
		executionThread = null;
	}
	
	 public void addListener(TestStateChangeListener testStateChangeListener) {
		 this.testStateChangeListener =  testStateChangeListener;
	    }
	 
	public void addTest(TestPKCS test){
		tests.add(test);
	}	
	
	public int getSize(){return tests.size();}
	
	public boolean IsExecuting(){return isExecuting;}
	
	public void executeAllTests() {
		executionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				isExecuting = true;
				for (TestPKCS test : tests) {
					startRunTestProcess(test);					
					if (executionThread.isInterrupted()) {
						notifyListener(test);
						break;
					}
				}
				isExecuting = false;
			}
		});
		executionThread.start();
	}
	
	public void interruptExecution(){
		executionThread.interrupt();
		}
	
	public void runTest(final int index){
		if (index >= 0 && index < getSize()) {
			new Thread(new Runnable() {				
				@Override
				public void run() {
					TestPKCS test = tests.get(index);
					startRunTestProcess(test);
				}
			}).start();
		}		
	}
		
	public void runTest(String name){
		for(final TestPKCS test : tests){
			if(test.getName().equals(name)){
				new Thread(new Runnable() {					
					@Override
					public void run() {						
						startRunTestProcess(test);
					}
				}).start();									
			}
		}	
	}
	
	public TestPKCS getTestByName(String name) {
		for (final TestPKCS test : tests) {
			if (test.getName().equals(name)) {
				return test;
			}
		}
		return null;
	}
	
	private void notifyListener(final TestPKCS test){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (testStateChangeListener != null)
					testStateChangeListener.onTestStateChange(test);
			}
		});
	}
	
	private void startRunTestProcess(TestPKCS test){
		isExecuting = true;
		test.setTestStatus(TestStatus.IN_PROCESS);//set status
		notifyListener(test);
		
		test.runTest();						//run test
		
		isExecuting = false;
		notifyListener(test);
	}

	
	public interface TestStateChangeListener{
		public void onTestStateChange(TestPKCS test);		
	}
}


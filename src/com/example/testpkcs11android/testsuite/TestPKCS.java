package com.example.testpkcs11android.testsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.testpkcs11android.MyApp;

import android.os.Environment;
import android.util.Log;

public abstract class TestPKCS {
	
	abstract public void runTest();
	
	private static final String TAG = TestPKCS.class.getName();
	public static final String ALGORITHM = "PKCS11"; 
	
	protected static String saved_user_pin = null;
	protected static String saved_so_pin = null;
	protected static boolean rememer_user_input = false;
	
	protected String name;
	protected String info;
	protected String logPath;	
	protected TestStatus testStatus;
	protected String log;
	
	public TestPKCS(String name){
		testStatus = TestStatus.NOT_PASSED;
		this.name = new String(name);
		info="";
		logPath = Environment.getExternalStorageDirectory() + File.separator
				+ /*"Logs"+File.separator+*/"Log_" + this.name + ".txt";	
		log = "";
	}
	
	public TestStatus getTestStatus(){return testStatus;}	
	public void setTestStatus(TestStatus testStatus){this.testStatus = testStatus;}
	
	public String getName(){return name;}
	
	public String getInfo(){return info;}
	public void setInfo(String info){this.info = new String(info);}
	
	public String getLog(){return log;}
	
	protected void readLog() {
		String ret = "";
		try {
			InputStream inputStream = new FileInputStream(logPath);

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString + System.getProperty("line.separator"));
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e(TAG, "Can not read file: " + e.toString());
		}

		log = new String(ret);		
	}
}

package com.example.testpkcs11android.testsuite;

import java.util.ArrayList;

import org.opensc.android.LibraryNativeLogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.testpkcs11android.R;

public class TestAllTest extends TestPKCS {
	
	private ArrayList<TestPKCS> testList;
	private Activity activityContext;
	private Object syncObj = new Object();
	
	public TestAllTest(String name,Activity activityContext, TestPKCS... tests) {
		super(name);
		testList = new ArrayList<TestPKCS>();
		
		this.activityContext = activityContext;	
		for(TestPKCS test : tests){
			testList.add(test);
		}
	}

	@Override
	public void runTest() {		
		try {						
		rememer_user_input = true;		

		activityContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LayoutInflater inflater = activityContext.getLayoutInflater();
				final View dialogView = inflater.inflate(
						R.layout.user_so_pin_dialog, null);
				AlertDialog dialog = new AlertDialog.Builder(activityContext)
						.setTitle(
								Html.fromHtml("<font color=\"#42aaff\">Enter User and SO PIN</font>"))
						.setView(dialogView)
						.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								synchronized (syncObj) {
									EditText userEdit = (EditText) dialogView
											.findViewById(R.id.edt_user_pin);
									EditText soEdit = (EditText) dialogView
											.findViewById(R.id.edt_so_pin);
									saved_user_pin = userEdit.getText().toString();									
									saved_so_pin = soEdit.getText().toString();
									syncObj.notify();
								}
							}
						}).setNegativeButton("Cancel", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								synchronized (syncObj) {
									syncObj.notify();
								}
							}
						}).show();
					dialog.show();
				}
			});

			synchronized (syncObj) {
				syncObj.wait();
				if (saved_so_pin == null || saved_user_pin == null) {
					testStatus = TestStatus.NOT_PASSED;
					return;
				}

				for (TestPKCS test : testList) {					
					test.runTest();		
					LibraryNativeLogger.writeToFile("------- Test : " + test.name + " finished logging");
					log+=test.getLog();
				}
				
				testStatus = TestStatus.PASSED;				
			}
		} catch (Throwable e) {
			testStatus = TestStatus.FAILED;
			e.printStackTrace();
		}
		finally 
		{
			rememer_user_input = false;
			saved_so_pin = null;
			saved_user_pin = null;
		}
	}

}

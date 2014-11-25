package com.example.testpkcs11android.testsuite;

import java.util.List;

import org.opensc.android.LibraryNativeLogger;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.wrap.PKCS11Session;
import org.opensc.pkcs11.wrap.PKCS11Slot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.testpkcs11android.MyApp;
import com.example.testpkcs11android.R;

public class PINTest extends TestPKCS {
	private String userPIN;
	private String SOPIN;
	private Activity activityContext;
	private Object syncObj = new Object();
	private PKCS11Provider provider = null;
	private PKCS11Session session = null;
	private List<PKCS11Slot> slots = null;

	public PINTest(Activity activityContext, String name) {
		super(name);
		this.activityContext = activityContext;
		info = MyApp.getContext().getString(R.string.pin_test_info);
	}
	
	

	@Override
	public void runTest() {
		LibraryNativeLogger.setLoggerFileName(logPath);

		try
		{
		if (!rememer_user_input) {
			activityContext.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					LayoutInflater inflater = activityContext
							.getLayoutInflater();
					final View dialogView = inflater.inflate(
							R.layout.user_so_pin_dialog, null);
					AlertDialog dialog = new AlertDialog.Builder(
							activityContext)
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
										userPIN = userEdit.getText().toString();
										SOPIN = soEdit.getText().toString();
										syncObj.notify();
									}
								}
							})
							.setNegativeButton("Cancel", new OnClickListener() {

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
			} else {				
				if (saved_so_pin != null && saved_user_pin != null) {							
						userPIN = new String(saved_user_pin);
						SOPIN = new String(saved_so_pin);						
				} else {
					throw new Exception("SO or PIN was not provided");
				}
			}
		} catch (Throwable e) {
			testStatus = TestStatus.NOT_PASSED;
			e.printStackTrace();
		}
		
		synchronized (syncObj) {
			try {
				if(!rememer_user_input){
					syncObj.wait();		
				}
				if (userPIN == null || SOPIN == null) {
					testStatus = TestStatus.NOT_PASSED;
					return;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				testStatus = TestStatus.NOT_PASSED;
				return;
			}
		}

		try {
			provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
			slots = PKCS11Slot.enumerateSlots(provider);
		} catch (Exception e) {
			uninitialize();
			e.printStackTrace();
			testStatus = TestStatus.FAILED;
			readLog();
			return;
		}

		try {
			if (userPIN != null && !userPIN.equals("")) {
				session = PKCS11Session.open(slots.get(0),
						PKCS11Session.OPEN_MODE_READ_ONLY);
				session.loginUser(userPIN.toCharArray());
				session.logout();
				session.destroy();
			}
		} catch (Exception e) {
			//do nothing
		}

		try {
			if (SOPIN != null && !SOPIN.equals("")) {
				session = PKCS11Session.open(slots.get(0),
						PKCS11Session.OPEN_MODE_READ_WRITE);
				session.loginSO(SOPIN.toCharArray());
				session.logout();
			}
		} catch (Exception e) {
			//do nothing
		}
		uninitialize();

		readLog();

		testStatus = TestStatus.PASSED;
	}

	private void uninitialize() {
		try {
			slots.get(0).destroy();
			provider.cleanup();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}

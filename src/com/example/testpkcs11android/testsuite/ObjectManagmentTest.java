package com.example.testpkcs11android.testsuite;

import java.security.cert.Certificate;
import java.util.Enumeration;

import org.opensc.android.LibraryNativeLogger;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.spi.PKCS11KeyStoreSpi;
import org.opensc.pkcs11.wrap.PKCS11Certificate;

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

public class ObjectManagmentTest extends TestPKCS {

	private Activity activity;
	private Object syncObj;
	private String userPIN;

	public ObjectManagmentTest(String name,Activity activity) {
		super(name);
		this.activity = activity;
		info = activity.getString(R.string.obj_mngmnt_test_info);
		userPIN = null;
		syncObj = new Object();
	}

	@Override
	public void runTest() {
		PKCS11Provider provider = null;
		try {
			LibraryNativeLogger.setLoggerFileName(logPath);
			provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
			// Dialog for user PIN
			if (!rememer_user_input) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LayoutInflater inflater = activity.getLayoutInflater();
						final View dialogView = inflater.inflate(
								R.layout.user_pin_dialog, null);
						AlertDialog dialog = new AlertDialog.Builder(activity)
								.setTitle(
										Html.fromHtml("<font color=\"#42aaff\">Enter User PIN</font>"))
								.setView(dialogView)
								.setPositiveButton("OK", new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										synchronized (syncObj) {
											EditText userEdit = (EditText) dialogView
													.findViewById(R.id.edt_user_pin);
											userPIN = userEdit.getText()
													.toString();
											syncObj.notify();
										}
									}
								})
								.setNegativeButton("Cancel",
										new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												synchronized (syncObj) {
													syncObj.notify();
												}
											}
										}).show();
						dialog.show();
					}
				});
				// end of Dialog
			}else{
				if (saved_user_pin != null) {
					userPIN = new String(saved_user_pin);					
			} else {
				throw new Exception("PIN was not provided");
			}
			}
			
			synchronized (syncObj) {
				try {
					if(!rememer_user_input){
						syncObj.wait();
					}
					if(userPIN == null){//if cancel was pressed in dialogBox
						testStatus = TestStatus.NOT_PASSED;						
						provider.cleanup();
						return;
					}
				} catch (Throwable e) {
					e.printStackTrace();
					provider.cleanup();
					return;
				}
			}
			
			//run test
			PKCS11KeyStoreSpi keyStore = new PKCS11KeyStoreSpi(provider,ALGORITHM);
			keyStore.engineLoad(null,userPIN.toCharArray());
			
			Enumeration<String> cardObjects = (Enumeration<String>) keyStore.engineAliases();//TODO digital signature key			
			int engineSize = keyStore.engineSize();
			
			while(cardObjects.hasMoreElements()){
				
				String alias = cardObjects.nextElement();
				
				keyStore.engineGetCreationDate(alias);			
				
//				
//				Certificate certificate = keyStore.engineGetCertificate(alias);
//				keyStore.engineGetCertificateAlias(certificate);
//				
//				Certificate[] certificateChain = keyStore.engineGetCertificateChain(alias);
//				
//				keyStore.engineIsCertificateEntry(alias);
				boolean isKey = keyStore.engineIsKeyEntry(alias);
				keyStore.engineGetKey(alias, userPIN.toCharArray());
				
				keyStore.engineDeleteEntry(cardObjects.nextElement());				
			}
						
			testStatus= TestStatus.PASSED;
						
 		} catch (Throwable e) {
			testStatus = TestStatus.FAILED;
			e.printStackTrace();
		}
		finally{
			provider.cleanup();
			readLog();
		}
		
	}
}

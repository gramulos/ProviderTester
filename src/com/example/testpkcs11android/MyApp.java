package com.example.testpkcs11android;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
	
	static {
    	System.loadLibrary("pcsclite");
    	System.loadLibrary("crypto");
    	System.loadLibrary("wrapper_p11");
	}
	
	 private static Context context;

	    @Override
	    public void onCreate() {
	        super.onCreate();
	        context = this;
	    }

	    public static Context getContext(){
	        return context;
	    }
}

package com.example.testpkcs11android.testsuite;

import org.opensc.android.LibraryNativeLogger;
import org.opensc.pkcs11.PKCS11Provider;
import com.example.testpkcs11android.MyApp;
import com.example.testpkcs11android.R;

public class BasicTest extends TestPKCS {

	public BasicTest(String name) {
		super(name);
		info = MyApp.getContext().getString(R.string.basic_test_info);
	}

	@Override
	public void runTest() {
		try {
			LibraryNativeLogger.setLoggerFileName(logPath);
			// the test
			PKCS11Provider provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
			provider.cleanup();
			//
			readLog();
		} catch (Throwable e) {
			e.printStackTrace();
			testStatus = TestStatus.FAILED;
		}
		testStatus = TestStatus.PASSED;
	}
}

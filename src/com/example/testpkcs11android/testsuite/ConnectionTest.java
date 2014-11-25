package com.example.testpkcs11android.testsuite;

import java.util.List;

import org.opensc.android.LibraryNativeLogger;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.wrap.PKCS11Session;
import org.opensc.pkcs11.wrap.PKCS11Slot;

import com.example.testpkcs11android.MyApp;
import com.example.testpkcs11android.R;

public class ConnectionTest extends TestPKCS {

	public ConnectionTest(String name) {
		super(name);
		info = MyApp.getContext().getString(R.string.connection_test_info);
	}

	@Override
	public void runTest() {
		try {
			LibraryNativeLogger.setLoggerFileName(logPath);
			PKCS11Provider provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
			List<PKCS11Slot> slots = PKCS11Slot.enumerateSlots(provider);
			if (slots.size() > 0) {
				PKCS11Session session = PKCS11Session.open(slots.get(0),
						PKCS11Session.OPEN_MODE_READ_ONLY);
				session.destroy();
			}

			for (PKCS11Slot slot : slots) {
				slot.destroy();
			}

			provider.cleanup();
			readLog();
		} catch (Exception e) {
			e.printStackTrace();
			testStatus = TestStatus.FAILED;
		}
		testStatus = TestStatus.PASSED;
	}
}

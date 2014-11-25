package com.example.testpkcs11android.testsuite;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;

import org.opensc.android.LibraryNativeLogger;
import org.opensc.pkcs11.PKCS11LoadStoreParameter;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.spec.PKCS11RSAKeyPairGenParameterSpec;
import org.opensc.pkcs11.spi.PKCS11CipherSpi;
import org.opensc.pkcs11.spi.PKCS11KeyPairGeneratorSpi;
import org.opensc.pkcs11.spi.PKCS11KeyStoreSpi;
import org.opensc.pkcs11.spi.PKCS11SignatureSpi;
import org.opensc.pkcs11.wrap.PKCS11DSAKeyPairGenerator;
import org.opensc.pkcs11.wrap.PKCS11Mechanism;
import org.opensc.pkcs11.wrap.PKCS11PrivateKey;
import org.opensc.pkcs11.wrap.PKCS11PublicKey;
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
import android.widget.Toast;

import com.example.testpkcs11android.MyApp;
import com.example.testpkcs11android.R;

public class CryptographyTest extends TestPKCS {

	private PKCS11Provider provider;
	private List<PKCS11Slot> slots;
	private PKCS11Session session;
	private KeyPair keyPair;
	private String userPIN;
	private String SOPIN;
	private Object syncObj;
	private Activity activityContext;
	
	private boolean signTest = true;
	
	public CryptographyTest(String name,Activity activityContext) {
		super(name);
		this.activityContext = activityContext;
		info = activityContext.getString(R.string.crypto_test_info);
		userPIN = null;
		SOPIN = null;
		syncObj = new Object();
	}

	@Override
	public void runTest() {

		if(signTest){
			signTest();
			return;
		}
		
		LibraryNativeLogger.setLoggerFileName(logPath);			
		try {
			//DialogBox
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
				if(e.getMessage().contains("SO or PIN was not provided")){
					throw e;
				}
				e.printStackTrace();
			}
			synchronized (syncObj) {
				if(!rememer_user_input){
					syncObj.wait();
				}
				if(userPIN == null || userPIN.equals("") || SOPIN == null || SOPIN.equals("")){
					testStatus = TestStatus.NOT_PASSED;
					return;//cancel was pressed in DialogBox
				}
				// initializing
				provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
				slots = PKCS11Slot.enumerateSlots(provider);

				// keyPair generation
				try {
					//*  1)Generate keyPair
					final int KEY_SIZE = 512;
					PKCS11RSAKeyPairGenParameterSpec genParameterSpec = new PKCS11RSAKeyPairGenParameterSpec(
							KEY_SIZE, new BigInteger("10"));
					genParameterSpec.setEncrypt(true);
					genParameterSpec.setDecrypt(true);				
					
					PKCS11LoadStoreParameter loadStoreParameter = new PKCS11LoadStoreParameter();
					loadStoreParameter.setWriteEnabled(true);
					loadStoreParameter.setProtectionPIN(userPIN.toCharArray());
					loadStoreParameter.setSlotId(slots.get(0).getId());
					
					genParameterSpec.setLoadStoreParameter(loadStoreParameter);
					
					PKCS11KeyPairGeneratorSpi keyPairGenerator = new PKCS11KeyPairGeneratorSpi(
							provider, "RSA");					
					keyPairGenerator.initialize(genParameterSpec,
							new SecureRandom(new byte[] { 1, 2, 3, 4, 5, 6 }));
					
					keyPair = keyPairGenerator.generateKeyPair();

				} catch (Throwable e) {
					e.printStackTrace();
				}
				// * 2)encrypt text
				String testDataToCrypt = "hello";
				byte[] encryptedData = null;
				PKCS11CipherSpiExtended cipherSpiExtended = null;
				try {
					
					cipherSpiExtended = new PKCS11CipherSpiExtended(provider, "RSA");
					encryptedData = cipherSpiExtended.encrypt(
							testDataToCrypt.getBytes(), 0,
							testDataToCrypt.getBytes().length,
							keyPair.getPublic());					
				} catch (Throwable e) {
					e.printStackTrace();
				}
				// * 3)decrypt text
				try {
					byte[] decryptedData = cipherSpiExtended.decrypt(
							encryptedData, 0, encryptedData.length,
							keyPair.getPrivate());
					if(!testDataToCrypt.equals(new String(decryptedData))){
						LibraryNativeLogger.writeToFile("Encrypt-Decrypt Test OK");						
					}else{
						LibraryNativeLogger.writeToFile("Encrypt-Decrypt Test FAILED");
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				// * 4)sign
				PKCS11SignatureSpiExtended signatureSpiExtended = new PKCS11SignatureSpiExtended(
						provider, "RSA");
				byte[] signature = null;
				try {
					signature = signatureSpiExtended.sign(testDataToCrypt.getBytes(), 0,
							testDataToCrypt.getBytes().length,
							keyPair.getPrivate());
				} catch (Throwable e) {
				}
				// * 5)verify
				try {
					if(signatureSpiExtended.verify(testDataToCrypt.getBytes(), 0,
							testDataToCrypt.getBytes().length, signature,
							keyPair.getPublic())){
						LibraryNativeLogger.writeToFile("Sign-Verify Test OK");
					}else{
						LibraryNativeLogger.writeToFile("Sign-Verify Test FAILED");
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				testStatus = TestStatus.PASSED;
			}
		} catch (Throwable e) {
			testStatus = TestStatus.FAILED;
			e.printStackTrace();
		}
		finally{
			uninitialize();
			readLog();
		}		
	}
	
	private void uninitialize() {
		try {
		//	session.logout();
			slots.get(0).destroy();
			provider.cleanup();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void signTest()
	{
		try {
			
			if(provider != null){
				provider.cleanup();
			}			
			
			provider = new PKCS11Provider(MyApp.getContext(), R.raw.libcmp11droid);
			List<PKCS11Slot> slots = PKCS11Slot.enumerateSlots(provider);
			PKCS11Session session = null;
			
			if (slots.size() > 0) {
			session = PKCS11Session.open(slots.get(0),
					PKCS11Session.OPEN_MODE_READ_ONLY);
			}
			
			PKCS11KeyStoreSpi keyStore = new PKCS11KeyStoreSpi(provider,ALGORITHM);
			keyStore.engineLoad(null,"111111".toCharArray());				
			
			PKCS11PrivateKey privateKey = PKCS11PrivateKey.getPrivateKeys(
					session).get(0);
		   				
			Signature signature = Signature.getInstance("NONEwithRSA", provider);
			signature.initSign(privateKey);
			signature.update(new byte[]{1,2,3,4,5,6,7,8,9,0}, 0, 10);
			byte[] realSig = signature.sign();
			
			session.destroy();
			for (PKCS11Slot slot : slots) {
				slot.destroy();
			}
			provider.cleanup();
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private class PKCS11CipherSpiExtended extends PKCS11CipherSpi {

		private SecureRandom secureRandom;

		public PKCS11CipherSpiExtended(PKCS11Provider provider, String algorithm) {
			super(provider, algorithm);
			secureRandom = new SecureRandom(
					new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
		}

		public byte[] encrypt(byte[] data,int offset,int len ,Key key) {
			try {
				engineInit(Cipher.ENCRYPT_MODE, key, secureRandom);
				return engineUpdate(data, offset, len);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}

			return null;
		}

		public byte[] decrypt(byte[] encryptedData,int offset,int len, Key key) {
			try {
				engineInit(Cipher.DECRYPT_MODE, key, secureRandom);
				return engineUpdate(encryptedData, offset, len);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}

	}
	
	private class PKCS11SignatureSpiExtended extends PKCS11SignatureSpi{

		public PKCS11SignatureSpiExtended(PKCS11Provider provider,
				String algorithm) {
			super(provider, algorithm);			
		}
		
		public byte[] sign(byte[] data,int offset,int len,PrivateKey privateKey) {
			try {
				engineInitSign(privateKey);
				engineUpdate(data, offset, len);
				return engineSign();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			return null;
		}

		public boolean verify(byte[] data,int offset,int len,byte[] signature,PublicKey publicKey) {
			try {
				engineInitVerify(publicKey);
				engineUpdate(data, offset, len);
				return engineVerify(signature);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			return false;
		}
		
	}

}

package com.example.testpkcs11android.ui;

import org.opensc.android.LibraryNativeLogger;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testpkcs11android.R;
import com.example.testpkcs11android.testsuite.BasicTest;
import com.example.testpkcs11android.testsuite.ConnectionTest;
import com.example.testpkcs11android.testsuite.TestContext;
import com.example.testpkcs11android.testsuite.TestContext.TestStateChangeListener;
import com.example.testpkcs11android.testsuite.TestPKCS;

public class ProviderTester extends Activity {

	public static final String EXTRA_KEY_TEST_TYPE = "testName";
	public static final String EXTRA_KEY_TEST_LOG = "testLog";
	public static final String EXTRA_KEY_TEST_INFO = "testInfo";

	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_test_launch);
		
		initializeActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_test_launch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}

	public void onClick(View v) {
		startDetailsActivity(v.getId());
	}

	@Override
	public void onBackPressed() {
		TextView msg = new TextView(this);
		msg.setText(getString(R.string.want_exit));
		msg.setGravity(Gravity.CENTER_HORIZONTAL);
		msg.setTextSize(msg.getTextSize() * 1.1F);

		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
				ProviderTester.this);
		myAlertDialog.setTitle(getString(R.string.pkcs_eleven));
		myAlertDialog.setView(msg);
		myAlertDialog.setPositiveButton(getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				});
		myAlertDialog.setNegativeButton(getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
		myAlertDialog.show();
	}

	@Override
	public void finish() {
		LibraryNativeLogger.unitinialize();
		super.finish();
	}

	private void initializeActionBar() {
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.tests_activity_title);
	}

	private void startDetailsActivity(int test) {
		Intent intent = new Intent(ProviderTester.this,
				DetailsActivity.class);
		intent.putExtra(EXTRA_KEY_TEST_TYPE, test);
		startActivity(intent);
	}
}

package com.example.testpkcs11android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.testpkcs11android.R;
import com.example.testpkcs11android.testsuite.BasicTest;
import com.example.testpkcs11android.testsuite.ConnectionTest;
import com.example.testpkcs11android.testsuite.CryptographyTest;
import com.example.testpkcs11android.testsuite.ObjectManagmentTest;
import com.example.testpkcs11android.testsuite.PINTest;
import com.example.testpkcs11android.testsuite.TestAllTest;
import com.example.testpkcs11android.testsuite.TestContext;
import com.example.testpkcs11android.testsuite.TestStatus;
import com.example.testpkcs11android.testsuite.TestContext.TestStateChangeListener;
import com.example.testpkcs11android.testsuite.TestPKCS;

public class DetailsActivity extends Activity implements
		TestStateChangeListener {

	private ActionBar actionBar;
	private TextView textView;
	private TextView textStartTest;
	private TextView functionsList;
	private ListView listView;
	private boolean logsTrigger;

	private String testLog;
	private String testInfo = "testInfo";
	private int testType;
	private View separator;
	private TestContext context;
	private TestPKCS test;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_details);
		setProgressBarIndeterminateVisibility(Boolean.FALSE);

		context = new TestContext(this);
		context.addListener(this);
		logsTrigger = false;
		test = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// test name
			testType = extras.getInt(ProviderTester.EXTRA_KEY_TEST_TYPE);
			switch (testType) {
			case R.id.btn_basic_test:
				test = new BasicTest(getString(R.string.basic_test));
				break;
			case R.id.btn_connection_test:
				test = new ConnectionTest(getString(R.string.connection_test));
				break;
			case R.id.btn_crypto_test:
				test = new CryptographyTest(getString(R.string.crypto_test),this);
				break;
			case R.id.btn_pin_test:
				test = new PINTest(this, getString(R.string.pin_test));
				break;
			case R.id.btn_object_management_test:
				test = new ObjectManagmentTest(getString(R.string.object_management_test), this);
				break;
			case R.id.btn_test_all:
				test = new TestAllTest(
						getString(R.string.test_all_tests),
						this,
						new BasicTest(getString(R.string.basic_test)),
						new ConnectionTest(getString(R.string.connection_test)),
						new PINTest(this, getString(R.string.pin_test)),
						new ObjectManagmentTest(
								getString(R.string.object_management_test),
								this),
						new CryptographyTest(
								getString(R.string.crypto_test), this));
				break;
			}
			if(test != null)
			{
				testInfo = test.getInfo();
				context.addTest(test);

				actionBar = getActionBar();
				actionBar.setDisplayHomeAsUpEnabled(true);

				actionBar.setTitle(test.getName() + " "
						+ getString(R.string.details_result));

				textView = (TextView) findViewById(R.id.textViewDetails);
				textView.setMovementMethod(new ScrollingMovementMethod());
				textView.setTransformationMethod(new SingleLineTransformationMethod());
				textView.setText(Html.fromHtml(testInfo),
						TextView.BufferType.SPANNABLE);

				textStartTest = (TextView) findViewById(R.id.txt_press_run);
				textStartTest.setText(Html
						.fromHtml(getString(R.string.press_run)));

				functionsList = (TextView) findViewById(R.id.functions_list);
				functionsList.setText(getString(R.string.functions_list));

				separator = findViewById(R.id.separator);
				listView = (ListView) findViewById(R.id.lst_passed_functions);

				disableFunctionsList();
			}
		}
	}

	private void disableFunctionsList() {
		listView.setVisibility(View.GONE);
		separator.setVisibility(View.GONE);
		functionsList.setVisibility(View.GONE);
	}

	private void enableFunctionsList() {
		listView.setVisibility(View.VISIBLE);
		separator.setVisibility(View.VISIBLE);
		functionsList.setVisibility(View.VISIBLE);
	}

	private void initFunctionsList() {					//Function for parsing logs.
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		String[] from = { "func_name", "func_status", };
		// Ids of views in listview_layout
		int[] to = { R.id.func_name, R.id.func_status };
		
		String[] lines = testLog.split("\\r?\\n");
		HashMap<String, String> listItem = null;
		
		for(int i = 0; i< lines.length; i++){
			if(lines[i].contains("BEGIN")){
				String functionName = lines[i].substring(0,
						lines[i].indexOf("BEGIN") - 1);
				boolean isEnded = false;
				
				for(int j = i+1; j < lines.length; j++){
					if(lines[j].contains("BEGIN")){
						String iterFuncName = lines[j].substring(0,
								lines[j].indexOf("BEGIN") - 1);
						if(iterFuncName.equals(functionName)){
							break;
						}
					}
					
					if(lines[j].contains("END")){
						String iterFuncName = lines[j].substring(0,
								lines[j].indexOf("END") - 1);
						if(iterFuncName.equals(functionName)){
							isEnded = true;
							break;
						}
					}
				}
				listItem = new HashMap<String, String>();
				listItem.put(from[0], functionName);
				if(isEnded){
					listItem.put(from[1], Integer
							.toString(R.drawable.btn_check_buttonless_off));
				}
				else{
					listItem.put(from[1], Integer
							.toString(R.drawable.not_passed));
				}
				list.add(listItem);
			}
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.functions_list_row, from, to);
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (!logsTrigger) {
			if (testLog == null) {
				getMenuInflater().inflate(R.menu.activity_details, menu);
			} else {
				getMenuInflater().inflate(R.menu.activity_details_w_logs, menu);
			}

		} else {
			getMenuInflater().inflate(R.menu.activity_details_log, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.logs_trigger:
			logsTrigger = !logsTrigger;
			if (logsTrigger) {
				disableFunctionsList();
				textView.setText(testLog);
				textView.setScrollY(0);
				invalidateOptionsMenu();
			} else {
				enableFunctionsList();
				textView.setText(Html.fromHtml(testInfo));
				textView.setScrollY(0);
				invalidateOptionsMenu();
			}
			break;
		case R.id.run_test:
		if(!context.IsExecuting())
		{
			refreshActivity();
			context.executeAllTests();
		}
		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}

	private void refreshActivity() {
		if (listView != null) {
			listView.removeAllViewsInLayout();
			listView.invalidate();
		}
		testLog = null;
		invalidateOptionsMenu();
	}

	@Override
	public void onTestStateChange(TestPKCS test) {
		switch (test.getTestStatus()) {
		case IN_PROCESS:
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
			break;
		case FAILED:
		case PASSED:
			setProgressBarIndeterminateVisibility(Boolean.FALSE);
			enableFunctionsList();
			testInfo = test.getInfo();
			testLog = test.getLog();
			textStartTest.setText(Html.fromHtml(getString(R.string.press_run_again)));
			initFunctionsList();
			invalidateOptionsMenu();
			
			testInfo = (test.getTestStatus() == TestStatus.FAILED) ? test
					.getName() + " is Failed" : test.getName() + " is Passed";			
			textView.setText(Html.fromHtml("<b>"+testInfo+"</b>"));
			
			break;
		case NOT_PASSED:
			recreate();
			break;
		default:
			break;
		}
	}

}

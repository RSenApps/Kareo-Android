package com.RSen.Kareo;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;

public class scheduleViewActivity extends SherlockActivity {
	private static final int MY_DATE_DIALOG_ID = 3;
	AsyncAdapter adapter;
	private int position;
	private ViewFlow viewFlow;
	//UpdateChecker updateChecker;
	Boolean showingColorKey = false;;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_view);
		final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		if (prefs.getString("password", "") == "") {
			Intent i = new Intent(scheduleViewActivity.this,
					LogInActivity.class);
			this.startActivity(i);
			this.finish();
		} else {
			
			setupViewFlow(-1); //-1 = today
			CacheManager.initialize(this);
			configureActionBar(new LocalDate());

		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		//Appointment.demoMode = prefs.getBoolean("demoMode", false);
		adapter = new AsyncAdapter(this);
		//viewFlow.setAdapter(adapter, position);
		// updateChecker = new UpdateChecker(this);
		
		
		
		DateTime now = DateTime.now();
		DateTime lastUsed = new DateTime(prefs.getLong("lastUsed", now.getMillis()));
		if (lastUsed.plusHours(2).isBefore(now))
		{
			Toast.makeText(this, "You have been logged out because of inactivity", Toast.LENGTH_SHORT).show();
			prefs.edit().putString("password", "").commit();
			Intent i = new Intent(scheduleViewActivity.this,
					LogInActivity.class);
			this.startActivity(i);
			this.finish();
		
		}
		
		else if (prefs.getBoolean("showColorHelp", true) && !showingColorKey)
		{
			showColorKeyDialog();
			
		}
	}
	private void showColorKeyDialog()
	{
		final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		AlertDialog.Builder builder= new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogView = inflater.inflate(R.layout.colors, null);  
		CheckBox dontshow = (CheckBox) dialogView.findViewById(R.id.dontshow);
		dontshow.setChecked(!prefs.getBoolean("showColorHelp", false));
		builder.setInverseBackgroundForced(true);
		builder.setView(dialogView);
		builder.setTitle("Color Key");
		showingColorKey = true;
		builder.show().setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				CheckBox dontshow = (CheckBox) dialogView.findViewById(R.id.dontshow);
				
					prefs.edit().putBoolean("showColorHelp", !dontshow.isChecked()).commit();
				
				showingColorKey = false;
			}
		});
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//updateChecker.mHandler = null;
		DateTime lastUsed = DateTime.now();
		
		SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		prefs.edit().putLong("lastUsed", lastUsed.getMillis()).commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		
		inflater.inflate(R.menu.action_bar, menu);
		SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) //no action bar
		{
			if (prefs.getBoolean("hideWeekends", true))
			{
				menu.findItem(R.id.hideWeekends).setTitle("Show Weekends");
			}
			else
			{
				menu.findItem(R.id.hideWeekends).setTitle("Hide Weekends");
			}

		}

			menu.findItem(R.id.hideWeekends).setChecked(prefs.getBoolean("hideWeekends", true));
		
			//menu.findItem(R.id.demoMode).setChecked(prefs.getBoolean("demoMode", false));
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		SharedPreferences prefs;
		switch (item.getItemId()) {
		case R.id.today:
			position = AsyncAdapter.getTodayPosition();
			viewFlow.setSelection(position);
			viewFlow.setSelection(position); //to also set indicator
			
			return true;
		case R.id.hideWeekends:
			item.setChecked(!item.isChecked());
			prefs = getSharedPreferences("prefs", MODE_PRIVATE);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) //no action bar
			{
				if (item.isChecked())
				{
					item.setTitle("Show Weekends");
				}
				else
				{
					item.setTitle("Hide Weekends");
				}

			}
			
			prefs.edit().putBoolean("hideWeekends", item.isChecked()).commit();
			MyTitleFlowIndicator indicator = (MyTitleFlowIndicator) findViewById(R.id.viewflowindic);
			 indicator.refresh(AsyncAdapter.getTodayPosition());
			viewFlow.setSelection(AsyncAdapter.getTodayPosition());
			 
			return true;
		case R.id.pickDate:
			showDialog(MY_DATE_DIALOG_ID);
			return true;
		case R.id.feedback:
			new ReportDialog(this);
			return true;
			
		case R.id.logout:
			prefs = getSharedPreferences("prefs", MODE_PRIVATE);
			prefs.edit().putString("password", "").commit();
			Intent i = new Intent(scheduleViewActivity.this,
					LogInActivity.class);
			this.startActivity(i);
			this.finish();
			return true;
			
		case R.id.colorKey:
			showColorKeyDialog();
			return true;
		case R.id.refresh:
			CacheManager.updateCache(this, AsyncAdapter.getPositionDate(position));
			viewFlow.setSelection(position);
			return true;
			/*
		case R.id.demoMode:
			item.setChecked(!item.isChecked());
			prefs = getSharedPreferences("prefs", MODE_PRIVATE);
			prefs.edit().putBoolean("demoMode", item.isChecked()).commit();
			Appointment.demoMode = item.isChecked();
			CacheManager.updateCache(this, AsyncAdapter.getTodayDate());
			viewFlow.setSelection(AsyncAdapter.getTodayPosition());
			 */
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void refreshViewFlow()
	{
		viewFlow.setSelection(position);
	}
	private void setupViewFlow(int newPosition) {
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);

		adapter = new AsyncAdapter(this);
		if (newPosition == -1)
		{
			position = AsyncAdapter.getTodayPosition();
		}
		else
		{
			position = newPosition;
		}
		
		viewFlow.setAdapter(adapter, position);
		
		  MyTitleFlowIndicator indicator = (MyTitleFlowIndicator) findViewById(R.id.viewflowindic);
		  indicator.setTitleProvider(adapter);
		  viewFlow.setFlowIndicator(indicator);
		 
		viewFlow.setOnViewSwitchListener(new ViewSwitchListener() {

			@Override
			public void onSwitched(View view, int position) {
				// TODO Auto-generated method stub
				//LocalDate date = adapter.getPositionDate(position);

				scheduleViewActivity.this.position = position;
				//configureActionBar(date);
				
			}
		});
	}

	private void configureActionBar(LocalDate curDate) {
		// TODO Auto-generated method stub
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		View actionBarView = getLayoutInflater().inflate(
				R.layout.actionbar_view, null);

		configureProviderSpinner(actionBarView);
		//configureDateSpinner(actionBarView, curDate);

		actionBar.setCustomView(actionBarView);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case MY_DATE_DIALOG_ID:
			 LocalDate current = AsyncAdapter.getPositionDate(position);
			final DatePickerDialog dateDlg = new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {


						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							LocalDate chosen = new LocalDate(year,
									monthOfYear + 1, dayOfMonth);  

							
							int newPosition = AsyncAdapter.getPositionOfDate(chosen);
							position = newPosition; // fix for bug in jelly bean
							viewFlow.setSelection(newPosition);
							viewFlow.setSelection(newPosition);
							scheduleViewActivity.this.removeDialog(MY_DATE_DIALOG_ID);
						}
					}, current.getYear(), current.getMonthOfYear()-1, current.getDayOfMonth());

			return dateDlg;
		}
		return null;

	}

	private void configureProviderSpinner(View actionBar) {
		Spinner spinnerProvider = (Spinner) actionBar
				.findViewById(R.id.provider);
		
		Gson gson = new Gson();
		final String[] providers = gson.fromJson(
				getSharedPreferences("prefs", MODE_PRIVATE).getString(
						"providers", null), String[].class);

		String provider = getSharedPreferences("prefs", MODE_PRIVATE)
				.getString("provider", providers[0]);
		int i = 0;
		Encrypter encrypter = new Encrypter();
		for (String a : providers) {  //Decrypt providers array

			try {
				providers[i] = encrypter.Decrypt(a);
			} catch (Exception e) {
				
				e.printStackTrace();
			}

			i++;
		}
		i = 0;
		for (String a : providers) {

			if (a.equals(provider)) {
				break;
			}

			i++;
		}

		ArrayAdapter<String> providerAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, providers);
		spinnerProvider.setAdapter(providerAdapter);
		if (i < providers.length) {
			spinnerProvider.setSelection(i);
		}
		spinnerProvider.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				// TODO Auto-generated method stub

				String provider = providers[position];
				SharedPreferences prefs = getSharedPreferences("prefs",
						MODE_PRIVATE);
				if (!prefs.getString("provider", "").equals(provider)) {
					prefs.edit().putString("provider", provider).commit();
					CacheManager.deleteCache();
					CacheManager.updateCache(scheduleViewActivity.this, AsyncAdapter.getPositionDate(scheduleViewActivity.this.position));
					viewFlow.setSelection(scheduleViewActivity.this.position);
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}
	/*
	private void configureDateSpinner(View actionBar, LocalDate curDate) {
		Spinner spinnerDate = (Spinner) actionBar.findViewById(R.id.date);

		String[] date = new String[1];
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()

		.appendDayOfWeekShortText().appendLiteral(' ').appendMonthOfYear(1)
				.appendLiteral('/').appendDayOfMonth(1).appendLiteral('/')
				.appendYear(4, 4).toFormatter();
		date[0] = curDate.toString(formatter);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, date);
		spinnerDate.setAdapter(spinnerAdapter);
		spinnerDate.setOnTouchListener(Spinner_OnTouch);
	}
	*/
	public static void appointmentLoginFailed(final Context context, final LocalDate dateSeed)
	{
		final Activity activity = (Activity) context;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Kareo");
		builder.setMessage("Sorry, your login failed...");
		builder.setPositiveButton("Retry", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				CallWebServiceAppointments.loginFailed = false;
				CacheManager.updateCache(context, dateSeed);
			}
		});
		builder.setNegativeButton("View Offline", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		builder.show();

	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
		setContentView(R.layout.schedule_view);

		if (getSharedPreferences("prefs", MODE_PRIVATE).getString("email", "") == "") {
			Intent i = new Intent(scheduleViewActivity.this,
					LogInActivity.class);
			this.startActivity(i);
			this.finish();
		} else {

			setupViewFlow(position); //-1 = today
			configureActionBar(AsyncAdapter.getPositionDate(position));

		}

		
	}

}

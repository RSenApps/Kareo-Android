package com.RSen.Kareo;

import java.util.Hashtable;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class CacheManager {
	private static final int cacheSize = 19;
	private static scheduleViewActivity activity;
	private static boolean webCallRunning=false;
	public static Hashtable<LocalDate, ListViewAdapter> content = new Hashtable<LocalDate, ListViewAdapter>();
	
		//manages data and refreshes
	public static ListViewAdapter getAppointments(Context context, LocalDate date)
	{
		if (content.get(date) == null && !webCallRunning)
		{
			webCallRunning = true;
			updateFromWebService(context, date);
		}
		return content.get(date);
	}
	public static void putAppointments(String[][][] appointments, LocalDate dateSeed)
	{
		LocalDate startDate = AsyncAdapter.getPositionDate(AsyncAdapter.getPositionOfDate(dateSeed) - cacheSize/2);
		for(String[][] appointmentDay : appointments)
		{
			LocalDate key = startDate;
			if (appointmentDay.length ==0)
			{ 
				appointmentDay= new String[1][6];
				appointmentDay[0][0] = "No Appointments";
				appointmentDay[0][1] = "";
				appointmentDay[0][2] = "";
				appointmentDay[0][3] = "";
				appointmentDay[0][4] = "";
				appointmentDay[0][5] = "";
			}
			Appointment[] appointmentsArray = new Appointment[appointmentDay.length];
			int aCount = 0;
			
			for (String[] a : appointmentDay)
			{
				
				Appointment newAppointment = new Appointment(a[0], a[1], a[2], a[3], a[4], a[5], "", "", startDate);
				appointmentsArray[aCount] = newAppointment;
				aCount++;
			}
			final ListViewAdapter listViewAdapter = new ListViewAdapter(activity, appointmentsArray);
			content.put(key, listViewAdapter);
			startDate = startDate.plusDays(1);
		}
		webCallRunning=false;
		activity.refreshViewFlow();
		
	}
	public static void updateCache(Context context, LocalDate newCacheSeed)
	{
		updateFromWebService(context, newCacheSeed);
		
	}
	public static void initialize(scheduleViewActivity activity)
	{
		CacheManager.activity = activity;
	}
	public static void deleteCache()
	{
		content.clear();
	}
	private static void updateFromWebService(Context context, LocalDate date)
	{
		SharedPreferences prefs = context.getSharedPreferences("prefs", Activity.MODE_PRIVATE);
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		.appendMonthOfYear(1)
		.appendLiteral('/').appendDayOfMonth(1).appendLiteral('/')
		.appendYear(4, 4).toFormatter();
		String startDate = AsyncAdapter.getPositionDate(AsyncAdapter.getPositionOfDate(date) - cacheSize/2).toString(formatter);
		String endDate =  AsyncAdapter.getPositionDate(AsyncAdapter.getPositionOfDate(date) + cacheSize/2).toString(formatter);
		new CallWebServiceAppointments().execute(prefs.getString("email", ""), prefs.getString("password", ""), context,prefs.getString("provider", ""), startDate, endDate, date, cacheSize);
	}
}

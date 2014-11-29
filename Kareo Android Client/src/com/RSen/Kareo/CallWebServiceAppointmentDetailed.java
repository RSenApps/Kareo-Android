package com.RSen.Kareo;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.Gson;

public class CallWebServiceAppointmentDetailed extends AsyncTask<Object, Object, Object> {
	Context context;
	Appointment appointment;
	AsyncAdapter adapter;
	View dialogView;
	@Override
	protected Object doInBackground(Object... arg) {
		Encrypter encrypter = new Encrypter();
		String encryptedEmail = "";
		String encryptedPassword = "";
		try {
			encryptedEmail = encrypter.Encrypt((String) arg[0]);
			encryptedPassword = encrypter.Encrypt((String) arg[1]);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		context = (Context) arg[2];

		HttpClient httpclient = new DefaultHttpClient();  


		appointment = (Appointment) arg[3];
		String id = appointment.patientID;
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		.appendMonthOfYear(1)
		.appendLiteral('/').appendDayOfMonth(1).appendLiteral('/')
		.appendYear(4, 4).toFormatter();
		String date = appointment.date.toString(formatter);
		adapter = (AsyncAdapter) arg[4];
		dialogView = (View) arg[5];
		String encryptedid ="";
		String encryptedDate="";
		try {
			encryptedid = encrypter.Encrypt(id);
			encryptedDate = encrypter.Encrypt(date);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String appointmentsURL = "http://kareo.dnsalias.net/kareo/KareoRestClass/getAppointmentDetailed?a=" + encryptedid + "&b=" + encryptedDate +"&c=" + encryptedEmail + "&d=" + encryptedPassword;
		HttpGet request = new HttpGet(appointmentsURL);  

		ResponseHandler<String> handler = new BasicResponseHandler();
		try {  
			String result = httpclient.execute(request, handler);
			Gson gson = new Gson();
			String[] decryptionArray = gson.fromJson(result, String[].class); 

			int i = 0;
			for (String b : decryptionArray)
			{

				try {
					if(b == null)
					{
						decryptionArray[i] = "";
					}
					else
					{
						decryptionArray[i] = encrypter.Decrypt(b);
					}
					i++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			httpclient.getConnectionManager().shutdown(); 
			return decryptionArray;
		} catch (ClientProtocolException e) {  
			e.printStackTrace();
		} catch (IOException e) {  
			e.printStackTrace();
		}  

		httpclient.getConnectionManager().shutdown();   
		return null;
	}

	@Override
	protected void onPostExecute(Object result) {



		if (result == null)
		{
			AlertDialog.Builder builder = new Builder(context);
			builder.setTitle("Can't show detailed appointment");
			builder.setMessage("Sorry, detailed appointment information requires an active internet connection.");
			builder.show();

		}
		else
		{
			String[] patientInfo = (String[]) result;
			appointment.alert = patientInfo[0];
			appointment.reason = patientInfo[1];
			appointment.resource = patientInfo[2];
			appointment.gender = patientInfo[3];
			appointment.dob = patientInfo[4];
			appointment.phone = patientInfo[5];
			appointment.notes = patientInfo[6];
			adapter.showDetailedAppointment(appointment, context, dialogView);
		}



	}
}


package com.RSen.Kareo;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.LocalDate;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

public class CallWebServiceAppointments extends
AsyncTask<Object, Object, String[][][]> {
	Context context;
	LocalDate dateSeed;
	static Boolean loginFailed = false;
	@Override
	protected String[][][] doInBackground(Object... arg) {
		// TODO Auto-generated method stub
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
		String provider = (String) arg[3];
		String startDate = (String) arg[4];
		String endDate = (String) arg[5];
		dateSeed = (LocalDate) arg[6];
		int expectedSize = (Integer) arg[7];
		String encryptedProvider = "";
		String encryptedStartDate = "";
		String encryptedEndDate = "";
		try {
			encryptedProvider = encrypter.Encrypt(provider);
			encryptedStartDate = encrypter.Encrypt(startDate);
			encryptedEndDate = encrypter.Encrypt(endDate);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String appointmentsURL = "http://kareo.dnsalias.net/kareo/KareoRestClass/getAppointmentRange?a="
				+ encryptedProvider
				+ "&b="
				+ encryptedStartDate
				+ "&c="
				+ encryptedEndDate
				+ "&d="
				+ encryptedEmail + "&e=" + encryptedPassword;
		HttpGet request = new HttpGet(appointmentsURL);

		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			String result = httpclient.execute(request, handler);
			Gson gson = new Gson();
			String[][][] decryptionArray = gson
					.fromJson(result, String[][][].class);

			int day = 0;
			for (String[][] d : decryptionArray) {
				int x = 0;
				for (String[] a : d) {
					int i = 0;
					for (String b : a) {

						try {
							if (b == null) {
								decryptionArray[day][x][i] = "";
							} else {
								decryptionArray[day][x][i] = encrypter.Decrypt(b);
							}
							i++;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					x++;
				}
				day++;
			}
			
			httpclient.getConnectionManager().shutdown();
			if (decryptionArray.length < expectedSize)
			{
				decryptionArray = new String[expectedSize][1][6];
				for (int i = 0; i < expectedSize; i++)
				{
					decryptionArray[i][0][0] = "No Appointments";
					decryptionArray[i][0][1] = "";
					decryptionArray[i][0][2] = "";
					decryptionArray[i][0][3] = "";
					decryptionArray[i][0][4] = "";
					decryptionArray[i][0][5] = "";
				}
			}
			return decryptionArray;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	protected void onPostExecute(String[][][] result) {
		// TODO Auto-generated method stub

		if (result == null)
		{

			if (!loginFailed)
			{
				loginFailed = true;
				scheduleViewActivity.appointmentLoginFailed(context, dateSeed);


			}
		}
		else
		{
			loginFailed = false;
			
			CacheManager.putAppointments(result, dateSeed);
		}
	}


}



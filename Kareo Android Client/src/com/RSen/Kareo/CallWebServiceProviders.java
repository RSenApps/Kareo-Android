package com.RSen.Kareo;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.gson.Gson;

public class CallWebServiceProviders extends
		AsyncTask<Object, Object, String[]> {
	Handler handler;
	Context context;

	@Override
	protected String[] doInBackground(Object... arg) {
		{
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
			handler = (Handler) arg[3];
			String providersURL = "http://kareo.dnsalias.net/kareo/KareoRestClass/getProviders"
					+ "?a=" + encryptedEmail + "&b=" + encryptedPassword;
			HttpGet request = new HttpGet(providersURL);

			String[] returnArray;
			ResponseHandler<String> handler = new BasicResponseHandler();
			try {
				String result = httpclient.execute(request, handler);

				context.getSharedPreferences("prefs", Activity.MODE_PRIVATE)
						.edit().putString("providers", result).commit();
				Gson gson = new Gson();

				returnArray = gson.fromJson(result, String[].class);
				int i = 0;
				for (String a : returnArray) {
					try {
						returnArray[i] = encrypter.Decrypt(a);
						i++;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				httpclient.getConnectionManager().shutdown();
				return returnArray;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	protected void onPostExecute(String[] result) {
		// TODO Auto-generated method stub

		
		if (result == null) {
			handler.sendEmptyMessage(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Kareo");
			builder.setMessage("Sorry, your login failed...");
			builder.show();
		} else {
			handler.sendEmptyMessage(1);
			final String[] providers = (String[]) result;

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Whose schedule would you like to view?");
			builder.setItems(providers, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					SharedPreferences prefs = context.getSharedPreferences(
							"prefs", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					
					
					editor.putString("provider", providers[which]);
					editor.commit();
					CacheManager.updateCache(context, AsyncAdapter.getTodayDate());
					LogInActivity activity = (LogInActivity) context;
					Intent i = new Intent(activity, scheduleViewActivity.class);
					context.startActivity(i);
					activity.finish();
				}
			});
			builder.show();
		}

	}
}
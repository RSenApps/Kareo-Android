package com.RSen.Kareo;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReportDialog {
	AlertDialog dialog;

	public ReportDialog(Context context) {
		AlertDialog.Builder alertDialog = new Builder(context);
		alertDialog.setTitle("Send Feedback");
		dialog = alertDialog.create();
		final View view = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.reportdialog, null);
		dialog.setView(view);
		dialog.setInverseBackgroundForced(true);
		Button send = (Button) view.findViewById(R.id.reportSend);
		send.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				// TODO Auto-generated method stub
				final String description = ((EditText) view
						.findViewById(R.id.reportDescription)).getText()
						.toString();
				final String returnEmail = ((EditText) view
						.findViewById(R.id.reportEmail)).getText().toString();
				if (description != "") {
					String tempname = ((EditText) view
							.findViewById(R.id.reportName)).getText()
							.toString();
					final String name;
					if (tempname == "") {
						name = "Anonymous";
					} else {
						name = tempname;
					}
					
					final Handler handler = new Handler(new Handler.Callback() {
						
						@Override
						public boolean handleMessage(Message msg) {
							// TODO Auto-generated method stub
							Toast.makeText(v.getContext(),
									"Sorry your feedback was not sent", Toast.LENGTH_SHORT)
									.show();
							return false;
						}
					});
					new Thread(new Runnable() {

						public void run() {
							// TODO Auto-generated method stub
							try {
								GmailSender sender = new GmailSender(
										"RSenApps@gmail.com",
										"nnkvcjsxuiwghpnf");
								String body = "App: Kareo Android: " + name
										+ "\nReturn Email: " + returnEmail
										+ "\nDescription: " + description;

								sender.sendMail("Reported: " + description
										+ " by: " + name, body,
										"RSenApps@gmail.com",
										"RSenApps@gmail.com");
							} catch (Exception e) {
								handler.sendEmptyMessage(0);
							}
						}
					}).start();
					Toast.makeText(v.getContext(),
							"Thank you for your feedback!", Toast.LENGTH_SHORT)
							.show();
					dialog.dismiss();

				} else {
					Toast.makeText(v.getContext(),
							"Please describe your report", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		dialog.show();
	}

}

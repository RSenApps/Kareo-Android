package com.RSen.Kareo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.taptwo.android.widget.TitleProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
//Class bridges data to viewflow by providing views
public class AsyncAdapter extends BaseAdapter implements TitleProvider {

	private static LayoutInflater mInflater;

	public static final int daysDepth = 1000000000; // 2739726.02739726 years
													// back and forward (arbitrary number to ensure going out of date won't happen)
	private static final int daysSize = daysDepth * 2 + 1;
	private static final LocalDate todayDate = LocalDate.now();
	// public static Date[] dates = new Date[ daysSize ];
	// public static String[] content = new String[ daysSize ];

	private class ViewHolder { //to minimize findViewById operations (expensive)
		ProgressBar mProgressBar;
		ListView listView;
		TextView lastUpdated;
	}

	public AsyncAdapter(Context context) {

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public static void initialize (Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public ListViewAdapter getItem(int position) { //gets items contents(I don't utilize)

		return CacheManager.getAppointments(mInflater.getContext(),
				getPositionDate(position));
	}

	

	@Override
	public View getView(final int position, final View convertView,
			ViewGroup parent) {
		//called by viewflow when loading view
		return drawView(position, convertView);
	}

	public View drawView(int position, View view) {
		ViewHolder holder = null;

		if (view == null) {
			view = mInflater.inflate(R.layout.day_view, null);

			holder = new ViewHolder();
			holder.lastUpdated = (TextView) view.findViewById(R.id.lastUpdated);
			holder.mProgressBar = (ProgressBar) view
					.findViewById(R.id.progress);
			holder.listView = (ListView) view.findViewById(R.id.list);
			holder.listView.setDrawingCacheEnabled(true);
			holder.listView.setDrawingCacheBackgroundColor(view.getContext()
					.getResources().getColor(R.color.white));
			view.setTag(holder);
			//to minimize findViewbyId(expensive)
		} else {
			holder = (ViewHolder) view.getTag(); //set viewholder
		}

		ListViewAdapter adapter = CacheManager.getAppointments(
				mInflater.getContext(), getPositionDate(position));
		if (adapter != null) {

			if (!CallWebServiceAppointments.loginFailed) {
				holder.lastUpdated.setText("Last Updated: "
						+ DateTime.now().toString(DateTimeFormat.shortTime()));
			}

			holder.listView.setAdapter(adapter);

			holder.mProgressBar.setVisibility(View.GONE);
			holder.listView.setVisibility(View.VISIBLE);
			holder.listView.setDivider(view.getContext().getResources()
					.getDrawable(R.color.orange));
			holder.listView.setDividerHeight(2);

			holder.listView.setOnItemClickListener(onItemClick);

		} else {
			holder.mProgressBar.setVisibility(View.VISIBLE);
			holder.listView.setVisibility(View.GONE);
			holder.lastUpdated.setText("Updating...");
			
		}
		return view;
	}

	@Override
	public int getCount() {
		return daysSize;
	}

	public static int getTodayPosition() {
		return daysDepth;
	}

	public static LocalDate getTodayDate() {
		return todayDate;
	}

	public void showDetailedAppointment(Appointment appointment,
			Context context, View dialogView) {

		if (!appointment.alert.equals("")) {
			final TextView alert = (TextView) dialogView
					.findViewById(R.id.alert);
			alert.setText(appointment.alert);
			alert.setVisibility(View.VISIBLE);
			alert.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (alert.getEllipsize() == TruncateAt.END) {
						alert.setMaxLines(Integer.MAX_VALUE);
						alert.setEllipsize(null);
					} else {
						alert.setMaxLines(1);
						alert.setEllipsize(TruncateAt.END);
					}
				}
			});
		}
		if (!appointment.reason.equals("")) {
			((TextView) dialogView.findViewById(R.id.reason))
					.setText(appointment.reason);
		} else {
			((TextView) dialogView.findViewById(R.id.reason))
					.setVisibility(View.GONE);
		}
		if (!appointment.resource.equals("")) {
			((TextView) dialogView.findViewById(R.id.resource))
					.setText("Resource: " + appointment.resource);
		} else {
			((TextView) dialogView.findViewById(R.id.resource))
					.setVisibility(View.GONE);
		}

		((TextView) dialogView.findViewById(R.id.gender)).setText("Gender: "
				+ appointment.gender);
		/*
		if (Appointment.demoMode)
		{
			((TextView) dialogView.findViewById(R.id.dob)).setText("DOB: "
					+ "1/2/3456");
			((TextView) dialogView.findViewById(R.id.phone)).setText("Mobile: "
					+ "555-555-5555");
		}
		else
		{
		*/
		((TextView) dialogView.findViewById(R.id.dob)).setText("DOB: "
				+ appointment.dob);
		((TextView) dialogView.findViewById(R.id.phone)).setText("Mobile: "
				+ appointment.phone);
		//}
		if (!appointment.notes.equals("")) {
			((TextView) dialogView.findViewById(R.id.notes))
					.setText(appointment.notes);
		} else {
			((TextView) dialogView.findViewById(R.id.notes))
					.setVisibility(View.GONE);
		}

		dialogView.findViewById(R.id.progress).setVisibility(View.GONE);
		dialogView.findViewById(R.id.detailLayout).setVisibility(View.VISIBLE);
		// dialog.setView(dialogView);

	}

	private OnItemClickListener onItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			SharedPreferences prefs = mInflater.getContext()
					.getSharedPreferences("prefs", Activity.MODE_PRIVATE);
			ListViewAdapter listViewAdapter = (ListViewAdapter) adapter
					.getAdapter();
			Appointment appointment = (Appointment) listViewAdapter
					.getItem(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					mInflater.getContext());
			LayoutInflater inflater = (LayoutInflater) mInflater.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View dialogView = inflater.inflate(R.layout.patientdetaildialog,
					null);
			((TextView) dialogView.findViewById(R.id.name))
					.setText(appointment.patientName);
			String when = appointment.startTime + " - " + appointment.endTime;
			((TextView) dialogView.findViewById(R.id.when)).setText(when);
			if (appointment.location.equals("")) {
				((TextView) dialogView.findViewById(R.id.where))
						.setVisibility(View.GONE);
			} else {
				((TextView) dialogView.findViewById(R.id.where))
						.setText(appointment.location);
			}
			if (appointment.status.equals("")) {
				((TextView) dialogView.findViewById(R.id.status))
						.setVisibility(View.GONE);
			} else {
				((TextView) dialogView.findViewById(R.id.status))
						.setText(appointment.status);
			}
			builder.setInverseBackgroundForced(true);
			builder.setView(dialogView);
			builder.show();
			if (!appointment.patientID.equals("")) {
				new CallWebServiceAppointmentDetailed().execute(
						prefs.getString("email", ""),
						prefs.getString("password", ""), view.getContext(),
						appointment, AsyncAdapter.this, dialogView);
			} else {
				showDetailedAppointment(appointment, view.getContext(),
						dialogView);

			}
		}
	};

	public static int getPositionOfDate(LocalDate date) {
		Boolean hideWeekends = mInflater.getContext()
				.getSharedPreferences("prefs", Activity.MODE_PRIVATE)
				.getBoolean("hideWeekends", true);

		if (hideWeekends) {
			LocalDate start;
			LocalDate end;
			LocalDate currentTime = todayDate;
			if (date.getDayOfWeek() > DateTimeConstants.FRIDAY) {
				date = date.plusWeeks(1)
						.withDayOfWeek(DateTimeConstants.MONDAY);
			}
			if (currentTime.isAfter(date)) {
				start = date;
				end = currentTime;

			} else {
				start = currentTime;
				end = date;

			}
			if (start.getDayOfWeek() == DateTimeConstants.SATURDAY) {
				start = start.plusDays(2);
			} else if (start.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				start = start.plusDays(1);
			}

			if (end.getDayOfWeek() == DateTimeConstants.SATURDAY) {
				end = end.plusDays(-1);
			} else if (end.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				end = end.plusDays(-2);
			}

			int diff = Days.daysBetween(start, end).getDays();

			int result = diff / 7 * 5 + diff % 7;

			if (end.getDayOfWeek() < start.getDayOfWeek()) {
				return result - 2 + daysDepth;
			} else if (currentTime.isAfter(date)) {
				return daysDepth - result;
			} else {
				return result + daysDepth;
			}
		} else {
			LocalDate now = todayDate;
			Days diff = Days.daysBetween(now, date);
			return daysDepth + diff.getDays();
		}
	}

	public static LocalDate getPositionDate(int position) {
		// TODO: needs serious work
		Boolean hideWeekends = mInflater.getContext()
				.getSharedPreferences("prefs", Activity.MODE_PRIVATE)
				.getBoolean("hideWeekends", true);
		if (hideWeekends) {
			int businessDays = position - daysDepth;
			LocalDate current = todayDate;
			if (current.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				current = current.plusDays(1);
			} else if (current.getDayOfWeek() == DateTimeConstants.SATURDAY) {
				current = current.plusDays(2);
			}
			LocalDate result;

			result = current;
			int weeks = businessDays / 5;
			int remainder = businessDays % 5;

			result = result.plusDays(weeks * 7);

			for (int i = 0; i < Math.abs(remainder); i++) {
				if (businessDays > 0) {
					if (result.getDayOfWeek() == DateTimeConstants.SUNDAY) {
						result = result.plusDays(2);

					} else if (result.getDayOfWeek() >= DateTimeConstants.FRIDAY) {
						result = result.plusDays(3);

					} else {
						result = result.plusDays(1);
					}
				} else {
					if (result.getDayOfWeek() == DateTimeConstants.SUNDAY
							|| result.getDayOfWeek() == DateTimeConstants.MONDAY) {
						result = result.plusDays(-3);

					} else if (result.getDayOfWeek() == DateTimeConstants.SATURDAY) {
						result = result.plusDays(-2);

					} else {
						result = result.plusDays(-1);
					}
				}

			}
			return result;
		} else {
			return todayDate.plusDays(position - daysDepth);
		}

	}

	/**
	 * Prepare dates for navigation, to past and to future
	 */

	@Override
	public String getTitle(int position) {
		// TODO Auto-generated method stub
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()

		.appendDayOfWeekShortText().appendLiteral(' ').appendMonthOfYear(1)
				.appendLiteral('/').appendDayOfMonth(1).appendLiteral('/')
				.appendYearOfCentury(2, 2).toFormatter();
		return getPositionDate(position).toString(formatter);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}

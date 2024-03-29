package com.RSen.Kareo;



import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<Appointment> {
	private final Context context;
	private final Appointment[] appointments;

	public ListViewAdapter(Context context, Appointment[] appointments) {
		super(context, R.layout.listview_item, appointments);
		
		this.context = context;
		this.appointments = appointments;
	}
	private class ViewHolder {
		TextView patientView;
		TextView timeView;
		TextView locationView;
		TextView statusView;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
        	LayoutInflater inflater = (LayoutInflater) context
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	
    		v = inflater.inflate(R.layout.listview_item, parent, false);
    		holder = new ViewHolder();
    		holder.patientView = (TextView) v.findViewById(R.id.patient);
    		holder.timeView = (TextView) v.findViewById(R.id.time);
    		holder.locationView = (TextView) v.findViewById(R.id.location);
    		holder.statusView = (TextView) v.findViewById(R.id.status);
    		v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}		
        
		
		
		
			holder.patientView.setText(appointments[position].patientName);
		
	 
		if (!appointments[position].startTime.equals(""))
		{
			String time = appointments[position].startTime + " - " + appointments[position].endTime;
			holder.timeView.setText(time);
		}
		else
		{
			holder.timeView.setVisibility(View.GONE);
		}
		holder.locationView.setText(appointments[position].location);
		
		String status = appointments[position].status;
		holder.statusView.setText(appointments[position].status);
		int color = 0;
		Resources resources = context.getResources();
		if (status.equals("No-show"))
		{
			color = resources.getColor(R.color.red);
		}
		else if (status.equals("Confirmed") || status.equals("Check-in") || status.matches("Check-out"))
		{
			color = resources.getColor(R.color.green);
		}
		else if (appointments[position].patientName.equals("Gap"))
		{
			color = resources.getColor(R.color.blueTint);
		}
		else
		{
			color = android.R.color.transparent;
		}

			v.setBackgroundColor(color);
		
		
		
		return v;
	}
} 
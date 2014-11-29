package com.RSen.Kareo;

import org.joda.time.LocalDate;
//Class acts as item for listview
public class Appointment {
	String patientName ="";
	String startTime="";
	String endTime="";
	String status="";
	String location="";
	String patientID="";
	String notes="";
	String dob="";
	LocalDate date;
	String alert="";
	String reason="";
	String resource="";
	String gender="";
	String phone="";
	//public static Boolean demoMode = false;
	public Appointment()
	{
		super();
	}
	public Appointment(String patientName, String startTime, String endTime, String status, String location, String patientID, String notes, String dob, LocalDate date)
	{
		super();
		if (patientName.equals("") || patientName.equals("No Patient"))
		{
			this.patientName = "No Patient";
			this.patientID = "";
		}
		else 
		{
			//if (!demoMode || patientName.matches("Gap") || patientName.matches("No Appointments"))
			//{
			this.patientName = patientName; // Demo .charAt(0) + "...";
			//}
			//else
			//{
			//	this.patientName = "Patient " + patientName.charAt(0);
			//}
			this.patientID = patientID;
		}
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.location = location;
		this.notes = notes;
		this.dob = dob;
		this.date = date;
	}
}
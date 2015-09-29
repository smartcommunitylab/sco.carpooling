package it.smartcommunitylab.carpooling.model;

import java.util.Date;

public class Booking {

	/** traveller description(reqd).**/
	private Traveller traveller;
	/** recurrent. **/
	private boolean recurrent;
	/** date of non-recurrent reservation of recurrent trip. **/
	private Date date;
	/** list of dates, on which travel is confirmed.**/
	private Date[] confirmed;
	/** whether booking has been requested/accepted/rejected by driver.**/
	private int accepted = 0;

	public Booking() {
	}

	public Booking(Traveller traveller, boolean recurrent, Date date, Date[] confirmed, int accepted) {
		super();
		this.traveller = traveller;
		this.recurrent = recurrent;
		this.date = date;
		this.confirmed = confirmed;
		this.accepted = accepted;
	}

	public Traveller getTraveller() {
		return traveller;
	}

	public void setTraveller(Traveller traveller) {
		this.traveller = traveller;
	}

	public boolean isRecurrent() {
		return recurrent;
	}

	public void setRecurrent(boolean recurrent) {
		this.recurrent = recurrent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date[] getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(Date[] confirmed) {
		this.confirmed = confirmed;
	}

	public int getAccepted() {
		return accepted;
	}

	public void setAccepted(int accepted) {
		this.accepted = accepted;
	}

}
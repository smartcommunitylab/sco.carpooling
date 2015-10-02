/**
 * Copyright 2015 Smart Community Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + (recurrent ? 1231 : 1237);
		result = prime * result + ((traveller == null) ? 0 : traveller.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Booking other = (Booking) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (recurrent != other.recurrent)
			return false;
		if (traveller == null) {
			if (other.traveller != null)
				return false;
		} else if (!traveller.equals(other.traveller))
			return false;
		return true;
	}
	
	

}
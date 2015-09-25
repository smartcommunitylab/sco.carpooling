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

import it.sayservice.platform.smartplanner.data.message.Itinerary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author nawazk
 *
 */
@Document
public class Travel {

	@Id
	private String id;
	/** From zone (reqd).**/
	private Zone from;
	/** To zone (reqd).**/
	private Zone to;
	/** date/time for the trip(either when or reccurrency sould be specified).**/
	private long timestamp;
	/** itinerary.**/
	private Itinerary route;
	/** driver id (reqd).**/
	private String userId;
	/** recurrency of trip (either when or reccurrency sould be specified).**/
	private Recurrency recurrency;
	/** number of places.**/
	private int places = 4;
	/** whether intermediate stops are enabled.**/
	private boolean intermediateStops;
	/** bookings for the trip.**/
	private List<Booking> bookings = new ArrayList<Travel.Booking>();
	/** whether the trip is acive.**/
	private boolean active;
	/** list of community Ids. **/
	List<String> communityIds = new ArrayList<String>();

	public Travel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Travel(Zone from, Zone to, long timestamp, Itinerary route, String userId, Recurrency recurrency,
			int places, boolean intermediateStops, List<Booking> bookings, boolean active, List<String> communityIds) {
		super();
		this.from = from;
		this.to = to;
		this.timestamp = timestamp;
		this.route = route;
		this.userId = userId;
		this.recurrency = recurrency;
		this.places = places;
		this.intermediateStops = intermediateStops;
		this.bookings = bookings;
		this.active = active;
		this.communityIds = communityIds;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Zone getFrom() {
		return from;
	}

	public void setFrom(Zone from) {
		this.from = from;
	}

	public Zone getTo() {
		return to;
	}

	public void setTo(Zone to) {
		this.to = to;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Itinerary getRoute() {
		return route;
	}

	public void setRoute(Itinerary route) {
		this.route = route;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Recurrency getRecurrency() {
		return recurrency;
	}

	public void setRecurrency(Recurrency recurrency) {
		this.recurrency = recurrency;
	}

	public int getPlaces() {
		return places;
	}

	public void setPlaces(int places) {
		this.places = places;
	}

	public boolean isIntermediateStops() {
		return intermediateStops;
	}

	public void setIntermediateStops(boolean intermediateStops) {
		this.intermediateStops = intermediateStops;
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(List<Booking> bookings) {
		this.bookings = bookings;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<String> getCommunityIds() {
		return communityIds;
	}

	public void setCommunityIds(List<String> communityIds) {
		this.communityIds = communityIds;
	}

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
}

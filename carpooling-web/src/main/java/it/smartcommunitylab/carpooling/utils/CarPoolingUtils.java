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

package it.smartcommunitylab.carpooling.utils;

import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Recurrency;
import it.smartcommunitylab.carpooling.model.RecurrentBooking;
import it.smartcommunitylab.carpooling.model.RecurrentTravel;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;
import it.smartcommunitylab.carpooling.mongo.repos.TravelRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarPoolingUtils {

	/** radius of circle. **/
	//	public static final double radius = 1;
	/** date format. **/
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	/** constants. **/
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR_MSG = "errorMsg";

	public static final String NOTIFICATION_AVALIABILITY = "TripAvailability";
	public static final String NOTIFICATION_BOOKING = "ParticipationRequest";
	public static final String NOTIFICATION_CONFIRM = "ParticipationResponse";
	public static final String NOTIFICATION_CHAT = "Chat";
	public static final String NOTIFICATION_RATING = "RatingRequest";
	/** no of instances for recurrent travel. **/
	public static final int INSTANCES_THRESHOLD = 30;

	public static Date getTimeByOffset(Date reqDate, int offset) {
		Date result;
		Calendar cal = Calendar.getInstance();
		cal.setTime(reqDate);
		cal.add(Calendar.HOUR, offset);
		result = cal.getTime();

		return result;
	}

	public static int getDayOfWeek(Date reqDate) {
		int day = -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(reqDate);
		day = cal.get(Calendar.DAY_OF_WEEK);
		return day;
	}

	public static int getDayOfMonth(Date reqDate) {
		int day = -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(reqDate);
		day = cal.get(Calendar.DAY_OF_MONTH);
		return day;
	}

	public static Date getDateFromReccurenceInterval(int reqDOW, int reqDOM, int time) {
		Date result;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, reqDOW);
		cal.set(Calendar.DAY_OF_MONTH, reqDOM);
		cal.set(Calendar.HOUR_OF_DAY, time);

		result = cal.getTime();

		return result;
	}

	public static int getHour(Date date) {
		int hour = -1;

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		hour = cal.get(Calendar.HOUR_OF_DAY);

		return hour;
	}

	public static boolean isOnSameDay(long bookingTime, long requestedTime) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(bookingTime);
		cal2.setTimeInMillis(requestedTime);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}

	private static boolean isBeforeDate(long time1, long time2) {

		boolean before = false;

		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(time1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(time2);

		before = cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);

		return before;

	}

	public static Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static boolean availableRecurrentTrip(Travel travel, TravelRequest travelRequest) {

		int capacity = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {

			if (booking.isRecurrent()) { // recurrent booking.

				if (booking.getAccepted() != -1) {
					capacity--;
				}

			} else { // non recurrent booking.

				// if booking has time before requested booking time - ignore.(just for performance since we only decrease
				// the counter if it is on same day as can be seen below.
				if (CarPoolingUtils.isBeforeDate(booking.getDate().getTime(), travelRequest.getWhen())) {
					continue;
				}

				if (booking.getAccepted() != -1
						&& CarPoolingUtils.isOnSameDay(booking.getDate().getTime(), travelRequest.getWhen())) {
					capacity--;
				}
			}

		}

		return (capacity > 0 ? true : false);
	}

	public static int getNonRecurrentAvailabiliy(Travel travel, TravelRequest travelRequest) {

		int availability = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {

			if (booking.getDate() != null
					&& CarPoolingUtils.isBeforeDate(booking.getDate().getTime(), travelRequest.getWhen())) {
				continue;
			}
			// if it is not rejected, occupied.
			if (booking.getAccepted() != -1) {
				availability--;
			}
		}

		return availability;
	}

	public static boolean ifBookable(Travel travel, Booking reqBooking, String userId) {

		boolean bookable = false;
		int availability = travel.getPlaces();

		if (travel.getBookings().isEmpty() && availability > 0) {

			bookable = true;

		} else {

			for (Booking booking : travel.getBookings()) {
				// if booking has time before requested booking time - ignore.(just for performance, since we only decrease
				// the counter if it is on same day as can be seen below.
				if (!booking.isRecurrent()
						&& CarPoolingUtils.isBeforeDate(booking.getDate().getTime(), reqBooking.getDate().getTime())) {
					continue;
				}
				// if it is not rejected, occupied.
				if (!booking.isRecurrent() && booking.getAccepted() != -1) {
					availability--;
				}
			}

			if (availability > 0 && CarPoolingUtils.isOnSameDay(reqBooking.getDate().getTime(), travel.getWhen())) {
				bookable = true;
			}
		}

		return bookable;
	}

	public static boolean havePlaces(Travel travel, Booking reqBooking, String userId) {

		boolean bookable = false;
		int availableNonRecurrent = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {

//			// if booking has time before requested booking time - ignore.(just
//			// for performance since we only decrease
//			if (CarPoolingUtils.isBeforeDate(booking.getDate().getTime(), reqBooking.getDate().getTime())) {
//				continue;
//			}

			// the counter if it is on same day as can be seen below.
			if (booking.getAccepted() != -1) {
				availableNonRecurrent--;
			}
		}

		bookable = availableNonRecurrent > 0 ? true : false; // reqBooking.getRqdPosts()

		return bookable;

	}
	

	public static boolean isValidUser(Travel travel, String userId, Booking reqBooking) {
		boolean valid = true;

		for (Booking booking : travel.getBookings()) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {

				valid = false;
				break;
//				/** existing recurrent user. **/
//				if (booking.isRecurrent() && !reqBooking.isRecurrent()) {
//					valid = false;
//					break;
//				}
//				/** existing non-recurrent user. **/
//				if (!booking.isRecurrent() && !reqBooking.isRecurrent()) { //&& CarPoolingUtils.isOnSameDay(booking.getDate().getTime(), reqBooking.getDate().getTime())
//					valid = false;
//					break;
//				}
			}
		}

		return valid;
	}
	
	public static boolean isValidUserRecurrentTravel(RecurrentTravel travel, String userId,
			RecurrentBooking reqBooking) {
		boolean valid = true;

		for (RecurrentBooking booking : travel.getBookings()) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
				// same user may not book recurrently the same recurrent travel twice
				valid = false;
			}
		}

		return valid;
	}

	public static Travel updateTravel(Travel travel, Booking reqBooking, String userId) {

		List<Booking> temp = new ArrayList<Booking>();
		temp.addAll(travel.getBookings());
		for (Booking booking : temp) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
				// existing booking is recurrent, reqBooking is recurrent
				if (booking.isRecurrent() && reqBooking.isRecurrent()) {
					travel.getBookings().remove(booking);
				}
				// existing booking is non-recurrent, reqBooking is recurrent.
				if (!booking.isRecurrent() && reqBooking.isRecurrent()) {
					travel.getBookings().remove(booking);
				}
			}
		}

		// update traveller.
		reqBooking.getTraveller().setUserId(userId);
		reqBooking.setAccepted(0);
		travel.getBookings().add(reqBooking);

		return travel;
	}
	
	public static List<Booking> getAllReccBookingForUserTravels(List<Travel> userTravels, String userId) {
		List<Booking> reccBookings = new ArrayList<Booking>();
		for (Travel travel : userTravels) {
			for (Booking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
					if (booking.isRecurrent()) {
						reccBookings.add(booking);
					}
				}
			}
		}
		return reccBookings;
	}

	public static List<Booking> getAllNonReccBookingForUserTravels(List<Travel> userTravels, String userId) {
		List<Booking> nonReccBookings = new ArrayList<Booking>();
		for (Travel travel : userTravels) {
			for (Booking booking : travel.getBookings()) {
				if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {
					if (!booking.isRecurrent()) {
						nonReccBookings.add(booking);
					}
				}
			}
		}

		return nonReccBookings;
	}

	public static long adjustNumberOfDaysToWhen(long when, int extendDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(when);
		calendar.add(Calendar.DAY_OF_MONTH, extendDay);
		return calendar.getTimeInMillis();
	}

	public static RecurrentTravel updateRecurrentTravel(RecurrentTravel travel, RecurrentBooking reqBooking,
			String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * check if recurrence applies.
	 * @param recurrency
	 * @param temp
	 * @return
	 */
	public static boolean ifRecurrencyApplies(Recurrency recurrency, long temp) {
		boolean applies = false;
		Date date = new Date(temp);
		if (recurrency.getDates() != null && !recurrency.getDates().isEmpty()) {
			// get days of month.
			List<Integer> dOM = recurrency.getDates();

			if (dOM.contains(CarPoolingUtils.getDayOfMonth(date))) {
				applies = true;
			}
		} else {
			// get days of week.
			List<Integer> dOW = recurrency.getDays();
			if (dOW.contains(CarPoolingUtils.getDayOfWeek(date))) {
				applies = true;
			}
		}

		return applies;
	}
	
	/**
	 * Harvesine Formula to get distance between coordinates.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return distance
	 */
	public static double calculateHarvesineDistance(double lat1, double lon1, double lat2, double lon2) {
		/**
		 * R = earth’s radius (mean radius = 6,371km)
		 * Δlat = lat2− lat1
		 * Δlong = long2− long1
		 * a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
		 * c = 2.atan2(√a, √(1−a))
		 * d = R.c
		 */
		double distance = 0;
		final int R = 6371; // Radius of the earth km.
		Double latDistance = toRad(lat2 - lat1);
		Double lonDistance = toRad(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = R * c;
		return distance;
	}

	private static Double toRad(Double value) {
		return value * Math.PI / 180;
	}

	public static ArrayList<Location> decode(String encodedString, double d) {
		ArrayList<Location> polyline = new ArrayList<Location>();
		int index = 0;
		int len = encodedString.length();
		double lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encodedString.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encodedString.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			Location p = new Location(lat * d, lng * d);
			polyline.add(p);
		}

		return polyline;
	}
	
	public static void main(String args[]) {
		Calendar calendar = Calendar.getInstance();
		calendar.get(Calendar.DAY_OF_WEEK);
		CarPoolingUtils.getDayOfWeek(calendar.getTime());

	}

}
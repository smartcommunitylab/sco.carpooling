package it.smartcommunitylab.carpooling.utils;

import it.smartcommunitylab.carpooling.model.Booking;
import it.smartcommunitylab.carpooling.model.Travel;
import it.smartcommunitylab.carpooling.model.TravelRequest;

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
	public static final double radius = 1;
	/** date format. **/
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

	public static boolean availableRecurrentTrip(Travel travel, TravelRequest travelRequest) {

		int capacity = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {

			if (booking.isRecurrent()) { // recurrent booking.
				if (booking.getAccepted() != -1) {
					capacity--;
				}
			} else { // non recurrent booking
				if (booking.getAccepted() != -1
						&& CarPoolingUtils.isOnSameDay(booking.getDate().getTime(), travelRequest.getWhen())) {
					capacity--;
				}
			}

		}

		return (capacity > 0 ? true : false);
	}

	public static int getNonRecurrentAvailabiliy(Travel travel) {

		int availability = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {
			if (booking.getAccepted() != -1) { // if it is not rejected, occupied.
				availability--;
			}
		}

		return availability;
	}

	public static boolean ifBookable(Travel travel, Booking reqBooking, String userId) {
		boolean bookable = false;

		int availability = travel.getPlaces();

		for (Booking booking : travel.getBookings()) {
			if (booking.getAccepted() != -1) { // if it is not rejected, occupied.
				availability--;
			}
		}

		if (availability > 0 && CarPoolingUtils.isOnSameDay(reqBooking.getDate().getTime(), travel.getWhen())) {
			bookable = true;
		}

		return bookable;
	}

	public static boolean ifBookableRecurr(Travel travel, Booking reqBooking, String userId) {

		boolean bookable = false;
		int capacity = travel.getPlaces();

		if (reqBooking.isRecurrent()) { // requested booking is recurrent.

			Map<Long, Integer> dateNonReccBooked = new HashMap<Long, Integer>();
			int nrOfRecurr = 0;
			int max = 0;

			for (Booking booking : travel.getBookings()) {
				int occupied = 0;
				if (!booking.isRecurrent() && booking.getAccepted() != -1) {
					// increment occupied.
					if (dateNonReccBooked.containsKey(booking.getDate().getTime())) {
						occupied = dateNonReccBooked.get(booking.getDate().getTime()) + 1;
					} else {
						occupied++;
					}
					max = Math.max(max, occupied);
					dateNonReccBooked.put(booking.getDate().getTime(), occupied); // booking.getAllocated();
				} else if (booking.getAccepted() != -1) {
					nrOfRecurr++;
				}
			}

			if ((max + nrOfRecurr) < capacity) {
				bookable = true;
			}

//			for (Long day : dateNonReccBooked.keySet()) {
//				int maxNonRecurr = dateNonReccBooked.get(day);
//				if (maxNonRecurr + nrOfRecurr > capacity) {
//					bookable = false;
//				}
//			}

		} else { // requested booking is non-recurrent.

			int availableNonRecurrent = capacity;

			for (Booking booking : travel.getBookings()) {
				if (booking.isRecurrent() && booking.getAccepted() != -1) {
					availableNonRecurrent--;
				}
				if (!booking.isRecurrent()
						&& CarPoolingUtils.isOnSameDay(booking.getDate().getTime(), reqBooking.getDate().getTime())
						&& booking.getAccepted() != -1) {
					availableNonRecurrent--;
				}
			}
			bookable = availableNonRecurrent > 0 ? true : false; // reqBooking.getRqdPosts()
		}

		return bookable;

	}

	public static boolean isValidUser(Travel travel, String userId, Booking reqBooking) {
		boolean valid = true;

		for (Booking booking : travel.getBookings()) {
			if (booking.getTraveller().getUserId().equalsIgnoreCase(userId)) {

				/** existing recurrent user. **/
				if (booking.isRecurrent() && !reqBooking.isRecurrent()) {
					valid = false;
					break;
				}
				/** existing non-recurrent user. **/
				if (!booking.isRecurrent() && !reqBooking.isRecurrent()
						&& CarPoolingUtils.isOnSameDay(booking.getDate().getTime(), reqBooking.getDate().getTime())) {
					valid = false;
					break;
				}
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

}

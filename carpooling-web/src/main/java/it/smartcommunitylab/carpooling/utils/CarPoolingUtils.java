package it.smartcommunitylab.carpooling.utils;

import java.util.Calendar;
import java.util.Date;

public class CarPoolingUtils {

	/** radius of circle. **/
	public static final double radius = 1;

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

}

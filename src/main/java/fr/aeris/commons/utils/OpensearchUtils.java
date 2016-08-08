package fr.aeris.commons.utils;

import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;


public class OpensearchUtils {

	protected OpensearchUtils() {

	}

	/**
	 * Convert date from ISO8601 to java.util.Date format
	 * 
	 * @param date
	 *            ISO8601 date
	 * @return java.util.Date formatted date
	 */
	public static Calendar dateFromIso8601(String date) {
		if (StringUtils.isEmpty(date)) {
			return null;
		} else {
			return DatatypeConverter.parseDateTime(date);
		}

	}

	/**
	 * Count days between two dates (Calendar.getTimeInMillis() format)
	 * 
	 * @param timeInMillis1
	 * @param timeInMillis2
	 * @return
	 */
	public static int daysBetween(long timeInMillis1, long timeInMillis2) {
		return (int) ((timeInMillis2 - timeInMillis1) / (1000 * 60 * 60 * 24));
	}

}

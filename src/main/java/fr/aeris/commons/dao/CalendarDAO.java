package fr.aeris.commons.dao;

import java.util.List;

import org.joda.time.DateTime;

import fr.aeris.commons.model.elements.CalendarDay;

public interface CalendarDAO {

	/**
	 * Check a given date for quicklook files
	 * 
	 * @param collection
	 * @param date
	 * @return an object representing the day
	 */
	CalendarDay checkDay(String collection, DateTime date);

	/**
	 * Check every day of the given period for quicklook files
	 * 
	 * @param collection
	 * @param startDate
	 * @param endDate
	 * @return a list of objects representing the days of the period
	 */
	List<CalendarDay> checkPeriod(String collection, DateTime startDate, DateTime endDate);

}

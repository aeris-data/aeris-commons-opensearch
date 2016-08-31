package fr.aeris.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import fr.aeris.commons.model.elements.opensearch.OSEntry;

/**
 * Sort the entries by date and hour
 * 
 * @author Gregory Klein
 *
 */
public class OSEntryComparator implements Comparator<OSEntry> {

	public int compare(OSEntry o1, OSEntry o2) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date1 = null;
		Date date2 = null;
		try {
			date1 = formatter.parse(o1.getDate());
			date2 = formatter.parse(o2.getDate());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		int dateComparison = date1.compareTo(date2);

		// Si les dates sont egales on compare en fonction du nom de fichier
		if (dateComparison == 0) {
			String name1 = o1.getMedia().getContent();
			String name2 = o2.getMedia().getContent();
			return name1.compareTo(name2);
		} else {
			return dateComparison;
		}
	}
}
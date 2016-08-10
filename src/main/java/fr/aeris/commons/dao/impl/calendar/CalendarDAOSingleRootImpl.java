package fr.aeris.commons.dao.impl.calendar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import fr.aeris.commons.dao.CalendarDAO;
import fr.aeris.commons.model.elements.CalendarDay;
import fr.aeris.commons.model.elements.CalendarDayStatus;

public class CalendarDAOSingleRootImpl implements CalendarDAO {

	String root;
	String template;
	int numFiles;

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public int getNumFiles() {
		return numFiles;
	}

	public void setNumFiles(int numFiles) {
		this.numFiles = numFiles;
	}

	@Override
	public CalendarDay checkDay(String collection, DateTime dt) {

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
		String dateFolder = dtf.print(dt);

		StringBuilder sb = new StringBuilder();
		String dataFolder = sb.append(root).append(File.separator).append(collection).append(File.separator)
				.append(dateFolder).toString();

		File folder = new File(dataFolder);

		CalendarDay day = new CalendarDay();
		DateTimeFormatter dtf2 = DateTimeFormat.forPattern("yyyy-MM-dd");
		day.setStart(dtf2.print(dt));
		day.setEnd(dtf2.print(dt));

		if (folder.exists()) {
			int count = folder.listFiles().length;
			if (count == 0) {
				day.setIs(CalendarDayStatus.UNAVAILABLE);
				day.setComment("Pas de fichiers");
			} else if (count >= numFiles) {
				day.setIs(CalendarDayStatus.FULL);
			} else {
				day.setIs(CalendarDayStatus.PARTIAL);
				String end = (count == 1) ? "" : "s";
				day.setComment(count + " fichier" + end + " sur " + numFiles);
			}
		} else {
			day.setIs(CalendarDayStatus.NOFOLDER);
			day.setComment("Non disponible");
		}

		return day;
	}

	@Override
	public List<CalendarDay> checkPeriod(String collection, DateTime startDate, DateTime endDate) {
		DateTime currentDate = startDate;
		List<CalendarDay> results = new ArrayList<>();

		while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
			CalendarDay day = checkDay(collection, currentDate);
			if (!day.getIs().equals(CalendarDayStatus.NOFOLDER.toString())) {
				results.add(day);
			}
			currentDate = currentDate.plusDays(1);
		}

		return results;
	}

}

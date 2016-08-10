package fr.aeris.commons.dao.impl.calendar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import fr.aeris.commons.dao.CalendarDAO;
import fr.aeris.commons.model.elements.CalendarDay;
import fr.aeris.commons.model.elements.CalendarDayStatus;

public class CalendarDAOSingleRootSingleFileImpl implements CalendarDAO {

	String root;
	String template;

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

	@Override
	public CalendarDay checkDay(String collection, DateTime dt) {

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
		String dateFolder = dtf.print(dt);

		StringBuilder sb = new StringBuilder();
		String dataFolder = sb.append(root).append(File.separator).append(collection).append(File.separator)
				.append(dateFolder).toString();

		Map<String, String> params = new HashMap<String, String>();
		params.put("date", dateFolder.toString());
		params.put("sat", "msg03");
		params.put("type", "006");
		params.put("time", "0600");

		File file = createFileFromTemplate(dataFolder, params);

		CalendarDay day = new CalendarDay();
		DateTimeFormatter dtf2 = DateTimeFormat.forPattern("yyyy-MM-dd");
		day.setStart(dtf2.print(dt));
		day.setEnd(dtf2.print(dt));

		if (file.exists()) {
			day.setIs(CalendarDayStatus.FULL);
		} else {
			day.setIs(CalendarDayStatus.UNAVAILABLE);
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

	private File createFileFromTemplate(String inFolder, Map<String, String> params) {
		StrSubstitutor sub = new StrSubstitutor(params);
		String fileName = sub.replace(template);
		File file = new File(inFolder + File.separator + fileName);
		return file;
	}

}

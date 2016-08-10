package fr.aeris.commons.model.elements;

import java.util.List;

public class CalendarResponse {

	List<CalendarDay> events;

	public List<CalendarDay> getEvents() {
		return events;
	}

	public void setEvents(List<CalendarDay> events) {
		this.events = events;
	}

}

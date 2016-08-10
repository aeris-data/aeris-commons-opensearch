package fr.aeris.commons.model.elements;

public enum CalendarDayStatus {

	FULL("full"), PARTIAL("partial"), UNAVAILABLE("unavailable"), NOFOLDER("no folder");

	private String status;

	CalendarDayStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}

}

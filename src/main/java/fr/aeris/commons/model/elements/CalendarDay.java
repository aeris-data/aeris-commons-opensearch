package fr.aeris.commons.model.elements;

public class CalendarDay {

	String is;
	String start;
	String end;
	String comment;

	public String getIs() {
		return is;
	}

	public void setIs(CalendarDayStatus is) {
		this.is = is.toString();
	}

	public String getComment() {
		if (comment == null) {
			comment = "";
		}
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

}

package fr.aeris.commons.formatter;

import javax.servlet.http.HttpServletRequest;

public interface ResponseFormatter {

	public String format(Object object, HttpServletRequest httpRequest) throws Exception;

}

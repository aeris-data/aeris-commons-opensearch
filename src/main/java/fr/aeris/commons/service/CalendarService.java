package fr.aeris.commons.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.aeris.commons.dao.CalendarDAO;
import fr.aeris.commons.model.elements.CalendarDay;
import fr.aeris.commons.model.elements.CalendarResponse;

@Path("/calendar")
public class CalendarService {

	private final static Logger log = LoggerFactory.getLogger(CalendarService.class);

	@Autowired
	private CalendarDAO calendarDAO;

	@Context
	HttpServletRequest httpRequest;

	@GET
	@Path("/isAlive")
	@Produces(MediaType.TEXT_PLAIN)
	public Response isAlive() {
		String answer = "Yes";
		log.info(httpRequest.getRemoteAddr() + " requested 'isAlive' service");
		return Response.status(200).entity(answer).build();

	}

	@GET
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkAvailability(@QueryParam("collection") String collection, @QueryParam("start") String start,
			@QueryParam("end") String end) {
		List<CalendarDay> results = new ArrayList<>();

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

		DateTime startDate;
		DateTime endDate;
		startDate = formatter.parseDateTime(start);
		endDate = formatter.parseDateTime(end).plusDays(1);

		results = calendarDAO.checkPeriod(collection, startDate, endDate);

		CalendarResponse resp = new CalendarResponse();
		resp.setEvents(results);

		return Response.ok().entity(resp).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization,X-Requested-With")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").build();
	}

}
